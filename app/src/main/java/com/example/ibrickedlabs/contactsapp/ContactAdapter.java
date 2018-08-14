package com.example.ibrickedlabs.contactsapp;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
        ImageView profImage = (ImageView) view.findViewById(R.id.contactImagevIEW);


        int nameColindex = cursor.getColumnIndex(ContactContract.ContactEntry.Contact_FirstName);
        int phnColindex = cursor.getColumnIndex(ContactContract.ContactEntry.Contact_PhoneNumber);
        int imgColindex = cursor.getColumnIndex(ContactContract.ContactEntry.Contact_Image);
        String name = cursor.getString(nameColindex);
        String phone = cursor.getString(phnColindex);
        final byte[] image = cursor.getBlob(imgColindex);
        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bmp);
        roundedBitmapDrawable.setCircular(true);

        if (TextUtils.isEmpty(name)) {
            name = "Unknown";
        }
        nameView.setText(name);
        phoneNumberView.setText(phone);
        profImage.setImageDrawable(roundedBitmapDrawable);


    }
}
