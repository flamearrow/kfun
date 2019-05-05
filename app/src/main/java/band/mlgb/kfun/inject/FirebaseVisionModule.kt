package band.mlgb.kfun.inject

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import dagger.Module
import dagger.Provides
import dagger.Reusable
import java.util.*
import javax.inject.Named

@Module
object FirebaseVisionModule {
    @Provides
    @Reusable
    @JvmStatic
    fun provideFirebaseVision(): FirebaseVision {
        return FirebaseVision.getInstance()
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideFirebaseVisionTextRecognizer(
        fv: FirebaseVision
    ): FirebaseVisionTextRecognizer {
        // supported languages: https://cloud.google.com/vision/docs/languages
        return fv.getCloudTextRecognizer(
            FirebaseVisionCloudTextRecognizerOptions.Builder().setLanguageHints(
                Arrays.asList(
                    "en",
                    "cn"
                )
            ).build()
        )
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideFirebaseVisionImageLabeler(
        fv: FirebaseVision
    ): FirebaseVisionImageLabeler {
        return fv.getOnDeviceImageLabeler(
            FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build()
        )
    }

    @Provides
    @Reusable
    @JvmStatic
    fun provideFirebaseVisionCloudLandmarkDetector(
        fv: FirebaseVision
    ): FirebaseVisionCloudLandmarkDetector {
        return fv.getVisionCloudLandmarkDetector(
            FirebaseVisionCloudDetectorOptions.Builder()
                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                .setMaxResults(15)
                .build()
        )
    }

    @Provides
    @Reusable
    @JvmStatic
    @Named("accurate")
    fun provideHighAccuracyFaceDetector(
        fv: FirebaseVision
    ): FirebaseVisionFaceDetector {
        return fv.getVisionFaceDetector(
            FirebaseVisionFaceDetectorOptions.Builder().setPerformanceMode(
                FirebaseVisionFaceDetectorOptions.ACCURATE
            ).setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS).build()
        )
    }

        @Provides
        @Reusable
        @JvmStatic
        @Named("realtime")
        fun provideRealtimeFaceDetector(
            fv: FirebaseVision
        ): FirebaseVisionFaceDetector {
            return fv.getVisionFaceDetector(
                FirebaseVisionFaceDetectorOptions.Builder()
                    .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                    .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                    .build()
            )
        }
}