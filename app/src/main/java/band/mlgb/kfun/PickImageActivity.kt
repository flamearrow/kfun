package band.mlgb.kfun

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import band.mlgb.kfun.camerax.CameraXActivity
import band.mlgb.kfun.camerax.LiveImageAnalyzer
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_pick_image.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// This is an arbitrary number we are using to keep tab of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

/**
 * activity to open camera for live feed back or pick a image from gallery or camera
 */
abstract class PickImageActivity : KFunBaseActivity(), LiveImageAnalyzer.LiveResultUpdater {
    companion object {
        // shl shift left, shr shift right
        const val TAKE_PICTURE_WITH_CAMERA = 0x1 shl 1
        const val PICK_PICTURE_WITH_GALLERY = 0x1 shl 2
    }

    private var lastPicTakenUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_image)
        fab.setOnClickListener(takePicListener)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        result.movementMethod = ScrollingMovementMethod()
        initializeViewFinder()

    }

    private fun initializeViewFinder() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            view_finder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Every time the provided texture view changes, recompute layout
        view_finder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private fun startCamera() {
        // Set up view_finder, create a preview usecase and anaylzer usecase
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 1024))
        }.build()

        val previewUseCase = Preview(previewConfig)
        // Every time the view_finder is updated, recompute layout
        previewUseCase.setOnPreviewOutputUpdateListener {
            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = view_finder.parent as ViewGroup
            parent.removeView(view_finder)
            parent.addView(view_finder, 0)
            view_finder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }


        // Setup image analysis pipeline that computes average pixel luminance, creates a ImageAnalysis as a usecase
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
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
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            analyzer = LiveImageAnalyzer(this@PickImageActivity)
        }

        // Bind use cases to lifecycle, both preview and imageCapture are usecases
        CameraX.bindToLifecycle(this, previewUseCase, analyzerUseCase)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = view_finder.width / 2f
        val centerY = view_finder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (view_finder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        view_finder.setTransform(matrix)
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

    private val takePicListener: View.OnClickListener = View.OnClickListener {
        lastPicTakenUri = null
        result.text = null
        // In order to take a higher resolution picture, need to save the file to uri first
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
        Intent(applicationContext, CameraXActivity::class.java).also { intent ->
            // 'it' is overriden to 'intent'
            intent.resolveActivity(packageManager)?.also {
                // we only apply the following logic when
                // intent.resolveActivity(packageManager) returns non null,
                // but we're not holding any return value of it to call upon
                try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }?.also { file ->
                    // create a file, if success then fire the intent
                    FileProvider.getUriForFile(
                        this, // note this is always the current activity
                        "band.mlgb.kfun.fileprovider",
                        file // it here is file
                    )?.also { photoUri ->
                        // buffer the uri, need to access it when activity returns
                        lastPicTakenUri = photoUri
                        // create file and tell the intent to save pic to this uri
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
//                        intent.putExtra("mlgb", file.absolutePath)
                        intent.putExtra(CameraXActivity.FILE_TO_SAVE, file)
                        startActivityForResult(intent, TAKE_PICTURE_WITH_CAMERA)
                    }
                }

            }
        }
    }

    private val pickPicListener: View.OnClickListener = View.OnClickListener {
        // directly use intent here will get the inferred instance from getIntent() setIntent()
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_PICTURE_WITH_GALLERY)
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        image.setImageBitmap(null)
        result.text = ""
        when (item.itemId) {
            R.id.live_camera -> {
                // hide fab
                // hide image
                // show and initialize view_finder
                fab.visibility = GONE
                image.visibility = GONE
                live_container.visibility = VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.take_picture -> {
                // put lambda inside ()
//                button.setOnClickListener ({
//                    // take picture
//                    startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_WITH_CAMERA)
//                })

                // can put lambda outside () since it's the last parameter
//                button.setOnClickListener () {
//                    // take picture
//                    startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_WITH_CAMERA)
//                }

                // simpler version: omit (), directly add labmda, note there's a default reserved param 'it'
                fab.visibility = VISIBLE
                image.visibility = VISIBLE
                live_container.visibility = GONE
                fab.setOnClickListener(takePicListener)
                fab.setImageResource(R.drawable.baseline_camera_24)
                return@OnNavigationItemSelectedListener true
            }
            R.id.gallery -> {
                fab.visibility = VISIBLE
                image.visibility = VISIBLE
                live_container.visibility = GONE
                fab.setOnClickListener(pickPicListener)
                fab.setImageResource(R.drawable.baseline_photo_24)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // when == switch
        when (requestCode) {
            TAKE_PICTURE_WITH_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {

                    lastPicTakenUri?.let { nonNullUri ->
                        MediaStore.Images.Media.getBitmap(contentResolver!!, nonNullUri).let { bitmap ->
                            rotateImageIfRequired(this, bitmap, lastPicTakenUri!!).let { rotatedBitmap ->
                                displayImage(rotatedBitmap)
                                handleImage(rotatedBitmap)
                            }
                        }
                    }

//                    val bitmap = data?.extras!!["data"] as Bitmap
                    // ?. if data is null then return null
                    // !! if extras is null then throw NPE
//                    displayImage(bitmap)
//                    handleImage(bitmap)
                }

            }
            PICK_PICTURE_WITH_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    // ?: if data? is null, instead of return null, return Uri.Empty
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data?.data ?: Uri.EMPTY)
                    displayImage(bitmap)
                    handleImage(bitmap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun displayImage(bitmap: Bitmap) {
        image.setImageBitmap(bitmap)
    }

    abstract fun handleImage(bitmap: Bitmap)

    open fun handleLiveImage(image: Image, rotation: Int) {}

    fun postResult(resultText: String?) {
        // ?: if result is null, instead of returning null, return ""
        result.text = resultText ?: ""

    }

    fun postLiveResult(resultText: String?) {
        // ?: if result is null, instead of returning null, return ""
        runOnUiThread { live_result.text = resultText ?: "" }

    }

    override fun postNewImage(image: Image) {
        // ?: if result is null, instead of returning null, return ""
        handleLiveImage(image, view_finder.display.rotation)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // use let to convert one type to a different type
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            File.createTempFile(
                "JPEG_$timeStamp", // magical way of formatting strings
                ".jpg",
                it
            )
        }!!
        // can save the absolute path of the file for further handling
//            .apply {
//                blah = absolutePath
//            }
    }

    protected fun toggleLoading(isLoading: Boolean) {
        // ternary operator in python style
//        result_view.text = if (isLoading) "loading..." else ""
        runOnUiThread {
            if (isLoading) {
                result.visibility = GONE
                progress_circular.visibility = VISIBLE
            } else {
                result.text = ""
                result.visibility = VISIBLE
                progress_circular.visibility = GONE
            }
        }
    }
}