package band.mlgb.kfun.camerax

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.util.concurrent.TimeUnit

/**
 *  An [ImageAnalysis.Analyzer] to provide the current image data every second, this needs to be registered
 *  into CameraX's [ImageAnalysis] usecase.
 */
class LiveImageAnalyzer(private val resultUpdater: LiveResultUpdater) : ImageAnalysis.Analyzer {

    interface LiveResultUpdater {
        fun postNewImage(image: Image)
    }

    private var lastAnalyzedTimestamp = 0L

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >=
            TimeUnit.SECONDS.toMillis(1)
        ) {
            image.image?.let {
                resultUpdater.postNewImage(it)
            }
            // Update timestamp of last analyzed frame
            lastAnalyzedTimestamp = currentTimestamp
        }
    }
}