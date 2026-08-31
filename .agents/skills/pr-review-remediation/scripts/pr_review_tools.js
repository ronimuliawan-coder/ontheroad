#!/usr/bin/env bun

/**
 * Project-Agnostic PR Review Remediation Tools
 *
 * Provides automated helpers for:
 * 1. Dynamically discovering repository owner and name from git / gh context (safe from shell injection)
 * 2. Waiting for all automated reviewers (CodeRabbit, Macroscope, Greptile) and CI check-suites to settle
 * 3. Fetching all unresolved review threads, bot summaries, outside diffs, and nitpicks
 * 4. Posting replies with informal sign-off attribution ("- AG-Ron") at the bottom
 * 5. Enforcing read-only review-thread handling; resolution remains with reviewers or Ron
 */

const { execFileSync } = require('child_process');

const ATTRIBUTION_SIGN_OFF = '\n\n- AG-Ron';

const RELEVANT_APPS = [
  'coderabbitai',
  'MacroscopeApp',
  'Greptile Apps',
  'GitHub Actions',
  'Vercel',
  'Netlify',
  'Blacksmith',
  'CircleCI'
];

const ALLOWED_BOT_LOGINS = new Set([
  'coderabbitai',
  'coderabbitai[bot]',
  'macroscopeapp',
  'macroscopeapp[bot]',
  'greptile',
  'greptile[bot]',
  'greptile-apps[bot]'
]);

const REPO_IDENTIFIER_REGEX = /^[a-zA-Z0-9_.-]+$/;
const COMMENT_SOURCES = new Set(['review-comment', 'issue-comment', 'review-summary']);

/**
 * Validate a repository owner or name before using it in a GitHub API path.
 */
function validateRepoIdentifier(val, label) {
  if (!val || typeof val !== 'string' || !REPO_IDENTIFIER_REGEX.test(val.trim())) {
    throw new Error(`Invalid repository ${label}: "${val}"`);
  }
  return val.trim();
}

/** Dynamically resolve the GitHub repository owner and name without shell interpolation. */
function getRepoInfo() {
  try {
    const raw = execFileSync('gh', ['repo', 'view', '--json', 'owner,name'], {
      encoding: 'utf8',
      stdio: ['pipe', 'pipe', 'ignore']
    });
    const info = JSON.parse(raw || '{}');
    if (info.name && info.owner?.login) {
      return {
        owner: validateRepoIdentifier(info.owner.login, 'owner'),
        name: validateRepoIdentifier(info.name, 'name')
      };
    }
  } catch {
    // Fallback: parse git remote origin URL
  }

  try {
    const remote = execFileSync('git', ['config', '--get', 'remote.origin.url'], {
      encoding: 'utf8',
      stdio: ['pipe', 'pipe', 'ignore']
    }).trim();
    const match = remote.match(/[:/]([^/]+)\/([^/]+?)(?:\.git)?$/);
    if (match) {
      return {
        owner: validateRepoIdentifier(match[1], 'owner'),
        name: validateRepoIdentifier(match[2], 'name')
      };
    }
  } catch {
    // Fallback failure
  }

  throw new Error('Unable to automatically determine repository owner and name. Ensure `gh` or `git remote` is configured.');
}

// Repository discovery is required only for commands that contact GitHub. Keeping the
// usage path local lets callers inspect the helper safely when no remote is configured.
const [,, command, ...args] = process.argv;
const repositoryCommands = new Set(['repo', 'suites', 'fetch', 'reply']);
const { owner: REPO_OWNER, name: REPO_NAME } = repositoryCommands.has(command)
  ? getRepoInfo()
  : { owner: null, name: null };

/** Execute a GitHub CLI command without shell interpolation. */
function runGh(args, input) {
  const options = { encoding: 'utf8', maxBuffer: 10 * 1024 * 1024 };
  if (input !== undefined) {
    options.input = typeof input === 'string' ? input : JSON.stringify(input);
  }
  return execFileSync('gh', args, options);
}

/** Execute a GitHub CLI request and parse its JSON response. */
function runGhJson(args, input) {
  const res = runGh(args, input);
  return JSON.parse(res || '{}');
}

/** Flatten the array of page payloads produced by `gh api --paginate --slurp`. */
function flattenPaginatedResults(payload, property) {
  const pages = Array.isArray(payload) ? payload : [payload];
  return pages.flatMap((page) => {
    if (property) return Array.isArray(page?.[property]) ? page[property] : [];
    return Array.isArray(page) ? page : [];
  });
}

