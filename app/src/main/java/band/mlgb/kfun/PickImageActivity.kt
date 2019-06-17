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
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.camera.core.CameraX
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
            view_finder.post { bindCameraXUsecases() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Every time the provided texture view changes, recompute layout
        view_finder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform(view_finder)
        }
    }

    // create a preview and analyze use case and bind camera to liveCameraOwner
    // When liveCameraOwner is destoryed, need to rebind to a started state
    private fun bindCameraXUsecases() {
        // Bind use cases to lifecycle, both preview and imageCapture are usecases
        CameraX.bindToLifecycle(
            liveCameraOwner,
            createPreviewUsecase(view_finder),
            createAnalysisUsecase(this@PickImageActivity)
        )
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

                liveCameraOwner.startCamera()
                bindCameraXUsecases()
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
                liveCameraOwner.stopCamera() // will unbind cameraX usesaces, need to rebind
                return@OnNavigationItemSelectedListener true
            }
            R.id.gallery -> {
                fab.visibility = VISIBLE
                image.visibility = VISIBLE
                live_container.visibility = GONE
                fab.setOnClickListener(pickPicListener)
                fab.setImageResource(R.drawable.baseline_photo_24)
                liveCameraOwner.stopCamera()
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