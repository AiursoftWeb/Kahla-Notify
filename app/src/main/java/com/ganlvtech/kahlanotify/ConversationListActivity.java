package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.components.AccountListItemAdapter;
import com.ganlvtech.kahlanotify.components.ConversationListItemAdapter;
import com.ganlvtech.kahlanotify.legacy.MainActivity;

import java.util.List;

public class ConversationListActivity extends Activity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listViewConversations;
    private ListView listViewAccounts;
    private TextView textViewLegacy;
    private TextView textViewNewAccount;
    private MyService myService;
    private ConversationListItemAdapter conversationListItemAdapter;
    private AccountListItemAdapter accountListItemAdapter;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.ServiceBinder serviceBinder = (MyService.ServiceBinder) service;
            myService = serviceBinder.getService();
            Log.d("onServiceConnected", "before loadKahlaClientList");
            loadKahlaClientList();
            Log.d("onServiceConnected", "after loadKahlaClientList");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };
    private KahlaClient kahlaClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        listViewConversations = findViewById(R.id.listViewConversations);
        listViewAccounts = findViewById(R.id.listViewAccounts);
        textViewLegacy = findViewById(R.id.textViewLegacy);
        textViewNewAccount = findViewById(R.id.textViewNewAccount);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadCurrentAccountConversationList();
            }
        });
        textViewLegacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConversationListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        textViewNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConversationListActivity.this, LoginActivity.class);
                intent.putExtra("isSecondAccount", true);
                startActivity(intent);
            }
        });
        conversationListItemAdapter = new ConversationListItemAdapter(ConversationListActivity.this);
        listViewConversations.setAdapter(conversationListItemAdapter);
        accountListItemAdapter = new AccountListItemAdapter(ConversationListActivity.this);
        listViewAccounts.setAdapter(accountListItemAdapter);
        listViewAccounts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                kahlaClient = (KahlaClient) accountListItemAdapter.getItem(position);
                loadCurrentAccountConversationList();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(serviceConnection);
        super.onStop();
    }

    private void loadKahlaClientList() {
        List<KahlaClient> kahlaClientList = myService.getKahlaClientList();
        accountListItemAdapter.clear();
        accountListItemAdapter.addAll(kahlaClientList);
    }

    private void loadCurrentAccountConversationList() {
        loadKahlaClientList();
        if (kahlaClient == null) {
            List<KahlaClient> kahlaClientList = myService.getKahlaClientList();
            if (!kahlaClientList.isEmpty()) {
                kahlaClient = kahlaClientList.get(0);
            }
        }
        if (kahlaClient != null) {
            conversationListItemAdapter.ossService = kahlaClient.getApiClient().oss();
            conversationListItemAdapter.clear();
            conversationListItemAdapter.addAll(kahlaClient.getConversationList());
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
