package inu.travel.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.skp.Tmap.TMapPOIItem;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import inu.travel.R;
import inu.travel.ViewHolder.SearchPlaceViewHolder;

/**
 * Created by Inhoi on 2016. 4. 1..
 */
public class FacilityAdapter extends BaseAdapter {
    Handler handler = new Handler();  // 외부쓰레드 에서 메인 UI화면을 그릴때 사용
    private ArrayList<TMapPOIItem> placeDatas;
    LayoutInflater layoutInflater;

    public FacilityAdapter(ArrayList<TMapPOIItem> placeDatas, Context context) {
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
        // SearchPlaceViewHolder 재활용
        // 한번 더 재활용
        SearchPlaceViewHolder viewHolder = new SearchPlaceViewHolder();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_facility, parent, false);
            viewHolder.txtNameItem = (TextView) convertView.findViewById(R.id.facility_place);
            viewHolder.txtAddrItem = (TextView) convertView.findViewById(R.id.facility_addr);
            viewHolder.btnViewDetail = (Button) convertView.findViewById(R.id.btnFacilityViewDetail);
            viewHolder.imageViewThumbnail = (ImageView) convertView.findViewById(R.id.facilityThumbnail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SearchPlaceViewHolder) convertView.getTag();
        }

        final TMapPOIItem place = placeDatas.get(position);
        viewHolder.txtNameItem.setText(place.getPOIName().toString());
        viewHolder.txtAddrItem.setText(place.address);
        final SearchPlaceViewHolder finalViewHolder = viewHolder;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {    // 오래 걸릴 작업을 구현한다
                // Auto-generated method stub
                try {
                    URL url = new URL(place.bizCatName);
                    InputStream is = url.openStream();
                    final Bitmap bm = BitmapFactory.decodeStream(is);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {  // 화면에 그려줄 작업
                            finalViewHolder.imageViewThumbnail.setImageBitmap(bm);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();

        // 각 리스트 마다 버튼 이벤트
        viewHolder.btnViewDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            }
        });

        return convertView;
    }
}
