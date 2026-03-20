package schulte.engine;

public class SchulteGridEngine {

    public enum State {
        STOPPED,
        RUNNING,
        PAUSED
    }

    private boolean isFourSquared = false;

    private int cur = 0;
    private int end = 16;

    private State state = State.STOPPED;

    public SchulteGridEngine() {
        end = isFourSquared ? 16 : 25;
    }

    public boolean isFourSquared() {
        return isFourSquared;
    }

    public int getCur() {
        return cur;
    }

    public int getEnd() {
        return end;
    }

    public State getState() {
        return state;
    }

//    public void change() {
//        if (state == State.RUNNING) {
//            return;
//        }
//        isFourSquared = !isFourSquared;
//        end = isFourSquared ? 16 : 25;
//    }

    public void start() {
        cur = 0;
        end = isFourSquared ? 16 : 25;
        state = State.RUNNING;
    }

    public void pause() {
        if (state == State.RUNNING) {
            state = State.PAUSED;
        }
    }

    public void resume() {
        if (state == State.PAUSED) {
            state = State.RUNNING;
        }
    }

    public void stop() {
        state = State.STOPPED;
        cur = 0;
    }

    // -1 false, 0 true, 1 complete
    public int click(int num) {

        if (state != State.RUNNING) {
            return -1;
        }

        if (num != cur + 1) {
            return -1;
        }

        if (num == end) {
            state = State.STOPPED;
            cur++;
            return 1;
        }

        cur++;
        return 0;
    }
}