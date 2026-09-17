package com.luanarabelo.treinodaluana.v12.wear;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

final class ProgressRingView extends View {
    private static final int ORANGE = Color.rgb(105, 84, 70);
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progress = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mainText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint smallText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc = new RectF();
    private final float ratio;
    private final String value;
    private final String caption;

    ProgressRingView(Context context, int done, int total, String value, String caption) {
        super(context);
        this.ratio = total <= 0 ? 0f : Math.max(0f, Math.min(1f, done / (float) total));
        this.value = value;
        this.caption = caption;
        track.setColor(Color.rgb(220, 211, 200));
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeWidth(dp(4));
        track.setStrokeCap(Paint.Cap.ROUND);
        progress.setColor(ORANGE);
        progress.setStyle(Paint.Style.STROKE);
        progress.setStrokeWidth(dp(4));
        progress.setStrokeCap(Paint.Cap.ROUND);
        mainText.setColor(Color.rgb(62, 50, 43));
        mainText.setTextAlign(Paint.Align.CENTER);
        mainText.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        mainText.setTextSize(dp(18));
        smallText.setColor(Color.rgb(132, 113, 99));
        smallText.setTextAlign(Paint.Align.CENTER);
        smallText.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        smallText.setTextSize(dp(6));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        float inset = dp(6);
        float left = (getWidth() - size) / 2f + inset;
        float top = (getHeight() - size) / 2f + inset;
        arc.set(left, top, left + size - inset * 2f, top + size - inset * 2f);
        canvas.drawArc(arc, 150f, 240f, false, track);
        canvas.drawArc(arc, 150f, 240f * ratio, false, progress);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        canvas.drawText(value, cx, cy + dp(4), mainText);
        canvas.drawText(caption, cx, cy + dp(14), smallText);
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
