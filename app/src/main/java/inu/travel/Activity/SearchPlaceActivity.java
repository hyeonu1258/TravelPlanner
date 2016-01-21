package inu.travel.Activity;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapView;

import inu.travel.R;


public class SearchPlaceActivity extends Activity {

    private Context mContext;
    private RelativeLayout mMainRelativeLayout = null;
    private TMapView mMapView = null;

    //지도 위 버튼들
    Button btnMT;
    Button btnTour;
    Button btnEat;
    Button btnAddPlace;
    Button btnRemovePlace;
    Button btnViewDetail;
    Button btnComplete;

    public static String mApiKey = "8818efcf-6165-3d1c-a056-93025f8b06c3"; // 발급받은 appKey

    /**
     * onCreate()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_place);

        mContext = this;
//버튼들
        btnMT = (Button) findViewById(R.id.btnMT);
        btnTour = (Button) findViewById(R.id.btnTour);
        btnEat = (Button) findViewById(R.id.btnEat);
        btnAddPlace = (Button) findViewById(R.id.btnAddPlace);
        btnRemovePlace = (Button) findViewById(R.id.btnRemovePlace);
        btnViewDetail = (Button) findViewById(R.id.btnViewDetail);
        btnComplete = (Button) findViewById(R.id.btnComplete);

        btnMT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "숙박", Toast.LENGTH_SHORT).show();
            }
        });

        btnTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "관광지", Toast.LENGTH_SHORT).show();

            }
        });

        btnEat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "맛집", Toast.LENGTH_SHORT).show();

            }
        });

        btnAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "장소추가", Toast.LENGTH_SHORT).show();

            }
        });

        btnRemovePlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "장소삭제", Toast.LENGTH_SHORT).show();

            }
        });

        btnViewDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "상세보기", Toast.LENGTH_SHORT).show();

            }
        });

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "완료", Toast.LENGTH_SHORT).show();

            }
        });

//        mMapView = new TMapView(this);
//        addView(mMapView);
        mMainRelativeLayout = (RelativeLayout) findViewById(R.id.map_view);
        mMapView = new TMapView(this);
        mMainRelativeLayout.addView(mMapView);
        mMapView.setSKPMapApiKey("8818efcf-6165-3d1c-a056-93025f8b06c3"); //SDK 인증키입력

    }


}

