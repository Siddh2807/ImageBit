package com.app.imagebit

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

import java.io.ByteArrayOutputStream

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, databaseName, null, 1) {
    private var db: SQLiteDatabase? = null

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val CREATE_IMAGE_TABLE = "CREATE TABLE " + TABLE_IMAGE + "("+ COL_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + IMAGE_BITMAP + " BLOB )"
        sqLiteDatabase.execSQL(CREATE_IMAGE_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGE")
        onCreate(sqLiteDatabase)
    }

   fun insertImage(selectedImageUri: String): Long {
        db = this.writableDatabase
        val values = ContentValues()
        values.put(IMAGE_BITMAP, selectedImageUri)
        val success = db!!.insert(TABLE_IMAGE, null, values)
        db!!.close()
        return success
    }

    fun readNumber(database: SQLiteDatabase): Cursor {
        val projection: Array<String>
        projection = arrayOf(COL_ID, IMAGE_BITMAP)
        return database.query(TABLE_IMAGE, projection, null, null, null, null, null)
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
        private val databaseName = "dbImage"
        private val TABLE_IMAGE = "ImageTable"

        val COL_ID = "col_id"
        val IMAGE_BITMAP = "image_bitmap"
    }
}