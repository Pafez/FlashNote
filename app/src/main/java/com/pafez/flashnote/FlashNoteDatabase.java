package com.pafez.flashnote;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Card.class, Deck.class},
        version = 4,
        exportSchema = false
)
public abstract class FlashNoteDatabase extends RoomDatabase {

    public abstract CardDao cardDao();
    public abstract DeckDao deckDao();

    private static volatile FlashNoteDatabase INSTANCE;

    public static FlashNoteDatabase getInstance(Context context) {

        if (INSTANCE == null) {

            synchronized (FlashNoteDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FlashNoteDatabase.class,
                            "flashnote_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }

        return INSTANCE;
    }
}