package com.pafez.flashnote;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String text;

    public long createdAt;

    public Note(String text, long createdAt) {
        this.text = text;
        this.createdAt = createdAt;
    }
}