package com.example.cognitive.main.home.model;

public class HomeRVModel {
    private final int image;
    private final String function;
    private final int background;

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