/** Parse a required positive integer CLI argument. */
function parsePositiveInteger(value, label) {
  if (!/^\d+$/.test(value || '')) {
    throw new Error(`Invalid ${label}: expected a positive integer`);
  }
  const parsed = Number(value);
  if (!Number.isSafeInteger(parsed) || parsed < 1) {
    throw new Error(`Invalid ${label}: expected a positive integer`);
  }
  return parsed;
}

/** Accept either a numeric pull-request number or a canonical GitHub PR URL. */
function parsePullRequestNumber(value) {
  if (!value) throw new Error('Missing pull-request number or URL');
  if (/^\d+$/.test(value)) return parsePositiveInteger(value, 'pull-request number');

  let url;
  try {
    url = new URL(value);
  } catch {
    throw new Error('Invalid pull-request URL');
  }

  if (url.protocol !== 'https:' || url.hostname !== 'github.com') {
    throw new Error('Pull-request URL must use https://github.com');
  }
  const match = url.pathname.match(/^\/([^/]+)\/([^/]+)\/pull\/(\d+)(?:\/|$)/);
  if (!match) throw new Error('Pull-request URL is missing a valid /pull/<number> path');
  if (match[1] !== REPO_OWNER || match[2] !== REPO_NAME) {
    throw new Error(`Pull-request URL must target ${REPO_OWNER}/${REPO_NAME}`);
  }
  return parsePositiveInteger(match[3], 'pull-request number');
}

/** Execute a GraphQL request with typed variables and normalized whitespace. */
function runGraphql(query, variables) {
  const args = ['api', 'graphql', '-f', `query=${query.replace(/\s+/g, ' ').trim()}`];
  for (const [name, value] of Object.entries(variables)) {
    if (value === null || value === undefined) continue;
    args.push(typeof value === 'number' ? '-F' : '-f', `${name}=${value}`);
  }
  const response = runGhJson(args);
  if (response.errors?.length) {
    throw new Error(`GitHub GraphQL request failed: ${response.errors.map((error) => error.message).join('; ')}`);
  }
  return response;
}

/**
 * Check if all automated review check-suites and CI have finished running for a given commit.
 */
function checkSuitesStatus(commitSha) {
  const data = runGhJson([
    'api',
    `repos/${REPO_OWNER}/${REPO_NAME}/commits/${commitSha}/check-suites`,
    '--paginate',
    '--slurp'
  ]);
  const suites = flattenPaginatedResults(data, 'check_suites');

  const monitored = suites.filter(s => s.app && RELEVANT_APPS.some(app => s.app.name?.toLowerCase().includes(app.toLowerCase())));

  const pending = monitored.filter(s => s.status !== 'completed');
  return {
    repo: `${REPO_OWNER}/${REPO_NAME}`,
    allCompleted: monitored.length > 0 && pending.length === 0,
    total: monitored.length,
    pending: pending.map(s => ({ app: s.app?.name, status: s.status, conclusion: s.conclusion })),
    completed: monitored.filter(s => s.status === 'completed').map(s => ({ app: s.app?.name, conclusion: s.conclusion }))
  };
}

/** Fetch one paginated pull-request page for reviews and review threads. */
function fetchPullRequestPage(prNumber, reviewsCursor, threadsCursor) {
  const query = `
    query($owner: String!, $repo: String!, $pull_number: Int!, $reviewsCursor: String, $threadsCursor: String) {
      repository(owner: $owner, name: $repo) {
        pullRequest(number: $pull_number) {
          id
          number
          title
          headRefOid
          headRefName
          baseRefName
          state
          reviews(first: 100, after: $reviewsCursor) {
            nodes {
              id
              databaseId
              state
              submittedAt
              url
              author { login }
              body
            }
            pageInfo { hasNextPage endCursor }
          }
          reviewThreads(first: 100, after: $threadsCursor) {
            nodes {
              id
              isResolved
              resolvedBy { login }
              comments(first: 50) {
                nodes {
                  id
                  databaseId
                  url
                  author { login }
                  body
                  path
                  line
                  createdAt
                }
                pageInfo { hasNextPage endCursor }
              }
            }
            pageInfo { hasNextPage endCursor }
          }
        }
      }
    }
  `;
  const response = runGraphql(query, {
    owner: REPO_OWNER,
    repo: REPO_NAME,
    pull_number: prNumber,
    reviewsCursor,
    threadsCursor
  });
  const pr = response.data?.repository?.pullRequest;
  if (!pr) throw new Error(`Could not find PR #${prNumber} in ${REPO_OWNER}/${REPO_NAME}`);
  return pr;
}

