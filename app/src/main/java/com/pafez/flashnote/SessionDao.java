package com.pafez.flashnote;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SessionDao {

    @Insert
    void insertSession(SessionRecord record);

    @Query("SELECT * FROM session_records WHERE deckId = :deckId ORDER BY timestamp DESC")
    List<SessionRecord> getSessionsForDeck(int deckId);
}
