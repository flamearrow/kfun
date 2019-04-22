package band.mlgb.kfun

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import kotlinx.android.synthetic.main.activity_main.*

class ImageLabelingActivity : AppCompatActivity() {
    companion object {
        // shl shift left, shr shift right
        const val TAKE_PICTURE_WITH_CAMERA = 0x1 shl 1
        const val PICK_PICTURE_WITH_GALLERY = 0x1 shl 2

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
                button.setOnClickListener {
                    startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_WITH_CAMERA)
                }

                button.setOnClickListener(takePicListener)
                button.setText(R.string.camera)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                button.setOnClickListener(pickPicListener)
                button.setText(R.string.gallery)

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
                    labelImage(bitmap)
                }

            }
            PICK_PICTURE_WITH_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    // ?: if data? is null, instead of return null, return Uri.Empty
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data?.data ?: Uri.EMPTY)
                    displayImage(bitmap)
                    labelImage(bitmap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun displayImage(bitmap: Bitmap) {
        image.setImageBitmap(bitmap)
    }

    private fun labelImage(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        // get labeler with threshold
        val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(
            FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build()
        )

        // use object: blah to create an actual listener instance
//        labeler.processImage(image).addOnSuccessListener(object: OnSuccessListener<List<FirebaseVisionImageLabel>> {
//            override fun onSuccess(p0: List<FirebaseVisionImageLabel>?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//        })
        labeler.processImage(image).addOnSuccessListener { labels ->
            for (label in labels) {
                val text = label.text
                val entityId = label.entityId
                val confidence = label.confidence
            }
            if (labels.size > 0) {
                Toast.makeText(applicationContext, labels[0].text, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "nothing detected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener(takePicListener)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
