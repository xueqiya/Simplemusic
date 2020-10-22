package com.example.simplemusic.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.example.simplemusic.bean.Music;
import com.example.simplemusic.util.NotifyHelper;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private MediaPlayer player;
    private List<OnStateChangeListenr> listenerList;
    private MusicServiceBinder binder;
    private AudioManager audioManager;
    private Music currentMusic; // 当前就绪的音乐
    private boolean autoPlayAfterFocus;    // 获取焦点之后是否自动播放
    private boolean isNeedReload;     // 播放时是否需要重新加载
    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 66) {//通知监听者当前的播放进度
                long played = player.getCurrentPosition();
                long duration = player.getDuration();
                for (OnStateChangeListenr l : listenerList) {
                    l.onPlayProgressChange(played, duration);
                }
                //间隔一秒发送一次更新播放进度的消息
                sendEmptyMessageDelayed(66, 1000);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        listenerList = new ArrayList<>();    //初始化监听器列表
        player = new MediaPlayer();   //初始化播放器
        binder = new MusicServiceBinder();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE); //获得音频管理服务
    }

    @Override
    public IBinder onBind(Intent intent) {
        //当组件bindService()之后，将这个Binder返回给组件使用
        return binder;
    }

    public void addPlayList(Music music) {
        currentMusic = music;
        isNeedReload = true;
        play();
    }

    //开始播放
    public void play() {
        try {
            //获取音频焦点
            audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (currentMusic == null) {
                return;
            }
            if (isNeedReload) {
                //需要重新加载音乐
                player.reset();
                //设置播放音乐的地址
                player = MediaPlayer.create(MusicService.this, currentMusic.songUrl);
            }
            player.start();
            for (OnStateChangeListenr l : listenerList) {
                l.onPlay(currentMusic);
            }
            isNeedReload = true;
            //移除现有的更新消息，重新启动更新
            handler.removeMessages(66);
            handler.sendEmptyMessage(66);
            startForeground();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //暂停
    public void pause() {
        player.pause();
        for (OnStateChangeListenr l : listenerList) {
            l.onPause();
        }
        // 暂停后不需要重新加载
        isNeedReload = false;
    }

    public void forward() {
        int position = player.getCurrentPosition();
        player.seekTo(position + 10000);
    }

    public void back() {
        int position = player.getCurrentPosition();
        player.seekTo(position - 10000);
    }

    //将音乐拖动到指定的时间
    public void seekTo(int pos) {
        player.seekTo(pos);
    }

    public Music getCurrentMusic() {
        return currentMusic;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    @SuppressLint("InlinedApi")
    private void startForeground() {
        startForeground(999, NotifyHelper.notify(currentMusic.title, currentMusic.artist));
    }

    // 注册监听器
    public void registerOnStateChangeListener(OnStateChangeListenr l) {
        listenerList.add(l);
    }

    // 注销监听器
    public void unregisterOnStateChangeListener(OnStateChangeListenr l) {
        listenerList.remove(l);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();

        listenerList.clear();
        handler.removeMessages(66);
        //注销音频管理服务
        audioManager.abandonAudioFocus(audioFocusListener);
    }

    //定义binder与活动通信
    public class MusicServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    //对外监听器接口
    public interface OnStateChangeListenr {
        void onPlayProgressChange(long played, long duration);  //播放进度变化

        void onPlay(Music item);    //播放状态变化

        void onPause();   //播放状态变化
    }

    //焦点控制
    private final AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (player.isPlaying()) {
                        //会长时间失去，所以告知下面的判断，获得焦点后不要自动播放
                        autoPlayAfterFocus = false;
                        pause();//因为会长时间失去，所以直接暂停
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (player.isPlaying()) {
                        //短暂失去焦点，先暂停。同时将标志位置成重新获得焦点后就开始播放
                        autoPlayAfterFocus = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    //重新获得焦点，且符合播放条件，开始播放
                    if (!player.isPlaying() && autoPlayAfterFocus) {
                        autoPlayAfterFocus = false;
                        play();
                    }
                    break;
            }
        }
    };
}
