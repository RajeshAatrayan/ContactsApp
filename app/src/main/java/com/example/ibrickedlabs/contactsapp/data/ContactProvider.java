package com.example.ibrickedlabs.contactsapp.data;

import android.content.ContentProvider;

import com.example.ibrickedlabs.contactsapp.data.ContactContract.ContactEntry;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by RajeshAatrayan on 10-08-2018.
 */

public class ContactProvider extends ContentProvider {
    private static final String LOG_TAG = ContactProvider.class.getSimpleName();
    /*
    Uri matcher code for the content uri of the contacts table
     */
    private static final int CONTACTS = 100;
    /*
   Uri matcher code for the content uri of the contacts table for the particular id
    */
    private static final int CONTACTS_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     *
     * Static BLOCK --> This is run the first time anything is called from this class.
     *
     */

    static {
        sUriMatcher.addURI(ContactContract.CONTENT_AUTHORITY, ContactContract.PATH_CONATACTS, CONTACTS);
        sUriMatcher.addURI(ContactContract.CONTENT_AUTHORITY, ContactContract.PATH_CONATACTS + "/#", CONTACTS_ID);

    }

    private ContactDbHelper mDbHelper;


    /**
     * Create method
     *
     * @return
     */


    @Override
    public boolean onCreate() {
        mDbHelper = new ContactDbHelper(getContext());

        return true;
    }


    /**
     * Query Method
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {


        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                cursor = db.query(ContactEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CONTACTS_ID:
                selection = ContactEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                cursor = db.query(ContactEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }

        /**
         * setNotificationUri
         * So when ever the data of a particular Uri changes we need to update it
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return insertContact(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertContact(Uri uri, ContentValues values) {
        /**
         * We are just validating the input here by check them individually
         */
        String fname = values.getAsString(ContactEntry.Contact_FirstName);
        if (fname == null) {
            throw new IllegalArgumentException("Contact requires name");
        }
        String phoneNum = values.getAsString(ContactEntry.Contact_PhoneNumber);
        if (phoneNum == null) {
            throw new IllegalArgumentException("Contact requires phone number");
        }

        Integer mode = values.getAsInteger(ContactEntry.Contact_Mode);
        if (mode == null || !ContactEntry.isValidMode(mode)) {
            throw new IllegalArgumentException("Contact requires valide mode");
        }
        /**
         * Get writeable database
         */
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(ContactEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "failed to insert the data for" + uri);
        }

        //notify all the listeners that the data has changed for the given content uri
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);


    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (match) {
            case CONTACTS:
                rowsDeleted = db.delete(ContactEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case CONTACTS_ID:
                selection = ContactEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(ContactEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Found some error in deleting the contact" + uri);

        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return rowsDeleted;
        }


        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return updateContact(uri, values, selection, selectionArgs);
            case CONTACTS_ID:
                selection = ContactEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateContact(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateContact(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(ContactEntry.Contact_FirstName)) {
            String fname = values.getAsString(ContactEntry.Contact_FirstName);
            if (fname == null) {
                throw new IllegalArgumentException("Contact requires name");
            }
        }
        if (values.containsKey(ContactEntry.Contact_PhoneNumber)) {
            String phoneNum = values.getAsString(ContactEntry.Contact_PhoneNumber);
            if (phoneNum == null) {
                throw new IllegalArgumentException("Contact requires phone number");
            }

        }
        if (values.containsKey(ContactEntry.Contact_Mode)) {
            Integer mode = values.getAsInteger(ContactEntry.Contact_Mode);
            if (mode == null || !ContactEntry.isValidMode(mode)) {
                throw new IllegalArgumentException("Contact requires valide mode");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //Perform the update on the database and get the number of rows affected
        int rowsEffected = db.update(ContactEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsEffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);

        }
        return rowsEffected;
    }
}
