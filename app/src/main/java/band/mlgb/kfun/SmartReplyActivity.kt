package band.mlgb.kfun

import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult
import java.lang.StringBuilder

class SmartReplyActivity : InputTextActivity() {

    var smartReplyHandler: FirebaseSmartReply? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smartReplyHandler = FirebaseNaturalLanguage.getInstance().smartReply
    }

    override fun buttonText(): String {
        return "Suggest Reply"
    }

    override fun hintText(): String? {
        return "Input some text here and get suggestions on replying them"
    }

    override fun handleText(input: String) {
        FirebaseTextMessage.createForLocalUser(input, System.currentTimeMillis()).let {
            smartReplyHandler.suggestReplies(it)?.addOnSuccessListener { result ->
                if (result.status == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                    toastShort("this language is not supported")
                } else if (result.status == SmartReplySuggestionResult.STATUS_SUCCESS) {
                    val sb = StringBuilder()
                    for (suggestion in result.suggestions) {
                        sb.appendln("${suggestion.text} : ${suggestion.confidence}")
                    }
                    postResult(sb.toString())

                }
            }?.addOnFailureListener { exception -> toastShort(exception.localizedMessage) }

        }
    }
}

// extension function
private fun FirebaseSmartReply?.suggestReplies(singleMessage: FirebaseTextMessage?): Task<SmartReplySuggestionResult>? {
    return this?.suggestReplies(mutableListOf(singleMessage))
}
