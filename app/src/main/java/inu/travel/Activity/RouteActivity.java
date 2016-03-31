package inu.travel.Activity;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapView;

import org.w3c.dom.Document;

import java.util.ArrayList;

import inu.travel.Component.ApplicationController;
import inu.travel.Network.TourNetworkService;
import inu.travel.R;

public class RouteActivity extends AppCompatActivity implements TMapView.OnClickListenerCallback, NavigationView.OnNavigationItemSelectedListener {

    // 네트워크 통신을 위한 자원드
    private TourNetworkService tourNetworkService;

    // Tmap을 위한 자원
    private RelativeLayout mMainRelativeLayout = null;
    private TMapView mMapView = null;

    // Sliding을 위한 자원
    private SlidingDrawer slidingLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        initNetworkService();
        initTMap();

        TMapPoint start_point = new TMapPoint(126.7636062976, 37.5026717226);
        TMapPoint end_point = new TMapPoint(126.9835815178, 37.5718842715);

        TMapData tmapdata = new TMapData();
        tmapdata.findPathDataAll(start_point, end_point, new TMapData.FindPathDataAllListenerCallback() {
            @Override
            public void onFindPathDataAll(Document document) {
                System.out.print(document);
            }
        });

    }

    /*
    * 네트워크 통신을 위해 초기화
    */
    private void initNetworkService() {
        tourNetworkService = ApplicationController.getInstance().getTourNetwork();
    }

    /*
     * Tmap 사용을 위한 초기화
     */
    private void initTMap() {
        mMainRelativeLayout = (RelativeLayout) findViewById(R.id.map_view);
        mMapView = new TMapView(this);
        mMapView.setSKPMapApiKey("8818efcf-6165-3d1c-a056-93025f8b06c3"); //SDK 인증키입력
        mMainRelativeLayout.addView(mMapView);
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
    }

    @Override
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }
}
