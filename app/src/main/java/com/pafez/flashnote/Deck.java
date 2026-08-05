package com.pafez.flashnote;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "decks")
public class Deck {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public Deck(String name) {
        this.name = name;
    }
}
