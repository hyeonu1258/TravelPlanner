package inu.travel.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import inu.travel.Adapter.FacilityAdapter;
import inu.travel.Adapter.SearchPlaceAdapter;
import inu.travel.Component.ApplicationController;
import inu.travel.Model.SearchPlace;
import inu.travel.Network.TourNetworkService;
import inu.travel.R;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import android.os.Handler;


public class FacilityActivity extends Activity implements TMapView.OnClickListenerCallback {
    Handler handler = new Handler();  // 외부쓰레드 에서 메인 UI화면을 그릴때 사용

    private Context mContext;
    private RelativeLayout mMainRelativeLayout = null;
    private TMapView mMapView = null;
    private TourNetworkService tourNetworkService;
    private String contentTypeId; //관광지:12, 숙박:32, 음식점:39
    private String radius = "5000"; //거리반경

    //아이콘 설정
    private Bitmap tourBitmap;
    private Bitmap defaultBitmap;
    private Bitmap selectedBitmap;
    //선택된 POI
    private TMapPOIItem selectedPOIItem = null;
    //검색해서 나오는 POI객체리스트
    ArrayList<TMapPOIItem> tMapPOISearchItems;

    //지도 위 버튼들
    Button btnMT;
    Button btnEat;
    Button btnEdit;

    //관광지좌표
    double mapX;
    double mapY;
    FacilityAdapter facilityAdapter;

    ListView listView;

