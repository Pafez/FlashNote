package com.pafez.flashnote;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "cards",
    foreignKeys = @ForeignKey(
        entity = Deck.class,
        parentColumns = "id",
        childColumns = "deckId",
        onDelete = CASCADE
    )
)
public class Card {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String front;
    public String back;

    public int deckId;
    public int position;

    public long createdAt;

    public Card(String front, String back, int deckId, int position, long createdAt) {
        this.front = front;
        this.back = back;
        this.deckId = deckId;
        this.position = position;
        this.createdAt = createdAt;
    }
}
