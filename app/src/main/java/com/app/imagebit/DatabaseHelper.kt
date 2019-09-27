package com.app.imagebit

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

import java.io.ByteArrayOutputStream

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, databaseName, null, databaseVersion) {
    private val TAG = "DatabaseHelperClass"
    private var db: SQLiteDatabase? = null

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val CREATE_IMAGE_TABLE = ("CREATE TABLE " + TABLE_IMAGE + "("
                + COL_ID + " INTEGER PRIMARY KEY ,"
                + IMAGE_ID + " TEXT,"
                + IMAGE_BITMAP + " BLOB )")
        sqLiteDatabase.execSQL(CREATE_IMAGE_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGE")
        onCreate(sqLiteDatabase)
    }

    fun insetImage(dbDrawable: Drawable, imageId: String) {
        db = this.writableDatabase
        val values = ContentValues()
        values.put(IMAGE_ID, imageId)
        val bitmap = (dbDrawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        values.put(IMAGE_BITMAP, stream.toByteArray())
        db!!.insert(TABLE_IMAGE, null, values)
        db!!.close()
    }

    fun insertImage(selectedImageUri: String) {


    }

    fun getNames(): List<String> {
        val list = ArrayList<String>()
        val cursor = db!!.rawQuery("SELECT name FROM places", null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast()) {
            list.add(cursor.getString(0))
            cursor.moveToNext()
        }
        cursor.close()
        return list
    }

    fun getImage(name: String): ByteArray? {
        var data: ByteArray? = null
        val cursor = db!!.rawQuery("SELECT image FROM places WHERE name = ?", arrayOf(name))
        cursor.moveToFirst()
        while (!cursor.isAfterLast()) {
            data = cursor.getBlob(0)
            break  // Assumption: name is unique
        }
        cursor.close()
        return data
    }

    /* fun getImage(imageId: String): ImageHelper {
         val db = this.writableDatabase

         val cursor2 = db.query(TABLE_IMAGE,
                 arrayOf(COL_ID, IMAGE_ID, IMAGE_BITMAP), IMAGE_ID
                 + " LIKE '" + imageId + "%'", null, null, null, null)
         val imageHelper = ImageHelper()

         if (cursor2.moveToFirst()) {
             do {
                 imageHelper.imageId = cursor2.getString(1)
                 imageHelper.imageByteArray = cursor2.getBlob(2)
             } while (cursor2.moveToNext())
         }

         cursor2.close()
         db.close()
         return imageHelper
     }*/

    companion object {
        private val databaseVersion = 1
        private val databaseName = "dbImage"
        private val TABLE_IMAGE = "ImageTable"

        private val COL_ID = "col_id"
        private val IMAGE_ID = "image_id"
        private val IMAGE_BITMAP = "image_bitmap"
    }
}
