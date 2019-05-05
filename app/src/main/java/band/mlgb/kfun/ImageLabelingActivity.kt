package band.mlgb.kfun

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import band.mlgb.kfun.inject.DaggerFirebaseComponent
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import javax.inject.Inject

// Object detection
class ImageLabelingActivity : PickImageActivity() {
    @Inject
    lateinit var labeler: FirebaseVisionImageLabeler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerFirebaseComponent.create().inject(this)
    }
    override fun handleImage(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        labeler.processImage(image).addOnSuccessListener { labels ->
            if (labels.size <= 0) {
                Toast.makeText(applicationContext, "nothing detected", Toast.LENGTH_SHORT).show()
            }
            val sb = StringBuilder()

            for (label in labels) {
                sb.append("text: ").append(label.text).append("\n").append("entityId: ").append(label.entityId)
                    .append("\n").append("confidence: ").append(label.confidence).append("\n").append("---------")
                    .append("\n")
            }


            if (sb.isNotEmpty()) {
                postResult(sb.toString())
            } else {
                Toast.makeText(applicationContext, "nothing detected", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
