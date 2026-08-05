package com.pafez.flashnote;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeckDao {

    @Insert
    long insert(Deck deck);

    @Query("SELECT * FROM decks")
    List<Deck> getAllDecks();

    @Delete
    void delete(Deck deck);

    @Query("SELECT COUNT(*) FROM decks")
    int getDeckCount();
}
