package com.pafez.flashnote;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CardBuilderActivity extends AppCompatActivity {

    private EditText frontEditText;
    private EditText backEditText;
    
    private FlashNoteDatabase database;
    private int deckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_builder);

        database = FlashNoteDatabase.getInstance(getApplicationContext());
        deckId = getIntent().getIntExtra("deck_id", -1);

        EditText sourceEditText = findViewById(R.id.sourceEditText);
        frontEditText = findViewById(R.id.frontEditText);
        backEditText = findViewById(R.id.backEditText);
        Button doneButton = findViewById(R.id.doneButton);

        // Get OCR text from intent
        String extractedText = getIntent().getStringExtra("extracted_text");
        if (extractedText != null) {
            sourceEditText.setText(extractedText);
        }

        doneButton.setOnClickListener(v -> {
            saveCard();
        });
    }

    private void saveCard() {
        String front = frontEditText.getText().toString().trim();
        String back = backEditText.getText().toString().trim();

        if (front.isEmpty() || back.isEmpty()) {
            Toast.makeText(this, "Please fill both Front and Back", Toast.LENGTH_SHORT).show();
            return;
        }

        if (deckId == -1) {
            Toast.makeText(this, "Error: No deck selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            int currentCount = database.cardDao().getCardCountForDeck(deckId);
            Card card = new Card(front, back, deckId, currentCount, System.currentTimeMillis());
            
            database.cardDao().insert(card);
            
            runOnUiThread(() -> {
                Toast.makeText(CardBuilderActivity.this, "Card created!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
