package com.example.zilinmusicplay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.zilinmusicplay.adapetrrlv.RecySongListApapter;
import com.example.zilinmusicplay.bean.Song;

import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity {
    private RecyclerView rlvSongList;
    private ArrayList<Song> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        initView();
        initData();
        initSongList();
    }

    private void initData() {
        songs = new ArrayList<>();
        songs.add(new Song("不要说话_陈奕迅.mp3"));
        songs.add(new Song("反方向的钟_周杰伦.mp3"));
        songs.add(new Song("安静_周杰伦.mp3"));
        songs.add(new Song("早开的晚霞_林宥嘉.mp3"));
        songs.add(new Song("烟花易冷_周杰伦.mp3"));
        songs.add(new Song("一样的月光-徐家莹.mp3"));
        /*songs.add(new Song("Die For You.mp3"));
        songs.add(new Song("初恋.mp3"));
        songs.add(new Song("安静.mp3"));
        songs.add(new Song("新地球.mp3"));*/
        /*songs.add(new Song("Say so"));
        songs.add(new Song("Streets"));
        songs.add(new Song("慢慢"));
        songs.add(new Song("如果这都不算爱"));
        songs.add(new Song("向天再借五百年"));*/
    }

    private void initSongList() {
        RecySongListApapter recySongListApapter = new RecySongListApapter(songs, this);
        recySongListApapter.setOnSongItemClickListener(new RecySongListApapter.OnSongItemClickListener() {
            @Override
            public void click(int position) {
                Intent intent = new Intent(MusicListActivity.this, MusicPlayActivity.class);
                intent.putExtra("key_song_list", songs);//好家伙，List还不能传输，只能传输ArrayList。
                intent.putExtra("key_song_index", position);
                startActivity(intent);
            }
        });
        rlvSongList.setAdapter(recySongListApapter);
        rlvSongList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initView() {
        rlvSongList = findViewById(R.id.rlv_song_list);
    }
}