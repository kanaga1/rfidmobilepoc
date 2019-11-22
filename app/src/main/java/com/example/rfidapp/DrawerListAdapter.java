package com.example.rfidapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DrawerListAdapter extends ArrayAdapter {
    Context mContext;
    List<DrawerListContent.DrawerItem> mData = null;

    /**
     * Construtor. Handles the initialization.
     *
     * @param context  - context to be used
     * @param resource - layout to be inflated
     * @param objects  - navidation drawer items
     */
    public DrawerListAdapter(Context context, int resource, List<DrawerListContent.DrawerItem> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mData = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
        }

        DrawerListContent.DrawerItem item = mData.get(position);
        //Set the label
        TextView label1 = (TextView) convertView.findViewById(R.id.drawerItemName);
        label1.setText(item.content);
        ImageView icon = (ImageView) convertView.findViewById(R.id.drawerIcon);
        //Set icon
        icon.setImageResource(item.icon);
        return convertView;
    }
}
