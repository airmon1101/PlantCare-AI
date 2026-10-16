package com.plantcareai.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for image handling: loading, resizing, rotation-correction,
 * and file management for plant photos.
 */
object ImageUtils {

    private const val MAX_IMAGE_DIMENSION = 1024
    private const val JPEG_QUALITY = 85

    /**
     * Loads a bitmap from a content URI, automatically downscaling if needed
     * and correcting EXIF rotation.
     *
     * @param context Android context for content resolver access.
     * @param uri Content URI of the image.
     * @return Decoded and corrected [Bitmap], or null on failure.
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            // First pass: decode bounds only
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // Calculate sample size for efficient memory usage
            options.inSampleSize = calculateInSampleSize(
                options.outWidth, options.outHeight, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION
            )
            options.inJustDecodeBounds = false

            // Second pass: decode the actual bitmap
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            // Correct rotation from EXIF data
            val rotation = getExifRotation(context, uri)
            if (rotation != 0) {
                rotateBitmap(bitmap, rotation)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Saves a bitmap to the app's internal storage for persistence.
     *
     * @param context Android context.
     * @param bitmap The bitmap to save.
     * @param conversationId Conversation ID for organizing files.
     * @return Absolute path of the saved file, or null on failure.
     */
    fun saveBitmapToInternal(
        context: Context,
        bitmap: Bitmap,
        conversationId: String
    ): String? {
        return try {
            val dir = File(context.filesDir, "plant_images/$conversationId")
            dir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(dir, "plant_${timestamp}.jpg")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }

            file.absolutePath
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Creates a temporary file for camera capture.
     */
    fun createTempImageFile(context: Context): File {
        val dir = File(context.cacheDir, "camera_photos")
        dir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(dir, "CAMERA_${timestamp}.jpg")
    }

    /**
     * Loads a bitmap from a file path.
     */
    fun loadBitmapFromPath(path: String): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            options.inSampleSize = calculateInSampleSize(
                options.outWidth, options.outHeight, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION
            )
            options.inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(
        width: Int, height: Int,
        reqWidth: Int, reqHeight: Int
    ): Int {
        var sampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / sampleSize) >= reqHeight &&
                (halfWidth / sampleSize) >= reqWidth
            ) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }

    private fun getExifRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                when (exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
