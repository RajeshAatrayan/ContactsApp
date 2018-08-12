package com.example.ibrickedlabs.contactsapp;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ibrickedlabs.contactsapp.data.ContactContract;

/**
 * Created by RajeshAatrayan on 11-08-2018.
 */

public class ContactAdapter extends CursorAdapter {
    ContactAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //ImageView imageView=(ImageView)view.findViewById(R.id.contactImagevIEW);
        TextView nameView = (TextView) view.findViewById(R.id.contactTextView);
        TextView phoneNumberView = (TextView) view.findViewById(R.id.phoneNumberTextview);


        int nameColindex = cursor.getColumnIndex(ContactContract.ContactEntry.Contact_FirstName);
        int phnColindex = cursor.getColumnIndex(ContactContract.ContactEntry.Contact_PhoneNumber);
        String name = cursor.getString(nameColindex);
        String phone = cursor.getString(phnColindex);

        if (TextUtils.isEmpty(name)) {
            name = "Unknown";
        }
        nameView.setText(name);
        phoneNumberView.setText(phone);

    }
}
