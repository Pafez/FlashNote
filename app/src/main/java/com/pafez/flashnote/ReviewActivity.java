package com.pafez.flashnote;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReviewActivity extends AppCompatActivity {

    private EditText extractedTextEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_review);

        extractedTextEditText = findViewById(R.id.extractedTextEditText);

        Button saveButton = findViewById(R.id.saveTextButton);

        // Get OCR text from ImportActivity
        String extractedText =
                getIntent().getStringExtra("extracted_text");

        if (extractedText != null) {
            extractedTextEditText.setText(extractedText);
        }

        saveButton.setOnClickListener(v -> {

            String editedText =
                    extractedTextEditText.getText().toString().trim();

            if (editedText.isEmpty()) {

                Toast.makeText(
                        this,
                        "Text cannot be empty.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Note note = new Note(
                    editedText,
                    System.currentTimeMillis()
            );

            FlashNoteDatabase database =
                    FlashNoteDatabase.getInstance(getApplicationContext());

            new Thread(() -> {

                database.noteDao().insert(note);

                runOnUiThread(() -> {

                    Toast.makeText(
                            ReviewActivity.this,
                            "Note saved!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            }).start();
        });
    }
}