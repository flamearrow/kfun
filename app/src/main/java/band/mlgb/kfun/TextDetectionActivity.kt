package band.mlgb.kfun

import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import band.mlgb.kfun.inject.DaggerFirebaseComponent
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import javax.inject.Inject

@Deprecated("Firebase API outdated")
class TextDetectionActivity : PickImageActivity() {
    @Inject
    lateinit var firebaseVisionTextRecognizer: FirebaseVisionTextRecognizer

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
        firebaseVisionTextRecognizer.processImage(image).addOnSuccessListener {
            // of Type FireBaseVisionText
                firebaseVisionText ->
            toggleLoading(false)
            val blocks = firebaseVisionText.textBlocks
            // only take the first result
            if (blocks.isNotEmpty()) {
                val sb = StringBuilder()
                for (block in blocks) {
                    val blockText = block.text
                    val blockConfidence = block.confidence
                    val blockLanguages = block.recognizedLanguages
                    val blockCornerPoints = block.cornerPoints
                    val blockFrame = block.boundingBox
                    for (line in block.lines) {
                        val lineText = line.text
                        sb.appendln(lineText)
                        val lineConfidence = line.confidence
                        val lineLanguages = line.recognizedLanguages
                        val lineCornerPoints = line.cornerPoints
                        val lineFrame = line.boundingBox
                        for (element in line.elements) {
                            val elementText = element.text
                            val elementConfidence = element.confidence
                            val elementLanguages = element.recognizedLanguages
                            val elementCornerPoints = element.cornerPoints
                            val elementFrame = element.boundingBox
                        }
                    }
                    sb.appendln("---------")
                }
                if (sb.isNotEmpty()) {
                    handleResult(sb.toString())
                } else {
                    handleEmptyResult()
                }
            } else {
                handleEmptyResult()

            }
        }.addOnFailureListener { result ->
            toggleLoading(false)
            toastShort(result.localizedMessage)
        }
    }

    override fun handleImage(bitmap: Bitmap) {
        processImage(FirebaseVisionImage.fromBitmap(bitmap), ::postResult) {
            toastShort("nothing detected")
        }
    }

    override fun handleLiveImage(image: Image, rotation: Int) {
        processImage(FirebaseVisionImage.fromMediaImage(image, rotation), ::postLiveResult) {}
    }
}