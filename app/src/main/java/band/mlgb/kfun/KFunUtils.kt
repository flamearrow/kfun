package band.mlgb.kfun

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import java.io.File


fun Uri.toFileWithAbsolutePath(contentResolver: ContentResolver): File? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    contentResolver.query(this, projection, null, null, null)?.let {
        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        it.moveToFirst()
        val s = it.getString(columnIndex)
        it.close()
        return File(s)
    }
    return null
}
