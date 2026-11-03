package com.izimboa.livreur.onboarding

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Photos des documents : prises par l'appareil photo du téléphone dans un fichier temporaire, puis réduites
 * (1600 px, JPEG) pour l'envoi sur un réseau mobile lent. Le fichier d'origine est effacé aussitôt.
 */
object Photos {

    private const val MAX_SIDE = 1600

    /** Fichier temporaire où l'appareil photo écrit la photo. */
    fun newCapture(context: Context): Pair<File, Uri> {
        val dir = File(context.cacheDir, "photos").apply { mkdirs() }
        val file = File(dir, "capture-${System.currentTimeMillis()}.jpg")
        return file to FileProvider.getUriForFile(context, context.packageName + ".photos", file)
    }

    /** Réduit la photo, la remet droite (orientation de l'appareil) et efface le fichier d'origine. */
    fun shrink(file: File): ByteArray? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= MAX_SIDE || bounds.outHeight / (sample * 2) >= MAX_SIDE) sample *= 2
        val raw = BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
        if (raw == null) null else {
            val rotation = when (ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            val scale = minOf(1f, MAX_SIDE.toFloat() / maxOf(raw.width, raw.height))
            val m = Matrix().apply { postScale(scale, scale); postRotate(rotation) }
            val bmp = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
            ByteArrayOutputStream().use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 82, out); out.toByteArray() }
        }
    } catch (e: Exception) {
        null
    } finally {
        file.delete()
    }

    /** Appareil photo du téléphone ; la caméra avant est demandée pour le selfie (si l'appareil le permet). */
    class TakePhoto : ActivityResultContract<TakePhoto.Request, Boolean>() {
        data class Request(val uri: Uri, val front: Boolean)

        override fun createIntent(context: Context, input: Request): Intent =
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, input.uri)
                clipData = ClipData.newRawUri("photo", input.uri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (input.front) {
                    putExtra("android.intent.extras.CAMERA_FACING", 1)
                    putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                    putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                }
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean = resultCode == Activity.RESULT_OK
    }
}
