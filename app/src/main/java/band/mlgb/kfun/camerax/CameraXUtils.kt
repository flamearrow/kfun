package band.mlgb.kfun.camerax

import android.graphics.Matrix
import android.os.Handler
import android.os.HandlerThread
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.camera.core.*

fun createPreviewUsecase(viewFinder: TextureView, lenseFacing: CameraX.LensFacing = CameraX.LensFacing.BACK): Preview {
    return Preview(
        PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 640))
            setLensFacing(lenseFacing)
        }.build()
    ).apply {
        // Every time the viewfinder is updated, recompute layout
        setOnPreviewOutputUpdateListener {
            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform(viewFinder)
        }
    }
}

fun createImageCaptureUsecase(lenseFacing: CameraX.LensFacing = CameraX.LensFacing.BACK): ImageCapture {
    return ImageCapture(
        ImageCaptureConfig.Builder()
            .apply {
                setTargetAspectRatio(Rational(1, 1))
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                setLensFacing(lenseFacing)
            }.build()
    )
}

fun createAnalysisUsecase(
    resultUpdater: LiveImageAnalyzer.LiveResultUpdater,
    lenseFacing: CameraX.LensFacing = CameraX.LensFacing.BACK
): ImageAnalysis {
    // Build the image analysis use case and instantiate our analyzer
    return ImageAnalysis(
        ImageAnalysisConfig.Builder().apply {
            // Use a worker thread for image analysis to prevent glitches
            val analyzerThread = HandlerThread(
                "LiveCameraAnalyze"
            ).apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
            )
            setLensFacing(lenseFacing)
        }.build()
    ).apply {
        analyzer = LiveImageAnalyzer(resultUpdater)
    }
}

fun updateTransform(viewFinder: TextureView) {
    val matrix = Matrix()

    // Compute the center of the view finder
    val centerX = viewFinder.width / 2f
    val centerY = viewFinder.height / 2f

    // Correct preview output to account for display rotation
    val rotationDegrees = when (viewFinder.display.rotation) {
        Surface.ROTATION_0 -> 0
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> return
    }
    matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

    // Finally, apply transformations to our TextureView
    viewFinder.setTransform(matrix)
}