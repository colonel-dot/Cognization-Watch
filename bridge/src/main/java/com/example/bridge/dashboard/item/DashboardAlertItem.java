package com.example.bridge.dashboard.item;

public class DashboardAlertItem implements DashboardItem {

    private final String tip;

    public DashboardAlertItem(String tip) {
        this.tip = tip;
    }

    @Override
    public int getViewType() {
        return DashboardItem.TYPE_ALERT;
    }

    public String getTip() {
        return tip;
    }
}
