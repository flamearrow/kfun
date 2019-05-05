package band.mlgb.kfun

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_input_text.*
import kotlinx.android.synthetic.main.activity_pick_image.result

abstract class InputTextActivity : KFunBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_text)
        fire.text = buttonText()
        hintText()?.let {
            input.hint = it
        }
    }

    abstract fun buttonText(): String

    open fun hintText(): String? {
        return null
    }

    fun postResult(resultString: String) {
        result.text = resultString
    }

    fun fire(view: View) {
        result.text = ""
        handleText(input.text?.toString()!!)
    }

    abstract fun handleText(input: String)
}