/** Fetch one additional page of comments for a review thread. */
function fetchReviewThreadCommentPage(threadId, commentsCursor) {
  const query = `
    query($threadId: ID!, $commentsCursor: String) {
      node(id: $threadId) {
        ... on PullRequestReviewThread {
          comments(first: 50, after: $commentsCursor) {
            nodes {
              id
              databaseId
              url
              author { login }
              body
              path
              line
              createdAt
            }
            pageInfo { hasNextPage endCursor }
          }
        }
      }
    }
  `;
  const response = runGraphql(query, { threadId, commentsCursor });
  const comments = response.data?.node?.comments;
  if (!comments) throw new Error(`Could not fetch comments for review thread ${threadId}`);
  return comments;
}

/** Accumulate every comment page before the thread is processed. */
function fetchAllReviewThreadComments(thread) {
  const comments = [...(thread.comments.nodes || [])];
  let cursor = thread.comments.pageInfo?.hasNextPage ? thread.comments.pageInfo.endCursor : null;
  while (cursor) {
    const page = fetchReviewThreadCommentPage(thread.id, cursor);
    comments.push(...(page.nodes || []));
    if (page.pageInfo?.hasNextPage && !page.pageInfo.endCursor) {
      throw new Error(`Review thread ${thread.id} reported another page without a cursor`);
    }
    cursor = page.pageInfo?.hasNextPage ? page.pageInfo.endCursor : null;
  }
  return { ...thread, comments: { nodes: comments, pageInfo: { hasNextPage: false, endCursor: null } } };
}

/**
 * Fetch all review data for a pull request across all entry points:
 * 1. Inline review threads (open and resolved with follow-ups)
 * 2. Top-level Pull Request Reviews (pullrequestreview-*)
 * 3. Issue comments / bot summaries (issuecomment-*)
 * 4. Discussion replies and follow-ups
 */
function fetchPRReviewData(prNumber) {
  const reviews = [];
  const reviewThreads = [];
  let reviewsCursor = null;
  let threadsCursor = null;
  let prMetadata;

  do {
    const page = fetchPullRequestPage(prNumber, reviewsCursor, threadsCursor);
    prMetadata ||= page;
    reviews.push(...(page.reviews.nodes || []));
    reviewThreads.push(...(page.reviewThreads.nodes || []));

    if (page.reviews.pageInfo?.hasNextPage && !page.reviews.pageInfo.endCursor) {
      throw new Error('Pull-request reviews reported another page without a cursor');
    }
    if (page.reviewThreads.pageInfo?.hasNextPage && !page.reviewThreads.pageInfo.endCursor) {
      throw new Error('Review threads reported another page without a cursor');
    }
    reviewsCursor = page.reviews.pageInfo?.hasNextPage ? page.reviews.pageInfo.endCursor : null;
    threadsCursor = page.reviewThreads.pageInfo?.hasNextPage ? page.reviewThreads.pageInfo.endCursor : null;
  } while (reviewsCursor || threadsCursor);

  const allReviewThreads = reviewThreads.map(fetchAllReviewThreadComments);

  // Also fetch issue comments for CodeRabbit / top-level bot summaries
  let issueComments = [];
  try {
    const data = runGhJson([
      'api',
      `repos/${REPO_OWNER}/${REPO_NAME}/issues/${prNumber}/comments`,
      '--paginate',
      '--slurp'
    ]);
    issueComments = flattenPaginatedResults(data);
  } catch (err) {
    console.warn(`[WARN] Failed to fetch issue comments for PR #${prNumber} (transient GitHub API error):`, err?.message);
  }

  const allThreads = allReviewThreads.map((t, idx) => {
    const comments = t.comments.nodes || [];
    const firstComment = comments[0] || {};
    const latestComment = comments[comments.length - 1] || {};
    const latestAuthor = latestComment.author?.login || 'unknown';
    const latestBody = latestComment.body || '';
    const hasUnansweredReviewerComment = !latestBody.trim().endsWith('- AG-Ron');

    return {
      index: idx + 1,
      threadId: t.id,
      isResolved: t.isResolved,
      resolvedBy: t.resolvedBy?.login || null,
      firstCommentId: firstComment.databaseId,
      latestCommentId: latestComment.databaseId,
      commentId: latestComment.databaseId || firstComment.databaseId,
      author: firstComment.author?.login,
      path: firstComment.path,
      line: firstComment.line,
      url: latestComment.url || firstComment.url,
      firstCommentBody: firstComment.body,
      latestAuthor,
      latestCommentDate: latestComment.createdAt,
      latestCommentBody: latestBody,
      hasUnansweredReviewerComment,
      allComments: comments
    };
  });

  const unresolvedThreads = allThreads.filter(t => !t.isResolved);
  const pendingActionThreads = allThreads.filter(t => t.hasUnansweredReviewerComment);

  return {
    repo: `${REPO_OWNER}/${REPO_NAME}`,
    prNumber: prMetadata.number,
    headSha: prMetadata.headRefOid,
    headBranch: prMetadata.headRefName,
    baseBranch: prMetadata.baseRefName,
    state: prMetadata.state,
    totalThreads: allThreads.length,
    unresolvedCount: unresolvedThreads.length,
    pendingActionCount: pendingActionThreads.length,
    unresolvedThreads,
    pendingActionThreads,
    allThreads,
    reviews: reviews.map(r => ({
      id: r.id,
      databaseId: r.databaseId,
      author: r.author?.login,
      state: r.state,
      submittedAt: r.submittedAt,
      url: r.url,
      body: r.body
    })),
    botSummaries: (Array.isArray(issueComments) ? issueComments : []).filter(c => {
      const login = c.user?.login?.toLowerCase();
      return login ? ALLOWED_BOT_LOGINS.has(login) : false;
    }).map(c => ({
      id: c.id,
      author: c.user?.login,
      createdAt: c.created_at,
      updatedAt: c.updated_at,
      body: c.body
    }))
  };
}

