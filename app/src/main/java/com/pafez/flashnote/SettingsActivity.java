package com.pafez.flashnote;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText apiKeyEditText;
    private TextView statusText;

    private GeminiSettings settings;
    private GeminiClient geminiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        apiKeyEditText = findViewById(R.id.apiKeyEditText);
        statusText = findViewById(R.id.statusText);

        Button saveButton = findViewById(R.id.saveApiKeyButton);
        Button deleteButton = findViewById(R.id.deleteApiKeyButton);
        Button testButton = findViewById(R.id.testConnectionButton);

        settings = new GeminiSettings(getApplicationContext());
        geminiClient = new GeminiClient();

        updateStatus();

        saveButton.setOnClickListener(v -> saveApiKey());
        deleteButton.setOnClickListener(v -> deleteApiKey());
        testButton.setOnClickListener(v -> testConnection());
    }

    private void saveApiKey() {
        String apiKey = apiKeyEditText.getText().toString().trim();

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Please enter an API key", Toast.LENGTH_SHORT).show();
            return;
        }

        settings.saveApiKey(apiKey);
        updateStatus();

        Toast.makeText(this, "API key saved", Toast.LENGTH_SHORT).show();
    }

    private void deleteApiKey() {
        settings.clearApiKey();
        apiKeyEditText.setText("");
        updateStatus();

        Toast.makeText(this, "API key deleted", Toast.LENGTH_SHORT).show();
    }

    private void testConnection() {
        String apiKey = settings.getApiKey();

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Save an API key first", Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText("Testing connection...");

        geminiClient.generateCard(
                apiKey,
                "Respond with exactly: FlashNote connection successful",
                new GeminiClient.Callback() {
                    @Override
                    public void onSuccess(String question, String answer) {
                        runOnUiThread(() -> {
                            Toast.makeText(
                                    SettingsActivity.this,
                                    "Gemini connection successful!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }

                    @Override
                    public void onError(String error) {

                        Log.e("GeminiTest", "Gemini request failed:");
                        Log.e("GeminiTest", error);

                        runOnUiThread(() -> {
                            Toast.makeText(
                                    SettingsActivity.this,
                                    "Gemini test failed. Check Logcat.",
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private void updateStatus() {
        if (settings.hasApiKey()) {
            statusText.setText("API key saved");
        } else {
            statusText.setText("No API key saved");
        }
    }
}