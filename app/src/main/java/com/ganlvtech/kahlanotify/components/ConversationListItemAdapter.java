package com.ganlvtech.kahlanotify.components;

import android.content.Context;
import android.support.annotation.NonNull;
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

public class ConversationListItemAdapter extends ArrayAdapter {
    public OssService ossService;

    public ConversationListItemAdapter(Context context) {
        super(context, R.layout.list_view_item_icon_title_content);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_icon_title_content, null);
        ImageView imageViewIcon = view.findViewById(R.id.imageViewIcon);
        TextView textViewTitle = view.findViewById(R.id.textViewTitle);
        TextView textViewContent = view.findViewById(R.id.textViewContent);

        Conversation conversation = (Conversation) getItem(position);
        if (conversation != null) {
            textViewTitle.setText(conversation.displayName);
            textViewContent.setText(conversation.getLatestMessageDecryptedSignleLine());
            if (ossService == null) {
                Picasso.get()
                        .load(R.drawable.icon_default_avatar)
                        .into(imageViewIcon);
            } else {
                Picasso.get()
                        .load(conversation.getDisplayImageUrl(ossService))
                        .placeholder(R.drawable.icon_default_avatar)
                        .into(imageViewIcon);
            }
        }
        return view;
    }
}
