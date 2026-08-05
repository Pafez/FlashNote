package com.pafez.flashnote;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CardViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);

        TextView frontTextView = findViewById(R.id.frontTextView);
        TextView backTextView = findViewById(R.id.backTextView);
        Button closeButton = findViewById(R.id.closeButton);

        String front = getIntent().getStringExtra("card_front");
        String back = getIntent().getStringExtra("card_back");

        if (front != null) frontTextView.setText(front);
        if (back != null) backTextView.setText(back);

        closeButton.setOnClickListener(v -> finish());
    }
}
