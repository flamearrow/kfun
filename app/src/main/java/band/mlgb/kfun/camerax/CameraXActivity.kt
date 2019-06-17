package band.mlgb.kfun.camerax

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.widget.Toast
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
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
            updateTransform(viewFinder)
        }

        lifecycle.addObserver(MLGBALifeCycleObserver())
    }

    private fun startCamera() {
        val imageCaptureUseCase = createImageCaptureUsecase()
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

        // Bind use cases to lifecycle, both preview and imageCapture are usecases
        CameraX.bindToLifecycle(this, createPreviewUsecase(viewFinder), imageCaptureUseCase)
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