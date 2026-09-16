package com.pafez.flashnote;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
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

    @ColumnInfo(defaultValue = "0")
    public long nextReviewDate;

    @ColumnInfo(defaultValue = "0")
    public int interval;

    @ColumnInfo(defaultValue = "2.5")
    public float easeFactor = 2.5f;

    @ColumnInfo(defaultValue = "0")
    public int repetitionCount;

    public Card() {
    }

    @Ignore
    public Card(String front, String back, int deckId, int position, long createdAt) {
        this.front = front;
        this.back = back;
        this.deckId = deckId;
        this.position = position;
        this.createdAt = createdAt;
        this.nextReviewDate = 0;
        this.interval = 0;
        this.easeFactor = 2.5f;
        this.repetitionCount = 0;
    }

    @Ignore
    public Card(String front, String back, int deckId, int position, long createdAt,
                long nextReviewDate, int interval, float easeFactor, int repetitionCount) {
        this.front = front;
        this.back = back;
        this.deckId = deckId;
        this.position = position;
        this.createdAt = createdAt;
        this.nextReviewDate = nextReviewDate;
        this.interval = interval;
        this.easeFactor = easeFactor;
        this.repetitionCount = repetitionCount;
    }
}
