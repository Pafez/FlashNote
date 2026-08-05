# Create Card Builder Screen

Implement a new screen where users can create flashcards from OCR-extracted text.

## Proposed Changes

### Data Model
- Create `Card.java` entity with `front`, `back`, and `createdAt` fields.
- Create `CardDao.java` for Room operations.
- Update `FlashNoteDatabase.java` to include the `Card` entity.

### UI / Activities
- Create `activity_card_builder.xml` with:
    - An `EditText` for the source OCR text (pre-filled from Intent).
    - Two `EditText` fields for "Front" and "Back".
    - A "Done" button to save the card.
- Create `CardBuilderActivity.java` to handle the logic.
- Update `ReviewActivity.java` or `ImportActivity.java` to launch the Card Builder.
    - *Decision*: I'll add a "Build Card" button to `ReviewActivity` so users can either save as a plain Note or go to the Card Builder.

### Navigation
- Add `CardBuilderActivity` to `AndroidManifest.xml`.

## Verification Plan

### Manual Verification
- Deploy the app.
- Go through the OCR flow.
- From the review screen, navigate to the Card Builder.
- Copy/paste text from the OCR box to Front and Back boxes.
- Click Done and verify the card is saved (I'll need to check how to display cards on the home screen as well, or just verify the database for now).
