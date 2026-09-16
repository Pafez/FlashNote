package com.pafez.flashnote;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class StudyActivity extends AppCompatActivity {

    private FlashNoteDatabase database;
    private GeminiClient geminiClient;
    private GeminiSettings geminiSettings;

    private int deckId;
    private boolean isCramMode = false;
    private List<Card> dueCards = new ArrayList<>();
    private int currentIndex = 0;

    private int cardsReviewedThisSession = 0;
    private int correctAnswers = 0;
    private int incorrectAnswers = 0;

    private LinearLayout studyHeaderLayout;
    private View studyContentScrollView;
    private View actionButtonLayout;
    private LinearLayout emptyStateLayout;
    private LinearLayout feedbackLayout;

    private TextView progressTextView;
    private TextView frontTextView;
    private EditText answerEditText;
    private TextView scoreBadgeTextView;
    private TextView hintTextView;
    private TextView backTextView;

    private SessionPieChartView sessionPieChartView;
    private TextView correctCountTextView;
    private TextView needsReviewCountTextView;

    private ProgressBar loadingProgressBar;
    private Button submitButton;
    private Button nextCardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        deckId = getIntent().getIntExtra("deck_id", getIntent().getIntExtra("deckId", -1));
        isCramMode = getIntent().getBooleanExtra("is_cram_mode", false);

        database = FlashNoteDatabase.getInstance(getApplicationContext());
        geminiClient = new GeminiClient();
        geminiSettings = new GeminiSettings(this);

        studyHeaderLayout = findViewById(R.id.studyHeaderLayout);
        studyContentScrollView = findViewById(R.id.studyContentScrollView);
        actionButtonLayout = findViewById(R.id.actionButtonLayout);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        feedbackLayout = findViewById(R.id.feedbackLayout);

        progressTextView = findViewById(R.id.progressTextView);
        frontTextView = findViewById(R.id.frontTextView);
        answerEditText = findViewById(R.id.answerEditText);
        scoreBadgeTextView = findViewById(R.id.scoreBadgeTextView);
        hintTextView = findViewById(R.id.hintTextView);
        backTextView = findViewById(R.id.backTextView);

        sessionPieChartView = findViewById(R.id.sessionPieChartView);
        correctCountTextView = findViewById(R.id.correctCountTextView);
        needsReviewCountTextView = findViewById(R.id.needsReviewCountTextView);

        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        submitButton = findViewById(R.id.submitButton);
        nextCardButton = findViewById(R.id.nextCardButton);
        Button finishEmptyButton = findViewById(R.id.finishEmptyButton);

        submitButton.setOnClickListener(v -> submitAnswer());
        nextCardButton.setOnClickListener(v -> moveToNextCard());
        finishEmptyButton.setOnClickListener(v -> finish());

        loadDueCards();
    }

    private void loadDueCards() {
        new Thread(() -> {
            long currentTimestamp = System.currentTimeMillis();
            if (isCramMode) {
                if (deckId != -1) {
                    dueCards = database.cardDao().getCardsForDeck(deckId);
                } else {
                    dueCards = database.cardDao().getAllCards();
                }
            } else {
                if (deckId != -1) {
                    dueCards = database.cardDao().getDueCardsForDeck(deckId, currentTimestamp);
                } else {
                    dueCards = database.cardDao().getAllCards();
                }
            }
            runOnUiThread(this::displayCurrentCard);
        }).start();
    }

    private void displayCurrentCard() {
        if (dueCards == null || dueCards.isEmpty() || currentIndex >= dueCards.size()) {
            showEmptyOrCompletedState();
            return;
        }

        emptyStateLayout.setVisibility(View.GONE);
        studyHeaderLayout.setVisibility(View.VISIBLE);
        studyContentScrollView.setVisibility(View.VISIBLE);
        actionButtonLayout.setVisibility(View.VISIBLE);

        Card currentCard = dueCards.get(currentIndex);

        progressTextView.setText(getString(R.string.card_progress_format, currentIndex + 1, dueCards.size()));
        frontTextView.setText(currentCard.front);
        answerEditText.setText("");
        answerEditText.setEnabled(true);

        feedbackLayout.setVisibility(View.GONE);
        submitButton.setVisibility(View.VISIBLE);
        submitButton.setEnabled(true);
        nextCardButton.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
    }

    private void submitAnswer() {
        String userAnswer = answerEditText.getText().toString().trim();
        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "Please type your answer", Toast.LENGTH_SHORT).show();
            return;
        }

        Card card = dueCards.get(currentIndex);
        submitButton.setEnabled(false);
        answerEditText.setEnabled(false);
        loadingProgressBar.setVisibility(View.VISIBLE);

        String apiKey = geminiSettings.getApiKey();
        if (geminiSettings.hasApiKey()) {
            geminiClient.gradeAnswer(
                    apiKey,
                    card.front,
                    card.back,
                    userAnswer,
                    new GeminiClient.Callback() {
                        @Override
                        public void onSuccess(int score, String hint) {
                            runOnUiThread(() -> processGradedResult(card, score, hint));
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                loadingProgressBar.setVisibility(View.GONE);
                                Toast.makeText(StudyActivity.this, "AI grading error: " + error, Toast.LENGTH_SHORT).show();
                                // Fallback comparison when API fails
                                int score = userAnswer.equalsIgnoreCase(card.back.trim()) ? 4 : 2;
                                String hint = score == 4 ? "Exact match with back of card." : "Review answer against back of card.";
                                processGradedResult(card, score, hint);
                            });
                        }
                    }
            );
        } else {
            // Local fallback when no API key is configured
            int score = userAnswer.equalsIgnoreCase(card.back.trim()) ? 4 : 2;
            String hint = score == 4 ? "Exact match!" : "Compare your response to the correct answer.";
            processGradedResult(card, score, hint);
        }
    }

    private void processGradedResult(Card card, int score, String hint) {
        cardsReviewedThisSession++;
        if (score >= 3) {
            correctAnswers++;
        } else {
            incorrectAnswers++;
        }

        if (!isCramMode) {
            // Update SM-2 parameters
            SpacedRepetitionEngine.updateCard(card, score);

            // Update database
            new Thread(() -> database.cardDao().update(card)).start();
        }

        // Update UI
        loadingProgressBar.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        nextCardButton.setVisibility(View.VISIBLE);

        scoreBadgeTextView.setText(getString(R.string.score_format, score));
        hintTextView.setText(hint != null && !hint.isEmpty() ? hint : "No hint provided.");
        backTextView.setText(card.back);
        feedbackLayout.setVisibility(View.VISIBLE);
    }

    private void moveToNextCard() {
        currentIndex++;
        displayCurrentCard();
    }

    private void showEmptyOrCompletedState() {
        studyHeaderLayout.setVisibility(View.GONE);
        studyContentScrollView.setVisibility(View.GONE);
        actionButtonLayout.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);

        TextView emptyStateTitle = findViewById(R.id.emptyStateTitle);
        TextView emptyStateSubtitle = findViewById(R.id.emptyStateSubtitle);
        if (emptyStateTitle != null) {
            emptyStateTitle.setText(R.string.all_caught_up_title);
        }
        if (emptyStateSubtitle != null) {
            emptyStateSubtitle.setText(R.string.all_caught_up_subtitle);
        }

        if (sessionPieChartView != null) {
            sessionPieChartView.setStats(correctAnswers, incorrectAnswers);
        }

        if (correctCountTextView != null) {
            correctCountTextView.setText(getString(R.string.correct_stat_format, correctAnswers));
        }
        if (needsReviewCountTextView != null) {
            needsReviewCountTextView.setText(getString(R.string.needs_review_stat_format, incorrectAnswers));
        }

        if (cardsReviewedThisSession > 0) {
            long currentTimestamp = System.currentTimeMillis();
            SessionRecord record = new SessionRecord(deckId, currentTimestamp, cardsReviewedThisSession, correctAnswers);
            new Thread(() -> database.sessionDao().insertSession(record)).start();
        }
    }
}
