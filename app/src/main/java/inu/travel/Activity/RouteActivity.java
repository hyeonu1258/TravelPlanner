package inu.travel.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapInfo;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapTapi;
import com.skp.Tmap.TMapView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

import inu.travel.Adapter.NaviListAdapter;
import inu.travel.Component.ApplicationController;
import inu.travel.Model.NaviDescript;
import inu.travel.Network.TourNetworkService;
import inu.travel.R;

public class RouteActivity extends AppCompatActivity implements TMapView.OnClickListenerCallback, NavigationView.OnNavigationItemSelectedListener {

    // 네트워크 통신을 위한 자원드
    private TourNetworkService tourNetworkService;

    // Tmap을 위한 자원
    private RelativeLayout mMainRelativeLayout = null;
    private TMapView mMapView = null;

    //네비경로를 그릴 두 좌표값
    private TMapPoint startPoint = new TMapPoint(0, 0);
    private TMapPoint endPoint = new TMapPoint(0, 0);

    //네비리스트 아답터
    private NaviListAdapter naviListAdapter;
    private ListView naviListView;

    //거리정보
    private TextView txtRouteTime, txtRouteTime2;
    private TextView txtRouteKm, txtRouteKm2;
    private TextView txtRouteTaxi, txtRouteTaxi2;

    private TextView txtStartEndPlace;

    private Button btnBackRoute;

    //TMap 연동을 위한 자원
    private TMapTapi tmaptapi;
    private Button btnGoToTmap;
    String startName;
    String endName;

    // tmp
    ArrayList<NaviDescript> itemDatas = new ArrayList<NaviDescript>();

    //slidewindow
    SlidingDrawer slidingDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        initView();
        initTmapAPI();
        initNetworkService();
        initTMap();
        initPoint();
        btnClickEvent();

        try {
            Thread getPathThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TMapData tmapdata = new TMapData();
                        System.out.println("시작점좌표 = " + startPoint.getLongitude() + ", " + startPoint.getLatitude());
                        System.out.println("도착점좌표 = " + endPoint.getLongitude() + ", " + endPoint.getLatitude());
                        Document pathDoc = tmapdata.findPathDataAll(startPoint, endPoint);

                        //xml 파싱
                        parseXML(pathDoc);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            getPathThread.start();
            getPathThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("JOIN 후");
        System.out.println("@@@@" + itemDatas.size());

        makeNaviListAdapter(itemDatas);
        naviListAdapter.notifyDataSetChanged();

        initLine();

    }

