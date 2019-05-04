package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.components.ConversationListItemAdapter;
import com.ganlvtech.kahlanotify.kahla.KahlaWebApiClient;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import java.io.IOException;
import java.util.List;

public class ConversationListActivity extends Activity {
    private String baseUrl = "https://server.kahla.app";
    private ListView listViewConversations;
    private TextView textViewLegacy;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String username;
    private String password;
    private KahlaWebApiClient client;
    private List<Conversation> conversationList;
    private Runnable updateConversationList = new Runnable() {
        @Override
        public void run() {
            listViewConversations.setAdapter(new ConversationListItemAdapter(ConversationListActivity.this, conversationList));
            swipeRefreshLayout.setRefreshing(false);
        }
    };
    private Handler handler;
    private Runnable getConversationList = new Runnable() {
        @Override
        public void run() {
            try {
                MyFriendsResponse myFriendsResponse = client.friendship().MyFriends();
                if (myFriendsResponse.code != 0) {
                    client.auth().AuthByPassword(username, password);
                    myFriendsResponse = client.friendship().MyFriends();
                }
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

        listViewConversations = findViewById(R.id.listViewConversations);
        textViewLegacy = findViewById(R.id.textViewLegacy);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", "");
        password = sharedPreferences.getString("password", "");
        client = new KahlaWebApiClient(baseUrl);
        handler = new Handler(Looper.getMainLooper());

        textViewLegacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConversationListActivity.this, LegacyMainActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        refresh();
    }

    public void refresh() {
        new Thread(getConversationList).start();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }
}
