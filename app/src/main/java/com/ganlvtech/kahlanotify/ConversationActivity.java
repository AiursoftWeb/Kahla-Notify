package com.ganlvtech.kahlanotify;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.components.MessageListItemAdapter;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.util.ConversationListActivitySharedPreferences;

import java.util.List;

public class ConversationActivity extends MyServiceActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView toolbalTextViewTitle;
    private TextView toolbalTextViewSubtitle;
    private ListView listViewConversations;
    private MessageListItemAdapter messageListItemAdapter;
    private ConversationListActivitySharedPreferences conversationListActivitySharedPreferences;
    private int conversationId;
    private KahlaClient kahlaClient;
    private Conversation conversation;
    private Handler kahlaClientOriginalHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        toolbalTextViewTitle = findViewById(R.id.toolbalTextViewTitle);
        toolbalTextViewSubtitle = findViewById(R.id.toolbalTextViewSubtitle);
        listViewConversations = findViewById(R.id.listViewConversations);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                kahlaClient.fetchConversationAsync(conversationId);
            }
        });
        messageListItemAdapter = new MessageListItemAdapter(this);
        listViewConversations.setAdapter(messageListItemAdapter);

        conversationListActivitySharedPreferences = new ConversationListActivitySharedPreferences(this);
        conversationListActivitySharedPreferences.load();
        conversationId = getIntent().getIntExtra("conversationId", 0);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        List<KahlaClient> kahlaClientList = myService.getKahlaClientList();
        kahlaClient = conversationListActivitySharedPreferences.findKahlaClient(kahlaClientList);
        if (kahlaClient == null) {
            finish();
        }

        kahlaClientOriginalHandler = kahlaClient.mainThreadHandler;
        kahlaClient.mainThreadHandler = new MyHandler(Looper.getMainLooper(), this);

        conversation = kahlaClient.findConversationById(conversationId);
        if (conversation == null) {
            finish();
        }
        swipeRefreshLayout.setRefreshing(true);
        updateMessageList();
        kahlaClient.fetchConversationAsync(conversationId);
    }

    private void updateMessageList() {
        toolbalTextViewTitle.setText(conversation.displayName);
        if (kahlaClient.userInfo != null) {
            toolbalTextViewSubtitle.setText(kahlaClient.userInfo.nickName + " " + kahlaClient.baseUrl);
        } else {
            toolbalTextViewSubtitle.setText(kahlaClient.email + " " + kahlaClient.baseUrl);
        }
        messageListItemAdapter.kahlaClient = kahlaClient;
        messageListItemAdapter.conversation = conversation;
        messageListItemAdapter.clear();
        if (conversation.messageList != null) {
            messageListItemAdapter.addAll(conversation.messageList);
        }
        swipeRefreshLayout.setRefreshing(false);
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

        MyHandler(Looper looper, ConversationActivity conversationActivity) {
            super(looper);
            this.conversationActivity = conversationActivity;
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case KahlaClient.MESSAGE_WHAT_FETCH_USER_INFO_RESPONSE:
                    conversationActivity.updateMessageList();
                case KahlaClient.MESSAGE_WHAT_FETCH_CONVERSATION_RESPONSE:
                    conversationActivity.updateMessageList();
                    break;
            }
            if (conversationActivity.kahlaClientOriginalHandler != null) {
                conversationActivity.kahlaClientOriginalHandler.handleMessage(msg);
            }
        }
    }
}
