package com.haier.ai.bluetoothspeaker1.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.haier.ai.bluetoothspeaker1.App;
import com.haier.ai.bluetoothspeaker1.Const;
import com.haier.ai.bluetoothspeaker1.DeviceConst;
import com.haier.ai.bluetoothspeaker1.R;
import com.haier.ai.bluetoothspeaker1.bean.music.RequestMusic;
import com.haier.ai.bluetoothspeaker1.bean.music.ResponseMusic;
import com.haier.ai.bluetoothspeaker1.event.ReconizeStatusEvent;
import com.haier.ai.bluetoothspeaker1.event.UrlMusicEvent;
import com.haier.ai.bluetoothspeaker1.model.RecordModel;
import com.haier.ai.bluetoothspeaker1.net.AIApiService;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * author: qu
 * date: 17-1-16
 * introduce: 音乐播放器管理类
 */

public class MusicPlayerManager implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener{
    private final String TAG = "MusicPlayerManager";

    public static MusicPlayerManager sMusicPlayerManager;
    private static AudioManager sAudioManager;
    private static MediaPlayer sMediaPlayer;
    private static final int minVoice = 0;

    private static Calendar sCalendar;
    private static String year;
    private static String month;
    private static String day;
    private static String week;
    //private static int musicState = Const.STATE_STOP;       //音乐播放状态
    private List<String> netMusicList = null;               //云端歌曲列表
    private List<String> localMusicList = null;             //本地歌曲列表


    public MusicPlayerManager(){
        DeviceConst.MUSIC_STATE = Const.STATE_STOP;

        getAudioInfo();

        getDateInfo();

        initLocalMusicList();
    }

    private void getDateInfo(){
        sCalendar = Calendar.getInstance();

        year = "" + sCalendar.get(Calendar.YEAR);
        month = "" + (sCalendar.get(Calendar.MONTH)+1);
        day = "" + sCalendar.get(Calendar.DAY_OF_MONTH);
        int weekIndex = sCalendar.get(Calendar.DAY_OF_WEEK);
        switch (weekIndex){
            case 1:
                week = "星期天";
                break;
            case 2:
                week = "星期一";
                break;
            case 3:
                week = "星期二";
                break;
            case 4:
                week = "星期三";
                break;
            case 5:
                week = "星期四";
                break;
            case 6:
                week = "星期五";
                break;
            case 7:
                week = "星期六";
                break;
            default:
                break;
        }
    }

