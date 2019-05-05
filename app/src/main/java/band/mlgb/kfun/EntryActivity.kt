package band.mlgb.kfun

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entry_activity)
    }

    fun imageLabeling(view: View) {
        startActivity(Intent(applicationContext, ImageLabelingActivity::class.java))
    }

    fun textDetect(view: View) {
        startActivity(Intent(applicationContext, TextDetectionActivity::class.java))
    }

    fun faceDetect(view: View) {
        startActivity(Intent(applicationContext, FaceDetectActivity::class.java))
    }

    fun barcodeDetect(view: View) {
        startActivity(Intent(applicationContext, BarcodeDetectorActivity::class.java))
    }

    fun landmarkDetect(view: View) {
        startActivity(Intent(applicationContext, LandmarkDetectorActivity::class.java))
    }

    fun langDetect(view: View) {
        startActivity(Intent(applicationContext, LangIdActivity::class.java))
    }

    fun smartReply(view: View) {
        startActivity(Intent(applicationContext, SmartReplyActivity::class.java))
    }
}