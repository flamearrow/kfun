package band.mlgb.kfun

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import band.mlgb.kfun.camerax.CameraXActivity

class EntryActivity : KFunBaseActivity() {
    companion object {
        const val CAMERA_X = 1 shl 1;
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entry_activity)
    }

    fun imageLabeling(view: View) {
        startActivity(Intent(applicationContext, ImageLabelingActivity::class.java))
    }

//    fun textDetect(view: View) {
//        startActivity(Intent(applicationContext, TextDetectionActivity::class.java))
//    }

    fun faceDetect(view: View) {
        startActivity(Intent(applicationContext, FaceDetectActivity::class.java))
    }

//    fun barcodeDetect(view: View) {
//        startActivity(Intent(applicationContext, BarcodeDetectorActivity::class.java))
//    }

//    fun landmarkDetect(view: View) {
//        startActivity(Intent(applicationContext, LandmarkDetectorActivity::class.java))
//    }

    fun langDetect(view: View) {
        startActivity(Intent(applicationContext, LangIdActivity::class.java))
    }

    fun smartReply(view: View) {
        startActivity(Intent(applicationContext, SmartReplyActivity::class.java))
    }

    fun translate(view: View) {
        startActivity(Intent(applicationContext, TranslatorActivity::class.java))
    }

    fun cameraX(view: View) {
        startActivityForResult(Intent(applicationContext, CameraXActivity::class.java), CAMERA_X)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            CAMERA_X -> {
                toastShort("mlgb")
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}