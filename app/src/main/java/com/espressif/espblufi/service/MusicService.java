package com.espressif.espblufi.service;


import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;


import com.espressif.espblufi.CESMainActivity;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends MyService {

    private MediaPlayer mediaPlayer;
    private Timer timer;
    private CESMainActivity musicAcitivity;
    private int treatmentTimes = 0 ;
    private boolean isTimerPaused = false;


    public static final String MUSIC_DATA_KEY = "MDK";
    //音乐id的key
    public static final String MUSIC_KEY = "MK";
    //时长数据的key
    public static final String TIME_KEY = "TK";

    //MUSIC_READY->MUSIC_PLAYING->MUSIC_PAUSE 结束->MUSIC_READY
    public static final int MUSIC_READY = 0;
    public static final int MUSIC_PLAYING = 1;
    public static final int MUSIC_PAUSE = 2;


    private int musicState = MUSIC_READY;
    private int play_time = 0 ;

    private String TAG = "MainActivity_MusicService";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        //mediaPlayer = new MediaPlayer();
        // 初始化音乐播放
        // 设置音乐资源等
    }

    public class MusicPlayerBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    /**
     * 绑定的方式设置音乐，按钮的方式打开
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        setMusicState(MUSIC_READY);
        //setMusic(intent.getIntExtra(MUSIC_KEY,0),intent.getIntExtra(TIME_KEY,0));
        Log.d(TAG,"onBind");
        return new MusicPlayerBinder();
    }
    /**
     * 非绑定的方式开启_开始计时播放音乐
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();
        playMusic();
        setMusicState(MUSIC_PLAYING);
        Log.d(TAG,"onStartCommand");
        return START_STICKY;
    }

    /**
     * 活动中按钮打开音乐
     */
    public void startTreatmentMusicStart()
    {
        if(getMusicState() == MUSIC_READY)
        {
            startTimer();
            playMusic();
            setMusicState(MUSIC_PLAYING);
            Log.d(TAG,"startTreatmentMusicStart");
        }
    }

    /**
     * 获取播放状态
     * @return
     */
    public int getMusicState() {
        return musicState;
    }

    /**
     * 设置音乐播放状态
     * @param musicState
     */
    private void setMusicState(int musicState) {
        this.musicState = musicState;
    }




    /**
     * 开启治疗计时器
     */
    private void startTimer() {
        Log.d(TAG,"startTimer");
        if(treatmentTimes>0) {
            Log.d(TAG,"treatmentTimes>0");
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mediaPlayer != null && !isTimerPaused) {
                        Log.d(TAG,"!isTimerPaused");
                        if(play_time<treatmentTimes)
                        {
                            play_time++;
                            musicAcitivity.onProgressUpdate(play_time);
                        }
                        else stopMusic();
                    }
                }
            }, 0, 1000);
        }
    }

    /**
     * 获取活动对象
     * @param myMusicActivity
     */
    public void setMusicPlayerListener(CESMainActivity myMusicActivity)
    {
        musicAcitivity = myMusicActivity;
    }

    /**
     * 设置音乐播放时间
     * @param musicId
     * @param mytreatmentTimes
     */
    public void setMusic(int musicId , int mytreatmentTimes)
    {
        treatmentTimes = mytreatmentTimes;
        mediaPlayer = MediaPlayer.create(this, musicId);
    }


    /**
     * 治疗开始 播放音乐
     */
    public void playMusic() {
        if(treatmentTimes>0&&mediaPlayer!=null)
        {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }


    /**
     * 音乐暂停
     */
    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isTimerPaused = true;
            setMusicState(MusicService.MUSIC_PAUSE);
        }
    }

    /**
     * 继续播放音乐与计时
     */
    public void resumeMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isTimerPaused = false;
            setMusicState(MUSIC_PLAYING);
        }
    }

    /**
     * 停止播放音乐与计时
     */
    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            musicAcitivity.onProgressUpdate(0);
            setMusicState(MUSIC_READY);
        }
    }


    @Override
    public void onDestroy() {
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if(timer!=null) {
            timer.cancel();
        }
        setMusicState(MUSIC_READY);
        super.onDestroy();

    }





    /**备用-------------------------------*/
    // 上一首
    public void playPrevious() {
        // 播放上一首音乐
    }
    // 下一首
    public void playNext() {
        // 播放下一首音乐
    }
    // 拖拽seek 服务前跳后跳
    public void SeekMusic(int seeknum) {
        // 跳至seek处
        mediaPlayer.seekTo(seeknum);
    }
    // 获取音乐播放进度
    public int getMusicProgress() {
        return mediaPlayer.getCurrentPosition();
    }
    // 获取音乐播放时长：毫秒
    public int getMusicLong() {
        return mediaPlayer.getDuration();
    }
    /**
     * 播放本地音乐
     * @param filePath
     */
    public void playLocalMusic(String filePath) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 播放在线音乐
     * @param url
     */
    public void playOnlineMusic(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 播放流音乐
     * @param streamUrl
     */
    public void playAudioStream(String streamUrl) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(streamUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
