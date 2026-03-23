package com.example.cogwatch.login.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import java.util.Random;

public class TypewriterTextView extends androidx.appcompat.widget.AppCompatTextView {

    private CharSequence mOriginalText = "";
    private int mIndex;
    private long mBaseDelay = 60;
    private boolean isCursorVisible = true;
    private final String mCursorSymbol = "|";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Random mRandom = new Random();

    public TypewriterTextView(Context context) {
        super(context);
    }

    public TypewriterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            if (mIndex <= mOriginalText.length()) {
                updateTextWithCursor(true);

                long randomDelay = mBaseDelay + mRandom.nextInt((int) (mBaseDelay * 0.5));
                if (mIndex > 0) {
                    char lastChar = mOriginalText.charAt(mIndex - 1);
                    if (lastChar == ',' || lastChar == '，' || lastChar == '.' || lastChar == '。' || lastChar == '?') {
                        randomDelay += 300;
                    }
                }

                mIndex++;
                mHandler.postDelayed(characterAdder, randomDelay);
            } else {
                mHandler.post(cursorBlinker);
            }
        }
    };

    private final Runnable cursorBlinker = new Runnable() {
        @Override
        public void run() {
            updateTextWithCursor(isCursorVisible);
            isCursorVisible = !isCursorVisible;
            mHandler.postDelayed(this, 500);
        }
    };

    private void updateTextWithCursor(boolean show) {
        String base = mOriginalText.subSequence(0, Math.min(mIndex, mOriginalText.length())).toString();
        String full = base + mCursorSymbol;
        SpannableString spannable = new SpannableString(full);

        if (!show) {
            spannable.setSpan(
                    new ForegroundColorSpan(Color.TRANSPARENT),
                    base.length(),
                    full.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        setText(spannable);
    }

    public void animateText(CharSequence text) {
        if (text == null) return;
        mOriginalText = text;
        mIndex = 0;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(characterAdder, mBaseDelay);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }
}