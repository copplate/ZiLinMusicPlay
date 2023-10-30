package com.example.zilinmusicplay.bean;

import java.io.Serializable;

public class Song implements Serializable {
    private String songName;

    public Song() {
    }

    public Song(String songName) {
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    @Override
    public String toString() {
        return "Song{" +
                "songName='" + songName + '\'' +
                '}';
    }
}
