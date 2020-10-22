package com.example.simplemusic.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.simplemusic.bean.Music;
import com.example.simplemusic.adapter.MusicAdapter;
import com.example.simplemusic.R;
import com.example.simplemusic.service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class LocalMusicActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView musicListView;
    private TextView playingTitleView;
    private TextView playingArtistView;
    private ImageView btnPlayOrPause;

    private List<Music> localMusicList;
    private MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localmusic);
        initData();
        init();
        // 列表项点击事件
        musicListView.setOnItemClickListener((parent, view, position, id) -> {
            Music music = localMusicList.get(position);
            musicService.addPlayList(music);
        });
    }

    private void initData() {
        localMusicList = new ArrayList<>();
        localMusicList.add(new Music(R.raw.test1, "富士山下1", "陈奕迅"));
        localMusicList.add(new Music(R.raw.test1, "富士山下2", "陈奕迅"));
        localMusicList.add(new Music(R.raw.test1, "富士山下3", "陈奕迅"));
    }

    private void init() {
        musicListView = findViewById(R.id.music_list);
        LinearLayout playerToolView = findViewById(R.id.player);
        playingTitleView = findViewById(R.id.playing_title);
        playingArtistView = findViewById(R.id.playing_artist);
        btnPlayOrPause = findViewById(R.id.play_or_pause);

        playerToolView.setOnClickListener(this);
        btnPlayOrPause.setOnClickListener(this);

        //绑定播放服务
        Intent i = new Intent(this, MusicService.class);
        bindService(i, mServiceConnection, BIND_AUTO_CREATE);

        // 本地音乐列表绑定适配器
        MusicAdapter adapter = new MusicAdapter(this, R.layout.music_item, localMusicList);
        musicListView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player:
                Intent intent = new Intent(LocalMusicActivity.this, PlayerActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.bottom_in, R.anim.bottom_silent);
                break;
            case R.id.play_or_pause:
                if (musicService.isPlaying()) {
                    musicService.pause();
                } else {
                    musicService.play();
                }
                break;
        }
    }

    // 定义与服务的连接的匿名类
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        // 绑定成功时调用
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 绑定成功后，取得MusicSercice提供的接口
            musicService = ((MusicService.MusicServiceBinder) service).getService();
            // 注册监听器
            musicService.registerOnStateChangeListener(listener);

            Music item = musicService.getCurrentMusic();

            if (musicService.isPlaying()) {
                // 如果正在播放音乐, 更新控制栏信息
                btnPlayOrPause.setImageResource(R.drawable.zanting);
                playingTitleView.setText(item.title);
                playingArtistView.setText(item.artist);
            } else if (item != null) {
                // 当前有可播放音乐但没有播放
                btnPlayOrPause.setImageResource(R.drawable.bofang);
                playingTitleView.setText(item.title);
                playingArtistView.setText(item.artist);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 断开连接时注销监听器
            musicService.unregisterOnStateChangeListener(listener);
        }
    };

    // 实现监听器监听MusicService的变化
    private final MusicService.OnStateChangeListenr listener = new MusicService.OnStateChangeListenr() {
        @Override
        public void onPlayProgressChange(long played, long duration) {
        }

        @Override
        public void onPlay(Music item) {
            // 播放状态变为播放时
            btnPlayOrPause.setImageResource(R.drawable.zanting);
            playingTitleView.setText(item.title);
            playingArtistView.setText(item.artist);
            btnPlayOrPause.setEnabled(true);
        }

        @Override
        public void onPause() {
            // 播放状态变为暂停时
            btnPlayOrPause.setImageResource(R.drawable.bofang);
            btnPlayOrPause.setEnabled(true);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localMusicList.clear();
        unbindService(mServiceConnection);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
