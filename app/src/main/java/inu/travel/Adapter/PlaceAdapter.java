package inu.travel.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import inu.travel.Model.Font;
import inu.travel.Model.Place;
import inu.travel.Model.PlaceList;
import inu.travel.R;
import inu.travel.ViewHolder.PlaceViewHolder;
import inu.travel.ViewHolder.SearchPlaceViewHolder;

/**
 * Created by Hyeonu on 2016-03-09.
 */
public class PlaceAdapter extends BaseAdapter {
    Handler handler = new Handler();  // 외부쓰레드 에서 메인 UI화면을 그릴때 사용
    private ArrayList<Place> placeDatas;
    LayoutInflater layoutInflater;
    Context context;

    public PlaceAdapter(ArrayList<Place> placeDatas, Context context) {
        this.placeDatas = placeDatas;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
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
            viewHolder.savedListThumbnail = (ImageView) convertView.findViewById(R.id.savedListThumbnail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PlaceViewHolder) convertView.getTag();
        }

//        Font.setGlobalFont(context, convertView);
//        viewHolder.txtNameItem.setTypeface(Typeface.createFromAsset(context.getAssets(), "NanumBrush.ttf"));
//        viewHolder.txtAddrItem.setTypeface(Typeface.createFromAsset(context.getAssets(), "NanumBrush.ttf"));
        final Place place = placeDatas.get(position);
        viewHolder.txtNameItem.setText(place.getPlacename().toString());
        viewHolder.txtAddrItem.setText(place.getAddress().toString());
        final PlaceViewHolder finalViewHolder = viewHolder;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {    // 오래 걸릴 작업을 구현한다
                // Auto-generated method stub
                try {
                    URL url = new URL(place.imgpath);
                    InputStream is = url.openStream();
                    final Bitmap bm = BitmapFactory.decodeStream(is);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {  // 화면에 그려줄 작업
                            finalViewHolder.savedListThumbnail.setImageBitmap(bm);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

        return convertView;
    }


}
