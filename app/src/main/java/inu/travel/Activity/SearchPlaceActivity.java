package inu.travel.Activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import inu.travel.Component.ApplicationController;
import inu.travel.Network.TourNetworkService;
import inu.travel.R;


public class SearchPlaceActivity extends Activity {

    private Context mContext;
    private RelativeLayout mMainRelativeLayout = null;
    private TMapView mMapView = null;
    private TourNetworkService tourNetworkService;
    private String searchContent; // 검색한 내용
    private String mapX, mapY; //투어 API로 보낼 좌표


    //지도 위 버튼들
    Button btnMT;
    Button btnTour;
    Button btnEat;
    Button btnAddPlace;
    Button btnRemovePlace;
    Button btnViewDetail;
    Button btnComplete;

    Button btnSearch;

    //검색바
    EditText editSearch;


    /**
     * onCreate()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_place);
        initNetworkService();
        initView();
        btnClickEvent();

        mContext = this;

        mMainRelativeLayout = (RelativeLayout) findViewById(R.id.map_view);
        mMapView = new TMapView(this);
        mMainRelativeLayout.addView(mMapView);
        mMapView.setSKPMapApiKey("8818efcf-6165-3d1c-a056-93025f8b06c3"); //SDK 인증키입력
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        //이미지 추가하는 방법
//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_launcher);
//        mMapView.setIcon(bitmap);
//        mMapView.setIconVisibility(true);

        //마커 표시
        TMapPoint tpoint = new TMapPoint(37.570841, 126.985302);
        TMapMarkerItem tItem = new TMapMarkerItem();
        tItem.setTMapPoint(tpoint);
        tItem.setName("관광지1");
        tItem.setVisible(TMapMarkerItem.VISIBLE);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
        tItem.setIcon(bitmap);

        tItem.setPosition(0.5f, 1.0f);
        mMapView.addMarkerItem("tour1", tItem);
    }

    private void btnClickEvent() {
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

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchContent = editSearch.getText().toString();
                Log.i("Mylog", searchContent);

                // 별도의 스레드로 검색한 지역의 좌표를 받아옴
                TMapData tmapdata = new TMapData();
                tmapdata.findAllPOI(searchContent, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(final ArrayList<TMapPOIItem> poiList) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("Mylog", poiList.get(0).noorLat);
                                Log.i("Mylog", poiList.get(0).noorLon);
                                mapX = poiList.get(0).noorLat;
                                mapY = poiList.get(0).noorLon;
                            }
                        });
                    }
                });

            }
        });
    }

    private void initView() {
        //버튼들
        btnMT = (Button) findViewById(R.id.btnMT);
        btnTour = (Button) findViewById(R.id.btnTour);
        btnEat = (Button) findViewById(R.id.btnEat);
        btnAddPlace = (Button) findViewById(R.id.btnAddPlace);
        btnRemovePlace = (Button) findViewById(R.id.btnRemovePlace);
        btnViewDetail = (Button) findViewById(R.id.btnViewDetail);
        btnComplete = (Button) findViewById(R.id.btnComplete);

        editSearch = (EditText) findViewById(R.id.editSearch);
        btnSearch = (Button) findViewById(R.id.btnSearch);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initNetworkService() {
        tourNetworkService = ApplicationController.getInstance().getTourNetwork();
    }


}

