package com.jz.vnovel.helper;

import android.content.Context;
import android.widget.Toast;


public class ToastHelper {

    public static void toast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
