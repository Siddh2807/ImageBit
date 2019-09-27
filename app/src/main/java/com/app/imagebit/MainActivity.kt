package com.app.imagebit

import android.Manifest
import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.content.pm.PackageManager
import android.widget.Toast
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    internal lateinit var imageView: ImageView
    internal lateinit var button: Button
    internal var photoFile: File? = null
    private var db: SQLiteDatabase? = null
    private var dbHelper: DatabaseHelper? = null

    internal lateinit var mCurrentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        button = findViewById(R.id.btnCaptureImage)

        button.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    captureImage()
                } else {
                    captureImage2()
                }
            }
    }

    private fun captureImage2() {

        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = createImageFile4()
            if (photoFile != null) {
                displayMessage(baseContext, photoFile!!.getAbsolutePath())
                Log.i("MAIN---", photoFile!!.getAbsolutePath())
                val photoURI = Uri.fromFile(photoFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST)
            }
        } catch (e: Exception) {
            displayMessage(baseContext, "Camera is not available.$e")
        }

    }

    private fun captureImage() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                try {
                    photoFile = createImageFile()
                    displayMessage(baseContext, photoFile!!.getAbsolutePath())
                    Log.i("MAIN---", photoFile!!.getAbsolutePath())

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        val photoURI = FileProvider.getUriForFile(this,
                                "com.app.imagebit.fileprovider",
                                photoFile!!)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
                    }
                } catch (ex: Exception) {
                    // Error occurred while creating the File
                    displayMessage(baseContext, ex.message.toString())
                    Log.e("Messs-- ","---" +ex.message.toString())
                }

            } else {
                displayMessage(baseContext, "Nulll")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //Bundle extras = data.getExtras()
        //Bitmap imageBitmap = (Bitmap) extras.get("data")
        //imageView.setImageBitmap(imageBitmap)
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val myBitmap = BitmapFactory.decodeFile(photoFile!!.getAbsolutePath())
            imageView.setImageBitmap(myBitmap)
        } else {
            displayMessage(baseContext, "Request cancelled or something went wrong.")
        }
    }

    private fun createImageFile4(): File? {
        // External sdcard location
        val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME)
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                displayMessage(baseContext, "Unable to create directory.")
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(Date())

        return File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg")

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */        )

        mCurrentPhotoPath = image.getAbsolutePath()

         saveImageInDB(mCurrentPhotoPath)
        Log.e("current path---", "---$mCurrentPhotoPath")
        return image
    }

    fun saveImageInDB(selectedImageUri: String): Boolean {
        try {
            db = dbHelper!!.writableDatabase
         /*   val iStream = contentResolver.openInputStream(selectedImageUri)
            val inputData = iStream*/
            dbHelper!!.insertImage(selectedImageUri)
            dbHelper!!.close()
            return true
        } catch (ioe: IOException) {
            Log.e("TAG--", "<saveImageInDB> Error : " + ioe.localizedMessage)
            dbHelper!!.close()
            return false
        }
    }

    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            }
        }
    }

    companion object {
        internal val CAPTURE_IMAGE_REQUEST = 1
        private val IMAGE_DIRECTORY_NAME = "VLEMONN"
    }
}