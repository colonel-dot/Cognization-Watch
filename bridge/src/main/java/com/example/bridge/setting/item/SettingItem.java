package com.example.bridge.setting.item;

public class SettingItem {
    private final int icon;
    private final String type;
    private final int position;

    private final boolean isSwitch;

    public SettingItem(int icon, String type, int position, boolean isSwitch) {
        this.icon = icon;
        this.type = type;
        this.position = position;
        this.isSwitch = isSwitch;
    }

    public int getIcon() {
        return icon;
    }

    public String getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public boolean isSwitch() {
        return isSwitch;
    }
}
