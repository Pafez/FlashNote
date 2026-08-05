package com.pafez.flashnote;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "decks")
public class Deck {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    @Ignore
    public List<Card> cards;

    public Deck(String name) {
        this.name = name;
    }

    @Ignore
    public Deck(String name, List<Card> cards) {
        this.name = name;
        this.cards = cards;
    }

    public void addCard(Card card) {
        if (cards != null) {
            cards.add(card);
        }
    }

    public void deleteCard(Card card) {
        if (cards != null) {
            cards.remove(card);
        }
    }
}
