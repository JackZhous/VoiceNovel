package com.jz.vnovel;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.jz.vnovel.helper.JLog;
import com.jz.vnovel.helper.SPHelper;
import com.jz.vnovel.helper.ToastHelper;
import com.jz.vnovel.queue.StringQueue;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.reactivex.functions.Consumer;


public class VoiceActivity extends AppCompatActivity implements InitListener, SynthesizerListener {

    SpeechSynthesizer mTts;
    //发音人 common xiaofeng xiaoyan
    String speaker = "xiaoyan";
    final String LETTER = "letter";
    boolean isSpeakEnd = true;
    boolean speaking = false;
    private StringQueue sq;
    private Thread speakThread;
    private Thread readThread;
    private SPHelper spHelper;
    private EditText etLoc;
    /**
     * 定位小说中字符个数
     */
    private long locLetter = 0;

    //缓存的字符个数，正在播放中的字符
    private int playingLetter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocie);

        initPermission();
        sq = new StringQueue(30);
        spHelper = SPHelper.Holder.newInstance(null);
        init();
        locLetter = spHelper.getLong(LETTER);
        etLoc = findViewById(R.id.read_letters);
        etLoc.setText(locLetter+"");
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!speaking){
                    speaking = true;
                    locLetter = Long.parseLong(etLoc.getText().toString().trim());

                    if(locLetter > 25265){
                        ToastHelper.toast(VoiceActivity.this, "所设置字数大于文件总字数");
                        return;
                    }

                    initSpech();
                    speakThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (speaking){
                                if(isSpeakEnd){
                                    String msg = sq.getMsg();
                                    if(TextUtils.isEmpty(msg)){
                                        continue;
                                    }
                                    isSpeakEnd = false;
                                    playingLetter = msg.length();
                                    int code = mTts.startSpeaking(msg, VoiceActivity.this);
                                    if (code != ErrorCode.SUCCESS) {
                                        showTip("语音合成失败,错误码: " + code+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
                                    }
                                }
                            }
                            spHelper.putLong(LETTER, locLetter);
                        }
                    });
                    speakThread.start();

                    readThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String line;
                                InputStreamReader inputReader = new InputStreamReader(getAssets().open("novel.txt"));

                                BufferedReader bufReader = new BufferedReader(inputReader);
                                if(locLetter != 0){
                                    bufReader.skip(locLetter);
                                }
                                while (speaking){
                                    line = bufReader.readLine();
                                    if(TextUtils.isEmpty(line)){
                                        continue;
                                    }
                                    sq.pushMsg(line);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    readThread.start();
                }
            }
        });


        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speaking = false;
                isSpeakEnd = true;
                mTts.stopSpeaking();
                sq.flush();
            }
        });
    }


    private void showTip(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.toast(VoiceActivity.this, str);
            }
        });
    }

    private void init(){
        mTts = SpeechSynthesizer.createSynthesizer(this, this);
    }



    @Override
    public void onInit(int code) {
        if (code != ErrorCode.SUCCESS) {
            ToastHelper.toast(this, "初始化失败,错误码："+code+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        }
    }

    private void initSpech(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH,getResourcePath());
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME,speaker);

        //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY,"1");//支持实时音频流抛出，仅在synthesizeToUri条件下支持
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

    //获取发音人资源路径
    private String getResourcePath(){
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/"+speaker+".jet"));
        return tempBuffer.toString();
    }

    @Override
    public void onSpeakBegin() {
    }

    @Override
    public void onBufferProgress(int i, int i1, int i2, String s) {

    }

    @Override
    public void onSpeakPaused() {

    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int i, int i1, int i2) {

    }

    @Override
    public void onCompleted(SpeechError speechError) {
        locLetter += playingLetter;
        etLoc.setText(locLetter+"");
        isSpeakEnd = true;
    }

    @Override
    public void onEvent(int i, int i1, int i2, Bundle bundle) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        spHelper.putLong(LETTER, locLetter);
        if(mTts != null){
            mTts.pauseSpeaking();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mTts != null){
            mTts.resumeSpeaking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTts != null){
            mTts.stopSpeaking();
            mTts.destroy();
        }
    }

    private void initPermission(){
        RxPermissions permissions = new RxPermissions(this);
        permissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if(!permission.granted){
                            JLog.i(permission.name);
                        }
                    }
                });
    }

}
