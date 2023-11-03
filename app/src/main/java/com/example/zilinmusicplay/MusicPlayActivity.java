package com.example.zilinmusicplay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.zilinmusicplay.bean.Song;
import com.example.zilinmusicplay.databinding.ActivityMusicPlayBinding;
import com.example.zilinmusicplay.service.MyMusicService;

import java.io.Serializable;
import java.util.ArrayList;

public class MusicPlayActivity extends AppCompatActivity {

    private ArrayList<Song> songs;
    private int curSongIndex;
    private MyMusicService.MyMusicBind mMusicBind;
    private ActivityMusicPlayBinding binding;
    private ServiceConnection conn = new ServiceConnection() {//ServiceConnection相当于是Activity和Service的桥梁
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {//当服务链接之后
            //componentName暂时用不到
            //IBinder不但在Activity这边活动，还在Service里活动
            //服务已经建立，传递信息
            mMusicBind = (MyMusicService.MyMusicBind) iBinder;
            mMusicBind.updateMusicList(songs);
            mMusicBind.updateCurrentMusicIndex(curSongIndex);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {//当服务取消链接之后

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_music_play);

        Intent intent = getIntent();
        songs = (ArrayList<Song>) intent.getSerializableExtra("key_song_list");
        curSongIndex = intent.getIntExtra("key_song_index",0);
        Log.d("tiktok", "onCreate: ----------" + songs);
        startMusicService();

        binding.ivPlay.setOnClickListener(v -> {
            if (mMusicBind.isPlaying()) {
                //暂停音乐
                mMusicBind.pause();
                binding.ivPlay.setImageResource(android.R.drawable.ic_media_play);
            } else {
                //播放音乐
                mMusicBind.play();
                binding.ivPlay.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        binding.ivNext.setOnClickListener(v -> {
            mMusicBind.next();
        });

        binding.ivPrevious.setOnClickListener(v -> {
            mMusicBind.previous();
        });

        binding.ivStop.setOnClickListener(v -> {
            mMusicBind.stop();
        });

    }


    private void startMusicService() {
        //通过bind的形式启动Service
        Intent intent = new Intent(this, MyMusicService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }
}