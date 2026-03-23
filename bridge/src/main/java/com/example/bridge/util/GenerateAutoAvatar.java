package com.example.bridge.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.core.graphics.ColorUtils;

public class GenerateAutoAvatar {

    public static Bitmap generate(String name, int size) {

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint bgPaint = new Paint();
        bgPaint.setColor(stringToColor(name));
        bgPaint.setAntiAlias(true);

        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, bgPaint);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size / 2f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        String letter = getInitial(name);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float y = radius - (fm.ascent + fm.descent) / 2;

        canvas.drawText(letter, radius, y, textPaint);

        return bitmap;
    }

    private static String getInitial(String name) {
        if (name == null || name.length() == 0) {
            return "?";
        }
        return name.substring(0, 1).toUpperCase();
    }

    private static int stringHash(String s) {
        int hash = 0;
        for (char c : s.toCharArray()) {
            hash = c + ((hash << 5) - hash);
        }
        return hash;
    }

    private static int stringToColor(String name) {
        int hash = stringHash(name);

        float[] hsl = new float[]{
                Math.abs(hash) % 256,
                0.5f,
                0.65f
        };

        return ColorUtils.HSLToColor(hsl);
    }
}
