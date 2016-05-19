package inu.travel.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapInfo;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import inu.travel.Adapter.ResultPlaceAdapter;
import inu.travel.Component.ApplicationController;
import inu.travel.Model.Place;
import inu.travel.Model.PlaceList;
import inu.travel.Model.Plan;
import inu.travel.Model.SearchPlace;
import inu.travel.Network.AwsNetworkService;
import inu.travel.Network.TourNetworkService;
import inu.travel.R;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class ResultActivity extends AppCompatActivity implements TMapView.OnClickListenerCallback, NavigationView.OnNavigationItemSelectedListener {
    Handler handler = new Handler();  // 외부쓰레드 에서 메인 UI화면을 그릴때 사용

    // 네트워크 통신을 위한 자원드
    private AwsNetworkService awsNetworkService;
    private TourNetworkService tourNetworkService;

    // Tmap을 위한 자원
    private RelativeLayout mMainRelativeLayout = null;
    private TMapView mMapView = null;

    // Tmap Line
    private ArrayList<String> mArrayLineID;
    private static int mLineID;

    // 네비게이션 뷰를 위한 자원
    DrawerLayout drawer;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    TextView txtPlanName;
    TextView txtPlanExplain;
    TextView btnLogout;
    TextView btnSetting;

    //id, planname
    private String id;
    private String planname;
    private String planexplain; //플랜설명

    private TextView txtResultKm;
    private TextView txtResultTime;

    private PlaceList placeList;
    private ArrayList<TMapPOIItem> savedPOIPlaceList;

    // 리스트뷰를 위한 자원
    private ListView resultPlaceListView;
    private ResultPlaceAdapter resultPlaceAdapter;

    private Bitmap[] savedBitmap = new Bitmap[9]; //장소추가했을때

    private boolean isShowTotalNavi = false; //전체경로보기
    private int commonZoomLevel;

    //팝업
    PopupWindow mPopupWindow;
    Display display;
    //클릭한 장소 좌표
    private double mapX;
    private double mapY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initView();

        initNetworkService();
        initNaviView();
        initListView();
        initIcon();
        initTMap();
        initWindowSize();

        getUser();
        initPlaceList();
        getPlaceList();

//TODO : 전체경로 보기


    }

    private void initView() {
        //류땅
//        txtPlanName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txt_plan_name);
//        txtPlanName.setText("플랜이름 : " + planname);
//        txtPlanExplain = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txt_plan_explain);
//        txtPlanExplain.setText("설명 : " + planexplain);

        btnLogout = (Button) findViewById(R.id.btn_logout);
        btnSetting = (Button) findViewById(R.id.btn_setting);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Logout btn clicked", Toast.LENGTH_SHORT).show();
            }
        });
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Setting btn clicked", Toast.LENGTH_SHORT).show();
            }
        });
        txtResultKm = (TextView) findViewById(R.id.txtResultKm);
        txtResultTime = (TextView) findViewById(R.id.txtResultTime);

    }

    private void initWindowSize() {
        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
    }

    private void initListView() {
        resultPlaceListView = (ListView) findViewById(R.id.resultitemlist);
    }

    private void initLine() {
        mArrayLineID = new ArrayList<String>();
        mLineID = 0;

        ArrayList<TMapPolyLine> polyLineArrayList = new ArrayList<>();
        TMapPoint startPoint, endPoint;
        TMapPoint centerPoint = new TMapPoint(0, 0);
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.marker_route);

        System.out.println("선그리기 => 장소갯수 : " + savedPOIPlaceList.size());

        for (int i = 1; i < savedPOIPlaceList.size(); i++) {
            TMapPolyLine polyLine = new TMapPolyLine();
            polyLine.setLineColor(0x00F7EF04);
//            polyLine.setLineWidth(10);
            //시작점
            startPoint = savedPOIPlaceList.get(i - 1).getPOIPoint();
            polyLine.addLinePoint(startPoint);
            //도착점
            endPoint = savedPOIPlaceList.get(i).getPOIPoint();
            polyLine.addLinePoint(endPoint);
            polyLineArrayList.add(polyLine);
            String strID = savedPOIPlaceList.get(i).getPOIID();
            mMapView.addTMapPolyLine(strID, polyLine);
            mArrayLineID.add(strID);

            //중간지점에 마커생성
            TMapMarkerItem markeritem = new TMapMarkerItem();
            System.out.println("start좌표 : " + startPoint.getLatitude() + ", " + startPoint.getLongitude());
            System.out.println("end좌표 : " + endPoint.getLatitude() + ", " + endPoint.getLongitude());
            centerPoint.setLatitude((endPoint.getLatitude() + startPoint.getLatitude()) / 2);
            centerPoint.setLongitude((endPoint.getLongitude() + startPoint.getLongitude()) / 2);
//            centerPoint.setLatitude(Math.abs(endPoint.getLatitude() - startPoint.getLatitude()));
//            centerPoint.setLongitude(Math.abs(endPoint.getLongitude() - startPoint.getLongitude()));
            markeritem.setName("경로보기");
            markeritem.setCalloutTitle(savedPOIPlaceList.get(i - 1).name); //출발위치
            markeritem.setCalloutSubTitle(savedPOIPlaceList.get(i).name); //도착위치

            markeritem.setTMapPoint(centerPoint);
            System.out.println("좌표2 : " + markeritem.getTMapPoint().getLatitude() + ", " + markeritem.getTMapPoint().getLongitude());
            markeritem.setVisible(TMapMarkerItem.VISIBLE);


            markeritem.setIcon(bitmap);

            mMapView.addMarkerItem(strID, markeritem);


        }

        for (int i = 0; i < polyLineArrayList.size(); i++) {
            System.out.println("선" + i + "의 출발 좌표 = " + polyLineArrayList.get(i).getLinePoint().get(0).getLatitude());
            System.out.println("선" + i + "의 도착 좌표 = " + polyLineArrayList.get(i).getLinePoint().get(1).getLatitude());
        }
    }

    private void initIcon() {
        savedBitmap[0] = BitmapFactory.decodeResource(this.getResources(), R.mipmap.place1);
        savedBitmap[1] = BitmapFactory.decodeResource(this.getResources(), R.mipmap.place2);
        savedBitmap[2] = BitmapFactory.decodeResource(this.getResources(), R.mipmap.place3);
        savedBitmap[3] = BitmapFactory.decodeResource(this.getResources(), R.mipmap.place4);
        savedBitmap[4] = BitmapFactory.decodeResource(this.getResources(), R.mipmap.place5);
        savedBitmap[5] = BitmapFactory.decodeResource(this.getResources(), R.mipmap.place6);
        savedBitmap[6] = BitmapFactory.decodeResource(this.getResources(), R.mipmap.place7);
        savedBitmap[7] = BitmapFactory.decodeResource(this.getResources(), R.mipmap.place8);
        savedBitmap[8] = BitmapFactory.decodeResource(this.getResources(), R.mipmap.place9);
    }

    @Override
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (mPopupWindow != null) {
            System.out.println("디스미스");
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (arrayList.size() > 0) {
            System.out.println("중간지점이 눌림");
            //출발지, 도착지 좌표저장
            TMapPolyLine polyline = mMapView.getPolyLineFromID(arrayList.get(0).getID()); //마커의 id를 line아이디와 같게해서 찾음
            double startX, startY, endX, endY;
            String startName =  arrayList.get(0).getCalloutTitle();
            String endName =  arrayList.get(0).getCalloutSubTitle();
            System.out.println("시작위치 : " + startName);
            System.out.println("도착위치 : " + endName);

            startX = polyline.getLinePoint().get(0).getLatitude();
            startY = polyline.getLinePoint().get(0).getLongitude();
            endX = polyline.getLinePoint().get(1).getLatitude();
            endY = polyline.getLinePoint().get(1).getLongitude();
            System.out.println("startX : " + startX);
            Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
//            intent.putExtra("Userid", id);
            intent.putExtra("startX", startX);
            intent.putExtra("startY", startY);
            intent.putExtra("endX", endX);
            intent.putExtra("endY", endY);
            intent.putExtra("startName", startName);
            intent.putExtra("endName", endName);
            startActivity(intent);
        }

        if (arrayList1.isEmpty() == false) {
            System.out.println("장소가눌림");
            popupOption(mMapView, arrayList1.get(0));
        }
        return false;
    }

    private void popupOption(TMapView v, TMapPOIItem item) {

        if (mPopupWindow == null) {
            System.out.println("들어갔다");
            View popupView = getLayoutInflater().inflate(R.layout.popup_option, null);
            mPopupWindow = new PopupWindow(popupView,
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mapX = item.getPOIPoint().getLongitude();
            mapY = item.getPOIPoint().getLatitude();
            mMapView.setCenterPoint(mapX, mapY, false);
            mPopupWindow.showAsDropDown(v, display.getWidth() / 2 -70, -870);

        } else {
            System.out.println("빠졌다");
        }
        /**
         * PopupWindow Show 메서드
         * showAsDropDown(anchor, xoff, yoff)
         * @View anchor : anchor View를 기준으로 PopupWindow 표시 (상,하)
         * PopupWindow가 최대한 화면에 표시되도록 시스템이 설정해 준다.
         * xoff, yoff : anchor View를 기준으로 PopupWindow를 표시된것을
         * 기준으로 xoff는 x좌표, yoff는 y좌표 만큼 이동 한다.
         * @int xoff : -숫자(화면 왼쪽으로 이동), +숫자(화면 오른쪽으로 이동)
         * @int yoff : -숫자(화면 위쪽으로 이동), +숫자(화면 아래쪽으로 이동)
         * achor View 를 덮는 것도 가능
         * 화면바깥 좌우, 위아래로 이동 가능 (짤린 상태로 표시됨)
         */
//                    mPopupWindow.setAnimationStyle(0); // 애니메이션 설정(-1:설정안함, 0:설정)

        /**
         * update() 메서드를 통해 PopupWindow의 좌우 사이즈, x좌표, y좌표
         * anchor View까지 재설정 해줄수 있습니다.
         */
//         mPopupWindow.update(anchor, xoff, yoff, width, height)(width, height);


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
        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //토글 생성
//        drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
//        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
//                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//                super.onDrawerSlide(drawerView, slideOffset);
//            }
//        };
//        drawer.setDrawerListener(toggle);
//        toggle.syncState(); //토글스위치 이미지 전환

        navigationView = (NavigationView) findViewById(R.id.nav_view2);
        disableOverScroll(navigationView);
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
        mMapView.setSKPMapApiKey("8818efcf-6165-3d1c-a056-93025f8b06c3"); //SDK 인증키입력
        mMainRelativeLayout.addView(mMapView);
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
    }

    private void getUser() {
        Intent i = getIntent();
        id = i.getStringExtra("Userid");
        planname = i.getStringExtra("PlanName");
        planexplain = i.getStringExtra("PlanExplain");
    }

    private void initPlaceList() {
        placeList = new PlaceList();
        placeList.setId(id);
        placeList.setPname(planname);
        savedPOIPlaceList = new ArrayList<>();
    }

    //상세히보기
    private void viewDetail(TMapPOIItem item) {
        // getDetailCommon API 사용하여 상세정보 뽑기
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("MobileOS", "AND");
        parameters.put("contentId", item.getPOIID()); //콘텐트ID
        parameters.put("defaultYN", "Y"); //기본정보
        parameters.put("firstImageYN", "Y"); //이미지
        parameters.put("addrinfoYN", "Y"); //주소
        parameters.put("overviewYN", "Y"); //개요

        //NetworkService에 정의된 메소드를 사용하여 서버에서 데이터를 받아옴(비동기식)

        Call<Object> dataCall = tourNetworkService.getDetailCommon(parameters);
        dataCall.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Response<Object> response, Retrofit retrofit) {
                int statusCode = response.code();
                if (response.isSuccess()) {
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(response.body());

                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        jsonObject = jsonObject.getJSONObject("response").getJSONObject("body").getJSONObject("items");
                        SearchPlace searchPlace = gson.fromJson(jsonObject.getJSONObject("item").toString(), SearchPlace.class);

                        Log.i("MyLog:jsonObject", jsonObject.toString());
                        Log.i("MyLog:SearchPlace", searchPlace.toString());

                        //상세히 보기 다이얼 로그 띄우기
                        showDetailView(searchPlace);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i("MyLog", "실패 : " + statusCode);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i("MyLog", t.getMessage());
            }
        });
    }

    //상세정보를 띄움
    private void showDetailView(final SearchPlace searchPlace) {
        LayoutInflater layoutInflater = (LayoutInflater) getLayoutInflater();
        final View dialogLayout = layoutInflater.inflate(R.layout.detail_view, null);

        //데이터 findByid
        final ImageView imageViewDetailPicture = (ImageView) dialogLayout.findViewById(R.id.imageViewDetailPicture);
        Button btnDetailClose = (Button) dialogLayout.findViewById(R.id.btnDetailClose);
        TextView txtDetailName = (TextView) dialogLayout.findViewById(R.id.txtDetailName);
        TextView txtDetailAddr = (TextView) dialogLayout.findViewById(R.id.txtDetailAddr);
        TextView txtDetailHomepage = (TextView) dialogLayout.findViewById(R.id.txtDetailHomepage);
        TextView txtDetailTel = (TextView) dialogLayout.findViewById(R.id.txtDetailTel);
        TextView txtDetailOverview = (TextView) dialogLayout.findViewById(R.id.txtDetailOverview);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {    // 오래 걸릴 작업을 구현한다
                // Auto-generated method stub
                try {
                    URL url = new URL(searchPlace.firstimage);
                    InputStream is = url.openStream();
                    final Bitmap bm = BitmapFactory.decodeStream(is);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {  // 화면에 그려줄 작업
                            imageViewDetailPicture.setImageBitmap(bm);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        try {
            txtDetailName.setText(searchPlace.title);
            txtDetailAddr.setText(searchPlace.addr1);
            txtDetailAddr.append(searchPlace.addr1);
            txtDetailHomepage.setText(Html.fromHtml(searchPlace.homepage));
            txtDetailTel.setText(searchPlace.tel);
            txtDetailOverview.setText(Html.fromHtml(searchPlace.overview));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //다이얼로그를 만들기 위한 빌더를 만들어줍니다. 이 때 인자는 해당 액티비티.this 로 해야합니다. 다이얼로그를 만들기 위한 틀입니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
        builder.setView(dialogLayout); //layout(위에서 layoutInflater를 통해 인플레이트한)을 다이얼로그가 뷰의 형태로 가져옵니다.
        final DialogInterface mPopupDlg = builder.show();
        btnDetailClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupDlg.dismiss();
            }
        });
    }

    private void getPlaceList() {

        HashMap<String, String> param = new HashMap<>();
        param.put("id", id);
        param.put("planname", planname);

        final Call<ArrayList<Place>> getPlaceList = awsNetworkService.getPlaceList(param);
        getPlaceList.enqueue(new Callback<ArrayList<Place>>() {
            @Override
            public void onResponse(Response<ArrayList<Place>> response, Retrofit retrofit) {

                if (response.code() == 200) {
                    System.out.println("Result:디비로딩성공");
                    savedPOIPlaceList.clear();
                    //System.out.println(response.body());

                    placeList.setItem(response.body());

                    ArrayList<TMapPoint> tMapPoints = new ArrayList<TMapPoint>();

                    //placeList를 TMAPPOIItem으로 바꿔서 맵에 표시하기
                    for (int i = 0; i < placeList.getItem().size(); i++) {
                        TMapPOIItem item = new TMapPOIItem();
                        item.Icon = savedBitmap[i];
                        item.noorLon = placeList.getItem().get(i).getMapx();
                        item.noorLat = placeList.getItem().get(i).getMapy();
                        item.name = placeList.getItem().get(i).getPlacename(); //장소명
                        item.address = placeList.getItem().get(i).getAddress(); //주소
                        item.setID(placeList.getItem().get(i).getContentid()); //자세히보기 API 요청시 필요함
                        item.bizCatName = placeList.getItem().get(i).getImgpath();

                        //저장된 장소 리스트에 넣기
                        savedPOIPlaceList.add(item);

                        tMapPoints.add(i, item.getPOIPoint()); //표시된 장소의 좌표를 기억해서 zoomLevel을 최적화
                    }

                    //이미 표시된 POI 지우기
                    mMapView.removeAllTMapPOIItem();

                    //DB에서 가져온 장소 리스트에 추가시키기

                    //맵에 POI 띄우기
                    mMapView.addTMapPOIItem(savedPOIPlaceList);

                    //위치찾기
                    TMapInfo info = mMapView.getDisplayTMapInfo(tMapPoints);
                    mMapView.setCenterPoint(info.getTMapPoint().getLongitude(), info.getTMapPoint().getLatitude(), true);
                    commonZoomLevel = info.getTMapZoomLevel();
                    mMapView.setZoomLevel(commonZoomLevel);
                    initLine();

                    // 받아온 장소들 리스트뷰에 띄우기
                    makeResultPlaceAdapter(savedPOIPlaceList);
                    resultPlaceAdapter.notifyDataSetChanged();

                } else if (response.code() == 404) {
                    System.out.println("해당플랜에 장소가 없음!");
                } else if (response.code() == 503) {
                    int statusCode = response.code();
                    Log.i("MyTag", "응답코드 : " + statusCode);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to load place", Toast.LENGTH_LONG).show();
                Log.i("MyTag", "에러내용 : " + t.getMessage());
            }
        });

        // 총거리 시간 받기
        final Call<Plan> getPlanInfo = awsNetworkService.getPlanInfo(param);
        getPlanInfo.enqueue(new Callback<Plan>() {
            @Override
            public void onResponse(Response<Plan> response, Retrofit retrofit) {
                if (response.code() == 200) {

                    Plan tmpPlan = response.body();
                    txtResultKm.setText(Integer.parseInt(tmpPlan.alldistance)/1000 + "." + Integer.parseInt(tmpPlan.alldistance)%1000 + "Km");
                    txtResultTime.setText(Integer.parseInt(tmpPlan.alltime)/60 + "분");

                } else if (response.code() == 503) {
                    int statusCode = response.code();
                    Log.i("MyTag", "응답코드 : " + statusCode);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to load place", Toast.LENGTH_LONG).show();
                Log.i("MyTag 총시간 ", "에러내용 : " + t.getMessage());
            }
        });

    }

    // 리스트뷰 어뎁터
    private void makeResultPlaceAdapter(final ArrayList<TMapPOIItem> tMapPOIItems) {
        resultPlaceAdapter = new ResultPlaceAdapter(tMapPOIItems, getApplicationContext());
        resultPlaceListView.setAdapter(resultPlaceAdapter);

        // 리스트 항목 클릭시 발생 이벤트
        resultPlaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //위치찾기
                double x = Double.parseDouble(tMapPOIItems.get(position).noorLon);
                double y = Double.parseDouble(tMapPOIItems.get(position).noorLat);
                System.out.println("y좌표는 : " + y);

                mMapView.setCenterPoint(x, y, true);
                mMapView.setZoomLevel(15);

                long viewId = view.getId();
                TMapPOIItem selectedPOIItem = tMapPOIItems.get(position);

                if (viewId == R.id.btnCompleteViewDetail) {
//                    Toast.makeText(getApplicationContext(), "상세보기 clicked", Toast.LENGTH_SHORT).show();
                    viewDetail(selectedPOIItem);
                }
            }
        });
    }


    //전체경로 띄움
    private void initTotalNavi() {

        System.out.println("전체경로보기");
        //다중경로test
        Thread getLineThread = new Thread(new Runnable() {
            @Override
            public void run() {    // 오래 걸릴 작업을 구현한다
                // Auto-generated method stub
                try {
                    final TMapData tmapdata = new TMapData();
                    TMapPoint startPoint = savedPOIPlaceList.get(0).getPOIPoint();
                    TMapPoint endPoint = savedPOIPlaceList.get(savedPOIPlaceList.size()-1).getPOIPoint();
                    ArrayList<TMapPoint> passList = new ArrayList<>();

                    System.out.println("출발지=> " + savedPOIPlaceList.get(0).name);

                    for(int i=1; i<savedPOIPlaceList.size()-1; i++){
                        passList.add(savedPOIPlaceList.get(i).getPOIPoint());
                        System.out.println(savedPOIPlaceList.get(i).name);
                    }
                    System.out.println("도착지=> " + savedPOIPlaceList.get(savedPOIPlaceList.size()-1).name);

                    TMapPolyLine tMapPolyLine = tmapdata.findMultiPointPathData(startPoint, endPoint, passList, 0);
                    mMapView.addTMapPath(tMapPolyLine); //출발지 도착지 경유지 표시

                    //찍은 좌표로 맵 이동, 최적화
                    mMapView.setZoomLevel(commonZoomLevel);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        getLineThread.start();
    }

    public void btnClick(View v) {
        switch (v.getId()) {
            case R.id.btnInfo: //주변정보
                Intent intent = new Intent(getApplicationContext(), FacilityActivity.class);
                intent.putExtra("mapX", mapX);
                intent.putExtra("mapY", mapY);
                startActivity(intent);
                break;
            case R.id.btnEdit: //수정버튼
//                Toast.makeText(getApplicationContext(), "수정", Toast.LENGTH_SHORT).show();
                Intent intent2 = new Intent(getApplicationContext(), SearchPlaceActivity.class);
                intent2.putExtra("Userid", id);
                intent2.putExtra("PlanName", planname);
                intent2.putExtra("PlanExplain", planexplain);
                startActivity(intent2);
                finish();
                break;
            case R.id.btnTotalNavi: //전체경로
                if(isShowTotalNavi==false){
                    isShowTotalNavi=true;
                    mMapView.removeAllTMapPOIItem();
                    mMapView.removeAllMarkerItem();
                    mMapView.removeAllTMapPolyLine();
                    initTotalNavi();
                }else{
                    isShowTotalNavi=false;
                    mMapView.removeTMapPath();
                    getPlaceList();
                }
                break;
                //TODO : 자동차 대중교통 버튼 추가
        }
    }
}
