
# 文字转语音

## 原理：使用的科大讯飞第三方库，本地进行文字转语音功能，减少眼部疲劳，方便快速阅读

+ StringQueue文字队列
+ SpeechSynthesizer 科大讯飞语音引擎

### StringQueue

就是一个文字消息队列，从文字中读取文字，放入队列，另一变语音线程不停的读数据，送入语音引擎进行发音

### SpeechSynthesizer

1. 初始化

SpeechSynthesizer createSynthesizer(Context var0, InitListener var1)

第二个参数是初始化回调，成功才能拿进行下一步

2. 设置引擎参数

```java
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

```


3. 发音

public int startSpeaking(String var1, SynthesizerListener var2) ;

第二个参数是发音回调，在回调中要判断是否读完，读完才能进行下一句语音读取
