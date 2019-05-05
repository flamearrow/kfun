package band.mlgb.kfun

import android.os.Bundle
import band.mlgb.kfun.inject.DaggerFirebaseComponent
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification
import javax.inject.Inject

class LangIdActivity : InputTextActivity() {
    @Inject
    lateinit var langIdIdentifier: FirebaseLanguageIdentification

    override fun buttonText(): String {
        return "Detect language"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerFirebaseComponent.create().inject(this)
    }

    override fun hintText(): String? {
        return "Input or paste some text here to check what languages it is"
    }

    override fun handleText(input: String) {
        // return all languages
        langIdIdentifier?.identifyPossibleLanguages(input)?.addOnSuccessListener { languages ->
            if (languages.isEmpty() || (languages[0].languageCode == "und")) {
                toastShort("no language detected")
                return@addOnSuccessListener
            }
            val sb = StringBuilder()
            for (lang in languages) {
                sb.appendln("${lang.languageCode} : ${lang.confidence}")
            }
            postResult(sb.toString())
        }?.addOnFailureListener { exception ->
            toastShort(exception.localizedMessage)
        }
//        langIdIdentifier?.identifyLanguage(input)?.addOnSuccessListener {
//            postResult(it)
//        }?.addOnFailureListener { exception ->
//            toastShort(exception.localizedMessage)
//        }
    }
}