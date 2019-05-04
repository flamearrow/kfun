package band.mlgb.kfun

import android.graphics.Bitmap
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import java.util.*

// OCR
class TextDetectionActivity : PickImageActivity() {
    override fun handleImage(bitmap: Bitmap) {
        toggleLoading(true)
        val image = FirebaseVisionImage.fromBitmap(bitmap)
//        val detector = FirebaseVision.getInstance().cloudTextRecognizer
//        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        // supported languages: https://cloud.google.com/vision/docs/languages
        val detector = FirebaseVision.getInstance().getCloudTextRecognizer(
            FirebaseVisionCloudTextRecognizerOptions.Builder().setLanguageHints(Arrays.asList("en", "cn")).build()
        )
        detector.processImage(image).addOnSuccessListener {
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
                    postResult(sb.toString())
                } else {
                    Toast.makeText(applicationContext, "nothing detected", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, "nothing detected", Toast.LENGTH_SHORT).show()

            }
        }.addOnFailureListener { result ->
            toggleLoading(false)
            Toast.makeText(applicationContext, result.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }
}