package band.mlgb.kfun.inject

import band.mlgb.kfun.*
import dagger.Component

@Component(modules = [FirebaseNlpModule::class, FirebaseVisionModule::class])
interface FirebaseComponent {
    fun inject(activity: TextDetectionActivity)
    fun inject(activity: LangIdActivity)
    fun inject(activity: SmartReplyActivity)
    fun inject(activity: LandmarkDetectorActivity)
    fun inject(activity: ImageLabelingActivity)
    fun inject(activity: FaceDetectActivity)
}