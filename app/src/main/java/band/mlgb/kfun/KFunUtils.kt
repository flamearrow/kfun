package band.mlgb.kfun

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
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

/**
 * prints out shit to mlgba
 */
class MLGBALifeCycleObserver : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun a() {
        Log.d("mlgba", "ON_RESUME")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun b() {
        // will be automatically called
        Log.d("mlgba", "ON_PAUSE")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun c() {
        // will be automatically called
        Log.d("mlgba", "ON_START")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun d() {
        // will be automatically called
        Log.d("mlgba", "ON_DESTROY")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun e() {
        // will be automatically called
        Log.d("mlgba", "ON_CREATE")
    }
}