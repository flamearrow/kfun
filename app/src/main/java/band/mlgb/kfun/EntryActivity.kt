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
}