package com.pafez.flashnote;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class CardViewActivity extends AppCompatActivity {

    private TextView frontTextView;
    private TextView backTextView;
    private FlashNoteDatabase database;
    private int cardId;
    private Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);

        frontTextView = findViewById(R.id.frontTextView);
        backTextView = findViewById(R.id.backTextView);
        Button closeButton = findViewById(R.id.closeButton);
        Button editFrontButton = findViewById(R.id.editFrontButton);
        Button editBackButton = findViewById(R.id.editBackButton);

        database = FlashNoteDatabase.getInstance(getApplicationContext());
        cardId = getIntent().getIntExtra("card_id", -1);

        loadCard();

        closeButton.setOnClickListener(v -> finish());
        editFrontButton.setOnClickListener(v -> showEditDialog(true));
        editBackButton.setOnClickListener(v -> showEditDialog(false));
    }

    private void loadCard() {
        if (cardId == -1) return;

        new Thread(() -> {
            card = database.cardDao().getCardById(cardId);
            runOnUiThread(() -> {
                if (card != null) {
                    frontTextView.setText(card.front);
                    backTextView.setText(card.back);
                }
            });
        }).start();
    }

    private void showEditDialog(boolean isFront) {
        if (card == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isFront ? "Edit Front" : "Edit Back");

        final EditText input = new EditText(this);
        input.setText(isFront ? card.front : card.back);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
        
        // Add some padding/margin to the EditText
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.topMargin = margin / 2;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty()) {
                saveEdit(isFront, newText);
            } else {
                Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveEdit(boolean isFront, String newText) {
        new Thread(() -> {
            if (isFront) {
                card.front = newText;
            } else {
                card.back = newText;
            }
            database.cardDao().update(card);
            runOnUiThread(() -> {
                if (isFront) {
                    frontTextView.setText(newText);
                } else {
                    backTextView.setText(newText);
                }
                Toast.makeText(CardViewActivity.this, "Card updated", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
