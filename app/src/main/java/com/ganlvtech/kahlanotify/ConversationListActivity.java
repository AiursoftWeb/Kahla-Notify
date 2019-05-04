package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;

import com.ganlvtech.kahlanotify.components.ConversationListItemAdapter;
import com.ganlvtech.kahlanotify.kahla.KahlaWebApiClient;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import java.io.IOException;
import java.util.List;

public class ConversationListActivity extends Activity {
    private String baseUrl = "https://server.kahla.app";
    private ListView listView;
    private String username;
    private String password;
    private KahlaWebApiClient client;
    private List<Conversation> conversationList;
    private Runnable updateConversationList = new Runnable() {
        @Override
        public void run() {
            listView.setAdapter(new ConversationListItemAdapter(ConversationListActivity.this, conversationList));
        }
    };
    private Handler handler;
    private Runnable getConversationList = new Runnable() {
        @Override
        public void run() {
            try {
                client.auth().AuthByPassword(username, password);
                MyFriendsResponse myFriendsResponse = client.friendship().MyFriends();
                conversationList = myFriendsResponse.items;
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.post(updateConversationList);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");
        password = sharedPreferences.getString("password", "");
        listView = findViewById(R.id.listViewConversations);
        client = new KahlaWebApiClient(baseUrl);
        handler = new Handler(Looper.getMainLooper());
        new Thread(getConversationList).start();
    }
}
