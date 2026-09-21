# FlashNote

[![GitHub Repository](https://img.shields.io/badge/GitHub-Pafez%2FFlashNote-181717?logo=github)](https://github.com/Pafez/FlashNote)

[![Watch the Presentation video](https://youtube.com)](https://youtu.be/PCFgivFdfRI)

FlashNote is a native Android flashcard application that turns photographed notes into editable study material and combines **OCR, AI-assisted flashcard generation, active recall, AI answer grading, and SM-2 spaced repetition** into one study workflow.

The core idea is:

> **Notes → OCR → Flashcards → Active Recall → Spaced Repetition**

Instead of manually converting physical notes into flashcards, FlashNote can extract text from a note image, let the user edit/select the relevant material, and use Gemini to generate an active-recall question and answer. Cards can then be studied through a dedicated review system that tracks performance and schedules future reviews.

---

## Features

### 📷 Note Import & OCR

- Import notes through the device's image workflow.
- Extract text using **Google ML Kit Text Recognition**.
- Review and edit extracted text before using it to build cards.
- Use selected/source text as input for AI-assisted flashcard generation.
- OCR does not depend on Gemini.

### 🗂️ Deck & Flashcard Management

FlashNote organizes cards into user-created decks.

Cards contain:

- Question/front
- Answer/back
- Deck association
- Position
- Creation timestamp
- Spaced-repetition state

Decks and cards are persisted locally using **Room**.

Deleting a deck also cascades deletion to its associated cards through the Room foreign-key relationship.

### ✨ AI Flashcard Generation

FlashNote can generate a flashcard from supplied source text using the **Google Gemini REST API**.

The generation prompt instructs Gemini to:

- Create exactly one active-recall flashcard.
- Test an important fact or concept.
- Keep the question concise.
- Provide a concise but sufficient answer.
- Use only information contained in the supplied source text.
- Avoid introducing outside information.

The response is constrained using a JSON schema containing:

```json
{
  "question": "string",
  "answer": "string"
}
```

The generated card can then be handled through the normal card-building workflow.

### 🧠 Active Recall Study Mode

`StudyActivity` provides the main review workflow.

For each card:

1. The question/front is displayed.
2. The user types an answer.
3. The answer is submitted.
4. Gemini evaluates the response when an API key is available.
5. The user receives a score and hint.
6. The correct answer is revealed.
7. The card's spaced-repetition state is updated unless Cram Mode is active.
8. The user moves to the next card.

The study session also tracks correct and incorrect responses.

### 🤖 AI Answer Grading

Gemini grades submitted answers on a **1–4 scale**:

| Score | Meaning |
|---:|---|
| **1** | Blackout / completely incorrect or missing |
| **2** | Incorrect / partially correct with significant omissions or major mistakes |
| **3** | Correct with hesitation / mostly correct with minor mistakes |
| **4** | Perfect response / fully accurate and complete |

The model also returns a short constructive hint explaining what was missing or how the answer could be improved.

The response is constrained by a JSON schema:

```json
{
  "score": 1,
  "hint": "string"
}
```

### 🔄 Local Grading Fallback

AI is not required for every study session.

If no Gemini API key is configured, FlashNote performs a local comparison between the user's answer and the card's stored answer.

The current fallback behavior is:

- Case-insensitive exact match → **4**
- Otherwise → **2**

If a Gemini request fails during grading, the same local fallback is used so that a study session can continue.

### 🧠 SM-2 Spaced Repetition

FlashNote implements an SM-2-based scheduling engine in `SpacedRepetitionEngine`.

Each card stores:

- `nextReviewDate`
- `interval`
- `easeFactor`
- `repetitionCount`

The application uses the study grade to update these values.

#### Grade mapping

FlashNote's 1–4 grading scale is mapped to SM-2 quality values:

| FlashNote score | SM-2 quality | Interpretation |
|---:|---:|---|
| 1 | 2 | Fail / Blackout |
| 2 | 3 | Incorrect / Hard |
| 3 | 4 | Good / Correct |
| 4 | 5 | Perfect / Easy |

The implementation uses:

- Default ease factor: **2.5**
- Minimum ease factor: **1.3**

For a failed recall (`1` or `2`):

```text
repetitionCount = 0
interval = 1 day
```

For successful recall (`3` or `4`):

```text
1st successful repetition → 1 day
2nd successful repetition → 6 days
Later repetitions         → previous interval × new ease factor
```

The resulting interval is used to calculate the card's next review timestamp.

Scores are clamped to the valid `1–4` range.

### ⚡ Cram Mode

Cram Mode allows the user to study an entire deck without modifying the cards' SM-2 scheduling data.

In normal study mode, FlashNote loads only cards whose `nextReviewDate` is due.

In Cram Mode, FlashNote loads all cards in the selected deck and does **not** update their spaced-repetition fields after grading.

This makes Cram Mode suitable for immediate revision without changing future review scheduling.

### 📊 Study Progress Tracking

FlashNote records completed study sessions using `SessionRecord`.

Each record contains:

- Deck ID
- Session timestamp
- Number of cards reviewed
- Number of correct answers

The application can retrieve study sessions for individual decks.

At the end of a study session, FlashNote displays:

- Correct answer count
- Needs-review/incorrect count
- Correct/incorrect ratio visualization

A custom `SessionPieChartView` renders the session statistics using Android's Canvas system.

### 📈 Due Cards & Mastery

The Room data layer provides queries for:

- All cards in a deck
- Due cards in a deck
- Total cards in a deck
- Due-card counts for a deck
- Total due cards across the application

The Home screen uses these values to provide dynamic study information and deck progress.

Cards can also be presented using mastery states such as:

- **New**
- **Learning**
- **Mastered**

### 🔔 Daily Study Reminders

FlashNote uses **AndroidX WorkManager** through `StudyReminderWorker`.

The worker:

1. Checks the current timestamp.
2. Queries the database for all due cards.
3. Sends a local notification if at least one card is due.

The notification reports the number of flashcards ready for review and opens FlashNote's `MainActivity`.

Android 13+ notification permission is declared and handled by the application.

---

# Technology Stack

| Category | Technology |
|---|---|
| Platform | Android |
| Language | Java |
| IDE | Android Studio |
| Build system | Gradle |
| Gradle configuration | Kotlin DSL |
| Android Gradle Plugin | 9.3.1 |
| Minimum SDK | API 24 |
| Compile SDK | API 37 |
| Target SDK | API 37 |
| Java compatibility | Java 11 |
| UI | Android XML Views |
| UI components | AndroidX + Material Components |
| OCR | Google ML Kit Text Recognition 16.0.1 |
| Local database | AndroidX Room 2.8.4 |
| Database | SQLite through Room |
| HTTP client | OkHttp 4.12.0 |
| AI backend | Google Gemini API |
| AI communication | REST API |
| AI model | `gemini-3.5-flash-lite` |
| Background work | AndroidX WorkManager 2.9.0 |
| Notifications | Android notification APIs |
| Charts | Custom Android Canvas View |
| API-key persistence | Android SharedPreferences |
| Testing | JUnit 4.13.2 + AndroidX Test + Espresso |
| Version control | Git / GitHub |

### Declared Dependencies

The current `app/build.gradle.kts` declares:

```text
androidx.activity:activity-ktx       1.8.0
androidx.appcompat:appcompat         1.6.1
androidx.constraintlayout:constraintlayout
                                      2.1.4
com.google.android.material:material 1.10.0

com.google.mlkit:text-recognition    16.0.1

androidx.room:room-runtime            2.8.4
androidx.room:room-compiler            2.8.4

com.squareup.okhttp3:okhttp            4.12.0

androidx.work:work-runtime             2.9.0

junit:junit                             4.13.2
androidx.test.ext:junit                1.1.5
androidx.test.espresso:espresso-core   3.5.1
```

Room's compiler is used through Java `annotationProcessor`.

---

# Architecture

FlashNote is organized around Android Activities, Room entities/DAOs, adapters, an AI client, and background work.

```text
                         FlashNote
                            │
        ┌───────────────────┼────────────────────┐
        │                   │                    │
        ▼                   ▼                    ▼
     Android UI          Data Layer           AI Layer
        │                   │                    │
        │                   │                    ├── GeminiClient
        │                   │                    │     ├── Card generation
        │                   │                    │     └── Answer grading
        │                   │                    │
        │                   ├── FlashNoteDatabase └── GeminiSettings
        │                   │
        │                   ├── CardDao
        │                   ├── DeckDao
        │                   └── SessionDao
        │
        ├── MainActivity
        ├── HomeActivity
        ├── ImportActivity
        ├── CardBuilderActivity
        ├── CardViewActivity
        ├── StudyActivity
        └── SettingsActivity
                            │
                            ▼
                   SpacedRepetitionEngine
                            │
                            ▼
                       SM-2 state

                    Background Services
                            │
                            ▼
                   StudyReminderWorker
                            │
                            ▼
                    Local notification
```

The application does not use a separate FlashNote server. Gemini is accessed directly from the Android application through its REST API.

---

# Project Structure

```text
app/
├── src/
│   ├── androidTest/
│   │   └── java/
│   │       └── com/
│   │           └── pafez/
│   │               └── flashnote/
│   │                   └── ExampleInstrumentedTest.java
│   │
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   │
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── pafez/
│   │   │           └── flashnote/
│   │   │               ├── Card.java
│   │   │               ├── CardAdapter.java
│   │   │               ├── CardBuilderActivity.java
│   │   │               ├── CardDao.java
│   │   │               ├── CardViewActivity.java
│   │   │               ├── Deck.java
│   │   │               ├── DeckAdapter.java
│   │   │               ├── DeckDao.java
│   │   │               ├── FlashNoteDatabase.java
│   │   │               ├── GeminiClient.java
│   │   │               ├── GeminiSettings.java
│   │   │               ├── HomeActivity.java
│   │   │               ├── ImportActivity.java
│   │   │               ├── MainActivity.java
│   │   │               ├── SessionDao.java
│   │   │               ├── SessionPieChartView.java
│   │   │               ├── SessionRecord.java
│   │   │               ├── SettingsActivity.java
│   │   │               ├── SpacedRepetitionEngine.java
│   │   │               ├── StudyActivity.java
│   │   │               └── StudyReminderWorker.java
│   │   │
│   │   ├── keepRules/
│   │   │   └── rules.keep
│   │   │
│   │   └── res/
│   │       ├── drawable/
│   │       ├── layout/
│   │       ├── mipmap-anydpi-v26/
│   │       ├── mipmap-hdpi/
│   │       ├── mipmap-mdpi/
│   │       ├── mipmap-xhdpi/
│   │       ├── mipmap-xxhdpi/
│   │       ├── mipmap-xxxhdpi/
│   │       ├── values/
│   │       ├── values-night/
│   │       └── xml/
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── pafez/
│                   └── flashnote/
│                       ├── ExampleUnitTest.java
│                       └── SpacedRepetitionEngineTest.java
```

---

# Core Components

## `MainActivity`

Application entry point and launcher activity.

It is declared as the exported launcher activity in the manifest and is also the destination used by study reminder notifications.

## `HomeActivity`

Displays the selected deck and its cards.

It integrates deck-level study information such as due-card counts and mastery-related information and provides access to study workflows including Cram Mode.

## `ImportActivity`

Handles the note-import workflow and ML Kit OCR processing.

It provides the source material used by the flashcard-building workflow.

## `CardBuilderActivity`

Handles creation of flashcards from source material.

It integrates Gemini-assisted generation while allowing the resulting question and answer to be edited before being stored.

## `CardViewActivity`

Displays an individual flashcard.

## `StudyActivity`

Implements the active-recall study loop.

It handles:

- Due-card loading.
- Cram Mode.
- User answer input.
- Gemini grading.
- Local fallback grading.
- SM-2 updates.
- Session statistics.
- End-of-session visualization.
- Session history persistence.

## `SettingsActivity`

Provides access to Gemini configuration.

## `GeminiClient`

Contains the REST API implementation for Gemini.

It currently supports:

```text
generateCard()
gradeAnswer()
```

Both operations execute asynchronously on a background thread.

## `GeminiSettings`

Stores the user's Gemini API key in Android `SharedPreferences`.

The current implementation supports:

```text
saveApiKey()
getApiKey()
clearApiKey()
hasApiKey()
```

The API key is **not stored in the Room database**.

## `SpacedRepetitionEngine`

Contains the application's SM-2 scheduling calculations.

It operates directly on the `Card` object's spaced-repetition fields.

## `StudyReminderWorker`

Uses WorkManager to check for due cards and issue a local notification when cards are ready for review.

## `SessionPieChartView`

A custom Android `View` that draws study-session correct/incorrect statistics using `Canvas`.

---

# Data Model

## `Deck`

Represents a collection of flashcards.

Cards reference their parent deck through `deckId`.

## `Card`

The current Room entity contains:

```text
id
front
back
deckId
position
createdAt

nextReviewDate
interval
easeFactor
repetitionCount
```

The card's foreign key references `Deck.id` and uses cascade deletion.

New cards start with:

```text
nextReviewDate = 0
interval        = 0
easeFactor      = 2.5
repetitionCount = 0
```

## `SessionRecord`

Study sessions are stored as:

```text
id
deckId
timestamp
cardsReviewed
correctCount
```

Sessions are retrieved per deck in reverse chronological order.

---

# Database

FlashNote uses **Room** over SQLite.

The current database version is:

```text
Version 6
```

Registered entities:

```text
Card
Deck
SessionRecord
```

Database name:

```text
flashnote_database
```

## Migrations

The project includes explicit migrations for the new spaced-repetition and study-history functionality.

### `MIGRATION_4_5`

Adds the following fields to `cards`:

```text
nextReviewDate INTEGER NOT NULL DEFAULT 0
interval       INTEGER NOT NULL DEFAULT 0
easeFactor     REAL    NOT NULL DEFAULT 2.5
repetitionCount INTEGER NOT NULL DEFAULT 0
```

### `MIGRATION_5_6`

Creates:

```text
session_records
```

with:

```text
id
deckId
timestamp
cardsReviewed
correctCount
```

The database builder registers both migrations.

The current implementation also calls `fallbackToDestructiveMigration()` for migration paths that are not explicitly supplied.

---

# Gemini Integration

FlashNote communicates with Gemini directly through the Google Generative Language REST API.

The current model is:

```text
gemini-3.5-flash-lite
```

The REST request uses:

```text
POST
https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
```

and authenticates using the user's API key in the:

```text
x-goog-api-key
```

request header.

## Flashcard Generation

The generation request sends source text to Gemini and asks for one active-recall card.

The response uses structured JSON output with:

```text
question
answer
```

This prevents the application from having to parse arbitrary natural-language responses.

## Answer Grading

The grading request sends:

```text
Card question
Correct answer
User answer
```

Gemini returns:

```text
score
hint
```

using a strict JSON schema.

The score is then passed to `SpacedRepetitionEngine`.

---

# API Key Storage

The Gemini API key is stored using Android `SharedPreferences`:

```text
Preference file: gemini_settings
Key: api_key
```

The application can:

- Save the key.
- Retrieve the key.
- Check whether a key exists.
- Delete the key.

This configuration is local to the Android application and is separate from the Room database.

> **Security consideration:** the current implementation is intentionally simple local persistence. Storing an API key in `SharedPreferences` should not be described as equivalent to secure hardware-backed secret storage.

---

# Error Handling

Gemini requests report unsuccessful HTTP responses to the application's callback.

The HTTP status and full response body are logged through Android Logcat during development.

The application has also been configured to handle Gemini service availability problems at the UI level. For example, a Gemini `503` response can be presented to the user as:

```text
Gemini currently in High demand
```

For answer grading, an API failure does not necessarily terminate the study flow. `StudyActivity` falls back to its local answer comparison.

---

# Study Workflow

Normal study mode:

```text
Open Study
     │
     ▼
Load cards where nextReviewDate <= now
     │
     ▼
Display question
     │
     ▼
User enters answer
     │
     ▼
Gemini grading
     │
     ├── Success ────────────────┐
     │                           │
     └── Failure → local fallback
                                 │
                                 ▼
                         Score 1–4
                                 │
                                 ▼
                       SM-2 calculation
                                 │
                                 ▼
                       Update Card in Room
                                 │
                                 ▼
                       Display feedback
                                 │
                                 ▼
                         Next card
                                 │
                                 ▼
                         Session complete
                                 │
                                 ▼
                       Save SessionRecord
```

Cram Mode follows the same study interaction but skips the SM-2 database update.

---

# Reminder Workflow

```text
WorkManager
     │
     ▼
StudyReminderWorker
     │
     ▼
Query cards where nextReviewDate <= now
     │
     ├── 0 due → no notification
     │
     └── >0 due
          │
          ▼
   Local notification
          │
          ▼
     MainActivity
```

The notification text includes the number of cards ready for review.

---

# Permissions

The manifest declares:

```text
android.permission.INTERNET
android.permission.POST_NOTIFICATIONS
```

`POST_NOTIFICATIONS` is relevant to Android 13+.

The application also registers an AndroidX `FileProvider` for controlled file/URI sharing.

Activities are configured with explicit exported states, with `MainActivity` serving as the exported launcher activity.

---

# Testing

The project currently contains:

### Unit tests

```text
ExampleUnitTest.java
SpacedRepetitionEngineTest.java
```

The presence of `SpacedRepetitionEngineTest` provides a dedicated test target for the scheduling logic.

### Instrumentation tests

```text
ExampleInstrumentedTest.java
```

Android instrumentation tests are configured through the Android test source set.

---

# Development Setup

## Requirements

- Android Studio
- Android SDK
- Java 11
- Android SDK API 37
- Android device or emulator running API 24+

The project uses Gradle Kotlin DSL.

## Clone

```bash
git clone https://github.com/Pafez/FlashNote.git
cd FlashNote
```

Open the project in Android Studio and allow Gradle to synchronize.

## Run

Select the `app` run configuration and launch it on an Android device or emulator.

## Gemini Setup

AI features require a Gemini API key.

The key can be configured from FlashNote's Settings screen.

The core local features do not require Gemini:

- Deck management
- Card management
- OCR
- Local storage
- Cram Mode
- Spaced-repetition calculations
- Study history
- Progress tracking
- Local reminders

Gemini is required for the cloud-based AI features:

- AI flashcard generation
- AI answer grading

---

# Local vs. Cloud Processing

| Feature | Local | Gemini / Internet |
|---|:---:|:---:|
| Image import | ✓ | |
| OCR | ✓ | |
| Text editing | ✓ | |
| Deck storage | ✓ | |
| Card storage | ✓ | |
| Flashcard management | ✓ | |
| SM-2 scheduling | ✓ | |
| Cram Mode | ✓ | |
| Study history | ✓ | |
| Progress statistics | ✓ | |
| Study reminders | ✓ | |
| AI card generation | | ✓ |
| AI answer grading | | ✓ |
| Local grading fallback | ✓ | |

This makes Gemini an optional enhancement to the local study system rather than a requirement for the application's core database and scheduling functionality.

---

# Project History

FlashNote began as an OCR-focused Android flashcard application:

```text
Photograph notes
      ↓
Extract text with ML Kit
      ↓
Create editable flashcards
      ↓
Store locally
```

The project was subsequently expanded with Gemini integration:

```text
Source text
      ↓
Gemini
      ↓
Generated question + answer
```

The latest study functionality extends the application into a complete active-recall and spaced-repetition workflow:

```text
Notes
  ↓
OCR
  ↓
Flashcards
  ↓
Active Recall
  ↓
AI / Local Grading
  ↓
SM-2 Scheduling
  ↓
Review Reminders
  ↓
Progress Tracking
```

---

# Current Feature Status

| Feature | Status |
|---|:---:|
| Note image import | ✅ |
| ML Kit OCR | ✅ |
| Editable extracted text | ✅ |
| Deck management | ✅ |
| Flashcard management | ✅ |
| Room persistence | ✅ |
| Gemini flashcard generation | ✅ |
| Structured Gemini responses | ✅ |
| AI answer grading | ✅ |
| Local grading fallback | ✅ |
| SM-2 spaced repetition | ✅ |
| Active-recall study mode | ✅ |
| Cram Mode | ✅ |
| Study session records | ✅ |
| Session correct/incorrect visualization | ✅ |
| Due-card tracking | ✅ |
| Deck progress/mastery information | ✅ |
| Daily study reminders | ✅ |
| Android 13+ notification permission | ✅ |
| Unit testing for SM-2 | ✅ |

---

# Repository

GitHub:

**https://github.com/Pafez/FlashNote/**

---

# License

FlashNote is under the **MIT License**.

Copyright (c) 2026 S. H. Pafez

---

# Credits / Technologies

FlashNote builds on the following technologies and libraries:

- Android
- Java
- AndroidX
- Material Components
- Google ML Kit
- AndroidX Room
- SQLite
- OkHttp
- Google Gemini API
- AndroidX WorkManager
- JUnit
- AndroidX Test
- Espresso
