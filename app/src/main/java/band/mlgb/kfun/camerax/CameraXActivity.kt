package band.mlgb.kfun.camerax

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import band.mlgb.kfun.KFunBaseActivity
import band.mlgb.kfun.R
import kotlinx.android.synthetic.main.camera_x_activity.*
import java.io.File


// This is an arbitrary number we are using to keep tab of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

/**
 * This activity takes a file from intent and use cameraX to take a picture and save to the file
 */
class CameraXActivity : KFunBaseActivity() {
    companion object {
        const val IMAGE_TAKEN = "IMAGE_TAKEN"
        const val FILE_TO_SAVE = "FILE_TO_SAVE"
    }

    // TextureView is used to display content stream, use it for cameraX
    // to update its content, override TextureView.surfaceTexture, CameraX's Preview provides this
    private lateinit var viewFinder: TextureView

    private var file: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_x_activity)


        intent.extras?.let {
            file = it[FILE_TO_SAVE] as File
        }

        viewFinder = view_finder

        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private fun startCamera() {
        // Set up viewFinder, create a Preview object as a usecase
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 640))
        }.build()

        val previewUseCase = Preview(previewConfig)
        // Every time the viewfinder is updated, recompute layout
        previewUseCase.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }


        // set up image capture button, create a ImageCapture as a usecase
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setTargetAspectRatio(Rational(1, 1))
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()

        val imageCaptureUseCase = ImageCapture(imageCaptureConfig)
//        capture_button.setOnClickListener {
//            imageCaptureUseCase.takePicture(
//                object : ImageCapture.OnImageCapturedListener() {
//                    override fun onCaptureSuccess(image: ImageProxy?, rotationDegrees: Int) {
////                        finish()
//                        finishActivityWithByteArrayConvertedFromMediaImage(image!!.image!!)
//
////                        image?.image?.let {
////                            finishActivityWithByteArrayConvertedFromMediaImage(it)
////                        }
//                    }
//
//                    override fun onError(
//                        useCaseError: ImageCapture.UseCaseError?,
//                        message: String?,
//                        cause: Throwable?
//                    ) {
//                        message?.let { toastShort(it) }
//                    }
//                }
//            )
//
//        }


        capture_button.setOnClickListener {
            file?.also {
                //only take picture when file is non null
                imageCaptureUseCase.takePicture(file,
                    object : ImageCapture.OnImageSavedListener {
                        override fun onImageSaved(file: File) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }

                        override fun onError(
                            useCaseError: ImageCapture.UseCaseError,
                            message: String,
                            cause: Throwable?
                        ) {
                            toastShort(message)
                        }

                    })
            }

        }

//        capture_button.setOnClickListener {
//            imageCaptureUseCase.takePicture(
//                object : ImageCapture.OnImageCapturedListener() {
//                    override fun onCaptureSuccess(image: ImageProxy?, rotationDegrees: Int) {
//                        // finish without returning anything, this would correctly finish current activty and return
//                        // finish() - ok
//
//                        // finish with a bitmap converted from image - stuck
//                        val intent = Intent()
//                        val buffer = image!!.planes[0].buffer
//                        val bytes = ByteArray(buffer.capacity())
//                        buffer.get(bytes)
//                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
//                        intent.putExtra(IMAGE_TAKEN, bitmap)
//                        setResult(Activity.RESULT_OK, intent)
//                        image.close()
//                        finish() // gets stuck, never finish, runOnUiThread doesn't work
//                    }
//
//                    override fun onError(
//                        useCaseError: ImageCapture.UseCaseError?,
//                        message: String?,
//                        cause: Throwable?
//                    ) {
//                        message?.let { toastShort(it) }
//                    }
//                }
//            )
//        }

        // Setup image analysis pipeline that computes average pixel luminance, creates a ImageAnalysis as a usecase
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // Use a worker thread for image analysis to prevent glitches
            val analyzerThread = HandlerThread(
                "LuminosityAnalysis"
            ).apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
            )
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            analyzer = ImageAnalyzer()
        }

        // Bind use cases to lifecycle, both preview and imageCapture are usecases
        CameraX.bindToLifecycle(this, previewUseCase, imageCaptureUseCase, analyzerUseCase)
    }

    private fun finishActivityWithByteArrayConvertedFromMediaImage(image: Image) {
        val intent = Intent()
//        intent.putExtra(IMAGE_TAKEN, image.toByteArray())
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        intent.putExtra(IMAGE_TAKEN, bitmap)
        setResult(Activity.RESULT_OK, intent)
        image.close()
        finish()
//        Intent().let {
//            it.putExtra(IMAGE_TAKEN, image.toByteArray())
//            setResult(Activity.RESULT_OK, it)
//            image.close()
//            finish()
//        }
    }

    private fun updateTransform() {
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

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }
}