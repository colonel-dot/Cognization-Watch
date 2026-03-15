package com.example.cognitive.main;

public class HomeRVModel {
    private int image;
    private String function;
    private int background;

    public HomeRVModel(int image, String function, int background) {
        this.image = image;
        this.function = function;
        this.background = background;
    }

    public int getImage() {
        return image;
    }

    public String getFunction() {
        return function;
    }

    public int getBackground() {
        return background;
    }
}
