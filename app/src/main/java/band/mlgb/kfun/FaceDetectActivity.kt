package band.mlgb.kfun

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.*
import java.lang.StringBuilder

class FaceDetectActivity : PickImageActivity() {
    private var highAccuracyFaceDetector: FirebaseVisionFaceDetector? = null
    private var realtimeFaceDetector: FirebaseVisionFaceDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseVisionFaceDetectorOptions.Builder().setPerformanceMode(
            FirebaseVisionFaceDetectorOptions.ACCURATE
        ).setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS).build().let {
                highAccuracyFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(it)
            }
        FirebaseVisionFaceDetectorOptions.Builder()
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build().let {
                realtimeFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(it)
            }


    }

    override fun handleImage(bitmap: Bitmap) {
        toggleLoading(true)
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        highAccuracyFaceDetector?.detectInImage(image)?.addOnSuccessListener { faces ->
            if (faces.isEmpty()) {
                Toast.makeText(applicationContext, "no face detected", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }
            val sb = StringBuilder().also { it.appendln("Detected ${faces.size} faces") }

            for ((index, face) in faces.withIndex()) {
                sb.appendln("-------------")
                sb.appendln("face $index")

                val bounds = face.boundingBox
                val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                // nose available):
                val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
                leftEar?.let {
                    val leftEarPos = leftEar.position
                }

                // If contour detection was enabled:
                val leftEyeContour = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).points
                val upperLipBottomContour = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).points

                // If classification was enabled:
                if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                    sb.appendln("smile prob: ${face.smilingProbability}")
                }
                if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                    sb.appendln("left eye open prob: ${face.leftEyeOpenProbability}")
                }
                if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                    sb.appendln("right eye open prob: ${face.rightEyeOpenProbability}")
                }

                // If face tracking was enabled:
                if (face.trackingId != FirebaseVisionFace.INVALID_ID) {
                    sb.appendln("tracking ID: ${face.trackingId}")

                }
            }
            toggleLoading(false)
            postResult(sb.toString())
        }?.addOnFailureListener { result ->
            Toast.makeText(applicationContext, result.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

}