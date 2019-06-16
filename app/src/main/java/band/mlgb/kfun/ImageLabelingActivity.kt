package band.mlgb.kfun

import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import band.mlgb.kfun.inject.DaggerFirebaseComponent
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import javax.inject.Inject

// Object detection
class ImageLabelingActivity : PickImageActivity() {
    @Inject
    lateinit var labeler: FirebaseVisionImageLabeler


    private fun processImage(
        fbImage: FirebaseVisionImage,
        handleResult: (String) -> Unit,
        handleEmptyResult: () -> Unit
    ) {
        labeler.processImage(fbImage).addOnSuccessListener { labels ->
            if (labels.size <= 0) {
                handleEmptyResult()
            }
            val sb = StringBuilder()

            for (label in labels) {
                sb.append("text: ").append(label.text).append("\n").append("entityId: ").append(label.entityId)
                    .append("\n").append("confidence: ").append(label.confidence).append("\n").append("---------")
                    .append("\n")
            }
            handleResult(sb.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerFirebaseComponent.create().inject(this)
    }

    override fun handleImage(bitmap: Bitmap) {
        processImage(FirebaseVisionImage.fromBitmap(bitmap), ::postResult) { toastShort("nothing detected") }
    }

    override fun handleLiveImage(image: Image, rotation: Int) {
        processImage(FirebaseVisionImage.fromMediaImage(image, rotation), ::postLiveResult) {}
    }
}
