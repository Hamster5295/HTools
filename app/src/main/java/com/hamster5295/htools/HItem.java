package com.hamster5295.htools;

import android.graphics.drawable.Drawable;

public class HItem {
    public String text;
    public Drawable icon;
    public Class<?> cls;
    public String extraString;

    private boolean containExtra = false;

    public HItem(String text, Drawable drawableId, Class<?> cls) {
        this.text = text;
        this.icon = drawableId;
        this.cls = cls;
    }

    public HItem(String text, Drawable icon, Class<?> cls, String extraString) {
        this.text = text;
        this.icon = icon;
        this.cls = cls;
        this.extraString = extraString;
        containExtra = true;
    }

    public boolean isContainExtra() {
        return containExtra;
    }
}
