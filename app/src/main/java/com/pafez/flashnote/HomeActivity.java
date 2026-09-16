package com.pafez.flashnote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements CardAdapter.OnCardLongClickListener {

    private CardAdapter cardAdapter;
    private FlashNoteDatabase database;
    private TextView emptyStateText;
    private TextView dueCountText;
    private TextView deckScoreText;
    
    private int deckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        deckId = getIntent().getIntExtra("deck_id", -1);
        String deckName = getIntent().getStringExtra("deck_name");

        TextView homeTitle = findViewById(R.id.homeTitle);
        if (deckName != null) {
            homeTitle.setText(deckName);
        }

        RecyclerView cardsRecyclerView = findViewById(R.id.cardsRecyclerView);
        ExtendedFloatingActionButton addCardButton = findViewById(R.id.addCardButton);
        MaterialButton studyDeckButton = findViewById(R.id.studyDeckButton);
        MaterialButton cramDeckButton = findViewById(R.id.cramDeckButton);

        emptyStateText = findViewById(R.id.emptyStateText);
        dueCountText = findViewById(R.id.dueCountText);
        deckScoreText = findViewById(R.id.deckScoreText);

        database = FlashNoteDatabase.getInstance(getApplicationContext());

        cardAdapter = new CardAdapter(new ArrayList<>(), this);
        cardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardsRecyclerView.setAdapter(cardAdapter);

        studyDeckButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StudyActivity.class);
            intent.putExtra("deck_id", deckId);
            intent.putExtra("is_cram_mode", false);
            startActivity(intent);
        });

        cramDeckButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StudyActivity.class);
            intent.putExtra("deck_id", deckId);
            intent.putExtra("is_cram_mode", true);
            startActivity(intent);
        });

        addCardButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ImportActivity.class);
            // Pass deckId forward so the builder knows where to save
            intent.putExtra("deck_id", deckId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCards();
    }

    private void loadCards() {
        new Thread(() -> {
            List<Card> cards;
            int dueCount;
            long now = System.currentTimeMillis();
            if (deckId != -1) {
                cards = database.cardDao().getCardsForDeck(deckId);
                dueCount = database.cardDao().getDueCardCountForDeck(deckId, now);
            } else {
                cards = database.cardDao().getAllCards();
                dueCount = cards.size();
            }

            int totalCardsCount = cards.size();
            int masteredCardsCount = 0;
            for (Card c : cards) {
                if (c.interval >= 21) {
                    masteredCardsCount++;
                }
            }
            int scorePercentage = totalCardsCount > 0 ? (masteredCardsCount * 100) / totalCardsCount : 0;

            runOnUiThread(() -> {
                cardAdapter.updateCards(cards);
                emptyStateText.setVisibility(cards.isEmpty() ? View.VISIBLE : View.GONE);
                dueCountText.setText(getString(R.string.due_count_format, dueCount));
                deckScoreText.setText(getString(R.string.deck_score_format, scorePercentage));
            });
        }).start();
    }

    @Override
    public void onCardLongClick(Card card) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Card")
                .setMessage("Are you sure you want to delete this card?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteCard(card))
                .show();
    }

    private void deleteCard(Card card) {
        new Thread(() -> {
            database.cardDao().delete(card);
            runOnUiThread(this::loadCards);
        }).start();
    }
}
