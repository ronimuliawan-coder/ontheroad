package com.ontheroad.receipt

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ontheroad.R
import com.ontheroad.core.model.Trip
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

fun shareDirectTripReceipt(context: Context, trip: Trip) {
    val content = trip.toDirectTripReceiptContent() ?: return
    var receiptFile: File? = null
    try {
        val file = createDirectTripReceiptImage(context, content)
        receiptFile = file

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = PNG_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(context.contentResolver, context.getString(R.string.share_direct_receipt), uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, context.getString(R.string.share_direct_receipt_title))
        if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (_: Exception) {
        receiptFile?.delete()
        Toast.makeText(context, R.string.share_direct_receipt_error, Toast.LENGTH_SHORT).show()
    }
}

internal fun createDirectTripReceiptImage(context: Context, content: DirectTripReceiptContent): File {
    val receiptDirectory = File(context.cacheDir, RECEIPT_CACHE_DIRECTORY)
    if (!receiptDirectory.exists() && !receiptDirectory.mkdirs()) {
        throw IOException("Could not create receipt cache directory")
    }
    val file = File.createTempFile(RECEIPT_FILE_PREFIX, RECEIPT_FILE_SUFFIX, receiptDirectory)
    var bitmap: Bitmap? = null
    try {
        val renderedBitmap = renderReceipt(context, content)
        bitmap = renderedBitmap
        FileOutputStream(file).use { output ->
            if (!renderedBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                throw IOException("PNG encoding failed")
            }
        }
        return file
    } catch (exception: Exception) {
        file.delete()
        throw exception
    } finally {
        bitmap?.recycle()
    }
}

private fun renderReceipt(context: Context, content: DirectTripReceiptContent): Bitmap {
    val width = RECEIPT_WIDTH_PX
    val horizontalPadding = RECEIPT_PADDING_PX
    val textWidth = width - horizontalPadding * 2
    val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = RECEIPT_TEXT_COLOR
        textSize = BODY_TEXT_SIZE_PX
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    val pickupLayout = wrappedAddressLayout(
        content.pickupAddress.ifBlank { context.getString(R.string.receipt_unknown_location) },
        bodyPaint,
        textWidth
    )
    val dropoffLayout = wrappedAddressLayout(
        content.dropoffAddress.ifBlank { context.getString(R.string.receipt_unknown_location) },
        bodyPaint,
        textWidth
    )
    val height = RECEIPT_BASE_HEIGHT_PX + pickupLayout.height + dropoffLayout.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(RECEIPT_BACKGROUND_COLOR)

    val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = RECEIPT_ACCENT_COLOR }
    canvas.drawRect(0f, 0f, width.toFloat(), ACCENT_BAR_HEIGHT_PX.toFloat(), accentPaint)

    val headingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = RECEIPT_TEXT_COLOR
        textSize = HEADING_TEXT_SIZE_PX
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = RECEIPT_SECONDARY_COLOR
        textSize = LABEL_TEXT_SIZE_PX
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val bodyTextPaint = bodyPaint
    val largeAmountPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = RECEIPT_ACCENT_COLOR
        textSize = AMOUNT_TEXT_SIZE_PX
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    var y = TOP_CONTENT_PX.toFloat()
    canvas.drawText(context.getString(R.string.app_name), horizontalPadding.toFloat(), y, headingPaint)
    y += HEADING_GAP_PX
    canvas.drawText(context.getString(R.string.receipt_heading), horizontalPadding.toFloat(), y, labelPaint)
    y += SECTION_GAP_PX
    y = drawDivider(canvas, horizontalPadding, width, y)

    y = drawLabelAndValue(
        canvas,
        context.getString(R.string.receipt_date_label),
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(content.completedAtMillis)),
        horizontalPadding,
        y,
        labelPaint,
        bodyTextPaint
    )
    y = drawWrappedSection(canvas, context.getString(R.string.receipt_pickup_label), pickupLayout, horizontalPadding, y, labelPaint)
    y = drawWrappedSection(canvas, context.getString(R.string.receipt_dropoff_label), dropoffLayout, horizontalPadding, y, labelPaint)
    y = drawLabelAndValue(
        canvas,
        context.getString(R.string.receipt_distance_label),
        formatDistance(content.actualDistanceMeters),
        horizontalPadding,
        y,
        labelPaint,
        bodyTextPaint
    )
    y += SECTION_GAP_PX
    canvas.drawText(context.getString(R.string.receipt_paid_label), horizontalPadding.toFloat(), y, labelPaint)
    y += AMOUNT_GAP_PX
    canvas.drawText(formatCurrency(content.customerPaidTotalAmountCents), horizontalPadding.toFloat(), y, largeAmountPaint)
    y += FOOTER_GAP_PX
    canvas.drawText(context.getString(R.string.receipt_footer), horizontalPadding.toFloat(), y, labelPaint)
    return bitmap
}

