package com.pafez.flashnote;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import android.content.Intent;

import java.io.File;

public class ImportActivity extends AppCompatActivity {

    private ImageView cardImagePreview;

    private Uri cameraImageUri;
    private Uri selectedImageUri;

    // Gallery
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            cardImagePreview.setImageURI(uri);
                        }
                    }
            );

    // Full-resolution camera
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success && cameraImageUri != null) {
                            selectedImageUri = cameraImageUri;
                            cardImagePreview.setImageURI(cameraImageUri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import);

        cardImagePreview = findViewById(R.id.cardImagePreview);

        Button galleryButton = findViewById(R.id.galleryButton);
        Button takePhotoButton = findViewById(R.id.takePhotoButton);
        Button ocrButton = findViewById(R.id.ocrButton);
        Button customTextButton = findViewById(R.id.customTextButton);

        galleryButton.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        takePhotoButton.setOnClickListener(v -> openCamera());

        ocrButton.setOnClickListener(v -> extractText());

        customTextButton.setOnClickListener(v -> {
            Intent intent = new Intent(ImportActivity.this, CardBuilderActivity.class);
            intent.putExtra("deck_id", getIntent().getIntExtra("deck_id", -1));
            startActivity(intent);
        });
    }

    private void openCamera() {

        File imageFile = new File(
                getCacheDir(),
                "flashcard_camera_image.jpg"
        );

        cameraImageUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                imageFile
        );

        cameraLauncher.launch(cameraImageUri);
    }

    private void extractText() {

        if (selectedImageUri == null) {
            Toast.makeText(
                    this,
                    "Please select or take a photo first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            InputImage image = InputImage.fromFilePath(
                    this,
                    selectedImageUri
            );

            TextRecognizer recognizer =
                    TextRecognition.getClient(
                            TextRecognizerOptions.DEFAULT_OPTIONS
                    );

            recognizer.process(image)
                    .addOnSuccessListener(result -> {

                        String extractedText = result.getText();

                        if (extractedText.isEmpty()) {

                            Toast.makeText(
                                    this,
                                    "No text found.",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Intent intent = new Intent(
                                    ImportActivity.this,
                                    CardBuilderActivity.class
                            );

                            intent.putExtra(
                                    "extracted_text",
                                    extractedText
                            );
                            
                            // Pass deck_id received from HomeActivity
                            intent.putExtra(
                                    "deck_id",
                                    getIntent().getIntExtra("deck_id", -1)
                            );

                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                this,
                                "OCR failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    });

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Could not process image: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        }
    }
}