package band.mlgb.kfun.inject

import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentificationOptions
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
object FirebaseNlpModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideFirebaseNaturalLanguage(): FirebaseNaturalLanguage {
        return FirebaseNaturalLanguage.getInstance()
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideFirebaseLanguageIdentification(
        fnl: FirebaseNaturalLanguage
    ): FirebaseLanguageIdentification {
        // without options, the default threshold is 0.01
//        langIdIdentifier = FirebaseNaturalLanguage.getInstance().languageIdentification
        return fnl.getLanguageIdentification(
            FirebaseLanguageIdentificationOptions.Builder().setConfidenceThreshold(0.2f).build()
        )
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideFirebaseSmartReply(
        fnl: FirebaseNaturalLanguage
    ): FirebaseSmartReply {
        return fnl.smartReply
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideFirebaseTranslatorZN_EN(
        fnl: FirebaseNaturalLanguage
    ): FirebaseTranslator {
        return fnl.getTranslator(
            FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.ZH)
                .setTargetLanguage(FirebaseTranslateLanguage.EN)
                .build()
        )
    }
}