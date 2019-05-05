package band.mlgb.kfun

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import band.mlgb.kfun.inject.DaggerFirebaseComponent
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import javax.inject.Inject

// OCR
class TextDetectionActivity : PickImageActivity() {
    @Inject
    lateinit var firebaseVisionTextRecognizer: FirebaseVisionTextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerFirebaseComponent.create().inject(this)
    }

    override fun handleImage(bitmap: Bitmap) {
        toggleLoading(true)
        val image = FirebaseVisionImage.fromBitmap(bitmap)
//        val firebaseVisionTextRecognizer = FirebaseVision.getInstance().cloudTextRecognizer
//        val firebaseVisionTextRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
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