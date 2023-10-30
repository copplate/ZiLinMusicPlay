package com.example.zilinmusicplay;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.zilinmusicplay.bean.Song;

import java.util.ArrayList;

public class MusicPlayActivity extends AppCompatActivity {

    private ArrayList<Song> songs;
    private int curSongIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
    }
}