    public void getAudioInfo(){
        sAudioManager = (AudioManager) App.getInstance().getSystemService(Context.AUDIO_SERVICE);

        DeviceConst.MAX_VOICE = sAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "getAudioInfo: max_voice：" + DeviceConst.MAX_VOICE);
    }

    public static MusicPlayerManager getInstance(){
        if(sMusicPlayerManager == null){
            sMusicPlayerManager = new MusicPlayerManager();
        }

        return sMusicPlayerManager;
    }

    /**
     * 播放云端音乐
     * @param url
     */
    public void playUrlMusic(String url){
        if(TextUtils.isEmpty(url)){
            return;
        }
        EventBus.getDefault().post(new ReconizeStatusEvent("正在加载资源"));
        initMediaPlayer();

        try {
            sMediaPlayer.reset();
            // 设置数据源 "http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3"
            sMediaPlayer.setDataSource(url); // 设置数据源

            sMediaPlayer.prepare(); // prepare自动播放

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 暂停播放
     */
    public void pauseMusic(){
        if(sMediaPlayer == null)
            return;

        if(sMediaPlayer.isPlaying()){
            sMediaPlayer.pause();
            DeviceConst.MUSIC_STATE = Const.STATE_PAUSE;
        }

        ProtocolManager.getInstance().syncMusicStatus(0, "pause");
    }

    /**
     * 继续播放
     */
    public void restartMusic(){
        if(sMediaPlayer == null)
            return;

        sMediaPlayer.start();
        DeviceConst.MUSIC_STATE = Const.STATE_PLAYING;

        ProtocolManager.getInstance().syncMusicStatus(0, "play");
    }

    /**
     * 停止播放
     */
    public void stopMusic(){
        if(sMediaPlayer == null){
            return ;
        }

        if(sMediaPlayer.isPlaying()){
            sMediaPlayer.stop();
            sMediaPlayer.release();
            sMediaPlayer = null;

        }

        DeviceConst.MUSIC_STATE = Const.STATE_STOP;
        ProtocolManager.getInstance().syncMusicStatus(0, "stop");
    }

    private void initMediaPlayer(){
        if(sMediaPlayer == null){
            sMediaPlayer = new MediaPlayer();
        }

        sMediaPlayer.setOnPreparedListener(this);
        sMediaPlayer.setOnBufferingUpdateListener(this);
        sMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.d(TAG, "onBufferingUpdate: percent:" + percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion: music play complated");
        DeviceConst.MUSIC_STATE = Const.STATE_STOP;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(sMediaPlayer != null){
            sMediaPlayer.start();
            DeviceConst.MUSIC_STATE = Const.STATE_PLAYING;

            ProtocolManager.getInstance().syncMusicStatus(0, "play");
            EventBus.getDefault().post(new ReconizeStatusEvent("正在播放"));
        }
    }


    /**
     * 播放下一首
     */
    public void playNextMusic(){

    }

    /**
     * 播放上一首
     */
    public void playPreviousMusic(){

    }

    public int getMusicState(){
        return DeviceConst.MUSIC_STATE;
    }

    /**
     * 播放本地音乐
     * @param song
     */
    public boolean playLocalMusic(String song){
        RecordModel.getInstance().waitForWakeup();
        if(TextUtils.isEmpty(song)){
            return false;
        }

        if(getMusicState() == Const.STATE_PLAYING){
            sMediaPlayer.stop();
        }

        int index = hasLocalMusic(song);
        if(index == -1){
            return false;
        }

        String songPath = getLocalMusicPath() + localMusicList.get(index);

        sMediaPlayer = new MediaPlayer();

        try {
            sMediaPlayer.setDataSource(songPath);
            sMediaPlayer.prepare();
            sMediaPlayer.start();
            DeviceConst.MUSIC_STATE = Const.STATE_PLAYING;
            ProtocolManager.getInstance().syncMusicStatus(0, "play");

            //跟app同步播放状态
            ResponseMusic.DataEntity data = new ResponseMusic.DataEntity();
            data.setSong(song);
            ProtocolManager.getInstance().syncMusicStatus(1, data.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * 初始化本地音乐播放列表
     */
    private void initLocalMusicList(){
        if(localMusicList == null){
            localMusicList = new ArrayList<>();
        }

        //添加本地歌曲
        localMusicList.add("白桦林.mp3");
        localMusicList.add("彼岸花.mp3");
        localMusicList.add("彩云之南.mp3");
        localMusicList.add("但愿人长久.mp3");
        localMusicList.add("蝶恋.mp3");
        localMusicList.add("独角戏.mp3");
        localMusicList.add("饿狼传说.mp3");
        localMusicList.add("刚刚好.mp3");
        localMusicList.add("寒衣调.mp3");
        localMusicList.add("黄豆.mp3");
        localMusicList.add("回到拉萨.mp3");
        localMusicList.add("记事本.mp3");
        localMusicList.add("酒干倘卖无.mp3");
        localMusicList.add("酒醉的探戈.mp3");
        localMusicList.add("就是我.mp3");
        localMusicList.add("看月亮爬上来.mp3");
        localMusicList.add("老街.mp3");
        localMusicList.add("摩天轮.mp3");
        localMusicList.add("女人花.mp3");
        localMusicList.add("朋友.mp3");
        localMusicList.add("骑士.mp3");
        localMusicList.add("时间都去哪儿了.mp3");
        localMusicList.add("涛声依旧.mp3");
        localMusicList.add("听海.mp3");
        localMusicList.add("同桌的你.mp3");
        localMusicList.add("忘记你我做不到.mp3");
        localMusicList.add("吻别.mp3");
        localMusicList.add("下沙.mp3");
        localMusicList.add("映山红.mp3");
        localMusicList.add("雨蝶.mp3");
        localMusicList.add("雨夜花.mp3");
        localMusicList.add("终于等到你.mp3");
        localMusicList.add("最美的太阳.mp3");
        localMusicList.add("尘鼓.mp3");
        localMusicList.add("渡口.mp3");
        localMusicList.add("天空.mp3");
        localMusicList.add("吻别.mp3");
        localMusicList.add("安静.mp3");  //播放一首安静的歌
    }

    /**
     * 播放列表中添加歌曲
     * @param songUrl
     */
    public void addNetMusic(String songUrl){
        if(TextUtils.isEmpty(songUrl)){
            return;
        }

        if (netMusicList == null){
            netMusicList = new ArrayList<>();
        }

        netMusicList.add(songUrl);
    }

    public String getLocalMusicPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/";
    }

    public int hasLocalMusic(String song){
        if(localMusicList == null){
            return -1;
        }

        for(String sel : localMusicList){
            if(sel.contains(song)){
                return localMusicList.indexOf(sel);
            }
        }

        return -1;
    }

    public void adjustSystemVoiceLow(){
        sAudioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);

        DeviceConst.CURRENT_VOICE_LEVEL = getCurrentVoice();
    }

    public void adjustSystemVoiceHigh(){
        sAudioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);

        DeviceConst.CURRENT_VOICE_LEVEL = getCurrentVoice();
    }

    public void setSystemVoiceMax(){
        sAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, DeviceConst.MAX_VOICE, 0);
        DeviceConst.CURRENT_VOICE_LEVEL = DeviceConst.MAX_VOICE;
    }

    public void setSystemVoiceMin(){
        sAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        DeviceConst.CURRENT_VOICE_LEVEL = 0;
    }

    public int getCurrentVoice(){
        if(sAudioManager == null){
            sAudioManager = (AudioManager) App.getInstance().getSystemService(Context.AUDIO_SERVICE);
        }

        DeviceConst.CURRENT_VOICE_LEVEL = sAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "getCurrentVoice: current:" + DeviceConst.CURRENT_VOICE_LEVEL);
        return DeviceConst.CURRENT_VOICE_LEVEL;
    }

    public String getTodayDate(){
        return "今天是" + year + "年" + month + "月" + day + "号";
    }

    public String getTodayWeek(){
        return "今天" + week + "哦";
    }

    public void playRandomUrlMusic(){
        RecordModel.getInstance().waitForWakeup();
        AIApiService aiApiService = RetrofitApiManager.getAiApiService();
        RequestMusic requestMusic = new RequestMusic();
        RequestMusic.KeywordsEntity keywordsEntity = new RequestMusic.KeywordsEntity();
        requestMusic.setDomain("music");
        requestMusic.setKeywords(keywordsEntity);

        aiApiService.getMusicContent("", requestMusic).enqueue(new Callback<ResponseMusic>() {
            @Override
            public void onResponse(Call<ResponseMusic> call, Response<ResponseMusic> response) {
                if(response.body().getRetCode().equals(Const.RET_CODE_SUCESS)){
                    final String url = response.body().getData().getUrl();
                    Log.d(TAG, "music url :" + url);
                    if(TextUtils.isEmpty(url)){
                        playRandomLocalMusic();
                    }else {
                        String songdata = response.body().getData().toString();
                        ProtocolManager.getInstance().syncMusicStatus(1, songdata);
                        EventBus.getDefault().post(new UrlMusicEvent(url));
                    }
                }else {
                    playRandomLocalMusic();
                    Log.e(TAG, "onFailure: playRandomUrlMusic:net error");
                }

            }

            @Override
            public void onFailure(Call<ResponseMusic> call, Throwable t) {
                Log.e(TAG, "onFailure: playRandomUrlMusic");
                playRandomLocalMusic();
            }
        });
    }

    public void playRandomLocalMusic() {
        Random random = new Random();
        int index = random.nextInt(localMusicList.size());

        String song = localMusicList.get(index);

        playLocalMusic(song);

        //跟app同步播放状态
        ResponseMusic.DataEntity data = new ResponseMusic.DataEntity();
        data.setSong(song);
        ProtocolManager.getInstance().syncMusicStatus(1, data.toString());
    }

    public void playNetError(){
        MediaPlayer player = MediaPlayer.create(App.getInstance().getApplicationContext(), R.raw.neterror);
        player.start();
    }
}
