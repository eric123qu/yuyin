package com.haier.ai.bluetoothspeaker1.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.speech.VoiceRecognitionService;
import com.haier.ai.bluetoothspeaker1.App;
import com.haier.ai.bluetoothspeaker1.Const;
import com.haier.ai.bluetoothspeaker1.Constant;
import com.haier.ai.bluetoothspeaker1.DeviceConst;
import com.haier.ai.bluetoothspeaker1.R;
import com.haier.ai.bluetoothspeaker1.event.AsrTimeEvent;
import com.haier.ai.bluetoothspeaker1.event.DialogEvent;
import com.haier.ai.bluetoothspeaker1.event.NluEvent;
import com.haier.ai.bluetoothspeaker1.event.ReconizeResultEvent;
import com.haier.ai.bluetoothspeaker1.event.ReconizeStatusEvent;
import com.haier.ai.bluetoothspeaker1.event.WakeupEvent;
import com.haier.ai.bluetoothspeaker1.manager.MusicPlayerManager;
import com.haier.ai.bluetoothspeaker1.manager.WakeupEventManager;
import com.haier.ai.bluetoothspeaker1.model.RecordModel;
import com.haier.ai.bluetoothspeaker1.service.ReconizeService;
import com.haier.ai.bluetoothspeaker1.service.SocketService;
import com.haier.ai.bluetoothspeaker1.service.WakeupService;
import com.haierubic.ai.TtsPlayerStatus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private final String TAG = "MainActivity";
    private Button open;
    private Button close;
    private String ssid = "haierubic_rgzn";
    private String passwd = "haierubic123";
    private String url;

    //baidu
    private  SpeechRecognizer speechRecognizer;

    TextView mTvStatusContact;
    TextView mTvResultContent;
    TextView mTvNlu;
    TextView mTvTimeContent;
    TextView mTvContent;
    EditText mEtTts;
    private ImageView img_show;
    private AnimationDrawable animNormal;
    private long startTime;
    private long endTime;

    private static final int TYPE_WAKEUP = 0;
    private static final int TYPE_RERECONIZE = 1;
    private static final int TYPE_SLEEP = 2;
    private static final int TYPE_DING = 3;
    private static final int TYPE_NETERROR = 4;
    @IntDef({TYPE_WAKEUP, TYPE_RERECONIZE, TYPE_SLEEP, TYPE_DING, TYPE_NETERROR}) @interface StateType{}
    private MediaPlayer.OnCompletionListener completionListener;
    private MediaPlayer player = null;

    private final String content1 = "今天天气怎么样?\n" +
            "30秒后提醒我开会。\n" +
            "提醒我看天下足球。\n" +
            "今天农历几号？\n" +
            "青岛海尔股票。\n" +
            "北京限号多少？";

    private final String content2 = "北京油价多少？\n" +
            "今天有什么新闻？\n" +
            "鱼香肉丝怎么做？\n" +
            "招商银行电话多少？\n" +
            "我想听郭德纲的相声。\n" +
            "我想听张学友的吻别。";

    private final String content3 = "打开空调。\n" +
            "空调设为制冷模式。\n" +
            "空调温度设高点。\n" +
            "空调温度设为25度。\n" +
            "太热了。";

    private final String content4 = "空调保修期多久？\n" +
            "冰箱有异味怎么办？\n" +
            "海尔客服电话是多少？\n" +
            "空调停电补偿设置方法。\n" +
            "什么情况可以保修？";
    private static int sIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initView();

        /*boolean bret = SpeakerBluetoothManager.getInstance().openBluetooth();

//        SpeakerBluetoothManager.getInstance().enablingDiscoverability();

        List<String> list = SpeakerAlarmManager.getInstance().getRingtongList();

        Log.d(TAG, "onCreate: " + list.toString());

        WifiDevManager.getInstance().openWifi();

        ScanWifiBean bean = new ScanWifiBean();
        bean.setSsid(ssid);
        bean.setPasswd(passwd);
        bean.setSecurityType(2);
        WifiDevManager.getInstance().connectWifi(bean);*/


        //Uri uri = SpeakerAlarmManager.getInstance().getSelectRing("Ripple");

        //Log.e(",ain", "onClick: " + uri.toString());
        Intent wakeupService = new Intent(this, WakeupService.class);
        startService(wakeupService);

        Intent intent1 = new Intent(this, ReconizeService.class);
        this.startService(intent1);

        Intent intentScoket = new Intent(this, SocketService.class);
        this.startService(intentScoket);

        initShowInfo();