    private void btnClickEvent() {
        btnBackRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnGoToTmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isTmapApp = tmaptapi.isTmapApplicationInstalled();

                if (isTmapApp) {
                    HashMap<String, String> pathinfo = new HashMap<String, String>();
                    // 출발지
                    pathinfo.put("rStName",  startName);
                    pathinfo.put("rStX", String.valueOf(startPoint.getLongitude()));
                    pathinfo.put("rStY", String.valueOf(startPoint.getLatitude()));
                    // 목적지
                    pathinfo.put("rGoName",  endName);
                    pathinfo.put("rGoX", String.valueOf(endPoint.getLongitude()));
                    pathinfo.put("rGoY", String.valueOf(endPoint.getLatitude()));

                    tmaptapi.invokeRoute(pathinfo);
                } else {
                    Toast.makeText(getApplicationContext(), "TMap이 설치되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initTmapAPI() {
        tmaptapi = new TMapTapi(this);
        tmaptapi.setSKPMapAuthentication("8818efcf-6165-3d1c-a056-93025f8b06c3");
    }

    private void initView() {
        txtRouteTime = (TextView) findViewById(R.id.txtRouteTime);
        txtRouteKm = (TextView) findViewById(R.id.txtRouteKm);
        txtRouteTaxi = (TextView) findViewById(R.id.txtRouteTaxi);
        txtRouteTime2 = (TextView) findViewById(R.id.txtRouteTime2);
        txtRouteKm2 = (TextView) findViewById(R.id.txtRouteKm2);
        txtRouteTaxi2 = (TextView) findViewById(R.id.txtRouteTaxi2);
        txtStartEndPlace = (TextView) findViewById(R.id.txtStartEndPlace);
        naviListView = (ListView) findViewById(R.id.routeitemlist);
        btnBackRoute = (Button) findViewById(R.id.btnBackRoute);
        btnGoToTmap = (Button) findViewById(R.id.btnGoToTmap);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.slide);
    }

    private void parseXML(Document pathDoc) {
        pathDoc.getDocumentElement().normalize();
        System.out.println("Root element :" + pathDoc.getDocumentElement().getNodeName());
        System.out.println("총 거리 : " + pathDoc.getElementsByTagName("tmap:totalDistance").item(0).getTextContent());
        System.out.println("총 시간 : " + pathDoc.getElementsByTagName("tmap:totalTime").item(0).getTextContent());
        System.out.println("택시 요금 : " + pathDoc.getElementsByTagName("tmap:taxiFare").item(0).getTextContent());
        System.out.println("스타트포인트 : " + pathDoc.getElementById("startPointStyle").getElementsByTagName("href").item(0).getTextContent());

        int totalTime = Integer.parseInt(pathDoc.getElementsByTagName("tmap:totalTime").item(0).getTextContent()) / 60;
        double totalDistance = Double.parseDouble(pathDoc.getElementsByTagName("tmap:totalDistance").item(0).getTextContent()) / 1000;
        int totalTaxi = Integer.parseInt(pathDoc.getElementsByTagName("tmap:taxiFare").item(0).getTextContent());

        txtRouteTime.setText("소요시간 : " + totalTime + "분");
        txtRouteKm.setText("거리 : " + totalDistance + "Km");
        txtRouteTaxi.setText("택시비 : " + totalTaxi + "원");
        txtRouteTime2.setText("소요시간 : " + totalTime + "분");
        txtRouteKm2.setText("거리 : " + totalDistance + "Km");
        txtRouteTaxi2.setText("택시비 : " + totalTaxi + "원");

        NodeList nList = pathDoc.getElementsByTagName("Placemark");
        System.out.println("----------------------------");
        System.out.println("doc길이 : " + nList.getLength());

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            System.out.println("\nCurrent Element :" + nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                try { //항목이 null인 것도 있음
                    System.out.println("description : " + eElement.getElementsByTagName("description").item(0).getTextContent());
                    NaviDescript item = new NaviDescript();
                    item.setDescription(eElement.getElementsByTagName("description").item(0).getTextContent());
                    itemDatas.add(item);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("----------------------------");
            }
        }

    }

    private void initPoint() {
        Intent i = getIntent();
        double startX = i.getDoubleExtra("startX", 000.000D);
        double startY = i.getDoubleExtra("startY", 000.000D);
        double endX = i.getDoubleExtra("endX", 000.000D);
        double endY = i.getDoubleExtra("endY", 000.000D);
        startName = i.getStringExtra("startName");
        endName = i.getStringExtra("endName");
        startPoint.setLatitude(startX);
        startPoint.setLongitude(startY);
        endPoint.setLatitude(endX);
        endPoint.setLongitude(endY);
        txtStartEndPlace.setText(startName + " -> " + endName);
    }

    private void initLine() {

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher);

        //다중경로test
        Thread getLineThread = new Thread(new Runnable() {
            @Override
            public void run() {    // 오래 걸릴 작업을 구현한다
                // Auto-generated method stub
                try {
                    final TMapData tmapdata = new TMapData();
                    TMapPolyLine tMapPolyLine = tmapdata.findMultiPointPathData(startPoint, endPoint, null, 0);
                    tMapPolyLine.setLineWidth(10);
                    tMapPolyLine.setLineColor(0x000000ff);

                    mMapView.addTMapPath(tMapPolyLine); //출발지 도착지 경유지 표시

                    //찍은 좌표로 맵 이동, 최적화
                    ArrayList<TMapPoint> tMapPoints = new ArrayList<TMapPoint>();
                    tMapPoints.add(startPoint);
                    tMapPoints.add(endPoint);
                    TMapInfo info = mMapView.getDisplayTMapInfo(tMapPoints);
                    mMapView.setCenterPoint(info.getTMapPoint().getLongitude(), info.getTMapPoint().getLatitude(), true);
                    mMapView.setZoomLevel(info.getTMapZoomLevel());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        getLineThread.start();
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

    private void makeNaviListAdapter(ArrayList<NaviDescript> items) {
        naviListAdapter = new NaviListAdapter(items, getApplicationContext());
        naviListView.setAdapter(naviListAdapter);
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
    public void onBackPressed() { // 백 버튼

        if(slidingDrawer.isOpened()){
            slidingDrawer.close();
        }
        else {
                super.onBackPressed();
        }

    }
}
