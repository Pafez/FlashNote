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
        implements CardAdapter.OnCardLongClickListener, CardAdapter.OnCardClickListener {

    private CardAdapter cardAdapter;
    private FlashNoteDatabase database;
    private TextView emptyStateText;
    
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
        FloatingActionButton addCardButton = findViewById(R.id.addCardButton);
        emptyStateText = findViewById(R.id.emptyStateText);

        database = FlashNoteDatabase.getInstance(getApplicationContext());

        cardAdapter = new CardAdapter(new ArrayList<>(), this, this);
        cardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardsRecyclerView.setAdapter(cardAdapter);

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
            if (deckId != -1) {
                cards = database.cardDao().getCardsForDeck(deckId);
            } else {
                cards = database.cardDao().getAllCards();
            }

            runOnUiThread(() -> {
                cardAdapter.updateCards(cards);
                emptyStateText.setVisibility(cards.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    @Override
    public void onCardClick(Card card) {
        Intent intent = new Intent(this, CardViewActivity.class);
        intent.putExtra("card_id", card.id);
        intent.putExtra("card_front", card.front);
        intent.putExtra("card_back", card.back);
        startActivity(intent);
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
