package inu.travel.Network;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import inu.travel.Model.Person;
import inu.travel.Model.Place;
import inu.travel.Model.PlaceList;
import inu.travel.Model.PlanList;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.QueryMap;

public interface AwsNetworkService {

    // Aws_URL
    //String baseUrl = "http//52.34.206.80:3000";
//    String baseUrl = "http://192.168.1.103:3000";
//    String baseUrl = "http://117.16.198.47:3000";
    String baseUrl = "http://192.168.2.80:3000";

    // TODO: 1. 서버와 네트워킹을 하기 위한 서비스(인터페이스로 구현)

    /**
     * GET 방식과 POST 방식의 사용법을 잘 이해하셔야 합니다.
     * GET("/경로") 경로는 서버 파트에게 물어보세요. (※baseUrl 뒤에 붙는 경로입니다.ex) http://baseUrl/경로)
     * ("/경로/{식별자}) ~~(@Path{"식별자"} String value) 어떤 식별자를 통해 리소스를 구분하여 요청합니다. uri의 정의 기억나시죠? ex) http://baseUrl/경로/value
     * POST 방식은 @Body 에 뭔가를 담아서 보내야하죠?
     */

    @POST("/join")
    Call<Object> memberJoin(@Body Person person);

    @POST("/login")
    Call<Object> memberLogin(@Body Person person);

    @GET("/plan/list/{id}")
    Call<List<PlanList>> getPlanList(@Path("id") String id);

    @POST("/plan/list/add")
    Call<Object> makePlanList(@Body PlanList planList);

    @POST("/plan/list/delete")
    Call<Object> removePlanList(@Body PlanList planList);

    @POST("/place/add")
    Call<Object> addPlace(@Body PlaceList placeList);

    @POST("/place/load")
    Call<ArrayList<Place>> getPlaceList(@QueryMap HashMap<String, String> param);

}