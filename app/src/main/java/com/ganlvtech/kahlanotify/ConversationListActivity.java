package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.ganlvtech.kahlanotify.components.ConversationListItemAdapter;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;

import java.util.ArrayList;
import java.util.List;

public class ConversationListActivity extends Activity {
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        listView = findViewById(R.id.listViewConversations);
        List<Conversation> arrayList = new ArrayList<>();
        Conversation conversation;
        for (int i = 0; i < 10; i++) {
            conversation = new Conversation();
            conversation.displayName = "EdgeNeko";
            conversation.displayImageKey = 4721;
            conversation.latestMessage = "U2FsdGVkX1+opFA8RTzA4tqLLiO6NAPf5/Qg40RrURzgEJrl0ZYmUdK8ImhqqbB8";
            conversation.aesKey = "407a9d96265f44b5995138494a997acd";
            arrayList.add(conversation);
            conversation = new Conversation();
            conversation.displayName = "GanlvTech";
            conversation.displayImageKey = 2611;
            conversation.latestMessage = null;
            conversation.aesKey = "cd4e759ee6894dfda0073c2d68cb89e5";
            arrayList.add(conversation);
        }
        listView.setAdapter(new ConversationListItemAdapter(this, arrayList));
    }
}
