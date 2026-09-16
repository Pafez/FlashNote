package com.pafez.flashnote;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "session_records")
public class SessionRecord {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int deckId;
    public long timestamp;
    public int cardsReviewed;
    public int correctCount;

    public SessionRecord() {
    }

    @Ignore
    public SessionRecord(int deckId, long timestamp, int cardsReviewed, int correctCount) {
        this.deckId = deckId;
        this.timestamp = timestamp;
        this.cardsReviewed = cardsReviewed;
        this.correctCount = correctCount;
    }
}
