package com.jz.vnovel;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.jz.vnovel.helper.SPHelper;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        StringBuffer param = new StringBuffer();
        param.append("appid=5d842bdf");
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(this, param.toString());
        super.onCreate();

        SPHelper.Holder.newInstance(this);
    }
}
