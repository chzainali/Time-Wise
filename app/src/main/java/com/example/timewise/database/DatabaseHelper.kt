package com.example.timewise.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.timewise.model.ChunkModel
import com.example.timewise.model.UsersModel

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {

        // Create the Users table
        db.execSQL(
            " CREATE TABLE " + TABLE_USERS + " (" +
                    KEY_ID_USER + " INTEGER PRIMARY KEY, " +
                    KEY_NAME + " TEXT NOT NULL, " +
                    KEY_EMAIL + " TEXT NOT NULL, " +
                    KEY_PHONE + " TEXT NOT NULL, " +
                    KEY_PASSWORD + " TEXT NOT NULL);"
        )

        // Create the Chunks table
        db.execSQL(
            "CREATE TABLE $TABLE_CHUNKS (" +
                    "$KEY_ID_CHUNK INTEGER PRIMARY KEY," +
                    "$KEY_ID_USER TEXT," +
                    "$KEY_TITLE TEXT," +
                    "$KEY_ACTIVITY_NAME TEXT," +
                    "$KEY_ACTIVITY_PICTURE TEXT," +
                    "$KEY_DATE TEXT," +
                    "$KEY_START_TIME TEXT," +
                    "$KEY_END_TIME TEXT," +
                    "$KEY_IS_REMINDER TEXT," +
                    "$KEY_IS_REPEAT TEXT," +
                    "$KEY_NOTES TEXT," +
                    "$KEY_TAGS TEXT," +
                    "$KEY_IMAGES TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop existing tables if they exist
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHUNKS")
        // Recreate the tables
        onCreate(db)
    }

    fun register(users: UsersModel) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NAME, users.userName)
        values.put(KEY_EMAIL, users.email)
        values.put(KEY_PHONE, users.phone)
        values.put(KEY_PASSWORD, users.password)
        db.insert(TABLE_USERS, null, values)
        db.close()
    }

    val allUsers: List<UsersModel>
        get() {
            val usersList: MutableList<UsersModel> = ArrayList()
            val selectQuery = "SELECT  * FROM $TABLE_USERS"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val users = UsersModel()
                    users.id = cursor.getString(0).toInt()
                    users.userName = cursor.getString(1)
                    users.email = cursor.getString(2)
                    users.phone = cursor.getString(3)
                    users.password = cursor.getString(4)
                    usersList.add(users)
                } while (cursor.moveToNext())
            }
            return usersList
        }

    fun updatePassword(userId: Int, newPassword: String?) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_PASSWORD, newPassword)

        // Update the row where KEY_ID_USER matches the user ID
        db.update(TABLE_USERS, values, "$KEY_ID_USER = ?", arrayOf(userId.toString()))
        db.close()
    }

    // Insert a new chunk into the database
    fun insertChunk(chunk: ChunkModel): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_ID_USER, chunk.userId)
        values.put(KEY_TITLE, chunk.title)
        values.put(KEY_ACTIVITY_NAME, chunk.activityName)
        values.put(KEY_ACTIVITY_PICTURE, chunk.activityPicture)
        values.put(KEY_DATE, chunk.date)
        values.put(KEY_START_TIME, chunk.startTime)
        values.put(KEY_END_TIME, chunk.endTime)
        values.put(KEY_IS_REMINDER, chunk.isReminder)
        values.put(KEY_IS_REPEAT, chunk.isRepeat)
        values.put(KEY_NOTES, chunk.notes)
        values.put(KEY_TAGS, chunk.tags?.joinToString(","))
        values.put(KEY_IMAGES, chunk.images?.joinToString(","))
        val insertedId = db.insert(TABLE_CHUNKS, null, values)
        db.close()
        return insertedId
    }

    fun getChunksData(userId: Int): List<ChunkModel> {
        val chunkList: MutableList<ChunkModel> = ArrayList()

        val selectQuery = "SELECT * FROM $TABLE_CHUNKS WHERE $KEY_ID_USER = ?"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val model = ChunkModel()
                model.id = cursor.getString(0).toInt()
                model.userId = cursor.getString(1)
                model.title = cursor.getString(2)
                model.activityName = cursor.getString(3)
                model.activityPicture = cursor.getString(4)
                model.date = cursor.getString(5)
                model.startTime = cursor.getString(6)
                model.endTime = cursor.getString(7)
                model.isReminder = cursor.getString(8)
                model.isRepeat = cursor.getString(9)
                model.notes = cursor.getString(10)
                val tagsString = cursor.getString(11)
                val imagesString = cursor.getString(12)

                model.tags = if (!tagsString.isNullOrEmpty()) {
                    ArrayList(tagsString.split(","))
                } else {
                    ArrayList()
                }

                model.images = if (!imagesString.isNullOrEmpty()) {
                    ArrayList(imagesString.split(","))
                } else {
                    ArrayList()
                }

                chunkList.add(model)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return chunkList
    }

    fun updateChunk(chunk: ChunkModel) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_ID_USER, chunk.userId)
        values.put(KEY_TITLE, chunk.title)
        values.put(KEY_ACTIVITY_NAME, chunk.activityName)
        values.put(KEY_ACTIVITY_PICTURE, chunk.activityPicture)
        values.put(KEY_DATE, chunk.date)
        values.put(KEY_START_TIME, chunk.startTime)
        values.put(KEY_END_TIME, chunk.endTime)
        values.put(KEY_IS_REMINDER, chunk.isReminder)
        values.put(KEY_IS_REPEAT, chunk.isRepeat)
        values.put(KEY_NOTES, chunk.notes)
        values.put(KEY_TAGS, chunk.tags.joinToString(","))
        values.put(KEY_IMAGES, chunk.images.joinToString(","))
        db.update(TABLE_CHUNKS, values, "$KEY_ID_CHUNK = ?", arrayOf(chunk.id.toString()))
        db.close()
    }

    fun getChunk(userId: Int, chunkId: Int): ChunkModel? {
        var chunk: ChunkModel? = null
        val db = this.writableDatabase
        val selectQuery = "SELECT * FROM $TABLE_CHUNKS WHERE $KEY_ID_USER = ? AND $KEY_ID_CHUNK = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(userId.toString(), chunkId.toString()))
        if (cursor.moveToFirst()) {
            chunk = ChunkModel()
            chunk.id = cursor.getString(0).toInt()
            chunk.userId = cursor.getString(1)
            chunk.title = cursor.getString(2)
            chunk.activityName = cursor.getString(3)
            chunk.activityPicture = cursor.getString(4)
            chunk.date = cursor.getString(5)
            chunk.startTime = cursor.getString(6)
            chunk.endTime = cursor.getString(7)
            chunk.isReminder = cursor.getString(8)
            chunk.isRepeat = cursor.getString(9)
            chunk.notes = cursor.getString(10)
            val tagsString = cursor.getString(11)
            val imagesString = cursor.getString(12)

            chunk.tags = if (!tagsString.isNullOrEmpty()) {
                ArrayList(tagsString.split(","))
            } else {
                ArrayList()
            }

            chunk.images = if (!imagesString.isNullOrEmpty()) {
                ArrayList(imagesString.split(","))
            } else {
                ArrayList()
            }
        }
        cursor.close()
        return chunk
    }

    fun deleteChunk(chunkId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_CHUNKS, "$KEY_ID_CHUNK = ?", arrayOf(chunkId.toString()))
        db.close()
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "time_wise_app"

        // Columns for the Users table
        private const val TABLE_USERS = "users_table"
        private const val KEY_ID_USER = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_PASSWORD = "password"

        private const val TABLE_CHUNKS = "chunks_table"
        private const val KEY_ID_CHUNK = "chunks_id"
        private const val KEY_TITLE = "chunks_title"
        private const val KEY_ACTIVITY_NAME = "activity_name"
        private const val KEY_ACTIVITY_PICTURE = "activity_picture"
        private const val KEY_DATE = "chunks_date"
        private const val KEY_START_TIME = "chunks_start_time"
        private const val KEY_END_TIME = "chunks_end_time"
        private const val KEY_IS_REMINDER = "is_reminder"
        private const val KEY_IS_REPEAT = "is_repeat"
        private const val KEY_NOTES = "notes"
        private const val KEY_TAGS = "tags"
        private const val KEY_IMAGES = "images"
    }
}
