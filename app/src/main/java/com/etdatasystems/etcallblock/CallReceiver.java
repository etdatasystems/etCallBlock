package com.etdatasystems.etcallblock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.lang.reflect.Method;
import android.net.Uri;
import android.database.Cursor;
import android.provider.ContactsContract;
import com.android.internal.telephony.ITelephony;

public class CallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ITelephony telephonyService;

        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    Class c = Class.forName(tm.getClass().getName());
                    Method m = tm.getClass().getDeclaredMethod("getITelephony");

                    m.setAccessible(true);
                    telephonyService = (ITelephony) m.invoke(tm);

                    if (!getContactName(number, context)) telephonyService.endCall();

                }catch (Exception e) {
                    Log.d("CLASS ERROR!",e.getMessage());
                }
            }
        }catch (Exception e) {
            Log.d("STATE ERROR!",e.getMessage());
        }
    }


    private boolean getContactName(String number, Context context) {
        String contactName ="";

        // // define the columns I want the query to return
        String[] projection = new String[] {
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.HAS_PHONE_NUMBER };

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        // query time
        Cursor cursor = context.getContentResolver().query(contactUri,
                projection, null, null, null);

        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        cursor.close();
        if (contactName.isEmpty()) {
            return false;
        }

        return true;

    }
}
