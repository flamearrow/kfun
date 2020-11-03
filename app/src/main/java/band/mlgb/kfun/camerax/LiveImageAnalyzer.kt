package band.mlgb.kfun.camerax

import android.media.Image
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
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

    @ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        Log.d("BGLM", "LiveImageAnalyzer.analyze")
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

        // need to close in order to trigger the next analyze call
        image.close()
    }
}

