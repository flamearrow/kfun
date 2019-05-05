package band.mlgb.kfun

import android.graphics.Bitmap
import android.os.Bundle
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode.FORMAT_AZTEC
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode.FORMAT_QR_CODE
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class BarcodeDetectorActivity : PickImageActivity() {
    private lateinit var barcodeDetector: FirebaseVisionBarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(
                FORMAT_QR_CODE,
                FORMAT_AZTEC
            )
            .build().let {
                barcodeDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(it)
            }
    }

    override fun handleImage(bitmap: Bitmap) {
        FirebaseVisionImage.fromBitmap(bitmap).let {
            toggleLoading(true)
            barcodeDetector?.detectInImage(it)?.addOnSuccessListener { barcodes ->
                val sb = StringBuilder().also {sb->
                    sb.appendln("found ${barcodes.size} barcodes")
                }

                for ((index, barcode) in barcodes.withIndex()) {
                    val bounds = barcode.boundingBox
                    val corners = barcode.cornerPoints

                    val rawValue = barcode.rawValue

                    val valueType = barcode.valueType
                    // See API reference for complete list of supported types
                    when (valueType) {
                        FirebaseVisionBarcode.TYPE_WIFI -> {
                            val ssid = barcode.wifi!!.ssid
                            val password = barcode.wifi!!.password
                            val type = barcode.wifi!!.encryptionType
                        }
                        FirebaseVisionBarcode.TYPE_URL -> {
                            val title = barcode.url!!.title
                            val url = barcode.url!!.url
                        }
                    }
                }
                toggleLoading(false)
                postResult(sb.toString())


            }?.addOnFailureListener {
                toggleLoading(false)

            }
        }
    }

}