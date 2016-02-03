package inu.travel.Network;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface TourNetworkService {

    /**
     * Tourapi에서 제공해주는 API_KEY를 String 형으로 저장
     * GET 어노테이션과 메소드 구현
     * 서버에 요청할 디렉토리를 GET 어노테이션에 인자로 넣어줌
     * Call<받고자 하는 데이터 타입> (request에 추가될 사항들)
     * request에 추가될 사항들을 예로 들면
     * Body가 될 수도 있고(POST 방식의 경우)
     * Path가 될 수도 있고
     * QueryMap, Query가 될 수도 있고
     * Body, Path, Query가 다 들어갈 수도 있습니다.
     */
    String apiKey = "5lBVFahPjOQQ3FgxFRffiOuRE7AvvXWzDVzIS5OAbRFOJA7Iye9tQhjPLKpgyJ2hPyvqRXsi3yurqH2oNdXnjA%3D%3D";
    String appName = "TravelPlanner";
    String baseUrl = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/";

    //장소 추가 화면이 시작할 때,
    //내주변 좌표(한국관광공사 주변)에서 1000m 이내에 있는 모든타입의 관광정보 조회 (한페이지에 10개씩, 첫페이지며, 리스트를 조회순으로 조회)
    //    http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?ServiceKey=5lBVFahPjOQQ3FgxFRffiOuRE7AvvXWzDVzIS5OAbRFOJA7Iye9tQhjPLKpgyJ2hPyvqRXsi3yurqH2oNdXnjA%3D%3D&mapX=126.981611&mapY=37.568477&radius=1000&pageNo=1&numOfRows=10&listYN=Y&arrange=B&MobileOS=AND&MobileApp=AppTesting
    // TODO: Get 방식으로 요청
    @GET("locationBasedList")
    Call<Object> getLocationList(@QueryMap HashMap<String, String> parameter);

    // Todo : 상세보기 요청
    @GET("detailCommon")
    Call<Object> getDetailCommon(@QueryMap HashMap<String, String> parameter);
    /*
        MobileOS	OS 구분	ETC	필수	IOS (아이폰), AND (안드로이드),
        WIN (윈도우폰), ETC
        MobileApp	서비스명		필수	서비스명=어플명
        contentId	콘텐츠ID		필수	콘텐츠ID
        contentTypeId	관광타입 ID			관광타입(관광지, 숙박등) ID
        defaultYN	기본정보조회	N		제목, 등록일, 수정일, 홈페이지, 전화번호, 전화번호명, 교과서속여행지여부
        firstImageYN	대표이미지조회	N		원본, 썸네일대표이미지
        areacodeYN	지역코드조회	N		지역코드, 시군구코드
        catcodeYN	서비스분류코드조회	N		대,중,소분류코드조회
        addrinfoYN	주소조회	N		주소, 상세주소조회
        mapinfoYN	좌표조회	N		좌표X, Y 조회
        overviewYN	개요조회	N		콘텐츠개요조회
     */
}
