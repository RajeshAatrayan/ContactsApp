package com.example.ibrickedlabs.contactsapp;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.ibrickedlabs.contactsapp.data.ContactContract.ContactEntry;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.ibrickedlabs.contactsapp.data.ContactDbHelper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ContactsAdd extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText firsName, surName, phoneNumber, email, address;
    private ImageView profileImageView;
    private Button pick_image;
    private Spinner contactCategory;
    private int categoryMode = 0;
    private ContactDbHelper mDbHelper;
    private static final int EXISTING_LOADER = 0;
    Uri currentContactUri;
    private boolean contactHasChanged = false;
    byte[] imageObtained;


    /**
     * Photo constant fields
     */
    private static final int SELECT_PHOTO = 1;
    private static final int CAPTURE_PHOTO = 2;

    Bitmap thumbnail;
    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarbHandler = new Handler();

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
        profileImageView = (ImageView) findViewById(R.id.contacts_def);
        profileImageView.setImageResource(R.drawable.contacts_def);

        pick_image = (Button) findViewById(R.id.pick_image);

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
            invalidateOptionsMenu();
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

         /*

        SETTING CAMER PERMISSIONS
         */
        if (ContextCompat.checkSelfPermission(ContactsAdd.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            profileImageView.setEnabled(false);
            ActivityCompat.requestPermissions(ContactsAdd.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            profileImageView.setEnabled(true);
        }
        /**
         * Here we are attaching the camerabutton to the listners
         */
        pick_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(ContactsAdd.this)
                        .title("Select contact image")
                        .items(R.array.uploadImages)
                        .itemsIds(R.array.itemIds)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                                switch (which) {
                                    case 0:
                                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                        photoPickerIntent.setType("image/*");
                                        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                                        break;
                                    case 1:
                                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        startActivityForResult(intent, CAPTURE_PHOTO);
                                        break;
                                    case 2:
                                        profileImageView.setImageResource(R.drawable.contacts_def);
                                        break;
                                }


                            }
                        }).show();
            }
        });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                profileImageView.setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), selectedImage);
                    roundedBitmapDrawable.setCircular(true);
                    //set Progress Bar
                    setProgressBar();
                    //set profile picture form gallery
                    profileImageView.setImageDrawable(roundedBitmapDrawable);


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } else if (requestCode == CAPTURE_PHOTO) {
            if (resultCode == RESULT_OK) {
                onCaptureImageResult(data);
            }
        }
    }

    private void setProgressBar() {

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Please wait...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBarStatus = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressBarStatus < 100) {
                    progressBarStatus += 30;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBarbHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressBarStatus);
                        }
                    });
                }
                if (progressBarStatus >= 100) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBar.dismiss();
                }
            }
        }).start();
    }

    private void onCaptureImageResult(Intent data) {
        thumbnail = (Bitmap) data.getExtras().get("data");
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), thumbnail);
        roundedBitmapDrawable.setCircular(true);

        //set Progress Bar
        setProgressBar();
        //set profile picture form camera
        profileImageView.setMaxWidth(200);
        profileImageView.setImageDrawable(roundedBitmapDrawable);

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
                showDeleteConfirmationDialog();
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
                DialogInterface.OnClickListener discardButton = new DialogInterface.OnClickListener() {
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

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this contact?");
        builder.setPositiveButton("Delete", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteContact();

            }
        });
        builder.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteContact() {
        int rowsdeleted = 0;
        if (currentContactUri != null) {
            rowsdeleted = getContentResolver().delete(currentContactUri, null, null);
        }
        if (rowsdeleted == 0) {
            Toast toast = Toast.makeText(ContactsAdd.this, "Error While Deleting Contact", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(ContactsAdd.this, "Contact Deleted", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     **/
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButton);

        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if the user clicks the keep editing button remain in the same activity
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!contactHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButton = new DialogInterface.OnClickListener() {
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
        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();
        Bitmap bitmap = profileImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        imageObtained = baos.toByteArray();


        /**
         * check if it is a new pet
         * make sure nothing has been entered by the user so simply return
         */
        if (currentContactUri == null && TextUtils.isEmpty(fName)
                && TextUtils.isEmpty(lName) && TextUtils.isEmpty(ph_Num)
                && TextUtils.isEmpty(eml) && TextUtils.isEmpty(add) && !hasImage(profileImageView)) {
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
            if (ph_Num.length() != 10) {
                phoneNumber.setError("Phone number must contain 10 digits");
                bool = false;
                boolVerify = true;
            }
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
        values.put(ContactEntry.Contact_Image, imageObtained);
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

    private boolean hasImage(ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define the string projection first
        String[] projection = {
                ContactEntry._ID,
                ContactEntry.Contact_Image,
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
            int imgcol = data.getColumnIndex(ContactEntry.Contact_Image);

            String firstnamerec = data.getString(fname);
            String lastNamerec = data.getString(lname);
            int catmoderec = data.getInt(cmode);
            String emailaddressrec = data.getString(emailad);
            String addressrec = data.getString(addresscol);
            String phonenumrec = data.getString(phnumber);
            byte[] image = data.getBlob(imgcol);
            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bmp);
            roundedBitmapDrawable.setCircular(true);

            Log.i(LOG_TAG, firstnamerec + "--" + lastNamerec + "--" + phonenumrec);

            firsName.setText(firstnamerec);
            surName.setText(lastNamerec);
            phoneNumber.setText(phonenumrec);
            email.setText(emailaddressrec);
            address.setText(addressrec);
            profileImageView.setImageDrawable(roundedBitmapDrawable);

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
        profileImageView.setImageResource(R.drawable.contacts_def);


    }
}
