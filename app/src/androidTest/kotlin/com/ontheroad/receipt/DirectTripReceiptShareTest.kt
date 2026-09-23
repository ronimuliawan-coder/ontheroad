package com.ontheroad.receipt

import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DirectTripReceiptShareTest {

    @Test
    fun generatedPngIsReadableOnlyThroughTheReceiptProviderPath() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val content = DirectTripReceiptContent(
            completedAtMillis = 1_700_000_000_000L,
            pickupAddress = "Central Station",
            dropoffAddress = "Airport",
            actualDistanceMeters = 12_300.0,
            customerPaidTotalAmountCents = 250_000L
        )

        val receipt = createDirectTripReceiptImage(context, content)
        try {
            assertTrue(receipt.canonicalPath.startsWith(File(context.cacheDir, "direct-receipts").canonicalPath))
            val bitmap = BitmapFactory.decodeFile(receipt.absolutePath)
                ?: throw AssertionError("Receipt was not a decodable PNG")
            assertTrue(bitmap.width >= 1080)
            bitmap.recycle()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                receipt
            )
            assertEquals("content", uri.scheme)

            val outsideReceiptDirectory = File(context.cacheDir, "not-a-receipt.png")
            outsideReceiptDirectory.writeBytes(byteArrayOf(1))
            try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    outsideReceiptDirectory
                )
                throw AssertionError("FileProvider exposed a file outside the receipt folder")
            } catch (_: IllegalArgumentException) {
                // Expected: the provider grants access only to cache/direct-receipts/.
            } finally {
                outsideReceiptDirectory.delete()
            }
        } finally {
            receipt.delete()
        }
    }
}
