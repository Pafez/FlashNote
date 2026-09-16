package com.pafez.flashnote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SpacedRepetitionEngineTest {

    @Test
    public void testFirstSuccessfulReviewScore3() {
        Card card = new Card("Question", "Answer", 1, 0, System.currentTimeMillis());
        long now = 1000000000L;

        Card updated = SpacedRepetitionEngine.updateCard(card, 3, now);

        assertNotNull(updated);
        assertEquals(1, updated.repetitionCount);
        assertEquals(1, updated.interval);
        assertEquals(2.5f, updated.easeFactor, 0.001f);
        assertEquals(now + TimeUnit.DAYS.toMillis(1), updated.nextReviewDate);
    }

    @Test
    public void testSecondSuccessfulReviewScore4() {
        Card card = new Card("Question", "Answer", 1, 0, System.currentTimeMillis());
        long now = 1000000000L;

        Card updated1 = SpacedRepetitionEngine.updateCard(card, 3, now);
        Card updated2 = SpacedRepetitionEngine.updateCard(updated1, 4, now);

        assertEquals(2, updated2.repetitionCount);
        assertEquals(6, updated2.interval);
        assertEquals(2.6f, updated2.easeFactor, 0.001f);
        assertEquals(now + TimeUnit.DAYS.toMillis(6), updated2.nextReviewDate);
    }

    @Test
    public void testThirdSuccessfulReviewScore3() {
        Card card = new Card("Question", "Answer", 1, 0, System.currentTimeMillis());
        long now = 1000000000L;

        SpacedRepetitionEngine.updateCard(card, 3, now); // rep 1, interval 1
        SpacedRepetitionEngine.updateCard(card, 3, now); // rep 2, interval 6, ease 2.5
        Card updated = SpacedRepetitionEngine.updateCard(card, 3, now); // rep 3, interval round(6 * 2.5) = 15

        assertEquals(3, updated.repetitionCount);
        assertEquals(15, updated.interval);
        assertEquals(2.5f, updated.easeFactor, 0.001f);
        assertEquals(now + TimeUnit.DAYS.toMillis(15), updated.nextReviewDate);
    }

    @Test
    public void testFailedReviewResetsRepetitionsAndInterval() {
        Card card = new Card("Question", "Answer", 1, 0, System.currentTimeMillis());
        long now = 1000000000L;

        SpacedRepetitionEngine.updateCard(card, 3, now);
        SpacedRepetitionEngine.updateCard(card, 3, now); // rep 2, interval 6
        Card updated = SpacedRepetitionEngine.updateCard(card, 1, now); // fail

        assertEquals(0, updated.repetitionCount);
        assertEquals(1, updated.interval);
        assertEquals(2.18f, updated.easeFactor, 0.01f); // ease drops by 0.32
        assertEquals(now + TimeUnit.DAYS.toMillis(1), updated.nextReviewDate);
    }

    @Test
    public void testEaseFactorMinimumBoundary() {
        Card card = new Card("Question", "Answer", 1, 0, System.currentTimeMillis());
        card.easeFactor = 1.3f;
        long now = 1000000000L;

        Card updated = SpacedRepetitionEngine.updateCard(card, 1, now); // fail reduces EF, but capped at 1.3

        assertEquals(1.3f, updated.easeFactor, 0.001f);
    }
}
