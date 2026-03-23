package com.example.bridge.dashboard.item;

public class DashboardCollectionItem implements DashboardItem {

    private int step;
    private double sleep;

    public DashboardCollectionItem(int step, double sleep) {
        this.step = step;
        this.sleep = sleep;
    }

    @Override
    public int getViewType() {
        return DashboardItem.TYPE_COLLECTION;
    }

    public int getStep() {
        return step;
    }

    public double getSleep() {
        return sleep;
    }
}
