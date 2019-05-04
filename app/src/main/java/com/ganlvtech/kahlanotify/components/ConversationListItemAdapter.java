package com.ganlvtech.kahlanotify.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.R;
import com.ganlvtech.kahlanotify.kahla.OssService;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ConversationListItemAdapter extends ArrayAdapter {
    private final OssService ossService;

    public ConversationListItemAdapter(Context context, List<Conversation> objects) {
        super(context, R.layout.list_view_item_conversationlist, objects);
        this.ossService = new OssService();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_conversationlist, null);
        ImageView imageViewUserAvatarIcon = view.findViewById(R.id.imageViewUserAvatarIcon);
        TextView textViewUserNickname = view.findViewById(R.id.textViewUserNickname);
        TextView textViewContent = view.findViewById(R.id.textViewContent);

        Conversation conversation = (Conversation) getItem(position);
        if (conversation != null) {
            textViewUserNickname.setText(conversation.displayName);
            textViewContent.setText(conversation.latestMessageDecrypted());
            Picasso.get().load(ossService.getDownloadFromKeyUrl(conversation.displayImageKey, 100, 100)).into(imageViewUserAvatarIcon);
        }
        return view;
    }
}
