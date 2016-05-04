package inu.travel.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapInfo;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

    // Tmap Line
    private ArrayList<String> mArrayLineID;
    private static int mLineID;

    //네비경로를 그릴 두 좌표값
    private TMapPoint startPoint = new TMapPoint(0, 0);
    private TMapPoint endPoint = new TMapPoint(0, 0);

    //네비리스트 아답터
    private NaviListAdapter naviListAdapter;
    private ListView naviListView;

    // Sliding을 위한 자원
    private SlidingDrawer slidingLayout = null;

    //거리정보
    private TextView txtRouteTime;
    private TextView txtRouteKm;
    private TextView txtRouteTaxi;

    // tmp
    ArrayList<NaviDescript> itemDatas = new ArrayList<NaviDescript>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        initView();

        initNetworkService();
        initTMap();
        initPoint();

        /*
        ArrayList<NaviDescript> tmpArrayList = new ArrayList<>();
        NaviDescript tmpNaviDescript = new NaviDescript();
        tmpNaviDescript.setDescription("Test");
        tmpArrayList.add(tmpNaviDescript);

        makeNaviListAdapter(tmpArrayList);
        naviListAdapter.notifyDataSetChanged();
        */


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

    private void initView() {
        txtRouteTime = (TextView) findViewById(R.id.txtRouteTime);
        txtRouteKm = (TextView) findViewById(R.id.txtRouteKm);
        txtRouteTaxi = (TextView) findViewById(R.id.txtRouteTaxi);
        naviListView = (ListView) findViewById(R.id.routeitemlist);
    }

    private void parseXML(Document pathDoc) {
        pathDoc.getDocumentElement().normalize();
        System.out.println("Root element :" + pathDoc.getDocumentElement().getNodeName());
        System.out.println("총 거리 : " + pathDoc.getElementsByTagName("tmap:totalDistance").item(0).getTextContent());
        System.out.println("총 시간 : " + pathDoc.getElementsByTagName("tmap:totalTime").item(0).getTextContent());
        System.out.println("택시 요금 : " + pathDoc.getElementsByTagName("tmap:taxiFare").item(0).getTextContent());
        System.out.println("스타트포인트 : " + pathDoc.getElementById("startPointStyle").getElementsByTagName("href").item(0).getTextContent());
        int totalTime = Integer.parseInt(pathDoc.getElementsByTagName("tmap:totalTime").item(0).getTextContent());
        double totalDistance = Double.parseDouble(pathDoc.getElementsByTagName("tmap:totalDistance").item(0).getTextContent())/1000;
        int totalTaxi = Integer.parseInt(pathDoc.getElementsByTagName("tmap:taxiFare").item(0).getTextContent());

        txtRouteTime.setText(totalTime + "분");
        txtRouteKm.setText(totalDistance + "Km");
        txtRouteTaxi.setText(totalTaxi + "원");

        NodeList nList = pathDoc.getElementsByTagName("Placemark");
        System.out.println("----------------------------");
        System.out.println("doc길이 : " + nList.getLength());

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            System.out.println("\nCurrent Element :" + nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                try { //항목이 null인 것도 있음
//                    System.out.println("name : " + eElement.getElementsByTagName("name").item(0).getTextContent());
//                    System.out.println("tmap:pointIndex : " + eElement.getElementsByTagName("tmap:pointIndex").item(0).getTextContent());
                    System.out.println("description : " + eElement.getElementsByTagName("description").item(0).getTextContent());
                    NaviDescript item = new NaviDescript();
                    item.setDescription(eElement.getElementsByTagName("description").item(0).getTextContent());
                    itemDatas.add(item);

//                    System.out.println("styleUrl : " + eElement.getElementsByTagName("styleUrl").item(0).getTextContent());
//                    System.out.println("tmap:nextRoadName : " + eElement.getElementsByTagName("tmap:nextRoadName").item(0).getTextContent());
//                    System.out.println("tmap:nodeType : " + eElement.getElementsByTagName("tmap:nodeType").item(0).getTextContent());
//                    System.out.println("tmap:turnType : " + eElement.getElementsByTagName("tmap:turnType").item(0).getTextContent());
//                    System.out.println("tmap:pointType : " + eElement.getElementsByTagName("tmap:pointType").item(0).getTextContent());
//                    System.out.println("coordinates : " + eElement.getElementsByTagName("coordinates").item(0).getTextContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("----------------------------");
            }
        }

//        makeNaviListAdapter(itemDatas);
//        naviListAdapter.notifyDataSetChanged();
    }

    private void initPoint() {
        Intent i = getIntent();
        double startX = i.getDoubleExtra("startX", 000.000D);
        double startY = i.getDoubleExtra("startY", 000.000D);
        double endX = i.getDoubleExtra("endX", 000.000D);
        double endY = i.getDoubleExtra("endY", 000.000D);
        startPoint.setLatitude(startX);
        startPoint.setLongitude(startY);
        endPoint.setLatitude(endX);
        endPoint.setLongitude(endY);
    }

    private void initLine() {
        mArrayLineID = new ArrayList<String>();
        mLineID = 0;

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher);

        //다중경로test
        Thread getLineThread = new Thread(new Runnable() {
            @Override
            public void run() {    // 오래 걸릴 작업을 구현한다
                // Auto-generated method stub
                try {
                    final TMapData tmapdata = new TMapData();
                    TMapPolyLine tMapPolyLine = tmapdata.findMultiPointPathData(startPoint, endPoint, null, 0);
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

    private void makeNaviListAdapter(ArrayList<NaviDescript> items)
    {
//        System.out.println("@@@@"+items.get(0).getDescription() + items.size());
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
}
