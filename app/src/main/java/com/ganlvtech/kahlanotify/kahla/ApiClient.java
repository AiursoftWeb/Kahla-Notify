package com.ganlvtech.kahlanotify.kahla;

import com.ganlvtech.kahlanotify.kahla.lib.CookieJar;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ApiClient {
    private CookieJar mCookieJar;
    private OkHttpClient mClient;
    private String mServer;
    private AuthService mAuthService;
    private FriendshipService mFriendshipService;
    private ConversationService mConversationService;
    private OssService mOssService;
    private String mOssServiceBaseUrl;

    public ApiClient(String server, String ossServer) {
        mCookieJar = new CookieJar();
        mClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .cookieJar(mCookieJar)
                .build();
        mServer = server;
        mOssServiceBaseUrl = ossServer;
    }

    public ApiClient(String server) {
        this(server, "https://oss.aiursoft.com");
    }

    public AuthService auth() {
        if (mAuthService == null) {
            mAuthService = new AuthService(mClient, mServer);
        }
        return mAuthService;
    }

    public FriendshipService friendship() {
        if (mFriendshipService == null) {
            mFriendshipService = new FriendshipService(mClient, mServer);
        }
        return mFriendshipService;
    }

    public ConversationService conversation() {
        if (mConversationService == null) {
            mConversationService = new ConversationService(mClient, mServer);
        }
        return mConversationService;
    }

    public OssService oss() {
        if (mOssService == null) {
            mOssService = new OssService(mClient, mOssServiceBaseUrl);
        }
        return mOssService;
    }
}
