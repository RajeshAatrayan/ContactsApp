package com.example.ibrickedlabs.contactsapp.data;

import android.content.Context;

import com.example.ibrickedlabs.contactsapp.data.ContactContract.ContactEntry;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by RajeshAatrayan on 10-08-2018.
 */

public class ContactDbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "contactsapp.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * CONSTRUCTOR
     */
    public ContactDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * overriding the onCreate table
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_CONTACTS_TABLE = "CREATE TABLE " + ContactEntry.TABLE_NAME + " (" +
                ContactEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ContactEntry.Contact_Image + " BLOB NOT NULL," +
                ContactEntry.Contact_FirstName + " TEXT NOT NULL," +
                ContactEntry.Contact_LastName + " TEXT," +
                ContactEntry.Contact_PhoneNumber + " TEXT NOT NULL," +
                ContactEntry.Contact_Mode + " INTEGER DEFAULT 0," +
                ContactEntry.Contact_Email + " TEXT," +
                ContactEntry.Contact_Address + " TEXT );";
        db.execSQL(SQL_CREATE_CONTACTS_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
