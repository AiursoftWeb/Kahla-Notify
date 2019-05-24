package com.ganlvtech.kahlanotify;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.components.AccountListItemAdapter;
import com.ganlvtech.kahlanotify.components.ContactInfoListItemAdapter;
import com.ganlvtech.kahlanotify.kahla.WebSocketClient;
import com.ganlvtech.kahlanotify.kahla.event.BaseEvent;
import com.ganlvtech.kahlanotify.kahla.models.ContactInfo;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;
import com.ganlvtech.kahlanotify.util.ConversationListActivitySharedPreferences;
import com.jaeger.library.StatusBarUtil;

import java.util.List;

public class ConversationListActivity extends MyServiceActivity {
    private DrawerLayout mDrawerLayoutConversationListActivity;
    private ActionBarDrawerToggle mDrawerToggle;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListViewConversations;
    private ListView mListViewAccounts;
    private Toolbar mToolbar;
    private TextView mToolbalTextViewTitle;
    private TextView mToolbalTextViewSubtitle;
    private TextView mTextViewNewAccount;
    private ContactInfoListItemAdapter mContactInfoListItemAdapter;
    private AccountListItemAdapter mAccountListItemAdapter;
    @Nullable
    private KahlaClient mKahlaClient;
    private ConversationListActivitySharedPreferences mConversationListActivitySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        mDrawerLayoutConversationListActivity = findViewById(R.id.drawerLayoutConversationListActivity);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mToolbalTextViewTitle = findViewById(R.id.toolbarTextViewTitle);
        mToolbalTextViewSubtitle = findViewById(R.id.toolbarTextViewSubtitle);
        mListViewConversations = findViewById(R.id.listViewConversations);
        mListViewAccounts = findViewById(R.id.listViewAccounts);
        mTextViewNewAccount = findViewById(R.id.textViewNewAccount);
        mToolbar = findViewById(R.id.toolbar);

        StatusBarUtil.setColorNoTranslucentForDrawerLayout(this, mDrawerLayoutConversationListActivity, getColor(R.color.main_theme));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayoutConversationListActivity,R.string.Menu,R.string.Menu);
        mDrawerLayoutConversationListActivity.addDrawerListener(mDrawerToggle);

        mSwipeRefreshLayout.setColorSchemeColors(getColor(R.color.main_theme));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mKahlaClient != null) {
                    mKahlaClient.fetchContactInfoList();
                }
            }
        });

        mTextViewNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginActivity(true);
            }
        });
        mContactInfoListItemAdapter = new ContactInfoListItemAdapter(ConversationListActivity.this);
        mListViewConversations.setAdapter(mContactInfoListItemAdapter);
        mListViewConversations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactInfo conversation = (ContactInfo) mContactInfoListItemAdapter.getItem(position);
                assert conversation != null;
                startConversationActivity(conversation.conversationId);
            }
        });
        mAccountListItemAdapter = new AccountListItemAdapter(ConversationListActivity.this);
        mListViewAccounts.setAdapter(mAccountListItemAdapter);
        mListViewAccounts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KahlaClient kahlaClient = (KahlaClient) mAccountListItemAdapter.getItem(position);
                assert kahlaClient != null;
                setCurrentKahlaClient(kahlaClient);
            }
        });
        mListViewAccounts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                KahlaClient kahlaClient = (KahlaClient) mAccountListItemAdapter.getItem(position);
                assert kahlaClient != null;
                signOut(kahlaClient);
                return true;
            }
        });

        mConversationListActivitySharedPreferences = new ConversationListActivitySharedPreferences(this);
        mConversationListActivitySharedPreferences.load();
    }

    protected void onServiceConnected() {
        updateKahlaClientList();
        mSwipeRefreshLayout.setRefreshing(true);
        assert mMyService != null;
        List<KahlaClient> kahlaClientList = mMyService.getKahlaClientList();
        for (final KahlaClient kahlaClient : kahlaClientList) {
            kahlaClient.setOnFetchContactInfoListResponseListener(new KahlaClient.OnFetchContactInfoListResponseListener() {
                @Override
                public void onFetchContactInfoListResponse(MyFriendsResponse meResponse, final KahlaClient kahlaClient) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateKahlaClientList();
                            if (kahlaClient == mKahlaClient) {
                                updateContactInfoList();
                            }
                        }
                    });
                }
            });
            kahlaClient.setOnFetchMyUserInfoResponseListener(new KahlaClient.OnFetchMyUserInfoResponseListener() {
                @Override
                public void onFetchMyUserInfoResponse(MeResponse meResponse, KahlaClient kahlaClient) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateKahlaClientList();
                        }
                    });
                }
            });
            kahlaClient.connectToPusher();
            kahlaClient.setWebSocketClientOnDecodedMessageListener(new WebSocketClient.OnDecodedMessageListener() {
                @Override
                public void onDecodedMessage(BaseEvent event) {
                    if (kahlaClient == mKahlaClient) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(true);
                            }
                        });
                        mKahlaClient.fetchContactInfoList();
                    }
                }
            });
            kahlaClient.fetchContactInfoList();
            kahlaClient.fetchMyUserInfo();
        }
    }

    private void updateKahlaClientList() {
        assert mMyService != null;
        List<KahlaClient> kahlaClientList = mMyService.getKahlaClientList();
        if (kahlaClientList.isEmpty()) {
            startLoginActivity(false);
            return;
        }
        mAccountListItemAdapter.clear();
        mAccountListItemAdapter.addAll(kahlaClientList);
        if (mKahlaClient == null) {
            KahlaClient kahlaClient = mMyService.getKahlaClientByServerEmail(mConversationListActivitySharedPreferences.server, mConversationListActivitySharedPreferences.email);
            if (kahlaClient == null) {
                kahlaClient = kahlaClientList.get(0);
            }
            setCurrentKahlaClient(kahlaClient);
        }
    }

    private void setCurrentKahlaClient(@NonNull KahlaClient kahlaClient) {
        mKahlaClient = kahlaClient;
        if (kahlaClient.getMyUserInfo() != null) {
            mToolbalTextViewTitle.setText(kahlaClient.getMyUserInfo().nickName);
        } else {
            mToolbalTextViewTitle.setText(kahlaClient.getEmail());
        }
        mToolbalTextViewSubtitle.setText(kahlaClient.getServer());
        updateContactInfoList();
        mConversationListActivitySharedPreferences.putKahlaClient(kahlaClient);
        mConversationListActivitySharedPreferences.save();
    }

    private void updateContactInfoList() {
        if (mKahlaClient == null) {
            return;
        }
        mContactInfoListItemAdapter.ossService = mKahlaClient.getApiClient().oss();
        mContactInfoListItemAdapter.clear();
        if (mKahlaClient.getContactInfoList() != null) {
            mContactInfoListItemAdapter.addAll(mKahlaClient.getContactInfoList());
        }
        mSwipeRefreshLayout.setRefreshing(false);
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
        intent.putExtra(ConversationActivity.INTENT_EXTRA_NAME_CONVERSATION_ID, conversationId);
        startActivity(intent);
    }

    private void signOut(@NonNull final KahlaClient kahlaClient) {
        new AlertDialog.Builder(this)
                .setTitle("Sign out")
                .setMessage("Are you sure to sign out?")
                .setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMyService.removeKahlaClient(kahlaClient);
                        updateKahlaClientList();
                        updateContactInfoList();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
