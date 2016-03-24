package inu.travel.Activity;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapView;

import java.util.ArrayList;

import inu.travel.Component.ApplicationController;
import inu.travel.Network.AwsNetworkService;
import inu.travel.Network.TourNetworkService;
import inu.travel.R;

public class ResultActivity extends AppCompatActivity implements TMapView.OnClickListenerCallback, NavigationView.OnNavigationItemSelectedListener {

    // 네트워크 통신을 위한 자원드
    private AwsNetworkService awsNetworkService;
    private TourNetworkService tourNetworkService;

    // Tmap을 위한 자원
    private RelativeLayout mMainRelativeLayout = null;
    private TMapView mMapView = null;

    // 네비게이션 뷰를 위한 자원
    DrawerLayout drawer;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    NavigationView navigationView3;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initNetworkService();
        initNaviView();
        initTMap();
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

    /*
     * 네트워크 통신을 위해 초기화
     */
    private void initNetworkService() {
        tourNetworkService = ApplicationController.getInstance().getTourNetwork();
        awsNetworkService = ApplicationController.getInstance().getAwsNetwork();
    }

    /*
     * 네비게이션 뷰 사용을 위한 초기화
     */
    private void initNaviView() {
        //툴바 생성
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();

        //토글 생성
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState(); //토글스위치 이미지 전환

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        disableOverScroll(navigationView);
        navigationView3 = (NavigationView) findViewById(R.id.nav_view3);
        disableOverScroll(navigationView3);
        navigationView3.setNavigationItemSelectedListener(this);
    }

    /*
     * 네비게이션 뷰 관련 메소
     */
    private void disableOverScroll(NavigationView navigationView) {
        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        }
    }

    /*
     * Tmap 사용을 위한 초기화
     */
    private void initTMap() {
        mMainRelativeLayout = (RelativeLayout) findViewById(R.id.map_view);
        mMapView = new TMapView(this);
        mMainRelativeLayout.addView(mMapView);
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
    }
}
