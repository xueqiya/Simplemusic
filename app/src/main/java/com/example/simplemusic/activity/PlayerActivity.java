package com.example.simplemusic.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.simplemusic.bean.Music;
import com.example.simplemusic.R;
import com.example.simplemusic.util.Utils;
import com.example.simplemusic.service.MusicService;
import com.example.simplemusic.view.RotateAnimator;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView musicTitleView;
    private TextView musicArtistView;
    private ImageView btnPlayOrPause;
    private TextView nowTimeView;
    private TextView totalTimeView;
    private SeekBar seekBar;
    private RotateAnimator rotateAnimator;
    private MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //初始化
        init();
    }

    // 控件监听
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forward:
                // 上一首
                musicService.forward();
                break;
            case R.id.back:
                // 下一首
                musicService.back();
                break;
            case R.id.play_or_pause:
                // 播放或暂停
                if (musicService.isPlaying()) {
                    musicService.pause();
                } else {
                    musicService.play();
                }
                break;
            default:
        }
    }

    private void init() {
        musicTitleView = findViewById(R.id.title);
        musicArtistView = findViewById(R.id.artist);
        ImageView musicImgView = findViewById(R.id.imageView);
        btnPlayOrPause = findViewById(R.id.play_or_pause);
        ImageView forward = findViewById(R.id.forward);
        ImageView back = findViewById(R.id.back);
        seekBar = findViewById(R.id.seekbar);
        nowTimeView = findViewById(R.id.current_time);
        totalTimeView = findViewById(R.id.total_time);

        // ToolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);

        btnPlayOrPause.setOnClickListener(this);
        forward.setOnClickListener(this);
        back.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //拖动进度条时
                nowTimeView.setText(Utils.formatTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seekTo(seekBar.getProgress());
            }
        });

        //初始化动画
        rotateAnimator = new com.example.simplemusic.view.RotateAnimator(this, musicImgView);
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
            musicService = ((MusicService.MusicServiceBinder) service).getService();
            //注册监听器
            musicService.registerOnStateChangeListener(listener);
            //获得当前音乐
            Music item = musicService.getCurrentMusic();

            if (item == null) {
                //当前音乐为空, seekbar不可拖动
                seekBar.setEnabled(false);
            } else if (musicService.isPlaying()) {
                //如果正在播放音乐, 更新信息
                musicTitleView.setText(item.title);
                musicArtistView.setText(item.artist);
                btnPlayOrPause.setImageResource(R.drawable.ic_pause);
                rotateAnimator.playAnimator();
            } else {
                //如果当前暂停
                musicTitleView.setText(item.title);
                musicArtistView.setText(item.artist);
                btnPlayOrPause.setImageResource(R.drawable.ic_play);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //断开连接之后, 注销监听器
            musicService.unregisterOnStateChangeListener(listener);
        }
    };

    //实现监听器监听MusicService的变化，
    private final MusicService.OnStateChangeListener listener = new MusicService.OnStateChangeListener() {

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
