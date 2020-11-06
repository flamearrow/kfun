package band.mlgb.kfun

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import band.mlgb.kfun.camerax.*
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
    private var liveCameraOwner: LiveCameraOwner = LiveCameraOwner()
    private var cameraProvider: ProcessCameraProvider? = null

    // this value gets flipped when button is clicked, need to rebind liveCameraUsecases to
    // reflect the change
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
        set(value) {
            field = value
            bindLiveCameraUsecases()
        }
    private lateinit var previewUseCase: Preview
    private lateinit var imageAnalysisUsecase: ImageAnalysis


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_image)
        fab.setOnClickListener(flipLiveCameraListener)
        btm_navigation_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        result.movementMethod = ScrollingMovementMethod()
        initializeCamera()
    }

    override fun onResume() {
        super.onResume()
        // Note when initialized, onResume is also called, but by then cameraProviderFuture is not
        // executed yet, so this won't trigger. It only triggers when app is minimized and
        // recovered.
        if (liveCameraOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            bindLiveCameraUsecases()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        liveCameraOwner.shutDown()
    }

    private fun initializeCamera() {
        // image capture
        imageAnalysisUsecase = createAnalysisUseCase(ContextCompat.getMainExecutor(this), this)
        // preview
        previewUseCase = createPreviewUseCase(view_finder.surfaceProvider)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

    }

    // Initialize CameraProvider and bind usecases, later when flipping/turning on/off camera,
    // no need to initialize CameraProvider
    private fun startCamera() {
//        // if not in CREATED(just initialized), should be in STARTED(paused, and swiped back)
//        if (liveCameraOwner.lifecycle.currentState == Lifecycle.State.CREATED) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // run when this future is done on main executor
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindLiveCameraUsecases()
            liveCameraOwner.startCamera()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindLiveCameraUsecases() {
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("CameraProvider not initialized.")

        cameraProvider.unbindAll()
        try {
            cameraProvider.bindToLifecycle(
                liveCameraOwner,
                lensFacing,
                previewUseCase,
                imageAnalysisUsecase
            )
        } catch (exec: Exception) {
            Log.e(CameraXActivity.TAG, "Use case binding failed", exec)
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

    private val flipLiveCameraListener = View.OnClickListener {
        // will trigger rebind
        lensFacing =
            if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
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
                        file
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

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            image.setImageBitmap(null)
            result.text = ""
            toggleLoading(false)
            when (item.itemId) {
                R.id.live_camera -> {
                    // hide fab
                    // hide image
                    // show and initialize view_finder
                    image.visibility = GONE
                    live_container.visibility = VISIBLE
                    fab.setOnClickListener(flipLiveCameraListener)
                    fab.setImageResource(R.drawable.baseline_switch_camera_24)
                    liveCameraOwner.startCamera()
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
                    image.visibility = VISIBLE
                    live_container.visibility = GONE
                    fab.setOnClickListener(takePicListener)
                    fab.setImageResource(R.drawable.baseline_camera_24)
                    liveCameraOwner.pauseCamera()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.gallery -> {
                    image.visibility = VISIBLE
                    live_container.visibility = GONE
                    fab.setOnClickListener(pickPicListener)
                    fab.setImageResource(R.drawable.baseline_photo_24)
                    liveCameraOwner.pauseCamera()
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
                        MediaStore.Images.Media.getBitmap(contentResolver!!, nonNullUri)
                            .let { bitmap ->
                                rotateImageIfRequired(
                                    this,
                                    bitmap,
                                    lastPicTakenUri!!
                                ).let { rotatedBitmap ->
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
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver,
                        data?.data ?: Uri.EMPTY
                    )
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
        view_finder.display?.let {
            handleLiveImage(image, it.rotation)
        }
        Log.d("BGLM", "result updated!")
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