private fun wrappedAddressLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
    StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setEllipsize(TextUtils.TruncateAt.END)
        .setMaxLines(MAX_ADDRESS_LINES)
        .build()

private fun drawLabelAndValue(
    canvas: Canvas,
    label: String,
    value: String,
    padding: Int,
    startY: Float,
    labelPaint: TextPaint,
    valuePaint: TextPaint
): Float {
    var y = startY + SECTION_GAP_PX
    canvas.drawText(label, padding.toFloat(), y, labelPaint)
    y += LABEL_VALUE_GAP_PX
    canvas.drawText(value, padding.toFloat(), y, valuePaint)
    return y + SECTION_GAP_PX
}

private fun drawWrappedSection(
    canvas: Canvas,
    label: String,
    layout: StaticLayout,
    padding: Int,
    startY: Float,
    labelPaint: TextPaint
): Float {
    var y = startY + SECTION_GAP_PX
    canvas.drawText(label, padding.toFloat(), y, labelPaint)
    y += LABEL_VALUE_GAP_PX
    canvas.save()
    canvas.translate(padding.toFloat(), y)
    layout.draw(canvas)
    canvas.restore()
    return y + layout.height + SECTION_GAP_PX
}

private fun drawDivider(canvas: Canvas, padding: Int, width: Int, y: Float): Float {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = RECEIPT_DIVIDER_COLOR
        strokeWidth = DIVIDER_STROKE_PX
    }
    canvas.drawLine(padding.toFloat(), y, (width - padding).toFloat(), y, paint)
    return y + SECTION_GAP_PX
}

private fun formatCurrency(amountCents: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return format.format(BigDecimal.valueOf(amountCents, 2))
}

private fun formatDistance(distanceMeters: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${format.format(distanceMeters / 1000.0)} km"
}

private const val RECEIPT_CACHE_DIRECTORY = "direct-receipts"
private const val RECEIPT_FILE_PREFIX = "direct-receipt-"
private const val RECEIPT_FILE_SUFFIX = ".png"
private const val PNG_MIME_TYPE = "image/png"
private const val RECEIPT_WIDTH_PX = 1080
private const val RECEIPT_PADDING_PX = 72
private const val RECEIPT_BASE_HEIGHT_PX = 1240
private const val ACCENT_BAR_HEIGHT_PX = 20
private const val TOP_CONTENT_PX = 100
private const val HEADING_GAP_PX = 60
private const val SECTION_GAP_PX = 52
private const val LABEL_VALUE_GAP_PX = 50
private const val AMOUNT_GAP_PX = 86
private const val FOOTER_GAP_PX = 76
private const val BODY_TEXT_SIZE_PX = 42f
private const val HEADING_TEXT_SIZE_PX = 50f
private const val LABEL_TEXT_SIZE_PX = 31f
private const val AMOUNT_TEXT_SIZE_PX = 60f
private const val DIVIDER_STROKE_PX = 2f
private const val MAX_ADDRESS_LINES = 5
private const val RECEIPT_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
private const val RECEIPT_TEXT_COLOR = 0xFF102A43.toInt()
private const val RECEIPT_SECONDARY_COLOR = 0xFF52606D.toInt()
private const val RECEIPT_ACCENT_COLOR = 0xFF12805C.toInt()
private const val RECEIPT_DIVIDER_COLOR = 0xFFD9E2EC.toInt()
