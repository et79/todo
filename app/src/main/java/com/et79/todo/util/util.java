package com.et79.todo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class util {

    static public final String DATE_PATTERN ="yyyy/MM/dd HH:mm:ss";

    public static String DateToString(Date date){
        String  str = null;
        if(date != null) {
            str = new SimpleDateFormat(DATE_PATTERN).format(date);
        }
        return str;
    }

    public static Date StringToDate(String str) {
        Date date = null;
        try {
            if ( str != null ) {
                date = new SimpleDateFormat(DATE_PATTERN).parse(str);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
