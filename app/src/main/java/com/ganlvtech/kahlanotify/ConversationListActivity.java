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
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.legacy.MainActivity;
import com.ganlvtech.kahlanotify.util.ConversationListActivitySharedPreferences;
import com.jaeger.library.StatusBarUtil;

import java.util.List;

public class ConversationListActivity extends Activity {
    private DrawerLayout drawerLayoutConversationListActivity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listViewConversations;
    private ListView listViewAccounts;
    private TextView toolbalTextViewTitle;
    private TextView toolbalTextViewSubtitle;
    private TextView textViewLegacy;
    private TextView textViewNewAccount;
    private MyService myService;
    private ConversationListItemAdapter conversationListItemAdapter;
    private AccountListItemAdapter accountListItemAdapter;
    private KahlaClient kahlaClient;
    private ConversationListActivitySharedPreferences conversationListActivitySharedPreferences;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.ServiceBinder serviceBinder = (MyService.ServiceBinder) service;
            myService = serviceBinder.getService();
            ConversationListActivity.this.onServiceConnected();
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
        drawerLayoutConversationListActivity = findViewById(R.id.drawerLayoutConversationListActivity);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        toolbalTextViewTitle = findViewById(R.id.toolbalTextViewTitle);
        toolbalTextViewSubtitle = findViewById(R.id.toolbalTextViewSubtitle);
        listViewConversations = findViewById(R.id.listViewConversations);
        listViewAccounts = findViewById(R.id.listViewAccounts);
        textViewLegacy = findViewById(R.id.textViewLegacy);
        textViewNewAccount = findViewById(R.id.textViewNewAccount);

        StatusBarUtil.setColorNoTranslucentForDrawerLayout(this, drawerLayoutConversationListActivity, getColor(R.color.main_theme));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (kahlaClient != null) {
                    kahlaClient.fetchConversationListAsync();
                }
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
        listViewConversations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Conversation conversation = (Conversation) conversationListItemAdapter.getItem(position);
                assert conversation != null;
                startConversationActivity(conversation.conversationId);
            }
        });
        accountListItemAdapter = new AccountListItemAdapter(ConversationListActivity.this);
        listViewAccounts.setAdapter(accountListItemAdapter);
        listViewAccounts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                kahlaClient = (KahlaClient) accountListItemAdapter.getItem(position);
                assert kahlaClient != null;
                setCurrentKahlaClient(kahlaClient);
            }
        });
        listViewAccounts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                kahlaClient = (KahlaClient) accountListItemAdapter.getItem(position);
                assert kahlaClient != null;
                signOut(kahlaClient);
                return true;
            }
        });

        conversationListActivitySharedPreferences = new ConversationListActivitySharedPreferences(this);
        conversationListActivitySharedPreferences.load();
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

    private void onServiceConnected() {
        updateKahlaClientList();
        updateConversationList();
        swipeRefreshLayout.setRefreshing(true);
        List<KahlaClient> kahlaClientList = myService.getKahlaClientList();
        for (KahlaClient kahlaClient : kahlaClientList) {
            kahlaClient.fetchConversationListAsync();
            kahlaClient.fetchUserInfoAsync();
        }
    }

    private void updateKahlaClientList() {
        List<KahlaClient> kahlaClientList = myService.getKahlaClientList();
        if (kahlaClientList.isEmpty()) {
            startLoginActivity(false);
        } else {
            for (KahlaClient kahlaClient : kahlaClientList) {
                kahlaClient.mainThreadHandler = new MyHandler(Looper.getMainLooper(), this, kahlaClient);
            }
            accountListItemAdapter.clear();
            accountListItemAdapter.addAll(kahlaClientList);
            if (kahlaClient == null) {
                kahlaClient = conversationListActivitySharedPreferences.findKahlaClient(kahlaClientList);
                if (kahlaClient == null) {
                    kahlaClient = kahlaClientList.get(0);
                    conversationListActivitySharedPreferences.putKahlaClient(kahlaClient);
                    conversationListActivitySharedPreferences.save();
                }
            }
        }
    }

    private void updateConversationList() {
        updateConversationList(kahlaClient);
    }

    private void updateConversationList(@NonNull KahlaClient kahlaClient) {
        updateConversationList(kahlaClient, false);
        if (this.kahlaClient == kahlaClient) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setCurrentKahlaClient(@NonNull KahlaClient kahlaClient) {
        updateConversationList(kahlaClient, true);
        conversationListActivitySharedPreferences.putKahlaClient(kahlaClient);
        conversationListActivitySharedPreferences.save();
    }

    private void updateConversationList(@NonNull KahlaClient kahlaClient, boolean isSetCurrentKahlaClient) {
        if (isSetCurrentKahlaClient) {
            this.kahlaClient = kahlaClient;
        } else if (this.kahlaClient != kahlaClient) {
            return;
        }
        if (kahlaClient.userInfo != null) {
            toolbalTextViewTitle.setText(kahlaClient.userInfo.nickName);
        } else {
            toolbalTextViewTitle.setText(kahlaClient.email);
        }
        toolbalTextViewSubtitle.setText(kahlaClient.baseUrl);
        conversationListItemAdapter.ossService = kahlaClient.apiClient.oss();
        conversationListItemAdapter.clear();
        if (kahlaClient.conversationList != null) {
            conversationListItemAdapter.addAll(kahlaClient.conversationList);
        }
        swipeRefreshLayout.setRefreshing(false);
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

    private void startConversationActivity(int conversationId) {
        Intent intent = new Intent(ConversationListActivity.this, ConversationActivity.class);
        intent.putExtra("conversationId", conversationId);
        startActivity(intent);
    }

    private void signOut(@NonNull final KahlaClient kahlaClient) {
        new AlertDialog.Builder(ConversationListActivity.this)
                .setTitle("Sign out")
                .setMessage("Are you sure to sign out?")
                .setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        kahlaClient.quitSafely();
                        myService.removeKahlaClient(kahlaClient);
                        updateKahlaClientList();
                        updateConversationList();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class MyHandler extends Handler {
        private ConversationListActivity conversationListActivity;
        private KahlaClient kahlaClient;

        MyHandler(Looper looper, ConversationListActivity conversationListActivity, KahlaClient kahlaClient) {
            super(looper);
            this.conversationListActivity = conversationListActivity;
            this.kahlaClient = kahlaClient;
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case KahlaClient.MESSAGE_WHAT_FETCH_USER_INFO_RESPONSE:
                    conversationListActivity.updateKahlaClientList();
                    conversationListActivity.updateConversationList();
                    break;
                case KahlaClient.MESSAGE_WHAT_FETCH_CONVERSATION_LIST_RESPONSE:
                    conversationListActivity.updateKahlaClientList();
                    conversationListActivity.updateConversationList(kahlaClient);
                    break;
            }
        }
    }
}
