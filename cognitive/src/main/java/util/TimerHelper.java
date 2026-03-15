package util;

import android.os.Handler;
import android.os.Looper;

public class TimerHelper {

    private long startTime;
    private long elapsedTime; // 已累计时间
    private boolean running;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable task = new Runnable() {
        @Override
        public void run() {

            if (!running) {
                return;
            }

            long time = System.currentTimeMillis() - startTime + elapsedTime;

            if (listener != null) {
                listener.onTick(time);
            }

            handler.postDelayed(this, 16);
        }
    };

    private OnTimerListener listener;

    public void setOnTimerListener(OnTimerListener listener) {
        this.listener = listener;
    }

    public void start() {
        elapsedTime = 0;
        startTime = System.currentTimeMillis();
        running = true;
        handler.post(task);
    }

    public void pause() {
        if (!running) {
            return;
        }

        running = false;
        handler.removeCallbacks(task);
        elapsedTime += System.currentTimeMillis() - startTime;
    }

    public void resume() {
        if (running) {
            return;
        }

        startTime = System.currentTimeMillis();
        running = true;
        handler.post(task);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(task);
        elapsedTime = 0;
    }
}