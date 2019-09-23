package com.jz.vnovel.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SPHelper {

    private final SharedPreferences mSharedPre;

    public SPHelper(Context context) {
        this.mSharedPre = PreferenceManager.getDefaultSharedPreferences(context);;
    }

    public void putLong(String key, long line){
        SharedPreferences.Editor editor = mSharedPre.edit();
        editor.putLong(key, line);
        editor.commit();
    }


    public long getLong(String key){
        return mSharedPre.getLong(key, 0);

    }

    public static class Holder{

        private static SPHelper helper;

        public static SPHelper newInstance(Context context){
            if(helper == null){
                helper = new SPHelper(context);
            }

            return helper;
        }

    }

}
