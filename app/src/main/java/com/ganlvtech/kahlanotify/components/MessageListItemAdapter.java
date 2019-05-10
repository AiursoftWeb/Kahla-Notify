package com.ganlvtech.kahlanotify.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.R;
import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.kahla.models.Message;

import java.util.ArrayList;

public class MessageListItemAdapter extends IconTitleContentArrayAdapter {
    public KahlaClient kahlaClient;
    public Conversation conversation;

    public MessageListItemAdapter(Context context) {
        super(context, new ArrayList<Message>());
    }

    @Override
    public IconTitleContent getIconTitleContentItem(int position) {
        Message message = (Message) getItem(position);
        if (message != null) {
            IconTitleContent iconTitleContent = new IconTitleContent();
            iconTitleContent.placeholderResId = R.drawable.icon_default_avatar;
            iconTitleContent.iconUrl = message.sender.getHeadImgFileUrl(kahlaClient.apiClient.oss());
            iconTitleContent.title = message.sender.nickName;
            if (conversation != null) {
                iconTitleContent.content = message.getContent(conversation.aesKey);
            } else {
                iconTitleContent.content = message.content;
            }
            iconTitleContent.unreadCount = 0;
            return iconTitleContent;
        }
        return super.getIconTitleContentItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textViewContent = view.findViewById(R.id.textViewContent);
        textViewContent.setMaxLines(Integer.MAX_VALUE);
        return view;
    }
}
