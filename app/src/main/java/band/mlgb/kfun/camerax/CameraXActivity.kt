package band.mlgb.kfun.camerax

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import band.mlgb.kfun.KFunBaseActivity
import band.mlgb.kfun.MLGBALifeCycleObserver
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
 * Used to take a picture and save to a file, the file needs to be passed from invoking activity.
 */
class CameraXActivity : KFunBaseActivity() {
    companion object {
        const val FILE_TO_SAVE = "FILE_TO_SAVE"
        val TAG = CameraXActivity::javaClass.name
    }


    private var file: File? = null
    private lateinit var previewUseCase: Preview
    private lateinit var imageCaptureUseCase: ImageCapture


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_x_activity)


        file = intent.extras?.let { it[FILE_TO_SAVE] as File } // ?: File("dumbFile")


        // image capture
        imageCaptureUseCase = createImageCaptureUseCase()

        // preview
        previewUseCase = createPreviewUseCase(view_finder.surfaceProvider)

        capture_button.setOnClickListener { takePhoto() }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        lifecycle.addObserver(MLGBALifeCycleObserver())
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // run when this future is done on main executor
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    previewUseCase,
                    imageCaptureUseCase
                )
            } catch (exec: Exception) {
                Log.e(TAG, "use case binding failed", exec)
            }
        }, ContextCompat.getMainExecutor(this))

    }

    fun takePhoto() {
        file?.let {
            imageCaptureUseCase.takePicture(
                ImageCapture.OutputFileOptions.Builder(it).build(),
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.d(TAG, "Image saved")
                        setResult(RESULT_OK)
                        finish()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        toastShort("Image save error")
                    }
                }
            )
        } ?: run {
            toastShort("No file to save, take nothing")
        }
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
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