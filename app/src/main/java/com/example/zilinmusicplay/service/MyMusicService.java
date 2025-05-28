package com.example.zilinmusicplay.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.zilinmusicplay.R;
import com.example.zilinmusicplay.bean.Song;
import com.example.zilinmusicplay.data.GlobalConstants;
import com.example.zilinmusicplay.listener.MyPlayerListener;
import com.example.zilinmusicplay.util.PlayModeHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MyMusicService extends Service {

    private static final String CHANNEL_ID = "song_play_channel";
    //前台Service三部曲 1、定义一个前台ServiceId
    public static final int FOREGROUND_ID = 1;
    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songs;
    private int curSongIndex;
    private int curPlayMode;//当前的播放模式
    private MyPlayerListener myPlayerListener;
    private RemoteViews remoteView;
    private boolean haveNotification = false;//是否创建了通知.
    private Notification notification;
    private NotificationManager notificationManager;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case GlobalConstants.ACTION_CLOSE_MUSIC:
                    break;
                case GlobalConstants.ACTION_PRE_MUSIC:
                    previous();
                    break;
                case GlobalConstants.ACTION_NEXT_MUSIC:
                    next();
                    break;
                case GlobalConstants.ACTION_PLAY_PAUSE_MUSIC:
                    if (isPlaying()) {
                        pause();
                    } else {
                        play();
                    }
                    break;
                case GlobalConstants.ACTION_START_PLAY_ACTIVITY:
                    break;
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        songs = new ArrayList<>();
//        stopSelf();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //整理一下思路，要想实现循环播放的功能，当播放完一首歌曲之后，我们要知道它播放完了，所以要在这里监听
                next();//本来我还想着在next()和previous()函数里做文章，确实不如这样简洁

            }
        });

        //广播注册
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GlobalConstants.ACTION_CLOSE_MUSIC);
        intentFilter.addAction(GlobalConstants.ACTION_NEXT_MUSIC);
        intentFilter.addAction(GlobalConstants.ACTION_PRE_MUSIC);
        intentFilter.addAction(GlobalConstants.ACTION_PLAY_PAUSE_MUSIC);
        intentFilter.addAction(GlobalConstants.ACTION_START_PLAY_ACTIVITY);
        registerReceiver(mReceiver, intentFilter);
    }

    private void createNotification() {
        if (haveNotification) {
            return;
        }
        //Notification Channel的创建
        //把Channel真正的创建出来
        //创建一个通知的渠道，需要一个通知的Manager,这个NotificationManager是一个系统的服务
        notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //参数CHANNEL_ID是我们自己定义的，相当于是唯一标识的一个名字。"音乐播放通知▶"是在手机app里可见的一个名字
            //NotificationManager.IMPORTANCE_HIGH代表着这种渠道(类别)的通知的重要性
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, "音乐播放通知▶", NotificationManager.IMPORTANCE_HIGH);
            //使用manager把渠道创建出来
            notificationManager.createNotificationChannel(channel);
        }
        //自定义Notification中的内容View
        //参数1是包名
        remoteView = new RemoteViews(getPackageName(), R.layout.notification_music_layout);
        //点击通知中内容View的子View，发出广播

        //设置通知中的TextView的文字
        Song song = getCurSong();
        if (song != null) {
            remoteView.setTextViewText(R.id.tv_notification_title,songs.get(curSongIndex).getSongName());
        }
        //点击通知中内容View的子View,发出广播
        int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_IMMUTABLE
                : 0;

        //下一曲
        Intent nextIntent = new Intent(GlobalConstants.ACTION_NEXT_MUSIC);
        PendingIntent nextPendIntent = PendingIntent.getBroadcast(this, 0, nextIntent, flag);
        remoteView.setOnClickPendingIntent(R.id.iv_next,nextPendIntent);

        //上一曲
        Intent preIntent = new Intent(GlobalConstants.ACTION_PRE_MUSIC);
        PendingIntent prePendIntent = PendingIntent.getBroadcast(this, 0, preIntent, flag);
        remoteView.setOnClickPendingIntent(R.id.iv_previous,prePendIntent);

        //暂停、播放
        Intent playPauseIntent = new Intent(GlobalConstants.ACTION_PLAY_PAUSE_MUSIC);
        PendingIntent playPausePendIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, flag);
        remoteView.setOnClickPendingIntent(R.id.iv_play_pause,playPausePendIntent);

        //关闭音乐(停止音乐服务)
        Intent closeIntent = new Intent(GlobalConstants.ACTION_CLOSE_MUSIC);
        PendingIntent closePendIntent = PendingIntent.getBroadcast(this, 0, closeIntent, flag);
        remoteView.setOnClickPendingIntent(R.id.iv_close,closePendIntent);


        //参数String channelId，是一个渠道的id号。在Android8.0之后，给通知规定了一个概念：通知可以分类别。
        //我们可以把同一类别的通知归到一个渠道里，因此就有了渠道号这个概念。渠道号其实就是一个标识，我们可以任意来指定。
        //前台Service三部曲 2、创建通知
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("这是音乐内容")
                .setContentTitle("这是音乐标题")
                .setSmallIcon(android.R.drawable.ic_media_play)
                //如果取消setCustomContentView(remoteView)，不去自定义View，就会用系统自带的
                .setCustomContentView(remoteView)
                //设置内容的Intent，当我们点击通知的时候，可以产生这样一个Intent(startSongPlayPendIntent)，去打开我们的Activity
