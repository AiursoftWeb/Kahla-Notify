package com.ganlvtech.kahlanotify.components;

import android.content.Context;

import com.ganlvtech.kahlanotify.R;
import com.ganlvtech.kahlanotify.kahla.OssService;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;

import java.util.ArrayList;

public class ConversationListItemAdapter extends IconTitleContentArrayAdapter {
    public OssService ossService;

    public ConversationListItemAdapter(Context context) {
        super(context, new ArrayList<Conversation>());
    }

    @Override
    public IconTitleContent getIconTitleContentItem(int position) {
        Conversation conversation = (Conversation) getItem(position);
        if (conversation != null) {
            IconTitleContent iconTitleContent = new IconTitleContent();
            iconTitleContent.placeholderResId = R.drawable.icon_default_avatar;
            if (ossService == null) {
                iconTitleContent.iconUrl = null;
            } else {
                iconTitleContent.iconUrl = conversation.getDisplayImageUrl(ossService);
            }
            iconTitleContent.title = conversation.displayName;
            iconTitleContent.content = conversation.getLatestMessageDecryptedSignleLine();
            iconTitleContent.unreadCount = conversation.unReadAmount;
            return iconTitleContent;
        }
        return super.getIconTitleContentItem(position);
    }
}
