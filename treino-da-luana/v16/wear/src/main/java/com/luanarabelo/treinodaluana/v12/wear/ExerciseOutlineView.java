package com.luanarabelo.treinodaluana.v12.wear;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

final class ExerciseOutlineView extends View {
    private final Paint body = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);

    ExerciseOutlineView(Context context) {
        super(context);
        body.setColor(Color.rgb(244, 245, 247));
        body.setStyle(Paint.Style.STROKE);
        body.setStrokeWidth(dp(2));
        body.setStrokeCap(Paint.Cap.ROUND);
        accent.setColor(Color.rgb(255, 138, 61));
        accent.setStyle(Paint.Style.STROKE);
        accent.setStrokeWidth(dp(3));
        accent.setStrokeCap(Paint.Cap.ROUND);
        setContentDescription("Contorno demonstrativo do movimento");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float cx = w * .5f;
        canvas.drawCircle(cx, h * .2f, h * .075f, body);
        canvas.drawLine(cx, h * .28f, cx - w * .08f, h * .55f, body);
        canvas.drawLine(cx - w * .08f, h * .55f, cx - w * .25f, h * .82f, body);
        canvas.drawLine(cx - w * .08f, h * .55f, cx + w * .2f, h * .78f, body);
        canvas.drawLine(cx - w * .02f, h * .36f, cx - w * .28f, h * .52f, accent);
        canvas.drawLine(cx - w * .02f, h * .36f, cx + w * .27f, h * .43f, accent);
        canvas.drawLine(cx - w * .34f, h * .51f, cx + w * .34f, h * .43f, body);
        canvas.drawLine(cx - w * .38f, h * .45f, cx - w * .36f, h * .57f, body);
        canvas.drawLine(cx + w * .38f, h * .37f, cx + w * .36f, h * .49f, body);
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
