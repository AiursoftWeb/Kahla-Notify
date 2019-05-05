package com.ganlvtech.kahlanotify.kahla.lib;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class CookieJar implements okhttp3.CookieJar {
    private final Map<String, List<Cookie>> cookiesMap = new HashMap<>();

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        String host = url.host();
        List<Cookie> cookiesList = cookiesMap.get(host);
        if (cookiesList != null) {
            cookiesMap.remove(host);
        }
        cookiesMap.put(host, cookies);
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        List<Cookie> cookiesList = cookiesMap.get(url.host());
        return cookiesList != null ? cookiesList : new ArrayList<Cookie>();
    }
}
