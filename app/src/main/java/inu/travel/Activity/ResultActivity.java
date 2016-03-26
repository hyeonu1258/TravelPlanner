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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
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
    NavigationView navigationView3;
    ListView listView;

    //수정버튼
    private Button btnEdit;

    //id, planname
    private String id;
    private String planname;

    private PlaceList placeList;
    private ArrayList<TMapPOIItem> savedPOIPlaceList;

    // 리스트뷰를 위한 자원
    private ListView resultPlaceListView;
    private ResultPlaceAdapter resultPlaceAdapter;

    private Bitmap savedBitmap; //장소추가했을때

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initNetworkService();
        initNaviView();
        initListView();
        initIcon();
        initTMap();
        initButton();
        btnClickEvent();

        getUser();
        initPlaceList();
        getPlaceList();
    }

    private void initListView() {
        resultPlaceListView = (ListView) findViewById(R.id.resultitemlist);
    }

    private void initLine() {
        mArrayLineID = new ArrayList<String>();
        mLineID = 0;

        ArrayList<TMapPolyLine> polyLineArrayList = new ArrayList<>();


        System.out.println("선그리기 => 장소갯수 : " + savedPOIPlaceList.size());
        TMapPoint point;
        for (int i = 1; i < savedPOIPlaceList.size(); i++) {
            TMapPolyLine polyLine = new TMapPolyLine();
            polyLine.setLineColor(0xFF5CD1E5);
            polyLine.setLineWidth(5);
            point = savedPOIPlaceList.get(i-1).getPOIPoint();
            polyLine.addLinePoint(point);
            point = savedPOIPlaceList.get(i).getPOIPoint();
            polyLine.addLinePoint(point);
            polyLineArrayList.add(polyLine);
            String strID = String.format("line%d", mLineID++);
            mMapView.addTMapPolyLine(strID, polyLine);
            mArrayLineID.add(strID);
        }

        for(int i=0; i<polyLineArrayList.size(); i++){
            System.out.println("선" +i + "의 출발 좌표 = " + polyLineArrayList.get(i).getLinePoint().get(0).getLatitude());
            System.out.println("선" +i + "의 도착 좌표 = " + polyLineArrayList.get(i).getLinePoint().get(1).getLatitude());
        }
    }

    private void initButton() {
        btnEdit = (Button) findViewById(R.id.btnEdit);
    }

    private void btnClickEvent() {
        btnEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "수정", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), SearchPlaceActivity.class);
                intent.putExtra("Userid", id);
                intent.putExtra("PlanName", planname);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initIcon() {
        savedBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.save);
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
        mMapView.setSKPMapApiKey("8818efcf-6165-3d1c-a056-93025f8b06c3"); //SDK 인증키입력
        mMainRelativeLayout.addView(mMapView);
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
    }

    private void getUser() {
        Intent i = getIntent();
        id = i.getStringExtra("Userid");
        planname = i.getStringExtra("PlanName");
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

        // TODO: NetworkService에 정의된 메소드를 사용하여 서버에서 데이터를 받아옴(비동기식)

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

        //핸들러사용
        // 인터넷 상의 이미지 보여주기
        // 1. 권한을 획득한다 (인터넷에 접근할수 있는 권한을 획득한다)  - 메니페스트 파일
        // 2. Thread 에서 웹의 이미지를 받아온다 - honeycomb(3.0) 버젼 부터 바뀜
        // 3. 외부쓰레드에서 메인 UI에 접근하려면 Handler 를 사용해야 한다.

        //Thread t = new Thread(Runnable 객체를 만든다);
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
                    //System.out.println(response.body());

                    placeList.setItem(response.body());

                    ArrayList<TMapPoint> tMapPoints = new ArrayList<TMapPoint>();

                    //TODO placeList를 TMAPPOIItem으로 바꿔서 맵에 표시하기
                    for (int i = 0; i < placeList.getItem().size(); i++) {
                        TMapPOIItem item = new TMapPOIItem();
                        item.Icon = savedBitmap;
                        item.noorLon = placeList.getItem().get(i).getMapx();
                        item.noorLat = placeList.getItem().get(i).getMapy();
                        item.name = placeList.getItem().get(i).getPlacename(); //장소명
                        item.address = placeList.getItem().get(i).getAddress(); //주소
                        item.setID(placeList.getItem().get(i).getContentid()); //자세히보기 API 요청시 필요함
                        item.bizCatName=placeList.getItem().get(i).getImgpath();

                        //저장된 장소 리스트에 넣기
                        savedPOIPlaceList.add(item);

                        tMapPoints.add(i, item.getPOIPoint()); //표시된 장소의 좌표를 기억해서 zoomLevel을 최적화
                    }

                    //이미 표시된 POI 지우기
                    mMapView.removeAllTMapPOIItem();

                    //TODO: DB에서 가져온 장소 리스트에 추가시키기

                    //맵에 POI 띄우기
                    mMapView.addTMapPOIItem(savedPOIPlaceList);

                    //위치찾기
                    TMapInfo info = mMapView.getDisplayTMapInfo(tMapPoints);
                    mMapView.setCenterPoint(info.getTMapPoint().getLongitude(), info.getTMapPoint().getLatitude(), true);
                    mMapView.setZoomLevel(info.getTMapZoomLevel());
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
    }

    // TODO : 종민
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

                if (viewId == R.id.btnViewDetail) {
                    Toast.makeText(getApplicationContext(), "상세보기 clicked", Toast.LENGTH_SHORT).show();
                    viewDetail(selectedPOIItem);
                }
            }
        });
    }

}
