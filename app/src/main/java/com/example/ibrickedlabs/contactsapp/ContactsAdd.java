package com.example.ibrickedlabs.contactsapp;

import com.example.ibrickedlabs.contactsapp.data.ContactContract.ContactEntry;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.ibrickedlabs.contactsapp.data.ContactDbHelper;

public class ContactsAdd extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText firsName, surName, phoneNumber, email, address;
    private Spinner contactCategory;
    private int categoryMode = 0;
    private ContactDbHelper mDbHelper;
    private static final int EXISTING_LOADER = 0;
    Uri currentContactUri;
    private boolean contactHasChanged = false;

    private static final String LOG_TAG = ContactsAdd.class.getSimpleName();

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            contactHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_add);
        firsName = (EditText) findViewById(R.id.firstName);
        surName = (EditText) findViewById(R.id.surname);
        phoneNumber = (EditText) findViewById(R.id.phoneNumberEntered);
        email = (EditText) findViewById(R.id.emailEntered);
        contactCategory = (Spinner) findViewById(R.id.contactSpinner);
        address = (EditText) findViewById(R.id.addressEntered);

        /**
         * Seting up ontouchListener
         */
        firsName.setOnTouchListener(mTouchListener);
        surName.setOnTouchListener(mTouchListener);
        phoneNumber.setOnTouchListener(mTouchListener);
        email.setOnTouchListener(mTouchListener);
        contactCategory.setOnTouchListener(mTouchListener);
        address.setOnTouchListener(mTouchListener);
        setupSpinner();
        /**
         * Getting the intents uri from the mainacativity
         */
        Intent intent = getIntent();
        currentContactUri = intent.getData();
        Log.i(LOG_TAG, "" + currentContactUri);
        if (currentContactUri == null) {
            setTitle("Add Contact");
        } else {
            setTitle("Edit Contact");
            getLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }


        /**
         * ToolBar setting Up
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.addContactsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /**
         * Creating Object for mDbHelper
         */
        mDbHelper = new ContactDbHelper(this);
    }

    /**
     * Setting up spinner
     */
    private void setupSpinner() {
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.spinnerCategory, R.layout.spinner_list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        contactCategory.setAdapter(spinnerAdapter);
        contactCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                switch (selection) {
                    case "Mobile":
                        categoryMode = ContactEntry.Mode_Mobile;
                        break;
                    case "Work":
                        categoryMode = ContactEntry.Mode_Work;
                        break;
                    case "Home":
                        categoryMode = ContactEntry.Mode_Home;
                        break;
                    case "Main":
                        categoryMode = ContactEntry.Mode_Main;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Setting up the menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.adding_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveButton:
                boolean b = saveContact();
                if (b) {
                    finish();
                }

                return true;

            case R.id.deleButton:
                saveContact();
                Toast toast2 = Toast.makeText(ContactsAdd.this, "Deleted", Toast.LENGTH_SHORT);
                toast2.show();
                finish();
                return true;


            case android.R.id.home:
                /**
                 * If nothing has been touched so we can go home
                 */
                if (!contactHasChanged) {
                    NavUtils.navigateUpFromSameTask(ContactsAdd.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButton= new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(ContactsAdd.this);
                    }
                };

                showUnsavedChangesDialog(discardButton);
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     **/
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard",discardButton );

        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if the user clicks the keep editing button remain in the same activity
                if(dialog!=null){
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if(!contactHasChanged){
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButton=new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButton);

    }

    private boolean saveContact() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String fName = firsName.getText().toString();
        String lName = surName.getText().toString();
        String ph_Num = phoneNumber.getText().toString();
        int mode = categoryMode;
        String eml = email.getText().toString();
        String add = address.getText().toString();

        /**
         * check if it is a new pet
         * make sure nothing has been entered by the user so simply return
         */
        if (currentContactUri == null && TextUtils.isEmpty(fName)
                && TextUtils.isEmpty(lName) && TextUtils.isEmpty(ph_Num)
                && TextUtils.isEmpty(eml) && TextUtils.isEmpty(add)) {
            Log.i(LOG_TAG, "inside if");
            return true;
        }

        if (TextUtils.isEmpty(fName)
                && TextUtils.isEmpty(lName) && TextUtils.isEmpty(ph_Num)
                && TextUtils.isEmpty(eml) && TextUtils.isEmpty(add)) {
            return true;
        }

        if (currentContactUri == null) {
            boolean bool = true;
            boolean boolVerify = false;
            if (TextUtils.isEmpty(fName)) {

                firsName.setError("Name canno't be empty");
                bool = false;
                boolVerify = true;

            } else if (TextUtils.isEmpty(ph_Num)) {
                phoneNumber.setError("Number canno't be empty");
                bool = false;
                boolVerify = true;
            }
            if (add == "" || add == null) add = " ";
            if (eml == "" || eml == null) eml = " ";
            if (lName == "" || lName == null) lName = " ";

            if (boolVerify)
                return bool;
        }


        ContentValues values = new ContentValues();
        values.put(ContactEntry.Contact_FirstName, fName);
        values.put(ContactEntry.Contact_LastName, lName);
        values.put(ContactEntry.Contact_PhoneNumber, ph_Num);
        values.put(ContactEntry.Contact_Mode, mode);
        values.put(ContactEntry.Contact_Email, eml);
        values.put(ContactEntry.Contact_Address, add);
        if (currentContactUri == null) {
            Uri newuri = getContentResolver().insert(ContactEntry.CONTENT_URI, values);
            Log.i(LOG_TAG, "HERE THE INSERTED" + newuri);
            if (newuri == null) {
                Toast toast = Toast.makeText(ContactsAdd.this, "Error with saving contact", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(ContactsAdd.this, "Contact saved", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(currentContactUri, values, null, null);
            if (rowsUpdated == 0) {
                Toast toast = Toast.makeText(ContactsAdd.this, "Error in updaing contact", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(ContactsAdd.this, "Contact updated", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define the string projection first
        String[] projection = {
                ContactEntry._ID,
                ContactEntry.Contact_FirstName,
                ContactEntry.Contact_PhoneNumber,
                ContactEntry.Contact_LastName,
                ContactEntry.Contact_Email,
                ContactEntry.Contact_Address,
                ContactEntry.Contact_Mode
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,//parent activity conetxt
                currentContactUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        if (data.moveToFirst()) {
            int fname = data.getColumnIndex(ContactEntry.Contact_FirstName);
            int lname = data.getColumnIndex(ContactEntry.Contact_LastName);
            int cmode = data.getColumnIndex(ContactEntry.Contact_Mode);
            int emailad = data.getColumnIndex(ContactEntry.Contact_Email);
            int addresscol = data.getColumnIndex(ContactEntry.Contact_Address);
            int phnumber = data.getColumnIndex(ContactEntry.Contact_PhoneNumber);

            String firstnamerec = data.getString(fname);
            String lastNamerec = data.getString(lname);
            int catmoderec = data.getInt(cmode);
            String emailaddressrec = data.getString(emailad);
            String addressrec = data.getString(addresscol);
            String phonenumrec = data.getString(phnumber);
            Log.i(LOG_TAG, firstnamerec + "--" + lastNamerec + "--" + phonenumrec);

            firsName.setText(firstnamerec);
            surName.setText(lastNamerec);
            phoneNumber.setText(phonenumrec);
            email.setText(emailaddressrec);
            address.setText(addressrec);

            switch (catmoderec) {
                case ContactEntry.Mode_Mobile:
                    contactCategory.setSelection(1);
                    break;
                case ContactEntry.Mode_Work:
                    contactCategory.setSelection(2);
                    break;
                case ContactEntry.Mode_Home:
                    contactCategory.setSelection(3);
                    break;
                case ContactEntry.Mode_Main:
                    contactCategory.setSelection(4);
                    break;
            }


            data.close();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        firsName.setText("");
        surName.setText("");
        phoneNumber.setText("");
        email.setText("");
        contactCategory.setSelection(1);
        address.setText("");


    }
}
