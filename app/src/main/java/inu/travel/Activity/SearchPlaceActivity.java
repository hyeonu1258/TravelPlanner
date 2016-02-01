package inu.travel.Activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import inu.travel.Component.ApplicationController;
import inu.travel.Model.SearchPlace;
import inu.travel.Network.TourNetworkService;
import inu.travel.R;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


public class SearchPlaceActivity extends Activity implements TMapView.OnClickListenerCallback {

    private Context mContext;
    private RelativeLayout mMainRelativeLayout = null;
    private TMapView mMapView = null;
    private TourNetworkService tourNetworkService;
    private String searchContent; // 검색한 내용
    private String mapX, mapY; //투어 API로 보낼 좌표
    private String contentTypeId = "12"; //관광지:12, 숙박:32, 음식점:39
    private String radius = "5000"; //거리반경
    //아이콘 설정
    private Bitmap defaultBitmap;
    private Bitmap selectedBitmap;
    //클릭하여 선택된 POI
    private TMapPOIItem selectedPOIItem;

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
        mContext = this;

        getSavedPlace();
        initView();
        btnClickEvent();


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
//        TMapPoint tpoint = new TMapPoint(37.570841, 126.985302);
//        TMapMarkerItem tItem = new TMapMarkerItem();
//        tItem.setTMapPoint(tpoint);
//        tItem.setName("관광지1");
//        tItem.setVisible(TMapMarkerItem.VISIBLE);
//
//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
//        tItem.setIcon(bitmap);
//
//        tItem.setPosition(0.5f, 1.0f);
//        mMapView.addMarkerItem("tour1", tItem);
    }

    //Todo: 이전에 만든 플랜에서 수정을 눌렀을 경우 이전에 저장된 장소를 서버에서 가져와야 함
    private void getSavedPlace() {

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
                //Todo : url인코딩 해야됨
                try {
                    searchContent = URLEncoder.encode(searchContent, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                Log.i("MyLog:searchContent", searchContent);

// TODO: NetworkService에 정의된 메소드를 사용하여 서버에서 데이터를 받아옴(비동기식)

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //Todo :  http://api.visitkorea.or.kr/openapi/service/rest/KorService/searchKeyword? API 사용하여 키워드로 검색할것
                        HashMap<String, String> parameters = new HashMap<>();

                        parameters.put("arrange", "B"); //정렬 B(조회순)
                        parameters.put("MobileOS", "AND");
                        parameters.put("contentTypeId", contentTypeId); //관광타입ID
                        parameters.put("keyword", searchContent); //검색

                        retrofit.Call<Object> dataCall = tourNetworkService.getLocationListByKeyword(parameters);
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

//                                Log.i("MyLog:jsonObject", jsonObject.toString());
//                                Log.i("MyLog:SearchPlace", searchPlaceList.get(0).toString());

                                        //리스트로 받아와도 TMapPOIItem 클래스와 일치해야 할 것 같음
                                        //TODO: SearchPlace를 리스트로 받아서 TMapPOIItem 리스트로 만들어준뒤 mMapView.addTMapPOIItem(tmappoi리스트)로 지도에 표시
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
                });

//Todo :  http://api.visitkorea.or.kr/openapi/service/rest/KorService/searchKeyword? API 사용하여 키워드로 검색할것
            }
        });
    }


    private void showPlaceOnMap(List<SearchPlace> searchPlace) { //지도에 검색된 장소들 띄우기

        ArrayList<TMapPOIItem> tMapPOIItems = new ArrayList<TMapPOIItem>();

        Log.i("MyLog:size", String.valueOf(searchPlace.size()));
        for (int i = 0; i < searchPlace.size(); i++) {
            TMapPOIItem item = new TMapPOIItem();
            item.Icon = defaultBitmap;
            Log.i("MyLog:place", searchPlace.get(i).title);
            item.noorLon = searchPlace.get(i).mapx;
            item.noorLat = searchPlace.get(i).mapy;
            item.name = searchPlace.get(i).title;
            tMapPOIItems.add(i, item);
        }

        //이미 표시된 POI 지우기
        mMapView.removeAllTMapPOIItem();
//
        //맵에 POI 띄우기
//        Log.i("MyLog error", tMapPOIItems.get(8).name);
        mMapView.addTMapPOIItem(tMapPOIItems);


//        Log.i("MyLog:item", item.noorLon);
//        Log.i("MyLog:item", item.getPOIPoint().toString());
        //System.out.println(searchPlace);
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

        //지도에 띄울 마크이미지 설정 기본이미지랑 클릭했을때 이미지
        defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
        selectedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initNetworkService() {
        tourNetworkService = ApplicationController.getInstance().getTourNetwork();
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
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        if (!arrayList1.isEmpty()) {
//            for(int i=0; i<arrayList1.size(); i++) {
//                Log.i("MyLog:clicked POI ", arrayList1.get(i).getPOIName());
//            }

            //get(0)으로 첫번째 거만 클릭이벤트 처리함
            try {

                Toast.makeText(getApplicationContext(), arrayList1.get(0).getPOIName(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //선택된 POI를 임시로 저장 -> 나중에 장소 추가할때 저장할 데이터
//            selectedPOIItem = arrayList1.get(0);
        }
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        return false;
    }
}

