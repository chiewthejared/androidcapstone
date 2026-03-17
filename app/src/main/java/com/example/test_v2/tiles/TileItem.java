package com.example.test_v2.tiles;

public class TileItem {
    private final int iconRes;
    private final String label;

    public TileItem(int iconRes, String label) {
        this.iconRes = iconRes;
        this.label = label;
    }

    public int getIconRes() { return iconRes; }
    public String getLabel() { return label; }
}