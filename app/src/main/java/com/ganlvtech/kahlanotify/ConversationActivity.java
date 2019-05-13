package com.ganlvtech.kahlanotify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.components.IconTitleContent;
import com.ganlvtech.kahlanotify.components.MessageListItemAdapter;
import com.ganlvtech.kahlanotify.kahla.WebSocketClient;
import com.ganlvtech.kahlanotify.kahla.event.BaseEvent;
import com.ganlvtech.kahlanotify.kahla.event.NewMessageEvent;
import com.ganlvtech.kahlanotify.kahla.event.TimerUpdatedEvent;
import com.ganlvtech.kahlanotify.kahla.models.ContactInfo;
import com.ganlvtech.kahlanotify.kahla.models.Message;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.GetMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.SendMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;
import com.ganlvtech.kahlanotify.util.ConversationListActivitySharedPreferences;
import com.ganlvtech.kahlanotify.util.SelectedFileInfo;

import java.util.List;

public class ConversationActivity extends MyServiceActivity {
    public static final String INTENT_EXTRA_NAME_SERVER = "server";
    public static final String INTENT_EXTRA_NAME_EMAIL = "email";
    public static final String INTENT_EXTRA_NAME_CONVERSATION_ID = "conversationId";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_FILE_REQUEST = PICK_IMAGE_REQUEST + 1;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mToolbarTextViewTitle;
    private TextView mToolbarTextViewSubtitle;
    private ListView mListViewConversations;
    private EditText mEditTextSend;
    private Button mButtonSendImage;
    private Button mButtonSendFile;
    private Button mButtonSend;
    private MessageListItemAdapter mMessageListItemAdapter;
    private ConversationListActivitySharedPreferences mConversationListActivitySharedPreferences;
    private String mServer;
    private String mEmail;
    private int mConversationId;
    @Nullable
    private KahlaClient mKahlaClient;
    @Nullable
    private ContactInfo mContactInfo;
    private ClipboardManager mClipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent extras
        mServer = getIntent().getStringExtra(INTENT_EXTRA_NAME_SERVER);
        mEmail = getIntent().getStringExtra(INTENT_EXTRA_NAME_EMAIL);
        if (mServer == null || mEmail == null) {
            // Load shared preferences
            mConversationListActivitySharedPreferences = new ConversationListActivitySharedPreferences(this);
            mConversationListActivitySharedPreferences.load();
            mServer = mConversationListActivitySharedPreferences.server;
            mEmail = mConversationListActivitySharedPreferences.email;
        }
        mConversationId = getIntent().getIntExtra(INTENT_EXTRA_NAME_CONVERSATION_ID, 0);

        // Get ClipboardManager
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        setContentView(R.layout.activity_conversation);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mToolbarTextViewTitle = findViewById(R.id.toolbarTextViewTitle);
        mToolbarTextViewSubtitle = findViewById(R.id.toolbarTextViewSubtitle);
        mListViewConversations = findViewById(R.id.listViewConversations);
        mEditTextSend = findViewById(R.id.editTextSend);
        mButtonSendImage = findViewById(R.id.buttonSendImage);
        mButtonSendFile = findViewById(R.id.buttonSendFile);
        mButtonSend = findViewById(R.id.buttonSend);

        mSwipeRefreshLayout.setColorSchemeColors(getColor(R.color.main_theme));
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
        mListViewConversations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IconTitleContent iconTitleContentItem = mMessageListItemAdapter.getIconTitleContentItem(position);
                if (iconTitleContentItem != null) {
                    showImage(iconTitleContentItem);
                }
            }
        });
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

        mEditTextSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mButtonSendImage.setVisibility(View.GONE);
                    mButtonSendFile.setVisibility(View.GONE);
                    mButtonSend.setVisibility(View.VISIBLE);
                } else {
                    mButtonSendImage.setVisibility(View.VISIBLE);
                    mButtonSendFile.setVisibility(View.VISIBLE);
                    mButtonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mButtonSendImage.setVisibility(View.VISIBLE);
        mButtonSendFile.setVisibility(View.VISIBLE);
        mButtonSend.setVisibility(View.GONE);
        mButtonSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
        mButtonSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("file/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
            }
        });
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextSend.getText().toString();
                if (mContactInfo != null && mKahlaClient != null) {
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
                    mKahlaClient.sendMessageAutoEncrypt(mConversationId, message);
                }
            }
        });
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        assert mMyService != null;
        mKahlaClient = mMyService.getKahlaClientByServerEmail(mServer, mEmail);
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
        mKahlaClient.connectToPusher();
        mKahlaClient.setWebSocketClientOnDecodedMessageListener(new WebSocketClient.OnDecodedMessageListener() {
            @Override
            public void onDecodedMessage(BaseEvent event) {
                switch (event.type) {
                    case BaseEvent.EVENT_TYPE_NEW_MESSAGE:
                        NewMessageEvent newMessageEvent = (NewMessageEvent) event;
                        if (newMessageEvent.conversationId == mConversationId) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSwipeRefreshLayout.setRefreshing(true);
                                }
                            });
                            mKahlaClient.fetchMessage(mConversationId);
                        }
                        break;
                    case BaseEvent.EVENT_TYPE_TIMER_UPDATED:
                        TimerUpdatedEvent timerUpdatedEvent = (TimerUpdatedEvent) event;
                        if (timerUpdatedEvent.conversationID == mConversationId) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSwipeRefreshLayout.setRefreshing(true);
                                }
                            });
                            mKahlaClient.fetchMessage(mConversationId);
                        }
                        break;
                }
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);
        mKahlaClient.fetchMessage(mConversationId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            switch (requestCode) {
                case PICK_IMAGE_REQUEST: {
                    if (mKahlaClient != null) {
                        Uri uri = data.getData();
                        SelectedFileInfo selectedFileInfo = new SelectedFileInfo(this, uri);
                        selectedFileInfo.decodeImage();
                        mKahlaClient.sendMedia(mConversationId, selectedFileInfo.bytes, selectedFileInfo.displayName, selectedFileInfo.width, selectedFileInfo.height);
                    }
                }
                break;
                case PICK_FILE_REQUEST: {
                    if (mKahlaClient != null) {
                        Uri uri = data.getData();
                        SelectedFileInfo selectedFileInfo = new SelectedFileInfo(this, uri);
                        mKahlaClient.sendFile(mConversationId, selectedFileInfo.bytes, selectedFileInfo.displayName);
                    }
                }
                break;
            }
        }
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
        mListViewConversations.setSelection(mMessageListItemAdapter.getCount() - 1);
    }

    private void copyMessage(Message message) {
        if (mContactInfo != null) {
            mClipboardManager.setPrimaryClip(ClipData.newPlainText("Kahla Notify Message", message.getContentDecrypted(mContactInfo.aesKey)));
        }
        Toast.makeText(this, "Message has been copied", Toast.LENGTH_SHORT).show();
    }

    private void showImage(IconTitleContent iconTitleContentItem) {
        if (iconTitleContentItem.contentImageUrl.length() > 0) {
            Intent intent = new Intent(this, ImageActivity.class);
            intent.putExtra(ImageActivity.INTENT_EXTRA_NAME_IMAGE_URL, iconTitleContentItem.contentImageUrl);
            startActivity(intent);
        }
    }
}
