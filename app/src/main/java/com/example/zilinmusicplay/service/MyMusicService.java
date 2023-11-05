package com.example.zilinmusicplay.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.zilinmusicplay.bean.Song;

import java.io.IOException;
import java.util.ArrayList;

public class MyMusicService extends Service {

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songs;
    private int curSongIndex;
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        songs = new ArrayList<>();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //整理一下思路，要想实现循环播放的功能，当播放完一首歌曲之后，我们要知道它播放完了，所以要在这里监听
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyMusicBind(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    private void updateMusicList(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public void updateCurrentMusicIndex(int index) {
        if (index < 0 || index >= songs.size()) {
            return;
        }
        this.curSongIndex = index;
        Song song = songs.get(index);
        String songName = song.getSongName();
        //播放该条歌曲
        AssetManager assetManager = getAssets();
        try {
            //释放上一个音乐的资源
            mediaPlayer.stop();
            mediaPlayer.reset();
            //AssetFileDescriptor是文件描述符
            //通过这个文件描述符可以取到数据
            AssetFileDescriptor fileDescriptor = assetManager.openFd(songName);
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
//            getExternalMediaDirs(),这个api看起来不错
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        if (!mediaPlayer.isPlaying()) {
            return;
        }
        mediaPlayer.pause();
    }

    public void play() {
        if (mediaPlayer.isPlaying()) {
            return;
        }
        mediaPlayer.start();
    }

    public int previous() {
        int preIndex = curSongIndex - 1;//curSongIndex - 1不会改变curSongIndex本身，而curSongIndex--会改变
        if (preIndex < 0) {
            preIndex = songs.size() - 1;
        }
        updateCurrentMusicIndex(preIndex);
        return preIndex;
    }

    public int next() {
        int nextIndex = curSongIndex + 1;//curSongIndex - 1不会改变curSongIndex本身，而curSongIndex--会改变
        if (nextIndex > songs.size() - 1) {
            nextIndex = 0;
        }
        updateCurrentMusicIndex(nextIndex);
        return nextIndex;
    }

    public void stop() {
        mediaPlayer.stop();//stop之后这个mediaPlayer的生命周期就结束了，如果不prepare，再次调用播放会失败
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getCurProgress() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    private void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    public class MyMusicBind extends Binder {

        private MyMusicService myMusicService;

        public MyMusicBind(MyMusicService myMusicService) {
            this.myMusicService = myMusicService;
        }

        public void startPlay() {

        }

        public void updateMusicList(ArrayList<Song> songs) {
            myMusicService.updateMusicList(songs);
        }
        public void updateCurrentMusicIndex(int index) {
            myMusicService.updateCurrentMusicIndex(index);
        }

        public boolean isPlaying() {
            return myMusicService.isPlaying();
        }

        public void pause() {
            myMusicService.pause();
        }

        public void play() {
            myMusicService.play();
        }

        public int previous() {
            int previous = myMusicService.previous();
            return previous;
        }

        public int next() {
            int next = myMusicService.next();
            return next;
        }

        public void stop() {
            myMusicService.stop();
        }

        public int getCurProgress() {
            return myMusicService.getCurProgress();
        }

        public int getDuration() {
            return myMusicService.getDuration();
        }

        public void seekTo(int progress) {
            myMusicService.seekTo(progress);
        }


    }



}