/** Reply to a review source using the endpoint that matches its source type. */
function replyToComment(prNumber, sourceType, sourceId, message) {
  if (!COMMENT_SOURCES.has(sourceType)) {
    throw new Error(`Unsupported or ambiguous comment source: ${sourceType}`);
  }
  const commentId = parsePositiveInteger(sourceId, 'comment ID');
  const trimmed = message.trim();
  const fullBody = trimmed.endsWith('- AG-Ron')
    ? trimmed
    : `${trimmed}${ATTRIBUTION_SIGN_OFF}`;

  if (sourceType === 'review-comment') {
    runGh(
      ['api', `repos/${REPO_OWNER}/${REPO_NAME}/pulls/${prNumber}/comments/${commentId}/replies`, '--input', '-'],
      { body: fullBody }
    );
  } else if (sourceType === 'issue-comment') {
    runGh(
      ['api', `repos/${REPO_OWNER}/${REPO_NAME}/issues/${prNumber}/comments`, '--input', '-'],
      { body: fullBody }
    );
  } else {
    runGh(
      ['api', `repos/${REPO_OWNER}/${REPO_NAME}/pulls/${prNumber}/reviews`, '--input', '-'],
      { body: fullBody, event: 'COMMENT' }
    );
  }
  console.log(`[OK] Replied to ${sourceType} ${commentId} in ${REPO_OWNER}/${REPO_NAME}`);
}

// CLI Command Router
switch (command) {
  case 'repo': {
    console.log(JSON.stringify({ owner: REPO_OWNER, name: REPO_NAME }, null, 2));
    break;
  }
  case 'suites': {
    const sha = args[0] || 'HEAD';
    const status = checkSuitesStatus(sha);
    console.log(JSON.stringify(status, null, 2));
    break;
  }
  case 'fetch': {
    let prNumber;
    try {
      prNumber = parsePullRequestNumber(args[0]);
    } catch (error) {
      console.error(`Usage: pr_review_tools.js fetch <pr_number|pr_url> (${error.message})`);
      process.exit(1);
    }
    const data = fetchPRReviewData(prNumber);
    console.log(JSON.stringify(data, null, 2));
    break;
  }
  case 'reply': {
    let prNumber;
    try {
      prNumber = parsePullRequestNumber(args[0]);
    } catch (error) {
      console.error(`Usage: pr_review_tools.js reply <pr_number|pr_url> <source_type> <comment_id> <message> (${error.message})`);
      process.exit(1);
    }
    const sourceType = args[1];
    const commentId = args[2];
    const message = args.slice(3).join(' ');
    if (!sourceType || !commentId || !message) {
      console.error('Usage: pr_review_tools.js reply <pr_number|pr_url> <source_type> <comment_id> <message>');
      process.exit(1);
    }
    replyToComment(prNumber, sourceType, commentId, message);
    break;
  }
  default:
    console.log('Usage: pr_review_tools.js [repo | suites <commit_sha> | fetch <pr_number|pr_url> | reply <pr_number|pr_url> <source_type> <comment_id> <msg>]');
}
