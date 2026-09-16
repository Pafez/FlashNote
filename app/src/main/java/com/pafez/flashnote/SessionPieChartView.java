package com.pafez.flashnote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SessionPieChartView extends View {

    private int correct = 0;
    private int incorrect = 0;

    private Paint arcPaint;
    private Paint textPaint;
    private RectF arcBounds;

    private int greenColor = Color.parseColor("#43A047");
    private int redColor = Color.parseColor("#E53935");
    private int neutralColor = Color.parseColor("#E0E0E0");

    public SessionPieChartView(Context context) {
        super(context);
        init();
    }

    public SessionPieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SessionPieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.parseColor("#3E2723"));
        textPaint.setFakeBoldText(true);

        arcBounds = new RectF();
    }

    public void setStats(int correct, int incorrect) {
        this.correct = Math.max(0, correct);
        this.incorrect = Math.max(0, incorrect);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        float strokeWidth = 16f * density;
        arcPaint.setStrokeWidth(strokeWidth);

        float cx = width / 2f;
        float cy = height / 2f;
        float radius = Math.min(width, height) / 2f - strokeWidth / 2f - (4f * density);

        arcBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);

        int total = correct + incorrect;

        if (total == 0) {
            // Draw placeholder ring
            arcPaint.setColor(neutralColor);
            canvas.drawArc(arcBounds, -90, 360, false, arcPaint);

            // Text
            textPaint.setTextSize(20f * density);
            float textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f);
            canvas.drawText("0%", cx, textY, textPaint);
        } else {
            float correctAngle = (correct * 360f) / total;
            float incorrectAngle = 360f - correctAngle;

            // Draw correct arc (green)
            if (correctAngle > 0) {
                arcPaint.setColor(greenColor);
                canvas.drawArc(arcBounds, -90, correctAngle, false, arcPaint);
            }

            // Draw incorrect arc (red)
            if (incorrectAngle > 0) {
                arcPaint.setColor(redColor);
                canvas.drawArc(arcBounds, -90 + correctAngle, incorrectAngle, false, arcPaint);
            }

            // Calculate percentage
            int percentage = Math.round((correct * 100f) / total);
            String percentText = percentage + "%";

            textPaint.setTextSize(22f * density);
            float textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f);
            canvas.drawText(percentText, cx, textY, textPaint);
        }
    }
}
