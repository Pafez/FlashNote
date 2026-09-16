package com.pafez.flashnote;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.util.Log;

public class GeminiClient {

    private static final String MODEL = "gemini-3.6-flash";

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL
                    + ":generateContent";

    private final OkHttpClient httpClient;

    public GeminiClient() {
        httpClient = new OkHttpClient();
    }

    public interface Callback {
        default void onSuccess(String question, String answer) {}
        default void onSuccess(int score, String hint) {
            onSuccess(String.valueOf(score), hint);
        }
        default void onError(String error) {}
    }

    public void generateCard(
            String apiKey,
            String sourceText,
            Callback callback
    ) {

        new Thread(() -> {

            try {

                /*
                 * Prompt
                 */
                String prompt =
                        "Create exactly one active-recall flashcard from the text below.\n\n"
                                + "Rules:\n"
                                + "- The question must test understanding of an important fact or concept.\n"
                                + "- The answer must directly answer the question.\n"
                                + "- Use only information contained in the provided text.\n"
                                + "- Do not introduce outside information.\n"
                                + "- Keep the question concise.\n"
                                + "- Keep the answer concise but sufficient.\n\n"
                                + "Source text:\n"
                                + sourceText;

                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);

                JSONArray parts = new JSONArray();
                parts.put(textPart);

                JSONObject content = new JSONObject();
                content.put("parts", parts);

                JSONArray contents = new JSONArray();
                contents.put(content);

                /*
                 * JSON schema for the model's response
                 */
                JSONObject properties = new JSONObject();

                JSONObject questionSchema = new JSONObject();
                questionSchema.put("type", "string");

                JSONObject answerSchema = new JSONObject();
                answerSchema.put("type", "string");

                properties.put("question", questionSchema);
                properties.put("answer", answerSchema);

                JSONArray required = new JSONArray();
                required.put("question");
                required.put("answer");

                JSONObject schema = new JSONObject();
                schema.put("type", "object");
                schema.put("properties", properties);
                schema.put("required", required);

                JSONObject generationConfig = new JSONObject();

                generationConfig.put(
                        "responseMimeType",
                        "application/json"
                );

                generationConfig.put(
                        "responseSchema",
                        schema
                );

                /*
                 * Complete request
                 */
                JSONObject requestJson = new JSONObject();
                requestJson.put("contents", contents);
                requestJson.put("generationConfig", generationConfig);

                RequestBody body = RequestBody.create(
                        requestJson.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("x-goog-api-key", apiKey)
                        .post(body)
                        .build();

                Log.d("GeminiClient", "Sending request to Gemini...");
                Log.d("GeminiClient", "Model: " + MODEL);

                try (Response response =
                             httpClient.newCall(request).execute()) {

                    String responseBody =
                            response.body() != null
                                    ? response.body().string()
                                    : "";

                    if (!response.isSuccessful()) {

                        Log.e(
                                "GeminiClient",
                                "HTTP " + response.code()
                        );

                        Log.e(
                                "GeminiClient",
                                "Response: " + responseBody
                        );

                        callback.onError(
                                "Gemini API error " + response.code()
                        );

                        return;
                    }

                    JSONObject json =
                            new JSONObject(responseBody);

                    JSONArray candidates =
                            json.getJSONArray("candidates");

                    JSONObject firstCandidate =
                            candidates.getJSONObject(0);

                    JSONObject responseContent =
                            firstCandidate.getJSONObject("content");

                    JSONArray responseParts =
                            responseContent.getJSONArray("parts");

                    String generatedText =
                            responseParts
                                    .getJSONObject(0)
                                    .getString("text");

                    /*
                     * Parse Gemini's JSON output
                     */
                    JSONObject cardJson =
                            new JSONObject(generatedText);

                    String question =
                            cardJson.getString("question").trim();

                    String answer =
                            cardJson.getString("answer").trim();

                    callback.onSuccess(question, answer);
                }

            } catch (Exception e) {

                callback.onError(
                        e.getClass().getSimpleName()
                                + ": "
                                + e.getMessage()
                );
            }

        }).start();
    }

