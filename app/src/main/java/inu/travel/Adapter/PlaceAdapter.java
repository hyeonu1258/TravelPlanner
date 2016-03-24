package inu.travel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import inu.travel.Model.Place;
import inu.travel.Model.PlaceList;
import inu.travel.R;
import inu.travel.ViewHolder.PlaceViewHolder;

/**
 * Created by Hyeonu on 2016-03-09.
 */
public class PlaceAdapter extends BaseAdapter {

    private ArrayList<Place> placeDatas;
    LayoutInflater layoutInflater;

    public PlaceAdapter(ArrayList<Place> placeDatas, Context context) {
        this.placeDatas = placeDatas;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setSource(ArrayList<Place> placeDatas) {
        this.placeDatas = placeDatas;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return placeDatas != null ? placeDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (placeDatas != null && (placeDatas.size() > position && position >= 0) ? placeDatas.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        PlaceViewHolder viewHolder = new PlaceViewHolder();
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_place, parent, false);
            viewHolder.txtNameItem = (TextView) convertView.findViewById(R.id.textView_place);
            viewHolder.txtAddrItem = (TextView) convertView.findViewById(R.id.textView_addr);
            viewHolder.btnRemove = (Button) convertView.findViewById(R.id.btnRemoveListPlace);
            viewHolder.btnDetail = (Button) convertView.findViewById(R.id.btnDetailListPlace);

            viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
                }
            });
            viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
                }
            });
            viewHolder.imgItem = (ImageView) convertView.findViewById(R.id.imageViewPlace);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PlaceViewHolder) convertView.getTag();
        }

        Place place = placeDatas.get(position);
        viewHolder.txtNameItem.setText(place.getPlacename().toString());
        viewHolder.txtAddrItem.setText(place.getAddress().toString());
//        viewHolder.btnRemove.setText(place.getAddress().toString());
//        viewHolder.btnDetail.setText(place.getAddress().toString());
//        viewHolder.imgItem.setText(place.getAddress().toString());

        return convertView;
    }


}
