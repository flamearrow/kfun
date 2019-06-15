@file:JvmName("ImageUtils")

package band.mlgb.kfun

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.IOException


@Throws(IOException::class)
fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {

    val input = context.contentResolver.openInputStream(selectedImage)!!
    val ei: ExifInterface
    ei = if (Build.VERSION.SDK_INT > 23)
        ExifInterface(input)
    else
        ExifInterface(selectedImage.path!!)

    return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
        else -> img
    }
}

private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
    return Matrix().let {
        it.postRotate(degree)
        val ret = Bitmap.createBitmap(img, 0, 0, img.width, img.height, it, true)
        img.recycle()
        ret
    }
}

fun Image.toByteArray(): ByteArray {
    val buffer = planes[0].buffer
//    val pixels = ByteArray(buffer.remaining())
    val pixels = ByteArray(23)
//    buffer.get(pixels)
//    System.arraycopy(buffer, 0, bytes, 0, buffer.capacity())
    return pixels
}

fun Image.toBitmap(): Bitmap {



    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)


//    val buffer = planes[0].buffer
//    val pixelStride = planes[0].pixelStride
//    val rowStride = planes[0].rowStride
//    val rowPadding = rowStride - pixelStride * width
//    var bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
//    bitmap.copyPixelsFromBuffer(buffer)
//    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
//    return bitmap
}

fun ImageToByteArray(image:Image): ByteArray {
    val buffer = image.planes[0].buffer
//    val pixels = ByteArray(buffer.remaining())
    val pixels = ByteArray(23)
//    buffer.get(pixels)
//    System.arraycopy(buffer, 0, bytes, 0, buffer.capacity())
    return pixels
}
