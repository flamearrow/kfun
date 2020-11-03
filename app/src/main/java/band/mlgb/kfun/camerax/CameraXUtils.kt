package band.mlgb.kfun.camerax

import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import java.util.concurrent.Executor

fun createPreviewUseCase(provider: Preview.SurfaceProvider): Preview {
    return Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .build()
        .also { it.setSurfaceProvider(provider) }
}

fun createImageCaptureUseCase(): ImageCapture {
    return ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setTargetAspectRatio(AspectRatio.RATIO_4_3).build()
}


fun createAnalysisUseCase(
    executor: Executor,
    resultUpdater: LiveImageAnalyzer.LiveResultUpdater
): ImageAnalysis {
    return ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build()
        .also {
            it.setAnalyzer(executor, LiveImageAnalyzer(resultUpdater))
        }

}
