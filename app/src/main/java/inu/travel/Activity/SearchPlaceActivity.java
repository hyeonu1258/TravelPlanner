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
    private Bitmap defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
    private Bitmap selectedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
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

        getSavedPlace();
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
                Log.i("MyLog:searchContent", searchContent);

//Todo :  http://api.visitkorea.or.kr/openapi/service/rest/KorService/searchKeyword? API 사용하여 키워드로 검색할것


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

//                                for(int i=0; i<poiList.size(); i++){
//                                    Log.i("POI Name: " ,  poiList.get(i).getPOIName().toString() + ", " +
//                                            "Address: " + poiList.get(i).getPOIAddress().replace("null", "") + ", " +
//                                            "Point: " + poiList.get(i).getPOIPoint().toString());
//                                }
                                Log.i("MyLog:upperAddrName", poiList.get(0).upperAddrName);
                                Log.i("MyLog:middleAddrName", poiList.get(0).middleAddrName);
                                Log.i("MyLog:lowerAddrName", poiList.get(0).lowerAddrName);
                                Log.i("MyLog:detailAddrName", poiList.get(0).detailAddrName);
                                Log.i("MyLog:noorLat", poiList.get(0).noorLat);
                                Log.i("MyLog:noorLon", poiList.get(0).noorLon);
                                mapX = poiList.get(0).noorLon;
                                mapY = poiList.get(0).noorLat;

                                getLocationListFromServer();
                            }
                        });
                    }
                });

            }
        });
    }

    private void getLocationListFromServer() {

        /**
         * query에 담을 parameter들을 HashMap을 통해 생성
         * http://developers.daum.net/services/apis/shopping/search
         * 위의 페이지에서 '요청 변수' 항목의 내용들을 변수명을 일치시키고
         * 값도 형식에 맞추어 입력해줍니다.
         * */
        // TODO: query에 사용될 parameter들을 HashMap을 이용하여 구현
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("arrange", "B"); //정렬 B(조회순)
        parameters.put("MobileOS", "AND");
        parameters.put("contentTypeId", contentTypeId); //관광타입ID
        parameters.put("mapX", mapX);
        parameters.put("mapY", mapY);
        parameters.put("radius", radius); //거리반경(m단위)
//numOfRows 검색결과 개수지정

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
        // TODO: NetworkService에 정의된 메소드를 사용하여 서버에서 데이터를 받아옴(비동기식)

        retrofit.Call<Object> dataCall = tourNetworkService.getLocationList(parameters);
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

    private void showPlaceOnMap(List<SearchPlace> searchPlace) { //지도에 검색된 장소들 띄우기
        //실제좌표를 화면좌표로 바꾸기
//        int x = mMapView.getMapXForPoint(searchPlace.mapx,searchPlace.mapy);
//        int y = mMapView.getMapXForPoint(searchPlace.mapx,searchPlace.mapy);
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

        //맵에 POI 띄우기
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
            Toast.makeText(getApplicationContext(), arrayList1.get(0).getPOIName(), Toast.LENGTH_SHORT).show();

            //선택된 POI를 임시로 저장 -> 나중에 장소 추가할때 저장할 데이터
            selectedPOIItem = arrayList1.get(0);
        }
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
        return false;
    }
}

