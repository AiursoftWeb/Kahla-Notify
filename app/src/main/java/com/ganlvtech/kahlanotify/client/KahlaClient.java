package com.ganlvtech.kahlanotify.client;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.ganlvtech.kahlanotify.kahla.ApiClient;
import com.ganlvtech.kahlanotify.kahla.AuthService;
import com.ganlvtech.kahlanotify.kahla.ConversationService;
import com.ganlvtech.kahlanotify.kahla.FriendshipService;
import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;
import com.ganlvtech.kahlanotify.kahla.models.ContactInfo;
import com.ganlvtech.kahlanotify.kahla.models.Message;
import com.ganlvtech.kahlanotify.kahla.models.User;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.GetMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.SendMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class KahlaClient {
    @NonNull
    private String mServer;
    @NonNull
    private String mEmail;
    @NonNull
    private String mPassword;
    @NonNull
    private ApiClient mApiClient;
    @Nullable
    private OnLoginResponseListener mOnLoginResponseListener;
    @Nullable
    private OnFailureListener mOnLoginFailureListener;
    @Nullable
    private OnFetchMyUserInfoResponseListener mOnFetchMyUserInfoResponseListener;
    @Nullable
    private OnFailureListener mOnFetchMyUserInfoFailureListener;
    @Nullable
    private OnFetchContactInfoListResponseListener mOnFetchContactInfoListResponseListener;
    @Nullable
    private OnFailureListener mOnFetchContactInfoListFailureListener;
    @Nullable
    private OnFetchMessageResponseListener mOnFetchMessageResponseListener;
    @Nullable
    private OnFailureListener mOnFetchMessageFailureListener;
    @Nullable
    private OnSendMessageResponseListener mOnSendMessageResponseListener;
    @Nullable
    private OnFailureListener mOnSendMessageFailureListener;
    private boolean mIsLogin;
    @Nullable
    private User mMyUserInfo;
    /**
     * Map conversation id to message list
     */
    @NonNull
    private SparseArray<List<Message>> mConversationMessageMap = new SparseArray<>();
    @Nullable
    private List<ContactInfo> mContactInfoList;

    public KahlaClient(@NonNull String server, @NonNull String email, @NonNull String password) {
        mServer = server;
        mEmail = email;
        mPassword = password;
        mApiClient = new ApiClient(mServer);
    }

    @NonNull
    public String getServer() {
        return mServer;
    }

    @NonNull
    public String getEmail() {
        return mEmail;
    }

    @NonNull
    public String getPassword() {
        return mPassword;
    }

    public boolean isLogin() {
        return mIsLogin;
    }

    @Nullable
    public User getMyUserInfo() {
        return mMyUserInfo;
    }

    @NonNull
    public ApiClient getApiClient() {
        return mApiClient;
    }

    @Nullable
    public List<ContactInfo> getContactInfoList() {
        return mContactInfoList;
    }

    @NonNull
    public SparseArray<List<Message>> getConversationMessageMap() {
        return mConversationMessageMap;
    }

    @Nullable
    public ContactInfo findConversationById(int conversationId) {
        if (mContactInfoList == null) {
            return null;
        }
        for (ContactInfo contactInfo : mContactInfoList) {
            if (contactInfo.conversationId == conversationId) {
                return contactInfo;
            }
        }
        return null;
    }

    public int getUnreadCount() {
        if (mContactInfoList == null) {
            return 0;
        }
        int unread = 0;
        for (ContactInfo contactInfo : mContactInfoList) {
            unread += contactInfo.unReadAmount;
        }
        return unread;
    }

    public boolean isSomeoneAtMe() {
        if (mContactInfoList == null) {
            return false;
        }
        for (ContactInfo contactInfo : mContactInfoList) {
            if (contactInfo.someoneAtMe) {
                return true;
            }
        }
        return false;
    }

    public void setOnLoginResponseListener(OnLoginResponseListener onLoginResponseListener) {
        mOnLoginResponseListener = onLoginResponseListener;
    }

    public void setOnLoginFailureListener(OnFailureListener onLoginFailureListener) {
        mOnLoginFailureListener = onLoginFailureListener;
    }

    public void setOnFetchMyUserInfoResponseListener(OnFetchMyUserInfoResponseListener onFetchMyUserInfoResponseListener) {
        mOnFetchMyUserInfoResponseListener = onFetchMyUserInfoResponseListener;
    }

    public void setOnFetchMyUserInfoFailureListener(OnFailureListener onFetchMyUserInfoFailureListener) {
        mOnFetchMyUserInfoFailureListener = onFetchMyUserInfoFailureListener;
    }

    public void setOnFetchContactInfoListResponseListener(OnFetchContactInfoListResponseListener onFetchContactInfoListResponseListener) {
        mOnFetchContactInfoListResponseListener = onFetchContactInfoListResponseListener;
    }

    public void setOnFetchContactInfoListFailureListener(OnFailureListener onFetchContactInfoListFailureListener) {
        mOnFetchContactInfoListFailureListener = onFetchContactInfoListFailureListener;
    }

    public void setOnFetchMessageResponseListener(OnFetchMessageResponseListener onFetchMessageResponseListener) {
        mOnFetchMessageResponseListener = onFetchMessageResponseListener;
    }

    public void setOnFetchMessageFailureListener(OnFailureListener onFetchMessageFailureListener) {
        mOnFetchMessageFailureListener = onFetchMessageFailureListener;
    }

    public void setOnSendMessageResponseListener(OnSendMessageResponseListener onSendMessageResponseListener) {
        mOnSendMessageResponseListener = onSendMessageResponseListener;
    }

    public void setOnSendMessageFailureListener(OnFailureListener onSendMessageFailureListener) {
        mOnSendMessageFailureListener = onSendMessageFailureListener;
    }

    public void login() {
        mApiClient.auth().newAuthByPasswordCall(mEmail, mPassword)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                        onLoginFailure(e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            AuthByPasswordResponse authByPasswordResponse = AuthService.parseAuthByPasswordResponse(response);
                            if (authByPasswordResponse.isResponseOK()) {
                                mIsLogin = true;
                            }
                            onLoginResponse(authByPasswordResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onLoginFailure(e);
                        }
                    }
                });
    }

    private void mustLogin(@NonNull final OnLoginListener onLoginListener) {
        if (mIsLogin) {
            onLoginListener.onLogin();
            return;
        }
        mApiClient.auth().newAuthByPasswordCall(mEmail, mPassword)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                        onLoginFailure(e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            AuthByPasswordResponse authByPasswordResponse = AuthService.parseAuthByPasswordResponse(response);
                            if (authByPasswordResponse.isResponseOK()) {
                                mIsLogin = true;
                            }
                            onLoginListener.onLogin();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onLoginFailure(e);
                        }
                    }
                });
    }

    public void fetchMyUserInfo() {
        mustLogin(new OnLoginListener() {
            @Override
            public void onLogin() {
                mApiClient.auth().newMeCall()
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                                onFetchMyUserInfoFailure(e);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                try {
                                    MeResponse meResponse = AuthService.parseMeResponse(response);
                                    if (meResponse.isResponseOK()) {
                                        mMyUserInfo = meResponse.value;
                                    }
                                    onFetchMyUserInfoResponse(meResponse);
                                } catch (ResponseCodeHttpUnauthorizedException | JSONException e) {
                                    e.printStackTrace();
                                    onFetchMyUserInfoFailure(e);
                                }
                            }
                        });
            }
        });
    }

    public void fetchContactInfoList() {
        mustLogin(new OnLoginListener() {
            @Override
            public void onLogin() {
                mApiClient.friendship().newMyFriendsCall(false)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                                onFetchContactInfoListFailure(e);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                try {
                                    MyFriendsResponse myFriendsResponse = FriendshipService.parseMyFriendsResponse(response);
                                    if (myFriendsResponse.isResponseOK()) {
                                        mContactInfoList = myFriendsResponse.items;
                                    }
                                    onFetchContactInfoListResponse(myFriendsResponse);
                                } catch (ResponseCodeHttpUnauthorizedException | JSONException e) {
                                    e.printStackTrace();
                                    onFetchContactInfoListFailure(e);
                                }
                            }
                        });
            }
        });
    }

    public void fetchMessage(final int conversationId) {
        mustLogin(new OnLoginListener() {
            @Override
            public void onLogin() {
                mApiClient.conversation().newGetMessageCall(conversationId, -1, 15)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                onFetchMessageFailure(e);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                try {
                                    GetMessageResponse getMessageResponse = ConversationService.parseGetMessageResponse(response);
                                    if (getMessageResponse.isResponseOK()) {
                                        mConversationMessageMap.put(conversationId, getMessageResponse.items);
                                    }
                                    onFetchMessageResponse(getMessageResponse);
                                } catch (ResponseCodeHttpUnauthorizedException | JSONException e) {
                                    e.printStackTrace();
                                    onFetchMessageFailure(e);
                                }
                            }
                        });
            }
        });
    }

    public void sendMessage(final int conversationId, final String content) {
        mustLogin(new OnLoginListener() {
            @Override
            public void onLogin() {
                mApiClient.conversation().newSendMessageCall(conversationId, content)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                onSendMessageFailure(e);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                try {
                                    SendMessageResponse sendMessageResponse = ConversationService.parseSendMessageResponse(response);
                                    onSendMessageResponse(sendMessageResponse);
                                } catch (ResponseCodeHttpUnauthorizedException | JSONException e) {
                                    e.printStackTrace();
                                    onSendMessageFailure(e);
                                }
                            }
                        });
            }
        });
    }

    private void onLoginResponse(AuthByPasswordResponse authByPasswordResponse) {
        if (mOnLoginResponseListener != null) {
            mOnLoginResponseListener.onLoginResponse(authByPasswordResponse, this);
        }
    }

    private void onLoginFailure(Exception e) {
        if (mOnLoginFailureListener != null) {
            mOnLoginFailureListener.onFailure(e, this);
        }
    }

    private void onFetchMyUserInfoResponse(MeResponse meResponse) {
        if (mOnFetchMyUserInfoResponseListener != null) {
            mOnFetchMyUserInfoResponseListener.onFetchMyUserInfoResponse(meResponse, this);
        }
    }

    private void onFetchMyUserInfoFailure(Exception e) {
        if (mOnFetchMyUserInfoFailureListener != null) {
            mOnFetchMyUserInfoFailureListener.onFailure(e, this);
        }
    }

    private void onFetchContactInfoListResponse(MyFriendsResponse meResponse) {
        if (mOnFetchContactInfoListResponseListener != null) {
            mOnFetchContactInfoListResponseListener.onFetchContactInfoListResponse(meResponse, this);
        }
    }

    private void onFetchContactInfoListFailure(Exception e) {
        if (mOnFetchContactInfoListFailureListener != null) {
            mOnFetchContactInfoListFailureListener.onFailure(e, this);
        }
    }

    private void onFetchMessageResponse(GetMessageResponse getMessageResponse) {
        if (mOnFetchMessageResponseListener != null) {
            mOnFetchMessageResponseListener.onFetchMessageResponse(getMessageResponse, this);
        }
    }

    private void onFetchMessageFailure(Exception e) {
        if (mOnFetchMessageFailureListener != null) {
            mOnFetchMessageFailureListener.onFailure(e, this);
        }
    }

    private void onSendMessageResponse(SendMessageResponse sendMessageResponse) {
        if (mOnSendMessageResponseListener != null) {
            mOnSendMessageResponseListener.onSendMessageResponse(sendMessageResponse, this);
        }
    }

    private void onSendMessageFailure(Exception e) {
        if (mOnSendMessageFailureListener != null) {
            mOnSendMessageFailureListener.onFailure(e, this);
        }
    }

    public interface OnLoginListener {
        void onLogin();
    }

    public interface OnLoginResponseListener {
        void onLoginResponse(AuthByPasswordResponse authByPasswordResponse, KahlaClient kahlaClient);

    }

    public interface OnFetchMyUserInfoResponseListener {
        void onFetchMyUserInfoResponse(MeResponse meResponse, KahlaClient kahlaClient);
    }

    public interface OnFetchContactInfoListResponseListener {
        void onFetchContactInfoListResponse(MyFriendsResponse meResponse, KahlaClient kahlaClient);
    }

    public interface OnFetchMessageResponseListener {
        void onFetchMessageResponse(GetMessageResponse getMessageResponse, KahlaClient kahlaClient);
    }

    public interface OnSendMessageResponseListener {
        void onSendMessageResponse(SendMessageResponse sendMessageResponse, KahlaClient kahlaClient);
    }

    public interface OnFailureListener {
        void onFailure(Exception e, KahlaClient kahlaClient);
    }
}
