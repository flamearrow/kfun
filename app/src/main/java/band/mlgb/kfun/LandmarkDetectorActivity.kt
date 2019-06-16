package band.mlgb.kfun

import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import band.mlgb.kfun.inject.DaggerFirebaseComponent
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import javax.inject.Inject


class LandmarkDetectorActivity : PickImageActivity() {
    @Inject
    lateinit var landmarkDetector: FirebaseVisionCloudLandmarkDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerFirebaseComponent.create().inject(this)
    }

    private fun processImage(
        image: FirebaseVisionImage,
        handleResult: (String) -> Unit,
        handleEmptyResult: () -> Unit
    ) {
        toggleLoading(true)
        image.let {
            landmarkDetector.detectInImage(it)?.addOnSuccessListener { landmarks ->
                if (landmarks.isEmpty()) {
                    toggleLoading(false)
                    handleEmptyResult()
                    return@addOnSuccessListener
                }
                val sb = StringBuilder().also { sb ->
                    sb.appendln("found ${landmarks.size} landmarks")
                }

                for ((index, landmark) in landmarks.withIndex()) {
                    sb.appendln("landmark $index")
                    val bounds = landmark.boundingBox
                    val landmarkName = landmark.landmark
                    sb.appendln(
                        "${landmark.landmark} with entityId ${landmark.entityId}"
                    )
                    sb.appendln("confidence: ${landmark.confidence}")

                    // Multiple locations are possible, e.g., the location of the depicted
                    // landmark and the location the picture was taken.
                    for (loc in landmark.locations) {
                        val latitude = loc.latitude
                        val longitude = loc.longitude
                    }
                }
                toggleLoading(false)
                handleResult(sb.toString())
            }?.addOnFailureListener { result ->
                toggleLoading(false)
                toastShort(result.localizedMessage)
            }
        }

    }

    override fun handleImage(bitmap: Bitmap) {
        processImage(FirebaseVisionImage.fromBitmap(bitmap), ::postResult) { toastShort("no land marks found") }
    }

    override fun handleLiveImage(image: Image, rotation: Int) {
        processImage(FirebaseVisionImage.fromMediaImage(image, rotation), ::postLiveResult) {}
    }

}