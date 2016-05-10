package inu.travel.Activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapInfo;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import inu.travel.Adapter.PlaceAdapter;
import inu.travel.Adapter.SearchPlaceAdapter;
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


public class SearchPlaceActivity extends AppCompatActivity implements TMapView.OnClickListenerCallback, NavigationView.OnNavigationItemSelectedListener {
    Handler handler = new Handler();  // 외부쓰레드 에서 메인 UI화면을 그릴때 사용
    AwsNetworkService awsNetworkService;

    private Context mContext;
    private RelativeLayout mMainRelativeLayout = null;
    private TMapView mMapView = null;
    private TourNetworkService tourNetworkService;
    private String searchContent; // 검색한 내용
    private String contentTypeId; //관광지:12, 숙박:32, 음식점:39
    private String radius = "50000"; //거리반경

    //아이콘 설정
    private Bitmap defaultBitmap;
    private Bitmap selectedBitmap; //클릭할때 바뀔 아이콘
    private Bitmap savedBitmap; //장소추가했을때

    //검색해서 나오는 POI객체리스트
    ArrayList<TMapPOIItem> tMapPOISearchItems;

    //클릭하여 선택된 장소 임시저장객체
    private TMapPOIItem selectedPOIItem = null; //TMAP에서 쓰일 객체
    private Place startPlace; //출발지 도착지 저장
    private Place endPlace;


    //장소들을 담을 자료구조. db에서 받아와서 저장하고, 장소추가로 저장할 곳
    private PlaceList placeList;
    private ArrayList<TMapPOIItem> savedPOIPlaceList;
    private PlaceAdapter adapter;
    private SearchPlaceAdapter searchPlaceAdapter;

    //id, planname
    private String id;
    private String planname;

    //류땅
    private String planexplain; //플랜설명
    //지도 위 버튼들
    Button btnComplete;
    Button btnZoomOut;
    Button btnZoomIn;

    ListView searchPlaceListView;
    SlidingDrawer SlidingDrawer;

    //검색바
    String editSearch;

    //네비게이션 뷰
    DrawerLayout drawer;
    SearchView searchView;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    ListView listView;
    TextView txtPlanName;
    TextView txtPlanExplain;
    TextView logoutTxt;
    TextView settingTxt;

    private int zoomLevel;

    //inhoi listView set background
    View preView;


    /**
     * onCreate()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_place);
        initNetworkService();
        mContext = this;

        //툴바 생성
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();

        // 슬라이딩 레이아웃 생성
        searchPlaceListView = (ListView) findViewById(R.id.searchitemlist);

        // 슬라이딩 레이아웃 내리기
        /*
        //btnSlidingClose = (Button)findViewById(R.id.btnslidingclose);
        btnSlidingClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slide);
                drawer.animateClose();
            }
        });
        */

        //토글 생성
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                adapter.setSource(placeList.getItem()); //액션바 리스트 초기화
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState(); //토글스위치 이미지 전환

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        disableOverScroll(navigationView);

        getUser();
        initPlaceList();
        initView();
        makeAdapter();
        btnClickEvent();

        //if수정일경우
        getPlaceList(); //db에서 장소 받아옴

        mMainRelativeLayout = (RelativeLayout) findViewById(R.id.map_view);
        mMapView = new TMapView(this);
        mMainRelativeLayout.addView(mMapView);
        mMapView.setSKPMapApiKey("8818efcf-6165-3d1c-a056-93025f8b06c3"); //SDK 인증키입력
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        SlidingDrawer = (SlidingDrawer) findViewById(R.id.slide);

