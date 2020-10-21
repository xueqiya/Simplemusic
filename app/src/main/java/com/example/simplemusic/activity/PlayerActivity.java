package com.example.simplemusic.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.simplemusic.bean.Music;
import com.example.simplemusic.R;
import com.example.simplemusic.util.Utils;
import com.example.simplemusic.service.MusicService;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView musicTitleView;
    private TextView musicArtistView;
    private ImageView musicImgView;
    private ImageView btnPlayOrPause;
    private TextView nowTimeView;
    private TextView totalTimeView;
    private SeekBar seekBar;
    private com.example.simplemusic.view.RotateAnimator rotateAnimator;
    private MusicService.MusicServiceBinder serviceBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //初始化
        initActivity();
    }

    // 控件监听
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pre:
                // 上一首
                serviceBinder.playPre();
                break;
            case R.id.play_next:
                // 下一首
                serviceBinder.playNext();
                break;
            case R.id.play_or_pause:
                // 播放或暂停
                serviceBinder.playOrPause();
                break;
            default:
        }
    }

    private void initActivity() {
        musicTitleView = findViewById(R.id.title);
        musicArtistView = findViewById(R.id.artist);
        musicImgView = findViewById(R.id.imageView);
        btnPlayOrPause = findViewById(R.id.play_or_pause);
        ImageView btnPlayPre = findViewById(R.id.play_pre);
        ImageView btnPlayNext = findViewById(R.id.play_next);
        seekBar = findViewById(R.id.seekbar);
        nowTimeView = findViewById(R.id.current_time);
        totalTimeView = findViewById(R.id.total_time);
        ImageView needleView = findViewById(R.id.ivNeedle);

        // ToolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);

        btnPlayOrPause.setOnClickListener(this);
        btnPlayPre.setOnClickListener(this);
        btnPlayNext.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //拖动进度条时
                nowTimeView.setText(Utils.formatTime((long) progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                serviceBinder.seekTo(seekBar.getProgress());
            }
        });

        //初始化动画
        rotateAnimator = new com.example.simplemusic.view.RotateAnimator(this, musicImgView, needleView);
        rotateAnimator.set_Needle();

        // 绑定service
        Intent i = new Intent(this, MusicService.class);
        bindService(i, mServiceConnection, BIND_AUTO_CREATE);
    }

    //定义与服务的连接的匿名类
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //绑定成功后，取得MusicSercice提供的接口
            serviceBinder = (MusicService.MusicServiceBinder) service;

            //注册监听器
            serviceBinder.registerOnStateChangeListener(listenr);

            //获得当前音乐
            Music item = serviceBinder.getCurrentMusic();

            if (item == null) {
                //当前音乐为空, seekbar不可拖动
                seekBar.setEnabled(false);
            } else if (serviceBinder.isPlaying()) {
                //如果正在播放音乐, 更新信息
                musicTitleView.setText(item.title);
                musicArtistView.setText(item.artist);
                btnPlayOrPause.setImageResource(R.drawable.ic_pause);
                rotateAnimator.playAnimator();
                ContentResolver resolver = getContentResolver();
                Bitmap img = Utils.getLocalMusicBmp(resolver, item.imgUrl);
                Glide.with(getApplicationContext())
                        .load(img)
                        .placeholder(R.drawable.defult_music_img)
                        .error(R.drawable.defult_music_img)
                        .into(musicImgView);
            } else {
                //当前有可播放音乐但没有播放
                musicTitleView.setText(item.title);
                musicArtistView.setText(item.artist);
                btnPlayOrPause.setImageResource(R.drawable.ic_play);
                ContentResolver resolver = getContentResolver();
                Bitmap img = Utils.getLocalMusicBmp(resolver, item.imgUrl);
                Glide.with(getApplicationContext())
                        .load(img)
                        .placeholder(R.drawable.defult_music_img)
                        .error(R.drawable.defult_music_img)
                        .into(musicImgView);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //断开连接之后, 注销监听器
            serviceBinder.unregisterOnStateChangeListener(listenr);
        }
    };

    //实现监听器监听MusicService的变化，
    private final MusicService.OnStateChangeListenr listenr = new MusicService.OnStateChangeListenr() {

        @Override
        public void onPlayProgressChange(long played, long duration) {
            seekBar.setMax((int) duration);
            totalTimeView.setText(Utils.formatTime(duration));
            nowTimeView.setText(Utils.formatTime(played));
            seekBar.setProgress((int) played);
        }

        @Override
        public void onPlay(final Music item) {
            //变为播放状态时
            musicTitleView.setText(item.title);
            musicArtistView.setText(item.artist);
            btnPlayOrPause.setImageResource(R.drawable.ic_pause);
            rotateAnimator.playAnimator();
            ContentResolver resolver = getContentResolver();
            Bitmap img = Utils.getLocalMusicBmp(resolver, item.imgUrl);
            Glide.with(getApplicationContext())
                    .load(img)
                    .placeholder(R.drawable.defult_music_img)
                    .error(R.drawable.defult_music_img)
                    .into(musicImgView);
        }

        @Override
        public void onPause() {
            //变为暂停状态时
            btnPlayOrPause.setImageResource(R.drawable.ic_play);
            rotateAnimator.pauseAnimator();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        //界面退出时的动画
        overridePendingTransition(R.anim.bottom_silent, R.anim.bottom_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
