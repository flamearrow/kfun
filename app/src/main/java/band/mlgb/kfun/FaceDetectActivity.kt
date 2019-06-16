package band.mlgb.kfun

import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import band.mlgb.kfun.inject.DaggerFirebaseComponent
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import javax.inject.Inject
import javax.inject.Named

class FaceDetectActivity : PickImageActivity() {
    // see https://kotlinlang.org/docs/reference/annotations.html for weird annotation
    // TlDR: when annotating a property or a primary constructor parameter,
    // weird prefixes(aka Annotation Use-site Targets) need to be applied,
    // here @field is added in front of @Named to make sense of it.
    // There are other prefixes to be applied also.
    @Inject
    @field:Named("accurate")
    lateinit var highAccuracyFaceDetector: FirebaseVisionFaceDetector
    @Inject
    @field:Named("realtime")
    lateinit var realtimeFaceDetector: FirebaseVisionFaceDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerFirebaseComponent.create().inject(this)

    }

    private fun processImage(
        faceDector: FirebaseVisionFaceDetector,
        fbImage: FirebaseVisionImage,
        handleResult: (String) -> Unit,
        handleEmptyResult: () -> Unit
    ) {
        toggleLoading(true)
        faceDector.detectInImage(fbImage)?.addOnSuccessListener { faces ->
            toggleLoading(false)
            if (faces.isEmpty()) {
                handleEmptyResult()
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

            handleResult(sb.toString())
        }?.addOnFailureListener { result ->
            toggleLoading(false)
            toastShort(result.localizedMessage)
        }

    }


    override fun handleImage(bitmap: Bitmap) {
        processImage(highAccuracyFaceDetector, FirebaseVisionImage.fromBitmap(bitmap), ::postResult) {
            toastShort("no face detected")
        }
    }

    override fun handleLiveImage(image: Image, rotation: Int) {
        processImage(realtimeFaceDetector, FirebaseVisionImage.fromMediaImage(image, rotation), ::postLiveResult) {}
    }

}