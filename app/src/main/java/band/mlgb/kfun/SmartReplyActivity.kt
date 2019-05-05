package band.mlgb.kfun

import android.os.Bundle
import band.mlgb.kfun.inject.DaggerFirebaseComponent
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult
import javax.inject.Inject

class SmartReplyActivity : InputTextActivity() {

    @Inject
    lateinit var smartReplyHandler: FirebaseSmartReply

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerFirebaseComponent.create().inject(this)
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

// extension function, this is not necessary at all, just fun to mess around
private fun FirebaseSmartReply?.suggestReplies(singleMessage: FirebaseTextMessage?): Task<SmartReplySuggestionResult>? {
    return this?.suggestReplies(mutableListOf(singleMessage))
}
