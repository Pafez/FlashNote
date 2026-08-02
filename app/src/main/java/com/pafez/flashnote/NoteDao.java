package com.pafez.flashnote;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    void insert(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    Note getNoteById(int id);
}