        //류땅
        txtPlanName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txt_plan_name);
        txtPlanName.setText(planname);
        txtPlanExplain = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txt_plan_explain);
        txtPlanExplain.setText(planexplain);

        logoutTxt = (TextView) findViewById(R.id.logout_txt);
        settingTxt = (TextView) findViewById(R.id.setting_txt);

        logoutTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Logout btn clicked", Toast.LENGTH_SHORT).show();
            }
        });
        settingTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Setting btn clicked", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void disableOverScroll(NavigationView navigationView) {
        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        }
    }

    private void makeAdapter() {
        adapter = new PlaceAdapter(placeList.getItem(), getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPOIItem = savedPOIPlaceList.get(position);
                if (view.getId() == R.id.btnDetailListPlace) {
                    System.out.println("상세보기클릭");
                    if (selectedPOIItem != null) //선택된게 있으면 실행
                        viewDetail(selectedPOIItem);
                    else
                        Toast.makeText(getApplicationContext(), "선택된 장소가 없습니다.", Toast.LENGTH_SHORT).show();
                } else if (view.getId() == R.id.btnRemoveListPlace) {
                    Toast.makeText(getApplicationContext(), "장소삭제", Toast.LENGTH_SHORT).show();
                    if (selectedPOIItem == null || placeList.getItem().isEmpty())
                        return;

                    savedPOIPlaceList.remove(position);
                    for (int i = 0; i < placeList.getItem().size(); i++) {
                        if (placeList.getItem().get(i).getContentid().equals(selectedPOIItem.id)) {
                            System.out.println("삭제할 장소 : " + placeList.getItem().get(i).getPlacename());
                            placeList.getItem().remove(i);

                            System.out.println("삭제성공");
                            adapter.setSource(placeList.getItem()); //액션바 리스트 초기화
                            //이미 표시된 POI 지우기
                            mMapView.removeAllTMapPOIItem();
                            //맵에 POI 띄우기
                            mMapView.addTMapPOIItem(savedPOIPlaceList);
                            printArray();
                        }

                    }
                    return;
                }
                System.out.println("클릭");
                double x = Double.parseDouble(placeList.getItem().get(position).getMapx());
                double y = Double.parseDouble(placeList.getItem().get(position).getMapy());

                drawer.closeDrawers();
                SlidingDrawer.close();
                //위치찾기
                mMapView.setCenterPoint(x, y, true);
                mMapView.setZoomLevel(15);
            }

        });

    }

    // TODO : 종민
    // 슬라이딩 레이아웃에 위치한 리스트뷰 어뎁터
    private void makeSearchPlaceAdapter(final ArrayList<TMapPOIItem> tMapPOIItems) {
        searchPlaceAdapter = new SearchPlaceAdapter(tMapPOIItems, getApplicationContext());
        searchPlaceListView.setAdapter(searchPlaceAdapter);


        // 리스트 항목 클릭시 발생 이벤트
        searchPlaceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override


            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //위치찾기
//                LinearLayout linearLayout;
//                if(preView != null) {
//                    linearLayout = (LinearLayout) preView.findViewById(R.id.tempLinear);
//                    linearLayout.setBackgroundResource(0);
//                }
//
//                linearLayout = (LinearLayout) view.findViewById(R.id.tempLinear);
//                linearLayout.setBackgroundResource(R.drawable.layout_border);

//                preView = view;

                double x = Double.parseDouble(tMapPOIItems.get(position).noorLon);
                double y = Double.parseDouble(tMapPOIItems.get(position).noorLat) - 0.005;
                System.out.println("y좌표는 : " + y);

                mMapView.setCenterPoint(x, y, true);
                mMapView.setZoomLevel(15);

                if (selectedPOIItem != null) { //이전에 저장된 값이 있으면
                    System.out.println("선택했네!");
                    selectedPOIItem.Icon = defaultBitmap;
//                        mMapView.addTMapPOIItem(tMapPOISearchItems); //이미지를 초기화 해준뒤
//                        selectedPOIItem = null; //임시저장된것 삭제
                }

                //선택된 POI를 임시로 저장 -> 나중에 장소 추가, 자세히 보기 할때 저장할 데이터
                selectedPOIItem = tMapPOISearchItems.get(position);
                selectedPOIItem.Icon = selectedBitmap;
                mMapView.addTMapPOIItem(tMapPOISearchItems); //갱신


                long viewId = view.getId();
                selectedPOIItem = tMapPOISearchItems.get(position);

                if (viewId == R.id.btnAddPlace) {
                    System.out.println("장소추가");
                    addPlace(selectedPOIItem);
                } else if (viewId == R.id.btnViewDetail) {
                    viewDetail(selectedPOIItem);
                }
//                drawer.closeDrawers();
            }
        });
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
        tMapPOISearchItems = new ArrayList<TMapPOIItem>();
    }

    //Todo: 이전에 만든 플랜에서 수정을 눌렀을 경우 이전에 저장된 장소를 서버에서 가져와야 함
    private void getPlaceList() {

        HashMap<String, String> param = new HashMap<>();
        param.put("id", id);
        param.put("planname", planname);

        final Call<ArrayList<Place>> getPlaceList = awsNetworkService.getPlaceList(param);
        getPlaceList.enqueue(new Callback<ArrayList<Place>>() {
            @Override
            public void onResponse(Response<ArrayList<Place>> response, Retrofit retrofit) {

                if (response.code() == 200) {
                    System.out.println("디비로딩성공");
                    placeList.setItem(response.body());
                    System.out.println(response.body());

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
                        item.bizCatName = placeList.getItem().get(i).getImgpath();

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

    private void btnClickEvent() {
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeList.getItem().size() == 0)
                    return;
                Toast.makeText(getApplicationContext(), "완료", Toast.LENGTH_SHORT).show();
                LayoutInflater layoutInflater = (LayoutInflater) getLayoutInflater(); //LayoutInflater를 가져오기 위한 다른 방법입니다. LayoutInflater는 Layout을 View의 형태로 변형해주는 역할이라고 3차 세미나 때 배웠었죠?
                View dialogLayout = layoutInflater.inflate(R.layout.dialog_set, null);//dialog_layout이라는 레이아웃을 만듭니다. 이를 뷰의 형태로 다이얼로그에 띄우기 위해 인플레이트 해줍니다.
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchPlaceActivity.this);
                builder.setTitle("출발점을 선택하시오");//다이얼로그의 상단에 표시되는 텍스트인 Title을 정해줍니다.


                builder.setView(dialogLayout); //layout(위에서 layoutInflater를 통해 인플레이트한)을 다이얼로그가 뷰의 형태로 가져옵니다.


                String temp[] = new String[placeList.getItem().size()];

                for (int i = 0; i < placeList.getItem().size(); i++)
                    temp[i] = placeList.getItem().get(i).getPlacename();


                startPlace = placeList.getItem().get(0);

                //출발지
                builder.setSingleChoiceItems(temp, 0,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // 각 리스트를 선택했을때
                                Toast.makeText(SearchPlaceActivity.this, "" + whichButton, Toast.LENGTH_SHORT).show();
                                startPlace = placeList.getItem().get(whichButton);
                            }
                        }).setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String temp[] = new String[placeList.getItem().size()];
                                for (int i = 0; i < placeList.getItem().size(); i++)
                                    temp[i] = placeList.getItem().get(i).getPlacename();


                                Toast.makeText(SearchPlaceActivity.this, "" + whichButton, Toast.LENGTH_SHORT).show();
                                LayoutInflater layoutInflater2 = (LayoutInflater) getLayoutInflater(); //LayoutInflater를 가져오기 위한 다른 방법입니다. LayoutInflater는 Layout을 View의 형태로 변형해주는 역할이라고 3차 세미나 때 배웠었죠?
                                View dialogLayout = layoutInflater2.inflate(R.layout.dialog_set, null);//dialog_layout이라는 레이아웃을 만듭니다. 이를 뷰의 형태로 다이얼로그에 띄우기 위해 인플레이트 해줍니다.
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(SearchPlaceActivity.this);
                                builder2.setTitle("도착점을 선택하시오");//다이얼로그의 상단에 표시되는 텍스트인 Title을 정해줍니다.

                                builder2.setView(dialogLayout);
                                endPlace = placeList.getItem().get(0);
                                builder2.setSingleChoiceItems(temp, 0,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // 각 리스트를 선택했을때
                                                Toast.makeText(SearchPlaceActivity.this, "" + whichButton, Toast.LENGTH_SHORT).show();
                                                endPlace = placeList.getItem().get(whichButton);
                                            }
                                        }).setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                Toast.makeText(SearchPlaceActivity.this, "" + whichButton, Toast.LENGTH_SHORT).show();

                                                PlaceList tmpPlaceList = new PlaceList(); //출발지와 도착지를 셋팅한 리스트
                                                tmpPlaceList.getItem().add(startPlace);
                                                for (int i = 0; i < placeList.getItem().size(); i++) {
                                                    if (placeList.getItem().get(i) != startPlace && placeList.getItem().get(i) != endPlace)
                                                        tmpPlaceList.getItem().add(placeList.getItem().get(i));
                                                }
                                                tmpPlaceList.getItem().add(endPlace);
                                                tmpPlaceList.setId(id);
                                                tmpPlaceList.setPname(planname);

                                                //TODO : 종민이한테 리스트를 넘겨줄것
                                                for (int i = 0; i < placeList.getItem().size(); i++)
                                                    System.out.println(tmpPlaceList.getItem().get(i).getPlacename());

                                                Call<Object> addPlace = awsNetworkService.addPlace(tmpPlaceList);
                                                addPlace.enqueue(new Callback<Object>() {
                                                    @Override
                                                    public void onResponse(Response<Object> response, Retrofit retrofit) {
                                                        if (response.code() == 200) {
                                                            Log.i("완료 => ", "성공");

                                                            // ResultActivity (결과 엑티비티로 이동)
                                                            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                                                            intent.putExtra("Userid", id);
                                                            intent.putExtra("PlanName", planname);
                                                            startActivity(intent);
                                                            finish();
                                                        } else if (response.code() == 503) {
                                                            int statusCode = response.code();
                                                            Log.i("MyTag", "응답코드 : " + statusCode);
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable t) {

                                                    }
                                                });
                                            }
                                        }).setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // Cancel 버튼 클릭시
                                                return;
                                            }
                                        });
                                AlertDialog alertDialog = builder2.create(); //만들어놓은 AlertDialog.Builder인 builder를 이용해서 새로운 AlertDialog를 만듭니다.
                                alertDialog.show(); //다이얼로그를 띄웁니다.

                            }
                        }).setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Cancel 버튼 클릭시
                                return;
                            }
                        });
                AlertDialog alertDialog = builder.create(); //만들어놓은 AlertDialog.Builder인 builder를 이용해서 새로운 AlertDialog를 만듭니다.
                alertDialog.show(); //다이얼로그를 띄웁니다.


            }
        });

        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomLevel = mMapView.getZoomLevel();
                zoomLevel += 2;
                if (zoomLevel < 19)
                    mMapView.setZoomLevel(zoomLevel);
                else
                    mMapView.setZoomLevel(19);
            }
        });

        btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomLevel = mMapView.getZoomLevel();
                zoomLevel -= 2;
                if (zoomLevel > 7)
                    mMapView.setZoomLevel(zoomLevel);
                else
                    mMapView.setZoom(7);
            }
        });

