package com.example.mmillward89.demtalk;

import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mark on 10/10/2015.
 */
class SpannedAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Spanned> mArticleList;

    public SpannedAdapter(Context context, ArrayList<Spanned> mArticleList) {
        mInflater = LayoutInflater.from(context);
        this.mArticleList = mArticleList;
    }

    @Override
    public int getCount() {
        return mArticleList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
                convertView = mInflater.inflate(R.layout.simplerow, null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.rowTextView);
                convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(mArticleList.get(position));

        return convertView;    }

    static class ViewHolder {
        TextView text;
    }
}