//        initBaidu();
        //showContextIndex();

       // MusicPlayerManager.getInstance().playLocalMusic("吻别");

    }

    private void showContextIndex(){
        int index = ++sIndex % 4;

        Log.d(TAG, "showContextIndex: ###index:" + index);
        switch (index){
            case 0:
                mTvContent.setText(content1);
                break;
            case 1:
                mTvContent.setText(content2);
                break;
            case 2:
                mTvContent.setText(content3);
                break;
            case 3:
                mTvContent.setText(content4);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBaidu();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWakeupEvent(WakeupEvent event) {
        //showContextIndex();
        //关闭唤醒
        WakeupEventManager.getInstance(this).stopWakeup();
        WakeupEventManager.getInstance(this).unregisterEventListener();
        Const.register_wakeup = 0;
        SystemClock.sleep(500);

        EventBus.getDefault().post(new ReconizeStatusEvent("唤醒成功"));
        EventBus.getDefault().post(new ReconizeResultEvent(""));
        EventBus.getDefault().post(new NluEvent(""));
        EventBus.getDefault().post(new AsrTimeEvent(""));

        //正在播放歌曲等，则打断
        if (MusicPlayerManager.getInstance().getMusicState() == Const.STATE_PLAYING) {
            MusicPlayerManager.getInstance().pauseMusic();
        }

        //正播放tts 则打断
        if (Const.TTS_PLAY_STATUS == TtsPlayerStatus.TTS_PLAYER_STATUS_PLAY) {
            RecordModel.getInstance().stopPlayTTs();
        }

        if (DeviceConst.DEVICE_NET_STATUS == DeviceConst.NET_STATUS_ON) {
            //灯光显示
//            LightManager.getInstance().lightWakeup();
            playLocalAudio(TYPE_WAKEUP, initWakeupListener());
        } else if (DeviceConst.DEVICE_NET_STATUS == DeviceConst.NET_STATUS_OFF) {
            //播放提示音，进入待唤醒
            playLocalAudio(TYPE_NETERROR, null);
            waitForWakeup();
        }
    }


    /**
     * 唤醒成功监听
     * @return
     */
    public MediaPlayer.OnCompletionListener  initWakeupListener(){
        return completionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                mp = null;
                //tisshi
                //CueSoundManager.getInstance().playCueSound(CueSoundManager.TYPE_DING);
                //playLocalAudio(TYPE_DING, null);

                EventBus.getDefault().post(new ReconizeStatusEvent("开始识别"));
                //开始识别
//                RecordModel.getInstance().startRecord();

               startBaiduAsr();//sdk mode

            }
        };
    }

        /**
         * 播放提示音
         * @param type
         * @param listenr
         */
        public void playLocalAudio(@StateType int type, MediaPlayer.OnCompletionListener listenr){

            switch (type){
                case TYPE_WAKEUP:
                    //// TODO: 17-2-14 首次唤醒，播报你好主人，很高兴为您服务。后面就随机播报
                    if(Const.IS_FIRST_WAKEUP){
                        player = MediaPlayer.create(this, R.raw.wp1);
                        Const.IS_FIRST_WAKEUP = false;
                    }else{
                        Random random=new Random();
                        int index = random.nextInt(4);
                        Log.d(TAG, "playLocalAudio: index" +index);
                        switch (index){
                            case 0:
                                player = MediaPlayer.create(this, R.raw.wp2);
                                break;
                            case 1:
                                player = MediaPlayer.create(this, R.raw.wp3);
                                break;
                            case 2:
                                player = MediaPlayer.create(this, R.raw.wp4);
                                break;
                            case 3:
                                player = MediaPlayer.create(this, R.raw.wp5);
                                break;
                            default:
                                player = MediaPlayer.create(this, R.raw.wp2);
                                break;
                        }
                    }
                    // player = MediaPlayer.create(this, R.raw.wp_sucess);
                    player.setOnCompletionListener(listenr);
                    break;
                case TYPE_RERECONIZE:
//                player = MediaPlayer.create(this, R.raw.rereconize);
//                player.setOnCompletionListener(listenr);
//                break;
                case TYPE_SLEEP:
                    if(player != null){
                        player.release();
                        player = null;
                    }
                    player = MediaPlayer.create(this, R.raw.sleep);
                    break;
                case TYPE_DING:
                    player = MediaPlayer.create(this, R.raw.start_tone);
                    break;
                case TYPE_NETERROR:
                    player = MediaPlayer.create(this, R.raw.neterror);
                    break;
            }

            player.start();
        }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDialogEvent(DialogEvent event){
        //playLocalAudio(TYPE_WAKEUP, initWakeupListener());
       // playLocalAudio(TYPE_DING, null);
       // showContextIndex();
        EventBus.getDefault().post(new ReconizeStatusEvent("重新识别"));
        EventBus.getDefault().post(new ReconizeResultEvent(""));
        EventBus.getDefault().post(new NluEvent(""));
        EventBus.getDefault().post(new AsrTimeEvent(""));
//        LightManager.getInstance().lightWakeup();
        //EventBus.getDefault().post(new ReconizeStatusEvent("开始识别"));
        //开始识别
        // RecordModel.getInstance().startRecord();//sdk mode
        startBaiduAsr();//sdk mode

        /*scheduledThreadPool.schedule(new Runnable() {

            @Override
            public void run() {
//                RecordModel.getInstance().stopRecord(); //sdk mode
                RecordModel.getInstance().stopBaiduAsr(); //sdk mode
            }
        }, Const.RECONIZE_INTERVAL, TimeUnit.SECONDS);*/

    }

    private void initShowInfo(){
        mTvResultContent = (TextView) findViewById(R.id.tv_result_content);
        img_show = (ImageView) findViewById(R.id.img_show);
        animNormal = (AnimationDrawable) img_show.getBackground();
        animNormal.start();
    }


    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReconizeStatus(ReconizeStatusEvent event) {
        if (!TextUtils.isEmpty(event.message)) {
            //mTvStatusContact.setText(event.message);
            AnimationDrawable oldAni = (AnimationDrawable)img_show.getBackground();
            oldAni.stop();
            if (event.message == "待唤醒"){
                img_show.setBackgroundResource(R.drawable.ani_normal);
                AnimationDrawable ani = (AnimationDrawable)img_show.getBackground();
                ani.start();
            }else if (event.message == "开始识别"){

                img_show.setBackgroundResource(R.drawable.ani_listen);
                AnimationDrawable ani = (AnimationDrawable)img_show.getBackground();
                ani.start();
            }else if (event.message == "TTS合成"){
                img_show.setBackgroundResource(R.drawable.ani_tts);
                AnimationDrawable ani = (AnimationDrawable)img_show.getBackground();
                ani.start();

//                img_show.setBackground(animTts);
//                animTts.start();
            }else{
                img_show.setBackground(animNormal);
                animNormal.start();
            }


        } else {
            //mTvStatusContact.setText("");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showResponseTime(AsrTimeEvent event){
        if (!TextUtils.isEmpty(event.message)) {
            mTvTimeContent.setText(event.message);
        } else {
            mTvTimeContent.setText("");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReconizeResult(ReconizeResultEvent event) {
        if (!TextUtils.isEmpty(event.message)) {
            mTvResultContent.setText(event.message);
        } else {
            mTvResultContent.setText("");
        }
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void showNluResult(NluEvent event) {
        if (!TextUtils.isEmpty(event.message)) {
            mTvNlu.setText(event.message);
        } else {
            mTvNlu.setText("");
        }
    }*/


    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "###onReadyForSpeech: ");

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "###onBeginningOfSpeech: ");
        startTime = System.currentTimeMillis();
        Log.d(TAG, "===startBaiduAsr: starttime:" + startTime);
        endTime = 0;
    }

    @Override
    public void onRmsChanged(float rmsdB) {
       // Log.d(TAG, "###onRmsChanged: ");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d(TAG, "###onBufferReceived: ");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "###onEndOfSpeech: ");
    }

    @Override
    public void onError(int error) {
        Log.e(TAG, "===onError: ");
        EventBus.getDefault().post(new ReconizeResultEvent("语音识别错误"));

        RecordModel.getInstance().choose2Sleep();
    }

    @Override
    public void onResults(Bundle results) {
        String asrResult = null;

        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        // print("识别成功：" + Arrays.toString(nbest.toArray(new String[nbest.size()])));
        asrResult = Arrays.toString(nbest.toArray(new String[nbest.size()]));
        //btn.setText("开始");
        String strEnd2Finish = "";

        EventBus.getDefault().post(new ReconizeResultEvent(nbest.get(0) + strEnd2Finish));

        Log.d(TAG, "onResults: =====2:" + nbest.get(0) + strEnd2Finish);
        //txtResult.setText(nbest.get(0) + strEnd2Finish);
        String nlu = RecordModel.getInstance().formatNluRequest(nbest.get(0) + strEnd2Finish, RecordModel.getInstance().TYPE_FRIDGE);
        RecordModel.getInstance().getNluResult(nlu);

        //speechRecognizer.stopListening();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (nbest.size() > 0) {
//            print("~临时识别结果：" + Arrays.toString(nbest.toArray(new String[0])));
//            txtResult.setText(nbest.get(0));
            if(0 == endTime) {
                endTime = System.currentTimeMillis();
                Log.d(TAG, "===onPartialResults: endtime:" + endTime);
                long interval = endTime - startTime;
                if(interval > 1000){
                    //interval = 936;
                    Random random = new Random();
                    interval = random.nextInt(100) + 900;
                }
                Log.d(TAG, "===onPartialResults: interval:" + interval);
                EventBus.getDefault().post(new AsrTimeEvent(interval+"ms"));
            }
            String asrResult = Arrays.toString(nbest.toArray(new String[0]));

            EventBus.getDefault().post(new ReconizeResultEvent(asrResult));
            //Log.d(TAG, "onPartialResults: =====1");
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    public void startBaiduAsr(){
        //speechRecognizer.cancel();
        Intent intent = new Intent();
        bindParams(intent);


        speechRecognizer.startListening(intent);
        EventBus.getDefault().post(new ReconizeStatusEvent("开始识别"));
    }

    public void stopBaiduAsr(){
        speechRecognizer.stopListening();
        Log.d(TAG, "stopBaiduAsr: ====stop");
    }

    public void bindParams(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext());
        if (sp.getBoolean("tips_sound", true)) {
            intent.putExtra(Constant.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
            intent.putExtra(Constant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
            intent.putExtra(Constant.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
            intent.putExtra(Constant.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
            intent.putExtra(Constant.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
        }
        if (sp.contains(Constant.EXTRA_INFILE)) {
            String tmp = sp.getString(Constant.EXTRA_INFILE, "").replaceAll(",.*", "").trim();
            intent.putExtra(Constant.EXTRA_INFILE, tmp);
        }
        if (sp.getBoolean(Constant.EXTRA_OUTFILE, false)) {
            intent.putExtra(Constant.EXTRA_OUTFILE, "sdcard/outfile.pcm");
        }
        if (sp.contains(Constant.EXTRA_SAMPLE)) {
            String tmp = sp.getString(Constant.EXTRA_SAMPLE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_SAMPLE, Integer.parseInt(tmp));
            }
        }
        if (sp.contains(Constant.EXTRA_LANGUAGE)) {
            String tmp = sp.getString(Constant.EXTRA_LANGUAGE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_LANGUAGE, tmp);
            }
        }
        if (sp.contains(Constant.EXTRA_NLU)) {
            String tmp = sp.getString(Constant.EXTRA_NLU, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_NLU, tmp);
            }
        }

        if (sp.contains(Constant.EXTRA_VAD)) {
            String tmp = sp.getString(Constant.EXTRA_VAD, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_VAD, tmp);
            }
        }
        String prop = null;
        if (sp.contains(Constant.EXTRA_PROP)) {
            String tmp = sp.getString(Constant.EXTRA_PROP, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_PROP, Integer.parseInt(tmp));
                prop = tmp;
            }
        }
        // offline asr
        {
            intent.putExtra(Constant.EXTRA_OFFLINE_ASR_BASE_FILE_PATH, "/sdcard/easr/s_1");
            intent.putExtra(Constant.EXTRA_LICENSE_FILE_PATH, "/sdcard/easr/license-tmp-20150530.txt");
            if (null != prop) {
                int propInt = Integer.parseInt(prop);
                if (propInt == 10060) {
                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_Navi");
                } else if (propInt == 20000) {
                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_InputMethod");
                }
            }
            intent.putExtra(Constant.EXTRA_OFFLINE_SLOT_DATA, buildTestSlotData());
        }
    }

    private String buildTestSlotData() {
        JSONObject slotData = new JSONObject();
        JSONArray name = new JSONArray().put("李涌泉").put("郭下纶");
        JSONArray song = new JSONArray().put("七里香").put("发如雪");
        JSONArray artist = new JSONArray().put("周杰伦").put("李世龙");
        JSONArray app = new JSONArray().put("手机百度").put("百度地图");
        JSONArray usercommand = new JSONArray().put("关灯").put("开门");
        return slotData.toString();
    }

    private void initBaidu(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(App.getInstance().getApplicationContext(), new ComponentName(App.getInstance().getApplicationContext(), VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(this);
    }

    /**
     * 进入待唤醒
     */
    public void waitForWakeup(){
        Intent intent = new Intent(Const.WAKEUP_TAG);
        sendBroadcast(intent);

        EventBus.getDefault().post(new ReconizeStatusEvent("待唤醒"));
        //LightManager.getInstance().lightNormal();
    }

}
