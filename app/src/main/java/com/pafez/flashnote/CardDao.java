package com.pafez.flashnote;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CardDao {

    @Insert
    void insert(Card card);

    @androidx.room.Update
    void update(Card card);

    @Query("SELECT * FROM cards WHERE id = :cardId")
    Card getCardById(int cardId);

    @Query("SELECT * FROM cards ORDER BY createdAt DESC")
    List<Card> getAllCards();

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY position ASC")
    List<Card> getCardsForDeck(int deckId);

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND nextReviewDate <= :maxTimestamp ORDER BY nextReviewDate ASC")
    List<Card> getDueCardsForDeck(int deckId, long maxTimestamp);

    @Delete
    void delete(Card card);

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    int getCardCountForDeck(int deckId);

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND nextReviewDate <= :maxTimestamp")
    int getDueCardCountForDeck(int deckId, long maxTimestamp);

    @Query("SELECT COUNT(*) FROM cards WHERE nextReviewDate <= :maxTimestamp")
    int getTotalDueCardCount(long maxTimestamp);
}
