package com.example.zilinmusicplay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.example.zilinmusicplay.bean.Song;
import com.example.zilinmusicplay.service.MyMusicService;

import java.util.ArrayList;

public class MusicPlayActivity extends AppCompatActivity {

    private ArrayList<Song> songs;
    private int curSongIndex;
    private ServiceConnection conn = new ServiceConnection() {//ServiceConnection相当于是Activity和Service的桥梁
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {//当服务链接之后
            //componentName暂时用不到
            //IBinder不但在Activity这边活动，还在Service里活动
            //服务已经建立，传递信息
            MyMusicService.MyMusicBind musicBind = (MyMusicService.MyMusicBind) iBinder;
            ((MyMusicService.MyMusicBind)iBinder).updateMusicList(songs);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {//当服务取消链接之后

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);


        startMusicService();
    }


    private void startMusicService() {
        //通过bind的形式启动Service
        Intent intent = new Intent(this, MyMusicService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }
}