package band.mlgb.kfun

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

abstract class KFunBaseActivity : AppCompatActivity() {
    fun toastShort(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
}