    public void gradeAnswer(
            String apiKey,
            String cardFront,
            String cardBack,
            String userAnswer,
            Callback callback
    ) {

        new Thread(() -> {

            try {

                String prompt =
                        "You are an active-recall flashcard evaluator.\n\n"
                                + "Evaluate the user's answer against the correct answer for the flashcard below.\n\n"
                                + "Flashcard Question (Front):\n" + cardFront + "\n\n"
                                + "Correct Answer (Back):\n" + cardBack + "\n\n"
                                + "User's Submitted Answer:\n" + userAnswer + "\n\n"
                                + "Instructions:\n"
                                + "- Compare the user's answer to the correct answer.\n"
                                + "- Assign an integer score from 1 to 4:\n"
                                + "  1 = Blackout / Completely incorrect or missing.\n"
                                + "  2 = Incorrect / Partially correct with significant omissions or major mistakes.\n"
                                + "  3 = Correct with hesitation / Mostly correct with minor mistakes.\n"
                                + "  4 = Perfect response / Fully accurate and complete.\n"
                                + "- Provide a brief, constructive string hint for remediation explaining what was missing or how to improve.";

                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);

                JSONArray parts = new JSONArray();
                parts.put(textPart);

                JSONObject content = new JSONObject();
                content.put("parts", parts);

                JSONArray contents = new JSONArray();
                contents.put(content);

                /*
                 * Strict JSON schema for the response
                 */
                JSONObject properties = new JSONObject();

                JSONObject scoreSchema = new JSONObject();
                scoreSchema.put("type", "integer");
                scoreSchema.put("description", "Evaluation score from 1 to 4");

                JSONObject hintSchema = new JSONObject();
                hintSchema.put("type", "string");
                hintSchema.put("description", "Brief hint for remediation");

                properties.put("score", scoreSchema);
                properties.put("hint", hintSchema);

                JSONArray required = new JSONArray();
                required.put("score");
                required.put("hint");

                JSONObject schema = new JSONObject();
                schema.put("type", "object");
                schema.put("properties", properties);
                schema.put("required", required);

                JSONObject generationConfig = new JSONObject();
                generationConfig.put("responseMimeType", "application/json");
                generationConfig.put("responseSchema", schema);

                JSONObject requestJson = new JSONObject();
                requestJson.put("contents", contents);
                requestJson.put("generationConfig", generationConfig);

                RequestBody body = RequestBody.create(
                        requestJson.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("x-goog-api-key", apiKey)
                        .post(body)
                        .build();

                Log.d("GeminiClient", "Sending grade request to Gemini...");

                try (Response response = httpClient.newCall(request).execute()) {

                    String responseBody =
                            response.body() != null
                                    ? response.body().string()
                                    : "";

                    if (!response.isSuccessful()) {

                        Log.e("GeminiClient", "HTTP " + response.code());
                        Log.e("GeminiClient", "Response: " + responseBody);

                        callback.onError("Gemini API error " + response.code());
                        return;
                    }

                    JSONObject json = new JSONObject(responseBody);
                    JSONArray candidates = json.getJSONArray("candidates");
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    JSONObject responseContent = firstCandidate.getJSONObject("content");
                    JSONArray responseParts = responseContent.getJSONArray("parts");
                    String generatedText = responseParts.getJSONObject(0).getString("text");

                    JSONObject resultJson = new JSONObject(generatedText);
                    int score = resultJson.getInt("score");
                    String hint = resultJson.getString("hint").trim();

                    callback.onSuccess(score, hint);
                }

            } catch (Exception e) {

                callback.onError(
                        e.getClass().getSimpleName()
                                + ": "
                                + e.getMessage()
                );
            }

        }).start();
    }
}