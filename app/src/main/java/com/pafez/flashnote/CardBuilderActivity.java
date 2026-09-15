package com.pafez.flashnote;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CardBuilderActivity extends AppCompatActivity {

    private EditText sourceEditText;
    private EditText frontEditText;
    private EditText backEditText;
    
    private FlashNoteDatabase database;
    private int deckId;
    private GeminiClient geminiClient;
    private GeminiSettings geminiSettings;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_builder);

        database = FlashNoteDatabase.getInstance(getApplicationContext());
        deckId = getIntent().getIntExtra("deck_id", -1);

        geminiClient = new GeminiClient();
        geminiSettings = new GeminiSettings(getApplicationContext());

        sourceEditText = findViewById(R.id.sourceEditText);
        frontEditText = findViewById(R.id.frontEditText);
        backEditText = findViewById(R.id.backEditText);
        Button doneButton = findViewById(R.id.doneButton);
        Button generateAiButton = findViewById(R.id.generateAiButton);

        // Allow scrolling inside NestedScrollView
        sourceEditText.setOnTouchListener((v, event) -> {
            if (v.getId() == R.id.sourceEditText) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
            return false;
        });
        generateAiButton.setOnClickListener(v -> generateWithAi());

        // Get OCR text from intent
        String extractedText = getIntent().getStringExtra("extracted_text");
        if (extractedText != null) {
            sourceEditText.setText(extractedText);
        }

        doneButton.setOnClickListener(v -> saveCard());
    }

    private void generateWithAi() {

        String sourceText = sourceEditText.getText().toString().trim();

        if (sourceText.isEmpty()) {
            Toast.makeText(
                    this,
                    "Enter some source text first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String apiKey = geminiSettings.getApiKey();

        if (apiKey.isEmpty()) {
            Toast.makeText(
                    this,
                    "Set your Gemini API key in Settings first",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Toast.makeText(
                this,
                "Generating card...",
                Toast.LENGTH_SHORT
        ).show();

        geminiClient.generateCard(
                apiKey,
                sourceText,
                new GeminiClient.Callback() {

                    @Override
                    public void onSuccess(
                            String question,
                            String answer
                    ) {

                        runOnUiThread(() -> {

                            frontEditText.setText(question);
                            backEditText.setText(answer);

                            Toast.makeText(
                                    CardBuilderActivity.this,
                                    "Card generated!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                    @Override
                    public void onError(String error) {

                        runOnUiThread(() -> {

                            if (error.startsWith("Gemini API error 503")) {

                                Toast.makeText(
                                        CardBuilderActivity.this,
                                        "Gemini currently in high demand",
                                        Toast.LENGTH_LONG
                                ).show();

                            } else {

                                Toast.makeText(
                                        CardBuilderActivity.this,
                                        "Generation failed: " + error,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
                    }
                }
        );
    }

    private void saveCard() {
        String front = frontEditText.getText().toString().trim();
        String back = backEditText.getText().toString().trim();

        if (front.isEmpty() || back.isEmpty()) {
            Toast.makeText(this, "Please fill both Front and Back", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (deckId) {
            case -1:
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
