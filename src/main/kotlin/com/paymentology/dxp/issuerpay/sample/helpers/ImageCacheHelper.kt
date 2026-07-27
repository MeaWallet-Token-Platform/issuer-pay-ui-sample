package com.paymentology.dxp.issuerpay.sample.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import java.io.File

private const val cardImageCacheDir: String = "card_image_cache"

fun saveBitmapToCacheAndGetUri(context: Context, bitmap: Bitmap): String {
    // Ensure the cache subdirectory exists
    val cacheSubdir = File(context.cacheDir, cardImageCacheDir)
    if (!cacheSubdir.exists()) {
        cacheSubdir.mkdirs()
    }
    // Save bitmap to a file in the cache subdirectory
    val file = File(cacheSubdir, "card_bg_${System.currentTimeMillis()}.png")
    file.outputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    // Get content Uri using FileProvider
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider", // Ensure provider is declared in manifest
        file
    )
    // Return the Uri as a string for passing in PaymentCardConfig
    return uri.toString()
}

fun clearCardImageCache(context: Context) {
    val cacheSubdir = File(context.cacheDir, cardImageCacheDir)
    if (cacheSubdir.exists()) {
        cacheSubdir.listFiles()?.forEach { it.delete() }
    }
}

fun getBitmapFromAssets(context: Context, assetPath: String): Bitmap? {
    return try {
        context.assets.open(assetPath).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}