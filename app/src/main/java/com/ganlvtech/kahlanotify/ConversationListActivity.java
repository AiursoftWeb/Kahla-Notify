package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.components.AccountListItemAdapter;
import com.ganlvtech.kahlanotify.components.ConversationListItemAdapter;
import com.ganlvtech.kahlanotify.legacy.MainActivity;
import com.ganlvtech.kahlanotify.util.LastAccountSharedPreferences;

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
    private KahlaClient kahlaClient;
    private MyHandler handler;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.ServiceBinder serviceBinder = (MyService.ServiceBinder) service;
            myService = serviceBinder.getService();
            loadKahlaClientList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };

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
                // TODO
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
                startLoginActivity(true);
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
        listViewAccounts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                kahlaClient = (KahlaClient) accountListItemAdapter.getItem(position);
                signOut(kahlaClient);
                return true;
            }
        });

        handler = new MyHandler(Looper.getMainLooper(), this);
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
        if (kahlaClientList.isEmpty()) {
            startLoginActivity(false);
        } else {
            for (KahlaClient kahlaClient : kahlaClientList) {
                kahlaClient.mainThreadHandler = handler;
            }
            accountListItemAdapter.clear();
            accountListItemAdapter.addAll(kahlaClientList);
        }
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
            conversationListItemAdapter.addAll(kahlaClient.conversationList);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void startLoginActivity(boolean isSecondAccount) {
        Intent intent = new Intent(ConversationListActivity.this, LoginActivity.class);
        if (isSecondAccount) {
            intent.putExtra("isSecondAccount", true);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        }
        startActivity(intent);
        if (!isSecondAccount) {
            finish();
        }
    }

    private void signOut(@NonNull final KahlaClient kahlaClient) {
        new AlertDialog.Builder(ConversationListActivity.this)
                .setTitle("Sign out")
                .setMessage("Are you sure to sign out?")
                .setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        kahlaClient.gracefulShutdown();
                        myService.removeKahlaClient(kahlaClient);
                        loadCurrentAccountConversationList();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class MyHandler extends Handler {
        private KahlaClient kahlaClient;
        private ConversationListActivity conversationListActivity;

        MyHandler(Looper looper, ConversationListActivity conversationListActivity) {
            super(looper);
            this.conversationListActivity = conversationListActivity;
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case KahlaClient.MESSAGE_WHAT_AUTH_ME_RESPONSE:
                    conversationListActivity.loadKahlaClientList();
                    break;
                case KahlaClient.MESSAGE_WHAT_FRIENDSHIP_GET_FRIENDS_RESPONSE:
                    conversationListActivity.loadCurrentAccountConversationList();
                    break;
                default:
                    // throw new AssertionError("Unknown handler message received: " + msg.what);
            }
        }
    }
}
