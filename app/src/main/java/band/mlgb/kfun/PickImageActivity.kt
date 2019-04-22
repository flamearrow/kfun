package band.mlgb.kfun

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_pick_image.*

// activity to pick a image from gallery or camera

abstract class PickImageActivity : AppCompatActivity() {
    companion object {
        // shl shift left, shr shift right
        const val TAKE_PICTURE_WITH_CAMERA = 0x1 shl 1
        const val PICK_PICTURE_WITH_GALLERY = 0x1 shl 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_image)
        fab.setOnClickListener(takePicListener)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        result_view.movementMethod = ScrollingMovementMethod()
    }

    private val takePicListener: View.OnClickListener = View.OnClickListener {
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_WITH_CAMERA)
    }

    private val pickPicListener: View.OnClickListener = View.OnClickListener {
        // directly use intent here will get the inferred instance from getIntent() setIntent()
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_PICTURE_WITH_GALLERY)
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        image.setImageBitmap(null)
        result_view.text = ""
        when (item.itemId) {
            R.id.navigation_home -> {
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
                fab.setOnClickListener(takePicListener)
                fab.setImageResource(R.drawable.baseline_camera_24)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                fab.setOnClickListener(pickPicListener)
                fab.setImageResource(R.drawable.baseline_photo_24)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        ACTION_CAMERA_REQUEST_CODE -> {
//            if(resultCode == Activity.RESULT_OK && data != null){
//                displayImage(data.extras.get("data") as Bitmap)
//            }
//        }
//
//        ACTION_ALBUM_REQUEST_CODE -> {
//            if(resultCode == Activity.RESULT_OK && data != null){
//                val resolver = this.contentResolver
//                val bitmap = MediaStore.Images.Media.getBitmap(resolver, data?.data)
//                displayImage(bitmap)
//
//            }
//        }

        when (requestCode) {
            TAKE_PICTURE_WITH_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    // ?. if data is null then return null
                    // !! if extras is null then throw NPE
                    val bitmap = data?.extras!!["data"] as Bitmap
                    displayImage(bitmap)
                    handleImage(bitmap)
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

    fun postResult(result: String?) {
        // ?: if result is null, instead of returning null, return ""
        result_view.text = result ?: ""
    }
}