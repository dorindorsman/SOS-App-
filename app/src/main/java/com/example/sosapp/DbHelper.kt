package com.example.sosapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.sosapp.contacts.ContactModel


class DbHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Database Version
        private const val DATABASE_VERSION = 1

        // Database Name
        private const val DATABASE_NAME = "contactdata"

        // Country table name
        private const val TABLE_NAME = "contacts"

        // Country Table Columns names
        private const val KEY_ID = "id"
        private const val NAME = "Name"
        private const val PHONENO = "PhoneNo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // create the table for the first time
        val CREATE_COUNTRY_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + NAME + " TEXT,"
                + PHONENO + " TEXT" + ")")
        db.execSQL(CREATE_COUNTRY_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}
    // method to add the contact
    fun addContact(contact: ContactModel) {
        val db = this.writableDatabase
        val c = ContentValues()
        c.put(NAME, contact.name)
        c.put(PHONENO, contact.phone)
        db.insert(TABLE_NAME, null, c)
        db.close()
    }

    // method to retrieve all the contacts in List
    val allContacts: List<ContactModel>
        @SuppressLint("Recycle")
        get() {
            val list: MutableList<ContactModel> = ArrayList()
            val query = "SELECT * FROM " + TABLE_NAME
            val db = this.readableDatabase
            val c: Cursor = db.rawQuery(query, null)
            if (c.moveToFirst()) {
                do {
                    list.add(ContactModel(c.getInt(0), c.getString(1), c.getString(2)))
                } while (c.moveToNext())
            }
            return list
        }

    // get the count of data, this will allow user
    // to not add more that five contacts in database
    fun count(): Int {
        var count = 0
        val query = "SELECT COUNT(*) FROM $TABLE_NAME"
        val db = this.readableDatabase
        val c: Cursor = db.rawQuery(query, null)
        if (c.getCount() > 0) {
            c.moveToFirst()
            count = c.getInt(0)
        }
        c.close()
        return count
    }

    // Deleting single country
    fun deleteContact(contact: ContactModel) {
        val db = this.writableDatabase
        val i = db.delete(TABLE_NAME, KEY_ID + " = ?", arrayOf(contact.id.toString()))
        db.close()
    }
}