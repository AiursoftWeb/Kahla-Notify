package com.ganlvtech.kahlanotify;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.components.MessageListItemAdapter;
import com.ganlvtech.kahlanotify.kahla.lib.CryptoJs;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.util.ConversationListActivitySharedPreferences;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ConversationActivity extends MyServiceActivity {
    public static final String INTENT_EXTRA_NAME_CONVERSATION_ID = "conversationId";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mToolbarTextViewTitle;
    private TextView mToolbarTextViewSubtitle;
    private ListView mListViewConversations;
    private EditText mEditTextSend;
    private Button mButtonSend;
    private MessageListItemAdapter mMessageListItemAdapter;
    private ConversationListActivitySharedPreferences mConversationListActivitySharedPreferences;
    private int mConversationId;
    @Nullable
    private KahlaClient kahlaClient;
    @Nullable
    private Conversation mConversation;
    @Nullable
    private MyHandler mMyHandler;
    private Handler kahlaClientOriginalHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load shared preferences
        mConversationListActivitySharedPreferences = new ConversationListActivitySharedPreferences(this);
        mConversationListActivitySharedPreferences.load();

        // Get intent extras
        mConversationId = getIntent().getIntExtra(INTENT_EXTRA_NAME_CONVERSATION_ID, 0);

        setContentView(R.layout.activity_conversation);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mToolbarTextViewTitle = findViewById(R.id.toolbarTextViewTitle);
        mToolbarTextViewSubtitle = findViewById(R.id.toolbarTextViewSubtitle);
        mListViewConversations = findViewById(R.id.listViewConversations);
        mEditTextSend = findViewById(R.id.editTextSend);
        mButtonSend = findViewById(R.id.buttonSend);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                kahlaClient.fetchConversationMessageAsync(mConversationId);
            }
        });

        mMessageListItemAdapter = new MessageListItemAdapter(this);
        mListViewConversations.setAdapter(mMessageListItemAdapter);

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextSend.getText().toString();
                try {
                    if (mConversation != null) {
                        String content = CryptoJs.aesEncrypt(message.getBytes("UTF-8"), mConversation.aesKey);
                        kahlaClient.sendMessageAsync(mConversationId, content);
                    }
                } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        // Create main thread handler
        mMyHandler = new MyHandler(this);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (mMyService == null) {
            return;
        }
        List<KahlaClient> kahlaClientList = mMyService.getKahlaClientList();
        kahlaClient = mConversationListActivitySharedPreferences.findKahlaClient(kahlaClientList);
        if (kahlaClient == null) {
            finish();
            return;
        }
        kahlaClientOriginalHandler = kahlaClient.mainThreadHandler;
        kahlaClient.mainThreadHandler = mMyHandler;
        mConversation = kahlaClient.findConversationById(mConversationId);
        if (mConversation == null) {
            finish();
            return;
        }
        mSwipeRefreshLayout.setRefreshing(true);
        updateMessageList();
        kahlaClient.fetchConversationMessageAsync(mConversationId);
    }

    private void updateMessageList() {
        mToolbarTextViewTitle.setText(mConversation.displayName);
        if (kahlaClient.myUserInfo != null) {
            mToolbarTextViewSubtitle.setText(kahlaClient.myUserInfo.nickName + " " + kahlaClient.baseUrl);
        } else {
            mToolbarTextViewSubtitle.setText(kahlaClient.email + " " + kahlaClient.baseUrl);
        }
        mMessageListItemAdapter.kahlaClient = kahlaClient;
        mMessageListItemAdapter.conversation = mConversation;
        mMessageListItemAdapter.clear();
        if (mConversation.messageList != null) {
            mMessageListItemAdapter.addAll(mConversation.messageList);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        if (kahlaClient != null) {
            if (kahlaClient.mainThreadHandler instanceof MyHandler) {
                kahlaClient.mainThreadHandler = kahlaClientOriginalHandler;
            }
        }
        super.onStop();
    }

    private static class MyHandler extends Handler {
        private ConversationActivity conversationActivity;

        MyHandler(ConversationActivity conversationActivity) {
            super(Looper.getMainLooper());
            this.conversationActivity = conversationActivity;
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case KahlaClient.MESSAGE_WHAT_FETCH_MY_USER_INFO_RESPONSE:
                    conversationActivity.updateMessageList();
                case KahlaClient.MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE_RESPONSE:
                    conversationActivity.updateMessageList();
                    break;
            }
        }
    }
}
