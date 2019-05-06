package com.ganlvtech.kahlanotify.kahla;

import com.ganlvtech.kahlanotify.kahla.lib.CookieJar;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ApiClient {
    private CookieJar cookieJar;
    private OkHttpClient client;
    private String baseUrl;
    private AuthService authService;
    private FriendshipService friendshipService;
    private ConversationService conversationService;
    private OssService ossService;
    private String ossServiceBaseUrl;

    public ApiClient(String baseUrl, String ossServiceBaseUrl) {
        cookieJar = new CookieJar();
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
        this.baseUrl = baseUrl;
        this.ossServiceBaseUrl = ossServiceBaseUrl;
    }

    public ApiClient(String baseUrl) {
        this(baseUrl, "https://oss.aiursoft.com");
    }

    public AuthService auth() {
        if (authService == null) {
            authService = new AuthService(client, baseUrl);
        }
        return authService;
    }

    public FriendshipService friendship() {
        if (friendshipService == null) {
            friendshipService = new FriendshipService(client, baseUrl);
        }
        return friendshipService;
    }

    public ConversationService conversation() {
        if (conversationService == null) {
            conversationService = new ConversationService(client, baseUrl);
        }
        return conversationService;
    }

    public OssService oss() {
        if (ossService == null) {
            ossService = new OssService(client, ossServiceBaseUrl);
        }
        return ossService;
    }
}
