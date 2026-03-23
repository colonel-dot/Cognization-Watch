package com.example.bridge.dashboard.item;

public class DashboardRiskItem implements DashboardItem {

    private double risk;
    private double yesterday;

    public DashboardRiskItem(double risk, double yesterday) {
        this.risk = risk;
        this.yesterday = yesterday;
    }

    @Override
    public int getViewType() {
        return DashboardItem.TYPE_RISK;
    }

    public double getRisk() {
        return risk;
    }

    public double getYesterday() {
        return yesterday;
    }
}
