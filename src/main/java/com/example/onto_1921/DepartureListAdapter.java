package com.example.onto_1921;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DepartureListAdapter extends BaseAdapter {
    private List<DepartureInfo> departureInfoList;
    private LayoutInflater inflater;

    public DepartureListAdapter(Context context, List<DepartureInfo> departureInfoList) {
        this.departureInfoList = departureInfoList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return departureInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return departureInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_departure, parent, false);
            holder = new ViewHolder();
            holder.dateTextView = convertView.findViewById(R.id.dateTextView);
            holder.infoTextView = convertView.findViewById(R.id.infoTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DepartureInfo departureInfo = departureInfoList.get(position);
        holder.dateTextView.setText(departureInfo.getDate());
        holder.infoTextView.setText(departureInfo.getInfo());

        return convertView;
    }

    private static class ViewHolder {
        TextView dateTextView;
        TextView infoTextView;
    }
}
