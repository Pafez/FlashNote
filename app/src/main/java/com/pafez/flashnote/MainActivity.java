package com.pafez.flashnote;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DeckAdapter.OnDeckClickListener {

    private FlashNoteDatabase database;
    private DeckAdapter deckAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FlashNoteDatabase.getInstance(getApplicationContext());

        RecyclerView decksRecyclerView = findViewById(R.id.decksRecyclerView);
        FloatingActionButton addDeckButton = findViewById(R.id.addDeckButton);

        deckAdapter = new DeckAdapter(new ArrayList<>(), this);
        decksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        decksRecyclerView.setAdapter(deckAdapter);

        addDeckButton.setOnClickListener(v -> showAddDeckDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDecks();
    }

    private void loadDecks() {
        new Thread(() -> {
            List<Deck> decks = database.deckDao().getAllDecks();
            runOnUiThread(() -> deckAdapter.updateDecks(decks));
        }).start();
    }

    private void showAddDeckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Deck");

        final EditText input = new EditText(this);
        input.setHint("Enter deck name");
        // 200 character limit
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                createNewDeck(name);
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createNewDeck(String name) {
        new Thread(() -> {
            Deck deck = new Deck(name);
            database.deckDao().insert(deck);
            loadDecks();
        }).start();
    }

    @Override
    public void onDeckClick(Deck deck) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("deck_id", deck.id);
        intent.putExtra("deck_name", deck.name);
        startActivity(intent);
    }
}
