package inu.travel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.skp.Tmap.TMapPOIItem;

import java.util.ArrayList;

import inu.travel.R;
import inu.travel.ViewHolder.SearchPlaceViewHolder;

/**
 * Created by kimjongmin on 2016. 3. 21..
 */
public class SearchPlaceAdapter extends BaseAdapter {

    private ArrayList<TMapPOIItem> placeDatas;
    LayoutInflater layoutInflater;

    public SearchPlaceAdapter(ArrayList<TMapPOIItem> placeDatas, Context context) {
        this.placeDatas = placeDatas;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setSource(ArrayList<TMapPOIItem> placeDatas) {
        this.placeDatas = placeDatas;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() { return placeDatas != null ? placeDatas.size() : 0; }

    @Override
    public Object getItem(int position) {
        return (placeDatas != null && (placeDatas.size() > position && position >= 0) ? placeDatas.get(position) : null);
    }

    @Override
    public long getItemId(int position) { return 0; }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        SearchPlaceViewHolder viewHolder = new SearchPlaceViewHolder();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_searchplace, parent, false);
            viewHolder.txtNameItem = (TextView) convertView.findViewById(R.id.textView_place);
            viewHolder.txtAddrItem = (TextView) convertView.findViewById(R.id.textView_addr);
            viewHolder.btnAddPlace = (Button) convertView.findViewById(R.id.btnAddPlace);
            viewHolder.btnViewDetail = (Button) convertView.findViewById(R.id.btnViewDetail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SearchPlaceViewHolder) convertView.getTag();
        }

        TMapPOIItem place = placeDatas.get(position);
        viewHolder.txtNameItem.setText(place.getPOIName().toString());
        viewHolder.txtAddrItem.setText(place.address);

        // 각 리스트 마다 버튼 이벤트
        viewHolder.btnAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });

        viewHolder.btnViewDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });

        return convertView;
    }
}
