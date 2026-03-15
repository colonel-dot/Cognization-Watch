package game;

public class BrainTrainingRVModel {
    private String function;
    private int icon;
    private String state;
    private int background;

    public BrainTrainingRVModel(String function, int icon, String state, int background) {
        this.function = function;
        this.icon = icon;
        this.state = state;
        this.background = background;
    }

    public String getFunction() {
        return function;
    }

    public int getIcon() {
        return icon;
    }

    public String getState() {
        return state;
    }

    public int getBackground() {
        return background;
    }
}
