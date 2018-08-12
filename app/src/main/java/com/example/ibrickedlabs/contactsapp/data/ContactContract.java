package com.example.ibrickedlabs.contactsapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.example.ibrickedlabs.contactsapp.MainActivity;

/**
 * Created by RajeshAatrayan on 10-08-2018.
 */

public class ContactContract {
    public  static  final  String CONTENT_AUTHORITY="com.example.ibrickedlabs.contactsapp";
    public  static  final  Uri BASE_CONTENT_URI= Uri.parse("content://"+CONTENT_AUTHORITY);
    public  static  final  String PATH_CONATACTS="contacts";

    public ContactContract() {
    }





    public static class ContactEntry implements BaseColumns {
        public  static  final  Uri CONTENT_URI=Uri.withAppendedPath(BASE_CONTENT_URI,PATH_CONATACTS);

        public static final String TABLE_NAME = "contacts";
        public static final String _ID = BaseColumns._ID;
        public static final String Contact_FirstName = "firstName";
        public static final String Contact_LastName = "lastName";
        public static final String Contact_PhoneNumber = "phoneNumber";
        public static final String Contact_Mode = "mode";
        public static final String Contact_Email = "email";
        public static final String Contact_Address = "address";

        /**
         * Constants for the mode of the contacts
         */
        public static final int Mode_Mobile = 0;
        public static final int Mode_Work = 1;
        public static final int Mode_Main = 3;
        public static final int Mode_Home = 2;

        public  static boolean isValidMode(int mode){
            if(mode==Mode_Home || mode==Mode_Work || mode==Mode_Main || mode==Mode_Mobile)return  true;
            else{
                return  false;
            }
        }


    }
}
