package com.ganlvtech.kahlanotify.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.R;
import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.kahla.models.ContactInfo;
import com.ganlvtech.kahlanotify.kahla.models.Message;

import java.util.ArrayList;

public class MessageListItemAdapter extends IconTitleContentArrayAdapter {
    public KahlaClient mKahlaClient;
    public ContactInfo mContactInfo;

    public MessageListItemAdapter(Context context) {
        super(context, new ArrayList<Message>());
    }

    public void setKahlaClient(KahlaClient kahlaClient) {
        mKahlaClient = kahlaClient;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        mContactInfo = contactInfo;
    }

    @Override
    public IconTitleContent getIconTitleContentItem(int position) {
        Message message = (Message) getItem(position);
        if (message != null) {
            IconTitleContent iconTitleContent = new IconTitleContent();
            iconTitleContent.placeholderResId = R.drawable.icon_default_avatar;
            iconTitleContent.iconUrl = message.sender.getHeadImgFileUrl(mKahlaClient.getApiClient().oss());
            iconTitleContent.title = message.sender.nickName;
            if (mContactInfo != null) {
                iconTitleContent.content = message.getContentDecrypted(mContactInfo.aesKey);
                Message.Image image = Message.parseContentImage(iconTitleContent.content);
                if (image != null) {
                    iconTitleContent.contentImageUrl = mKahlaClient.getApiClient().oss().getDownloadFromKeyUrl(image.ossFileKey);
                }
            } else {
                iconTitleContent.content = message.content;
            }
            iconTitleContent.unreadCount = 0;
            if (mKahlaClient.getMyUserInfo() != null && message.isAt(mKahlaClient.getMyUserInfo().id)) {
                iconTitleContent.at = true;
            }
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
