package com.ganlvtech.kahlanotify.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class IconTitleContentArrayAdapter extends ArrayAdapter {
    public IconTitleContentArrayAdapter(Context context) {
        this(context, new ArrayList<IconTitleContent>());
    }

    public IconTitleContentArrayAdapter(Context context, List objects) {
        super(context, R.layout.list_view_item_icon_title_content, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_icon_title_content, null);
        ImageView imageViewIcon = view.findViewById(R.id.imageViewIcon);
        TextView textViewTitle = view.findViewById(R.id.textViewTitle);
        TextView textViewContent = view.findViewById(R.id.textViewContent);
        TextView textViewUnreadCount = view.findViewById(R.id.textViewUnreadCount);

        IconTitleContent item = (IconTitleContent) getIconTitleContentItem(position);
        if (item != null) {
            Picasso.get()
                    .load(item.iconUrl)
                    .placeholder(item.placeholderResId)
                    .into(imageViewIcon);
            textViewTitle.setText(item.title);
            textViewContent.setText(item.content);
            if (item.unreadCount <= 0) {
                textViewUnreadCount.setVisibility(View.GONE);
            } else {
                textViewUnreadCount.setVisibility(View.VISIBLE);
                textViewUnreadCount.setText(String.valueOf(item.unreadCount));
            }
        }
        return view;
    }

    public IconTitleContent getIconTitleContentItem(int position) {
        return (IconTitleContent) getItem(position);
    }
}
