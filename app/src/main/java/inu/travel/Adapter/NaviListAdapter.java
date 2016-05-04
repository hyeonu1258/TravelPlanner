package inu.travel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import inu.travel.Model.NaviDescript;
import inu.travel.R;
import inu.travel.ViewHolder.NaviListViewHolder;

/**
 * Created by jingyu on 2016-04-12.
 */
public class NaviListAdapter extends BaseAdapter {
    private ArrayList<NaviDescript> itemDatas;
    LayoutInflater layoutInflater;

    public NaviListAdapter(ArrayList<NaviDescript> items, Context context) {
        this.itemDatas = items;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return itemDatas != null ? itemDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {

        return (itemDatas != null && (itemDatas.size() > position && position >= 0) ? itemDatas.get(position) : null);

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
    //public View getView(int position, View convertView, ViewGroup parent) {

        NaviListViewHolder naviListViewHolder = new NaviListViewHolder();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_navi, parent, false);
            naviListViewHolder.imgNaviMaker = (ImageView) convertView.findViewById(R.id.imgNaviMarker);
            naviListViewHolder.txtNaviDescription = (TextView) convertView.findViewById(R.id.txtNaviDesc);
            convertView.setTag(naviListViewHolder);
        } else {
            naviListViewHolder = (NaviListViewHolder)convertView.getTag();
        }

        naviListViewHolder.txtNaviDescription.setText(itemDatas.get(position).getDescription());

        return convertView;
    }
}
