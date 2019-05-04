package band.mlgb.kfun

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class LandmarkDetectorActivity : PickImageActivity() {
    private var landmarkDetector: FirebaseVisionCloudLandmarkDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseVisionCloudDetectorOptions.Builder()
            .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
            .setMaxResults(15)
            .build().let {
                landmarkDetector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector(it)
            }

    }

    override fun handleImage(bitmap: Bitmap) {
        toggleLoading(true)
        FirebaseVisionImage.fromBitmap(bitmap).let {
            landmarkDetector?.detectInImage(it)?.addOnSuccessListener { landmarks ->
                if (landmarks.isEmpty()) {
                    Toast.makeText(applicationContext, "no landmarks found", Toast.LENGTH_SHORT).show()
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
                        "${landmark.landmark} with entityId ${landmark.entityId}")
                    sb.appendln("confidence: ${landmark.confidence}")

                    // Multiple locations are possible, e.g., the location of the depicted
                    // landmark and the location the picture was taken.
                    for (loc in landmark.locations) {
                        val latitude = loc.latitude
                        val longitude = loc.longitude
                    }
                }
                toggleLoading(false)
                postResult(sb.toString())
            }?.addOnFailureListener { result ->
                toggleLoading(false)
                Toast.makeText(applicationContext, result.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

}