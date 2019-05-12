package com.ganlvtech.kahlanotify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.components.MessageListItemAdapter;
import com.ganlvtech.kahlanotify.kahla.lib.CryptoJs;
import com.ganlvtech.kahlanotify.kahla.models.ContactInfo;
import com.ganlvtech.kahlanotify.kahla.models.Message;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.GetMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.SendMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;
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
    private KahlaClient mKahlaClient;
    @Nullable
    private ContactInfo mContactInfo;
    private ClipboardManager mClipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load shared preferences
        mConversationListActivitySharedPreferences = new ConversationListActivitySharedPreferences(this);
        mConversationListActivitySharedPreferences.load();

        // Get intent extras
        mConversationId = getIntent().getIntExtra(INTENT_EXTRA_NAME_CONVERSATION_ID, 0);

        // Get ClipboardManager
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

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
                if (mKahlaClient != null) {
                    mKahlaClient.fetchMessage(mConversationId);
                }
            }
        });

        mMessageListItemAdapter = new MessageListItemAdapter(this);
        mListViewConversations.setAdapter(mMessageListItemAdapter);
        mListViewConversations.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = (Message) mMessageListItemAdapter.getItem(position);
                if (message != null) {
                    copyMessage(message);
                }
                return true;
            }
        });

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextSend.getText().toString();
                try {
                    if (mContactInfo != null && mKahlaClient != null) {
                        String content = CryptoJs.aesEncrypt(message.getBytes("UTF-8"), mContactInfo.aesKey);
                        mKahlaClient.setOnSendMessageResponseListener(new KahlaClient.OnSendMessageResponseListener() {
                            @Override
                            public void onSendMessageResponse(SendMessageResponse sendMessageResponse, KahlaClient kahlaClient) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mEditTextSend.setText("");
                                        mButtonSend.setText(getString(R.string.send));
                                        mButtonSend.setEnabled(true);
                                    }
                                });
                            }
                        });
                        mKahlaClient.setOnSendMessageFailureListener(new KahlaClient.OnFailureListener() {
                            @Override
                            public void onFailure(Exception e, KahlaClient kahlaClient) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mButtonSend.setText(getString(R.string.send));
                                        mButtonSend.setEnabled(true);
                                    }
                                });
                            }
                        });
                        mButtonSend.setText(getString(R.string.sending___));
                        mButtonSend.setEnabled(false);
                        mKahlaClient.sendMessage(mConversationId, content);
                    }
                } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        assert mMyService != null;
        List<KahlaClient> kahlaClientList = mMyService.getKahlaClientList();
        mKahlaClient = mConversationListActivitySharedPreferences.findKahlaClient(kahlaClientList);
        if (mKahlaClient == null) {
            finish();
            return;
        }
        mContactInfo = mKahlaClient.getConversationById(mConversationId);
        if (mContactInfo == null) {
            finish();
            return;
        }
        mContactInfo.unReadAmount = 0;
        updateTitle();
        updateMessageList();
        mKahlaClient.setOnFetchContactInfoListResponseListener(new KahlaClient.OnFetchContactInfoListResponseListener() {
            @Override
            public void onFetchContactInfoListResponse(MyFriendsResponse meResponse, KahlaClient kahlaClient) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTitle();
                    }
                });
            }
        });
        mKahlaClient.setOnFetchMyUserInfoResponseListener(new KahlaClient.OnFetchMyUserInfoResponseListener() {
            @Override
            public void onFetchMyUserInfoResponse(MeResponse meResponse, KahlaClient kahlaClient) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTitle();
                    }
                });
            }
        });
        if (mKahlaClient.getMyUserInfo() == null) {
            mKahlaClient.fetchMyUserInfo();
        }
        mKahlaClient.setOnFetchMessageResponseListener(new KahlaClient.OnFetchMessageResponseListener() {
            @Override
            public void onFetchMessageResponse(GetMessageResponse getMessageResponse, KahlaClient kahlaClient) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMessageList();
                    }
                });
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);
        mKahlaClient.fetchMessage(mConversationId);
    }

    private void updateTitle() {
        if (mContactInfo != null) {
            mToolbarTextViewTitle.setText(mContactInfo.displayName);
        }
        if (mKahlaClient != null) {
            if (mKahlaClient.getMyUserInfo() != null) {
                mToolbarTextViewSubtitle.setText(String.format("%s %s", mKahlaClient.getMyUserInfo().nickName, mKahlaClient.getServer()));
            } else {
                mToolbarTextViewSubtitle.setText(String.format("%s %s", mKahlaClient.getEmail(), mKahlaClient.getServer()));
            }
        }
    }

    private void updateMessageList() {
        mMessageListItemAdapter.setKahlaClient(mKahlaClient);
        mMessageListItemAdapter.setContactInfo(mContactInfo);
        mMessageListItemAdapter.clear();
        if (mKahlaClient != null) {
            List<Message> messageList = mKahlaClient.getConversationMessageMap().get(mConversationId);
            if (messageList != null) {
                mMessageListItemAdapter.addAll(messageList);
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void copyMessage(Message message) {
        if (mContactInfo != null) {
            mClipboardManager.setPrimaryClip(ClipData.newPlainText("Kahla Notify Message", message.getContentDecrypted(mContactInfo.aesKey)));
        }
        Toast.makeText(this, "Message has been copied", Toast.LENGTH_SHORT).show();
    }
}
