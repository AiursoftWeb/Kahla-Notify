package com.ganlvtech.kahlanotify.components;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

public class IconTitleContent {
    @DrawableRes
    public int placeholderResId;
    @NonNull
    public String iconUrl = "";
    public int unreadCount;
    @NonNull
    public String title = "";
    @NonNull
    public String content = "";
    public boolean at;
    @NonNull
    public String contentImageUrl = "";
}
