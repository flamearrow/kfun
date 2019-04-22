package band.mlgb.kfun

import android.graphics.Bitmap
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions

// Object detection
class ImageLabelingActivity : PickImageActivity() {
    override fun handleImage(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        // get labeler with threshold
        val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(
            FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build()
        )

        // use object: blah to create an actual listener instance
//        labeler.processImage(image).addOnSuccessListener(object: OnSuccessListener<List<FirebaseVisionImageLabel>> {
//            override fun onSuccess(p0: List<FirebaseVisionImageLabel>?) {
//            }
//
//        })
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
