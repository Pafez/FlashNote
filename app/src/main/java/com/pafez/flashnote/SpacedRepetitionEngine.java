package com.pafez.flashnote;

import java.util.concurrent.TimeUnit;

public class SpacedRepetitionEngine {

    public static final float MIN_EASE_FACTOR = 1.3f;
    public static final float DEFAULT_EASE_FACTOR = 2.5f;

    /**
     * Updates a Card's spaced repetition fields (interval, easeFactor, repetitionCount, nextReviewDate)
     * using the standard SM-2 algorithm based on a grade score of 1 to 4.
     *
     * @param card  The Card to update.
     * @param score Grade score from 1 to 4 (1: Fail/Blackout, 2: Incorrect/Hard, 3: Good/Correct, 4: Perfect).
     * @return The updated Card object.
     */
    public static Card updateCard(Card card, int score) {
        return updateCard(card, score, System.currentTimeMillis());
    }

    /**
     * Updates a Card's spaced repetition fields using the standard SM-2 algorithm at a given timestamp.
     *
     * @param card             The Card to update.
     * @param score            Grade score from 1 to 4.
     * @param currentTimestamp Timestamp in milliseconds representing the current review time.
     * @return The updated Card object.
     */
    public static Card updateCard(Card card, int score, long currentTimestamp) {
        if (card == null) {
            return null;
        }

        // Clamp score between 1 and 4
        int clampedScore = Math.max(1, Math.min(4, score));

        // Map score (1 to 4) to SM-2 quality grade q (2 to 5)
        // score 1 -> q=2 (Fail, q < 3)
        // score 2 -> q=3 (Hard, q >= 3)
        // score 3 -> q=4 (Good, q >= 3)
        // score 4 -> q=5 (Easy, q >= 3)
        float q = clampedScore + 1;

        // Calculate new Ease Factor (EF)
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        float currentEase = card.easeFactor > 0 ? card.easeFactor : DEFAULT_EASE_FACTOR;
        float newEase = currentEase + (0.1f - (5.0f - q) * (0.08f + (5.0f - q) * 0.02f));
        if (newEase < MIN_EASE_FACTOR) {
            newEase = MIN_EASE_FACTOR;
        }

        int newRepetition;
        int newInterval;

        // Score 1 or 2 is considered failed recall (< 3 in 1-4 scale)
        if (clampedScore < 3) {
            newRepetition = 0;
            newInterval = 1;
        } else {
            // Successful recall
            newRepetition = card.repetitionCount + 1;
            if (newRepetition == 1) {
                newInterval = 1;
            } else if (newRepetition == 2) {
                newInterval = 6;
            } else {
                int prevInterval = card.interval > 0 ? card.interval : 1;
                newInterval = Math.round(prevInterval * newEase);
            }
        }

        long nextReviewDate = currentTimestamp + TimeUnit.DAYS.toMillis(newInterval);

        card.easeFactor = newEase;
        card.repetitionCount = newRepetition;
        card.interval = newInterval;
        card.nextReviewDate = nextReviewDate;

        return card;
    }
}
