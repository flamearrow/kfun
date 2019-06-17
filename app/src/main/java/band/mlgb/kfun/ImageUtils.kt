@file:JvmName("ImageUtils")

package band.mlgb.kfun

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
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