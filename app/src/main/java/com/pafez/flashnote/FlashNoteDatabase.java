package com.pafez.flashnote;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {Card.class, Deck.class, SessionRecord.class},
        version = 6,
        exportSchema = false
)
public abstract class FlashNoteDatabase extends RoomDatabase {

    public abstract CardDao cardDao();
    public abstract DeckDao deckDao();
    public abstract SessionDao sessionDao();

    private static volatile FlashNoteDatabase INSTANCE;

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE cards ADD COLUMN nextReviewDate INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE cards ADD COLUMN interval INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE cards ADD COLUMN easeFactor REAL NOT NULL DEFAULT 2.5");
            database.execSQL("ALTER TABLE cards ADD COLUMN repetitionCount INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `session_records` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`deckId` INTEGER NOT NULL, "
                    + "`timestamp` INTEGER NOT NULL, "
                    + "`cardsReviewed` INTEGER NOT NULL, "
                    + "`correctCount` INTEGER NOT NULL)");
        }
    };

    public static FlashNoteDatabase getInstance(Context context) {

        if (INSTANCE == null) {

            synchronized (FlashNoteDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FlashNoteDatabase.class,
                            "flashnote_database"
                    )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }

        return INSTANCE;
    }
}