//        btnSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
//                searchArea("12"); //default로 관광지
//            }
//        });
    }

    private void addPlace(TMapPOIItem selectedPOIItem) {
        Toast.makeText(getApplicationContext(), "장소추가", Toast.LENGTH_SHORT).show();
        if (this.selectedPOIItem == null) {
            System.out.println("선택한장소가 없습니다.");
            return;
        }
        //중복검사
        for (int i = 0; i < placeList.getItem().size(); i++) {
            if (placeList.getItem().get(i).getContentid().equals(this.selectedPOIItem.id)) {
                System.out.println("이미 추가한 장소입니다.");
                return;
            }
        }

        //리스트에 장소객체추가
        Place place = new Place();
        place.setPlacename(this.selectedPOIItem.name);
        place.setContentid(this.selectedPOIItem.id);
        place.setContenttypeid(contentTypeId);
        place.setMapx(this.selectedPOIItem.noorLon);
        place.setMapy(this.selectedPOIItem.noorLat);
        place.setAddress(this.selectedPOIItem.address);
        place.setImgpath(this.selectedPOIItem.bizCatName);
        placeList.getItem().add(place);
        System.out.println("이미지경로 = " + this.selectedPOIItem.bizCatName);
        savedPOIPlaceList.add(this.selectedPOIItem);
        System.out.println("장소리스트 개수 : " + placeList.getItem().size());
        System.out.println("추가한 장소 : " + placeList.getItem().get(placeList.getItem().size() - 1).getPlacename());
        printArray();
    }

    private void printArray() {
        for (int i = 0; i < placeList.getItem().size(); i++) {
            System.out.println(i + " : " + placeList.getItem().get(i).getPlacename());
        }
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
        //정보가 없는 장소가 있으므로 예외처리할것
//        try {
//            Log.i("MyLog:detailname", selectedPOIItem.name);
//            if (searchPlace.addr1 != null)
//                Log.i("MyLog:detailaddr1", searchPlace.addr1);
//            if (searchPlace.addr2 != null)
//                Log.i("MyLog:detailaddr2", searchPlace.addr2);
//            if (searchPlace.tel != null)
//                Log.i("MyLog:detailtel", searchPlace.tel);
//            if (searchPlace.homepage != null)
//                Log.i("MyLog:detailhomepage", searchPlace.homepage);
//            if (searchPlace.overview != null)
//                Log.i("MyLog:detailoverview", searchPlace.overview);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

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

        //runOnUiThread사용방법
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // 인터넷 상의 이미지 보여주기
//
//                // 1. 권한을 획득한다 (인터넷에 접근할수 있는 권한을 획득한다)  - 메니페스트 파일
//                // 2. Thread 에서 웹의 이미지를 받아온다 - honeycomb(3.0) 버젼 부터 바뀜
//                // 3. 외부쓰레드에서 메인 UI에 접근하려면 Handler 를 사용해야 한다.
//
//                try {
//                    URL url = new URL(searchPlace.firstimage);
//                    InputStream is = url.openStream();
//                    Bitmap bm = BitmapFactory.decodeStream(is);
//                    imageViewDetailPicture.setImageBitmap(bm);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        try {
//        if (selectedPOIItem.name != null)
            txtDetailName.setText(searchPlace.title);
//        if (searchPlace.addr1 != null)
            txtDetailAddr.setText(searchPlace.addr1);
//        if (searchPlace.addr2 != null)
            txtDetailAddr.append(searchPlace.addr1);
//        if (searchPlace.homepage != null)
            txtDetailHomepage.setText(Html.fromHtml(searchPlace.homepage));
//        if (searchPlace.tel != null)
            txtDetailTel.setText(searchPlace.tel);
//        if (searchPlace.overview != null)
            txtDetailOverview.setText(Html.fromHtml(searchPlace.overview));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //다이얼로그를 만들기 위한 빌더를 만들어줍니다. 이 때 인자는 해당 액티비티.this 로 해야합니다. 다이얼로그를 만들기 위한 틀입니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchPlaceActivity.this);
        builder.setView(dialogLayout); //layout(위에서 layoutInflater를 통해 인플레이트한)을 다이얼로그가 뷰의 형태로 가져옵니다.
        final DialogInterface mPopupDlg = builder.show();
        btnDetailClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupDlg.dismiss();
            }
        });

    }

    //Tmap API에서 검색한 장소의 주요 지점 찾기
    private void searchArea(String contentTypeId) {


        //초기화 - 선택된 객체하나, 검색된 객체 배열
        selectedPOIItem = null; //클릭해서 임시저장한 데이터 삭제
        tMapPOISearchItems.clear();
        searchView.clearFocus(); //검색 포커스제거

        this.contentTypeId = contentTypeId;
        try {
            searchContent = editSearch;
            Log.i("MyLog:searchContent", searchContent);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        //키보드 내리기 -> 액션바 쓰면 자동으로 사라짐
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        // 별도의 스레드로 검색한 지역의 좌표를 받아옴
        TMapData tmapdata = new TMapData();
        tmapdata.findAllPOI(searchContent, new TMapData.FindAllPOIListenerCallback() {
            @Override
            public void onFindAllPOI(final ArrayList<TMapPOIItem> poiList) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (poiList.isEmpty() == true) {
                            Toast.makeText(getApplicationContext(), "검색결과가 없습니다..", Toast.LENGTH_SHORT).show();
                            return;
                        }

//                        System.out.println("아이템개수 : " + poiList.size());
//                                for(int i=0; i<poiList.size(); i++){
//                                    Log.i("POI Name: " ,  poiList.get(i).getPOIName().toString() + ", " +
//                                            "Address: " + poiList.get(i).getPOIAddress().replace("null", "") + ", " +
//                                            "Point: " + poiList.get(i).getPOIPoint().toString());
//                                }
                        Log.i("MyLog:upperAddrName", poiList.get(0).upperAddrName);
                        Log.i("MyLog:middleAddrName", poiList.get(0).middleAddrName);
                        Log.i("MyLog:lowerAddrName", poiList.get(0).lowerAddrName);
//                                Log.i("MyLog:detailAddrName", poiList.get(0).detailAddrName);
                        Log.i("MyLog:noorLat", poiList.get(0).noorLat);
                        Log.i("MyLog:noorLon", poiList.get(0).noorLon);

                        // Tour API에서 쓸 좌표를 넘긴다.
                        getLocationListFromServer(poiList.get(0).noorLon, poiList.get(0).noorLat);
                    }
                });
            }
        });


    }

    //Tour API에서
    private void getLocationListFromServer(String mapX, String mapY) {

        /**
         * query에 담을 parameter들을 HashMap을 통해 생성
         * http://developers.daum.net/services/apis/shopping/search
         * 위의 페이지에서 '요청 변수' 항목의 내용들을 변수명을 일치시키고
         * 값도 형식에 맞추어 입력해줍니다.
         * */
        // query에 사용될 parameter들을 HashMap을 이용하여 구현
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("arrange", "E"); //정렬 E(거리순)
        parameters.put("MobileOS", "AND");
        parameters.put("contentTypeId", contentTypeId); //관광타입ID
        parameters.put("mapX", mapX);
        parameters.put("mapY", mapY);
        parameters.put("radius", radius); //거리반경(m단위)
        parameters.put("numOfRows", "20"); //numOfRows 검색결과 개수지정

        /**
         * Call<Object> 형의 서버에 요청을 해주는 객체를 만듭니다.
         * networkService의 @GET으로 만들었던 메소드에 parameters를 인자로 넣어줍니다.
         * 그러면 baseUrl에 parameters들이 query의 형태로 덧붙여지고 해당 url로 서버에 요청을 합니다.
         * 비동기 방식은 enqueue 메소드를 사용합니다.
         * enqueue 메소드는 onResponse 콜백메소드를 구현할 Callback<Object>를 인자로 받습니다.
         * (onClickListener과 비슷한 구조죠? 역시나 new 까지 치고 자동완성 하시면 됩니다.)
         */


        /**
         * 서버와 통신을 성공하면 서버가 보내준 응답을 객체(response)를 통해 받아옵니다.
         * response를 성공적으로 받아왔는지 Boolean 타입의 response.isSuccess()로 확인 가능합니다.
         * response.isSuccess()가 true이면 서버와 통신이 성공했고 response를 제대로 받아온 것입니다.
         * response.isSuccess()가 false면 서버와 통신은 됐지만 요청이 잘못되었던가, 서버측에서 무언가 내부 에러가 있다던가
         * 우리가 원하는 response를 받아오지 못한 경우입니다.
         * 이 경우 Status Code가 뭔지 로그를 통해 확인해봅니다.
         *
         * 서버와 통신이 제대로 된 경우(response.isSuccess()가 true인 경우)
         * 서버는 클라이언트에게 Object의 형태로 response를 보내줍니다.
         * 우리는 이를 gson을 통해 jsonString으로 만든 후 다시 새로운 JSONObject를 만들고 channel 객체에 해당하는 JSONObject로 저장합니다.
         * 위에서 저장한 jsonObject를 fromJson 메소드를 통해 ShopResult 객체에 저장합니다.
         * ShopResult에서 우리가 필요한건 shopResult.item 이죠? shopResult에 선언했던 ShopItem 형 객체입니다.
         * 이 ShopItem 객체를 adapter에 setter를 통해 넣어주면 됩니다.
         */
        //NetworkService에 정의된 메소드를 사용하여 서버에서 데이터를 받아옴(비동기식)

        Call<Object> dataCall = tourNetworkService.getLocationList(parameters);
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
                        List<SearchPlace> searchPlaceList = gson.fromJson(jsonObject.getJSONArray("item").toString(), new TypeToken<List<SearchPlace>>() {
                        }.getType());

                        Log.i("MyLog:jsonObject", jsonObject.toString());
                        Log.i("MyLog:SearchPlace", searchPlaceList.get(0).toString());

                        //리스트로 받아와도 TMapPOIItem 클래스와 일치해야 할 것 같음
                        //SearchPlace를 리스트로 받아서 TMapPOIItem 리스트로 만들어준뒤 mMapView.addTMapPOIItem(tmappoi리스트)로 지도에 표시
                        //지도에 띄우기
                        showPlaceOnMap(searchPlaceList);

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

    //지도에 검색된 장소들 띄우기
    private void showPlaceOnMap(List<SearchPlace> searchPlace) {
        ArrayList<TMapPoint> tMapPoints = new ArrayList<TMapPoint>();
        String areaCode = searchPlace.get(0).areacode;

        Log.i("MyLog:size", String.valueOf(searchPlace.size()));
        for (int i = 0; i < searchPlace.size(); i++) {
            int j = 0; //array의 인덱스
            if (areaCode.equals(searchPlace.get(i).areacode) == false) // 지역코드가 다르면 넘어감
                continue;

            TMapPOIItem item = new TMapPOIItem();
            item.Icon = defaultBitmap;
            Log.i("MyLog:place", searchPlace.get(i).title);
            item.noorLon = searchPlace.get(i).mapx;
            item.noorLat = searchPlace.get(i).mapy;
            item.name = searchPlace.get(i).title; //장소명
            item.address = searchPlace.get(i).addr1; //주소
            item.setID(searchPlace.get(i).contentid); //자세히보기 API 요청시 필요함
            item.bizCatName = searchPlace.get(i).firstimage2;//POI아무변수에 썸네일저장 (리스트뷰에 보여짐)
            tMapPOISearchItems.add(j, item);
            tMapPoints.add(j, item.getPOIPoint()); //표시된 장소의 좌표를 기억해서 zoomLevel을 최적화
            j++;
        }
        //이미 표시된 POI 지우기
        mMapView.removeAllTMapPOIItem();

        // TODO 종민 -> 슬라이딩에 리스트 추가
        makeSearchPlaceAdapter(tMapPOISearchItems);
        searchPlaceAdapter.notifyDataSetChanged();

        //TODO: DB에서 가져온 장소 리스트에 추가시키기

        //맵에 POI 띄우기 => 검색한거 먼저 띄우고 저장된거 띄워서 겹치면 덮어쓰기
        mMapView.addTMapPOIItem(tMapPOISearchItems);
        mMapView.addTMapPOIItem(savedPOIPlaceList);


        //찍은 좌표로 맵 이동, 최적화
        TMapInfo info = mMapView.getDisplayTMapInfo(tMapPoints);
        mMapView.setCenterPoint(info.getTMapPoint().getLongitude(), info.getTMapPoint().getLatitude(), true);
        mMapView.setZoomLevel(info.getTMapZoomLevel());
//        Log.i("MyLog:item", item.noorLon);
//        Log.i("MyLog:item", item.getPOIPoint().toString());
        //System.out.println(searchPlace);

        //슬라이딩띄우기
        SlidingDrawer.open();
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initView() {
        //버튼들

        btnComplete = (Button) findViewById(R.id.btnComplete);

        listView = (ListView) findViewById(R.id.listView);

        //지도에 띄울 마크이미지 설정 기본이미지랑 클릭했을때 이미지
        defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.travel_app_8_8_01);
        selectedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.travel_app_7_1_01);
        savedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.save);

        btnZoomOut = (Button) findViewById(R.id.btn_zoom_out);
        btnZoomIn = (Button) findViewById(R.id.btn_zoom_in);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop실행");

    }

    private void initNetworkService() {
        tourNetworkService = ApplicationController.getInstance().getTourNetwork();
        awsNetworkService = ApplicationController.getInstance().getAwsNetwork();
    }

    //TODO:화면 클릭시 POI를 반환하여 이벤트 처리할것
    //명소 클릭시 처리하는 부분
    /*
        Parameters
        - Markerlist : 클릭된 마커들
        - Poilist : 클릭된 POI 들
        - Point : 화면좌표값을 위도, 경도로 반환한 값
        - Pointf : 화면좌표값
    */
    @Override
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> poiArrayList, TMapPoint tMapPoint, PointF pointF) {
        if (!poiArrayList.isEmpty()) { //장소를 클릭했을 경우
            //저장된것 검사해서 넘어감
            for (int i = 0; i < poiArrayList.size(); i++) {
                for (int j = 0; j < savedPOIPlaceList.size(); j++) {
                    if (poiArrayList.get(i).getPOIID() == savedPOIPlaceList.get(j).getPOIID()) {
                        System.out.println("저장된것클릭");
                        selectedPOIItem = poiArrayList.get(0);

                        return false;
                    }
                }
            }

            try {
                if (poiArrayList.get(0).Icon == selectedBitmap) { //같은장소를 또 클릭햇을 경우 선택을 해제한다.
                    selectedPOIItem.Icon = defaultBitmap;
                    mMapView.addTMapPOIItem(poiArrayList); //이미지를 초기화 해준뒤
                    selectedPOIItem = null; //임시저장된것 삭제
                } else {//다른장소를 선택할 경우 이전의 것 이미지 초기화 해준뒤 선택한 장소 이미지 바꾸고 임시저장
                    //get(0)으로 첫번째 거만 클릭이벤트 처리함
                    Toast.makeText(getApplicationContext(), poiArrayList.get(0).getPOIName(), Toast.LENGTH_SHORT).show();

                    if (selectedPOIItem != null) { //이전에 저장된 값이 있으면
                        selectedPOIItem.Icon = defaultBitmap;
                        mMapView.addTMapPOIItem(poiArrayList); //이미지를 초기화 해준뒤
                        selectedPOIItem = null; //임시저장된것 삭제
                    }

                    //선택된 POI를 임시로 저장 -> 나중에 장소 추가, 자세히 보기 할때 저장할 데이터
                    selectedPOIItem = poiArrayList.get(0);
                    selectedPOIItem.Icon = selectedBitmap;
                    mMapView.addTMapPOIItem(poiArrayList); //갱신
//                mMapView.setIconVisibility(true); //SKT타워에 마커가 자꾸 생김

                    //클릭한것 스크롤뷰로 이동
                    int position = 0;
                    int listViewSize = tMapPOISearchItems.size();
                    for (int i = 0; i < listViewSize; i++) {
                        if (selectedPOIItem.getPOIID() == tMapPOISearchItems.get(i).getPOIID())
                            position = i;
                    }
                    if (!SlidingDrawer.isOpened())
                        SlidingDrawer.animateOpen();
                    searchPlaceListView.smoothScrollToPositionFromTop(position, 0);





                    /*
                    View convertView = new View(getApplicationContext());
                    LinearLayout layout;
                    layout = (LinearLayout) searchPlaceAdapter.getView(position, convertView, searchPlaceListView).findViewById(R.id.tempLinear);
                    layout.setBackgroundResource(R.drawable.layout_border);
                    */

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (SlidingDrawer.isOpened())
                SlidingDrawer.animateClose();
        }
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //검색할 시 호출되는 함수
            @Override
            public boolean onQueryTextSubmit(String query) {
                editSearch = query;
                searchArea("12"); //default로 관광지
                return false;
            }

            //검색창에 글자를 입력 시 호출되는 함수
            @Override
            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(SearchPlaceActivity.this, "test for TextChange", Toast.LENGTH_SHORT).show();
                editSearch = newText;
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {
            Toast.makeText(SearchPlaceActivity.this, "share is selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_send) {
            Toast.makeText(SearchPlaceActivity.this, "send is selected", Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        searchView.clearFocus(); //검색 포커스제거
        return true;
    }
}
