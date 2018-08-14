package com.example.ibrickedlabs.contactsapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ibrickedlabs.contactsapp.data.ContactContract;
import com.example.ibrickedlabs.contactsapp.data.ContactContract.ContactEntry;
import com.example.ibrickedlabs.contactsapp.data.ContactDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private FloatingActionButton fab;
    private Toolbar mainActivityToolbar;
    private ListView listView;
    private ContactDbHelper mDbHelper;
    private static final int CON_LOADER = 0;
    ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivityToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        listView = (ListView) findViewById(R.id.listView);
        setSupportActionBar(mainActivityToolbar);
        mDbHelper = new ContactDbHelper(this);
        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no contact data yet (until the loader finishes) so pass in null for the Cursor.
        contactAdapter = new ContactAdapter(this, null);
        listView.setAdapter(contactAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ContactsAdd.class);
                Uri curi = ContentUris.withAppendedId(ContactEntry.CONTENT_URI, id);
                intent.setData(curi);
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(CON_LOADER, null, this);
    }

    public void fabClicked(View view) {
        Intent intent = new Intent(MainActivity.this, ContactsAdd.class);
        startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define the string projection first
        String[] projection = {
                ContactEntry._ID,
                ContactEntry.Contact_Image,
                ContactEntry.Contact_FirstName,
                ContactEntry.Contact_PhoneNumber
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,//parent activity conetxt
                ContactEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //It will load the data of the adaptersince we passed empty args into contactadapter
        contactAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        contactAdapter.swapCursor(null);
    }
}