    /**
     * onCreate()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_facility);
        initNetworkService();
        mContext = this;
        mMainRelativeLayout = (RelativeLayout) findViewById(R.id.map_view);
        mMapView = new TMapView(this);
        mMainRelativeLayout.addView(mMapView);

        initPlace();
        initView();
        initPoint();
        btnClickEvent();



    }

    private void initPlace() {
        tMapPOISearchItems = new ArrayList<TMapPOIItem>();
    }

    private void makeAdapter() {
        facilityAdapter = new FacilityAdapter(tMapPOISearchItems, getApplicationContext());
        listView.setAdapter(facilityAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                System.out.println("클릭");
                double x = Double.parseDouble(tMapPOISearchItems.get(position).noorLon);
                double y = Double.parseDouble(tMapPOISearchItems.get(position).noorLat);

                //위치찾기
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

                if(viewId == R.id.btnFacilityViewDetail) {
                    viewDetail(selectedPOIItem);
                }
            }

        });

    }

    private void initPoint() {
        Intent i = getIntent();
        mapX = i.getDoubleExtra("mapX",0.0);
        mapY = i.getDoubleExtra("mapY",0.0);

        //관광지 지도에 띄우기
        TMapMarkerItem tourMarkerItem = new TMapMarkerItem();
        TMapPoint tpoint = new TMapPoint(mapY, mapX);
        tourMarkerItem.setTMapPoint(tpoint);
        tourMarkerItem.setVisible(TMapMarkerItem.VISIBLE);
        tourMarkerItem.setIcon(tourBitmap);

        mMapView.setCenterPoint(mapX,mapY , false);
        mMapView.addMarkerItem("tourMarker", tourMarkerItem);
    }


    private void btnClickEvent() {
        btnMT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img1);
                searchArea("32");
            }
        });

        btnEat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img2);
                searchArea("39");
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //상세히보기
    private void viewDetail(TMapPOIItem item) {
        // TODO: getDetailCommon API 사용하여 상세정보 뽑기
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("MobileOS", "AND");
        parameters.put("contentId", item.getPOIID()); //POIID
        parameters.put("defaultYN", "Y"); //기본정보
        parameters.put("firstImageYN", "Y"); //이미지
        parameters.put("addrinfoYN", "Y"); //주소
        parameters.put("overviewYN", "Y"); //개요

        // TODO: NetworkService에 정의된 메소드를 사용하여 서버에서 데이터를 받아옴(비동기식)

        retrofit.Call<Object> dataCall = tourNetworkService.getDetailCommon(parameters);
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

                        //Todo : 상세히 보기 다이얼 로그 띄우기
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
        try {
            Log.i("MyLog:detailname", selectedPOIItem.name);
            if (searchPlace.addr1 != null)
                Log.i("MyLog:detailaddr1", searchPlace.addr1);
            if (searchPlace.addr2 != null)
                Log.i("MyLog:detailaddr2", searchPlace.addr2);
            if (searchPlace.tel != null)
                Log.i("MyLog:detailtel", searchPlace.tel);
            if (searchPlace.homepage != null)
                Log.i("MyLog:detailhomepage", searchPlace.homepage);
            if (searchPlace.overview != null)
                Log.i("MyLog:detailoverview", searchPlace.overview);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            public void run() {
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


        if (selectedPOIItem.name != null)
            txtDetailName.setText(selectedPOIItem.name);
        if (searchPlace.addr1 != null)
            txtDetailAddr.setText(searchPlace.addr1);
        if (searchPlace.addr2 != null)
            txtDetailAddr.append(searchPlace.addr1);
        if (searchPlace.homepage != null)
            txtDetailHomepage.setText(Html.fromHtml(searchPlace.homepage));
        if (searchPlace.tel != null)
            txtDetailTel.setText(searchPlace.tel);
        if (searchPlace.overview != null)
            txtDetailOverview.setText(Html.fromHtml(searchPlace.overview));

        //다이얼로그를 만들기 위한 빌더를 만들어줍니다. 이 때 인자는 해당 액티비티.this 로 해야합니다. 다이얼로그를 만들기 위한 틀입니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(FacilityActivity.this);
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
        if (tMapPOISearchItems.isEmpty() != true) {
            tMapPOISearchItems.clear();
        }
        this.contentTypeId = contentTypeId;
        // Tour API에서 쓸 좌표를 넘긴다.
        getLocationListFromServer(mapX, mapY);
    }

    //Tour API에서
    private void getLocationListFromServer(double mapX, double mapY) {
        // query에 사용될 parameter들을 HashMap을 이용하여 구현
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("arrange", "E"); //정렬 E(거리순)
        parameters.put("MobileOS", "AND");
        parameters.put("contentTypeId", contentTypeId); //관광타입ID
        parameters.put("mapX", String.valueOf(mapX));
        parameters.put("mapY", String.valueOf(mapY));
        parameters.put("radius", radius); //거리반경(m단위)
        parameters.put("numOfRows", "20"); //numOfRows 검색결과 개수지정

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
        makeAdapter();
        facilityAdapter.notifyDataSetChanged();

        //맵에 POI 띄우기 => 검색한거 먼저 띄우고 저장된거 띄워서 겹치면 덮어쓰기
        mMapView.addTMapPOIItem(tMapPOISearchItems);

        //찍은 좌표로 맵 이동, 최적화
        TMapInfo info = mMapView.getDisplayTMapInfo(tMapPoints);
        mMapView.setCenterPoint(info.getTMapPoint().getLongitude(), info.getTMapPoint().getLatitude(), true);
        mMapView.setZoomLevel(info.getTMapZoomLevel());
    }


    private void initView() {
        //버튼들
        btnMT = (Button) findViewById(R.id.btnMT);
        btnEat = (Button) findViewById(R.id.btnEat);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        listView = (ListView) findViewById(R.id.facilityItemListView);
        //지도에 띄울 마크이미지 설정 기본이미지랑 클릭했을때 이미지
        tourBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
        defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
        selectedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        defaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img2);
        searchArea("39");
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
    public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> poiArrayList, TMapPoint tMapPoint, PointF pointF) {
        if (!poiArrayList.isEmpty()) { //장소를 클릭했을 경우
            //저장된것 검사해서 넘어감
            for (int i = 0; i < poiArrayList.size(); i++) {
                for (int j = 0; j < tMapPOISearchItems.size(); j++) {
                    if (poiArrayList.get(i).getPOIID() == tMapPOISearchItems.get(j).getPOIID()) {
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

                    //클릭한것 스크롤뷰로 이동
                    int position = 0;
                    int listViewSize = tMapPOISearchItems.size();
                    for (int i = 0; i < listViewSize; i++) {
                        if (selectedPOIItem.getPOIID() == tMapPOISearchItems.get(i).getPOIID())
                            position = i;
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {

        return false;
    }


}

