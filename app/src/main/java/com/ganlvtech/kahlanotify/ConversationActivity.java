package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;

import com.ganlvtech.kahlanotify.components.ConversationItemAdapter;
import com.ganlvtech.kahlanotify.components.ConversationListItemAdapter;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import java.io.IOException;

public class ConversationActivity extends Activity {
    private ListView listViewConversations;
    private Runnable updateConversationList = new Runnable() {
        @Override
        public void run() {
            // listViewConversations.setAdapter(new ConversationListItemAdapter(ConversationListActivity.this, conversationList, apiClient.oss()));
        }
    };
    private Handler handler;
    private Runnable getConversationList = new Runnable() {
        @Override
        public void run() {
            // try {
            //     MyFriendsResponse myFriendsResponse = apiClient.friendship().MyFriends();
            //     if (myFriendsResponse.code != 0) {
            //         apiClient.auth().AuthByPassword(email, password);
            //         myFriendsResponse = apiClient.friendship().MyFriends();
            //     }
            //     conversationList = myFriendsResponse.items;
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
            // handler.post(updateConversationList);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        listViewConversations = findViewById(R.id.list_view_conversation);

        // listViewConversations.setAdapter(new ConversationItemAdapter(this, conversationList, apiClient.oss()));

    }
}