//                .setContentIntent(startSongPlayPendIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),android.R.drawable.ic_media_play))
                .build();
        //前台Service三部曲 3、联合通知将当前Service启动成前台Service
        startForeground(FOREGROUND_ID,notification);
        haveNotification = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();
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
        //解除注册
        unregisterReceiver(mReceiver);
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

        //更新通知里的歌曲名
        remoteView.setTextViewText(R.id.tv_notification_title,songs.get(curSongIndex).getSongName());
        //然后更新通知
        notificationManager.notify(FOREGROUND_ID,notification);

    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        if (!mediaPlayer.isPlaying()) {
            return;
        }
        //设置通知中View的图标
        notification.contentView.setImageViewResource(R.id.iv_play_pause,android.R.drawable.ic_media_play);
        //然后更新通知
        notificationManager.notify(FOREGROUND_ID,notification);

        mediaPlayer.pause();
    }

    public void play() {
        if (mediaPlayer.isPlaying()) {
            return;
        }
        //设置通知中View的图标
        notification.contentView.setImageViewResource(R.id.iv_play_pause,android.R.drawable.ic_media_pause);
        //然后更新通知
        notificationManager.notify(FOREGROUND_ID,notification);

        mediaPlayer.start();
    }

    public int previous() {
        if (curPlayMode == PlayModeHelper.PLAY_MODE_CIRCLE) {
            updateCurrentMusicIndex(curSongIndex);
//            myPlayerListener.onPre(curSongIndex, songs.get(curSongIndex));
            return curSongIndex;
        } else if (curPlayMode == PlayModeHelper.PLAY_MODE_RANDOM) {
            int nextRandomIndex = getNextRandomIndex();
            updateCurrentMusicIndex(nextRandomIndex);
            myPlayerListener.onNext(nextRandomIndex, songs.get(curSongIndex));
            return nextRandomIndex;
        } else{
            int preIndex = curSongIndex - 1;//curSongIndex - 1不会改变curSongIndex本身，而curSongIndex--会改变
            if (preIndex < 0) {
                preIndex = songs.size() - 1;
            }
            updateCurrentMusicIndex(preIndex);
//            myPlayerListener.onPre(preIndex, songs.get(preIndex));
            return preIndex;
        }
    }

    public int next() {
        if (myPlayerListener == null) {
            return -1;
        }
        if (curPlayMode == PlayModeHelper.PLAY_MODE_CIRCLE) {
            updateCurrentMusicIndex(curSongIndex);
            myPlayerListener.onNext(curSongIndex, songs.get(curSongIndex));
            return curSongIndex;
        } else if (curPlayMode == PlayModeHelper.PLAY_MODE_RANDOM) {
            int nextRandomIndex = getNextRandomIndex();
            updateCurrentMusicIndex(nextRandomIndex);
            myPlayerListener.onNext(nextRandomIndex, songs.get(curSongIndex));
            return nextRandomIndex;
        } else {
            int nextIndex = curSongIndex + 1;//curSongIndex - 1不会改变curSongIndex本身，而curSongIndex--会改变
            if (nextIndex > songs.size() - 1) {
                nextIndex = 0;
            }
            updateCurrentMusicIndex(nextIndex);
            myPlayerListener.onNext(nextIndex, songs.get(nextIndex));
            return nextIndex;
        }

    }

    public void stop() {
        mediaPlayer.stop();//stop之后这个mediaPlayer的生命周期就结束了，如果不prepare，再次调用播放会失败
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Song getCurSong() {
        if (curSongIndex < 0 || curSongIndex >= songs.size()) {
            return null;
        }
        return songs.get(curSongIndex);
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

    public void setPlayMode(int mode) {
        this.curPlayMode = mode;
    }

    public void setMyPlayerListener(MyPlayerListener myPlayerListener) {
        this.myPlayerListener = myPlayerListener;
    }

    private int getNextRandomIndex() {
        Random random = new Random();
        int randomIndex = random.nextInt(songs.size());
        return randomIndex;
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

        public void setPlayMode(int mode) {
            myMusicService.setPlayMode(mode);
        }

        public void setMyPlayerListener(MyPlayerListener myPlayerListener) {
            myMusicService.setMyPlayerListener(myPlayerListener);
        }

    }



}
