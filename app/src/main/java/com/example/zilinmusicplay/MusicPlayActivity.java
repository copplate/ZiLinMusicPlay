package com.example.zilinmusicplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;

import com.example.zilinmusicplay.bean.Song;
import com.example.zilinmusicplay.databinding.ActivityMusicPlayBinding;
import com.example.zilinmusicplay.listener.MyPlayerListener;
import com.example.zilinmusicplay.service.MyMusicService;
import com.example.zilinmusicplay.util.PlayModeHelper;
import com.example.zilinmusicplay.util.TimeUtil;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.PrimitiveIterator;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayActivity extends AppCompatActivity {

    private ArrayList<Song> songs;
    private int curSongIndex;
    private MyMusicService.MyMusicBind mMusicBind;
    private ActivityMusicPlayBinding binding;
    private boolean isSeekBarDragging;//进度条是否正在被拖动
    private Timer timer;
    private int currentPlayMode = PlayModeHelper.PLAY_MODE_ORDER;//当前播放模式
    private Intent intentMusicService;
    private ServiceConnection conn = new ServiceConnection() {//ServiceConnection相当于是Activity和Service的桥梁
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {//当服务链接之后
            //componentName暂时用不到
            //IBinder不但在Activity这边活动，还在Service里活动
            //服务已经建立，传递信息
            mMusicBind = (MyMusicService.MyMusicBind) iBinder;
            mMusicBind.updateMusicList(songs);
            mMusicBind.updateCurrentMusicIndex(curSongIndex);
            mMusicBind.setPlayMode(currentPlayMode);
            mMusicBind.setMyPlayerListener(new MyPlayerListener() {
                @Override
                public void onComplete(int songIndex, Song song) {

                }

                @Override
                public void onNext(int songIndex, Song song) {
                    binding.tvMusicTitle.setText(songIndex + "、" + songs.get(songIndex).getSongName());
                    curSongIndex = songIndex;
                }

                @Override
                public void onPre(int songIndex, Song song) {
//                    binding.tvMusicTitle.setText(songs.get(songIndex).getSongName());
                }

                @Override
                public void onPause(int songIndex, Song song) {

                }

                @Override
                public void onPlay(int songIndex, Song song) {

                }
            });
            updateUI();

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
        binding.tvMusicTitle.setText(curSongIndex + "、" + songs.get(curSongIndex).getSongName());
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
            int next = mMusicBind.next();
            binding.tvMusicTitle.setText(next + "、" + songs.get(next).getSongName());
        });

        binding.ivPrevious.setOnClickListener(v -> {
            int previous = mMusicBind.previous();
            binding.tvMusicTitle.setText(previous + "、" + songs.get(previous).getSongName());
        });

        binding.ivStop.setOnClickListener(v -> {
            mMusicBind.stop();
            binding.ivPlay.setImageResource(android.R.drawable.ic_media_play);
            binding.seekBarMusic.setProgress(0);
        });

        /*binding.ivCover.setOnClickListener(v -> {
            Log.d("tiktok", "onCreate: -----binding.ivCover-----");
            stopService(intentMusicService);
        });*/

        binding.seekBarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateCurTimeText(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarDragging = false;
                int progress = binding.seekBarMusic.getProgress();
                mMusicBind.seekTo(progress);
            }
        });

        binding.tvPlayMode.setOnClickListener(v -> {
            currentPlayMode = PlayModeHelper.changePlayMode(currentPlayMode);
            String strPlayMode = PlayModeHelper.strPlayMode(currentPlayMode);
            binding.tvPlayMode.setText(strPlayMode);
            mMusicBind.setPlayMode(currentPlayMode);
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /*@Override
    public void onBackPressed() {
//        super.onBackPressed();
        //参数nonRoot的意思是你当前要结束(保护)的这个Activity是不是一个根节点，
        // 即它是不是一个最底层的Activity。
        // 如果参数填false的话，它就仅仅会对根节点生效，填true可以对任意节点生效
        moveTaskToBack(true);
    }*/

    private void startMusicService() {
        //通过bind的形式启动Service
        intentMusicService = new Intent(this, MyMusicService.class);
        bindService(intentMusicService, conn, BIND_AUTO_CREATE);
        startService(intentMusicService);

    }

    private void updateUI() {
        //当前时间更新
        int curProgress = mMusicBind.getCurProgress();
        String timeFormatCur = TimeUtil.millToTimeFormat(curProgress);
        binding.tvCurTime.setText(timeFormatCur);
        //总时间更新
        int duration = mMusicBind.getDuration();
        String timeFormatDur = TimeUtil.millToTimeFormat(duration);
        binding.tvDurTime.setText(timeFormatDur);

        //更新进度条
        binding.seekBarMusic.setMax(duration);
        binding.seekBarMusic.setProgress(curProgress);

        if (timer != null) {
            return;
        }
        //使用轮询查询的工具，Timer。这是一个定时系列，它可以启动一个任务，不断地去做某一件事情。
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int curProgress = mMusicBind.getCurProgress();
                String timeFormatCur = TimeUtil.millToTimeFormat(curProgress);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mMusicBind.isPlaying()是为了只有歌曲播放的时候才更新ui，歌曲不播
                        // 放的时候还更新ui就比较多余
                        if (!isSeekBarDragging && mMusicBind.isPlaying()) {
//                            binding.tvCurTime.setText(timeFormatCur);
                            binding.seekBarMusic.setProgress(curProgress);
                        }

                    }
                });

            }
        },0,500);
    }

    private void updateCurTimeText(int curProgress) {//拖动进度条时更新时间文字
        String timeFormatCur = TimeUtil.millToTimeFormat(curProgress);
        binding.tvCurTime.setText(timeFormatCur);
    }

}