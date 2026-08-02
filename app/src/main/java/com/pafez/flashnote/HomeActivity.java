package com.pafez.flashnote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NoteAdapter.OnNoteLongClickListener {

    private NoteAdapter noteAdapter;

    private FlashNoteDatabase database;

    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        RecyclerView notesRecyclerView =
                findViewById(R.id.notesRecyclerView);

        FloatingActionButton addNoteButton =
                findViewById(R.id.addNoteButton);

        emptyStateText =
                findViewById(R.id.emptyStateText);

        // Get database
        database =
                FlashNoteDatabase.getInstance(
                        getApplicationContext()
                );

        // Create adapter
        noteAdapter =
                new NoteAdapter(
                        new ArrayList<>(),
                        this
                );

        // RecyclerView setup
        notesRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        notesRecyclerView.setAdapter(
                noteAdapter
        );

        // Add note button
        addNoteButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            ImportActivity.class
                    );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadNotes();
    }

    private void loadNotes() {

        new Thread(() -> {

            List<Note> notes =
                    database
                            .noteDao()
                            .getAllNotes();

            runOnUiThread(() -> {

                noteAdapter.updateNotes(notes);

                // Update empty state
                if (notes.isEmpty()) {

                    emptyStateText.setVisibility(
                            View.VISIBLE
                    );

                } else {

                    emptyStateText.setVisibility(
                            View.GONE
                    );
                }
            });

        }).start();
    }

    // Called by NoteAdapter
    // when a note is long-pressed
    @Override
    public void onNoteLongClick(Note note) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage(
                        "Are you sure you want to delete this note?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            deleteNote(note);
                        }
                )
                .show();
    }

    private void deleteNote(Note note) {

        new Thread(() -> {

            // Delete from Room
            database
                    .noteDao()
                    .delete(note);

            // Reload the notes
            runOnUiThread(() -> {

                loadNotes();

            });

        }).start();
    }
}