package inu.travel.Component;

import android.app.Application;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URLEncoder;

import inu.travel.Network.AwsNetworkService;
import inu.travel.Network.TourNetworkService;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class ApplicationController extends Application {

    /**
     * Application 클래스를 상속받은 ApplicationController 객체는 어플리케이션에서 단 하나만 존재해야 합니다.
     * 따라서 내부에 ApplicationController 형의 instance를 만들어준 후
     * getter를 통해 자신의 instance를 가져오는 겁니다.
     */
    // TODO: ApplicationController 인스턴스 생성 및 getter 설정
    private static ApplicationController instance;

    public static ApplicationController getInstance() {
        return instance;
    }

    // NetworkService도 마찬가지로 Application을 상속받은 ApplicationController 내에서 관리해주는 것이 좋습니다.
    // TODO: TourNetworkService 인스턴스 생성 및 getter 설정
    private TourNetworkService tourNetworkService;
    private AwsNetworkService awsNetworkService;

    public TourNetworkService getTourNetwork() {
        return tourNetworkService;
    }

    public AwsNetworkService getAwsNetwork() {
        return awsNetworkService;
    }

    //통신할 서버의 주소입니다. 클라이언트는 이 주소에 query 또는 path 등을 추가하여 요청합니다.
    // TODO:baseUrl 설정
    final String tourBaseUrl = TourNetworkService.baseUrl;
    final String AwsBaseUrl = AwsNetworkService.baseUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * 어플이 실행되자마자 ApplicationController가 실행됩니다.
         * 자신의 instance를 생성하고 networkService를 만들어줍니다.
         */
        Log.i("MyTag", "Application 객체가 가장 먼저 실행됩니다.");
        // TODO: 인스턴스 가져오고 서비스 실행
        ApplicationController.instance = this;
        this.buildTourService();
        this.buildAwsService();
    }

    private void buildTourService() {

        /**
         * 쿠키를 관리하기 위해 CookieHandler를 만들어주고
         * OkHttpClient 객체에 setCookieHandler로 달아줍니다.
         */
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        OkHttpClient client = new OkHttpClient();
        client.setCookieHandler(cookieManager);

        /**
         * OkHttpClient 객체인 client에는 interceptors라는 것이 있는데
         * 이는 클라이언트가 요청할 request를 원하는대로 만들어주기 위해 사용합니다.
         * 우리는 request에 API_KEY를 query에 담아서 넣어야 합니다.
         */
        client.interceptors().clear();
        client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                /**
                 * Chain 객체는 서버와 통신을 하는데 필요한 request 또는 proceed 등의 메소드를 포함하고 있습니다.
                 * Request 객체인 original은 원래 우리가 요청한 요청(chain,requset() <- 원래 우리가 요청했던 request를 반환하는 메소드)을 저장합니다.
                 * HttpUrl 객체를 통해 원래의 url을 가져온 후 우리가 원하는 query 파라미터(apikey)를 추가합니다.
                 * 이 과정은 HttpUrl.Builder를 통해 이뤄집니다.
                 */
                Request original = chain.request();
                // TODO: url에 apikey 쿼리문 추가하기
                HttpUrl originalHttpUrl = original.httpUrl();
                HttpUrl.Builder urlBuilder = originalHttpUrl.newBuilder()
                        .addQueryParameter(
                                "ServiceKey", tourNetworkService.API_KEY
                        ).addQueryParameter(
                                "_type", "json"
                        );

                HttpUrl httpUrl = urlBuilder.build();
//                httpUrl.encodedQuery();
                // TODO: %253D 오류 url 인코딩오류같음
                Log.i("MyTag", "apikey가 추가된 Url : " + httpUrl.toString());

                /**
                 * Request 빌더를 통해 위에서 새로 생성한 url을 달아주고
                 * 기존 요청의 HTTP 메소드와 담고 있던 body를 그대로 사용합니다.
                 */

                // TODO: requestBuilder를 통해 새로운 request 만들기
                Request.Builder requestBuilder = original.newBuilder().url(httpUrl)
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();

                //chian.proceed() 메소드는 Request 객체를 인자로 받아 서버에 보내주고 응답을 받아와서 반환합니다. 따라서 Response 객체로 반환을 하겠죠?
                return chain.proceed(request);
            }
        });

        /**
         * 이제 NetworkService를 만들어줘야 합니다.
         * 위에서 작성했던 코드들이 이미 있던 요청에 무언가를 바꿔주는 것이라면
         * 지금 작성하는 코드는 Retrofit 객체를 사용하여 통신을 위해 필요한
         * NetworkService를 만드는 과정입니다.
         * baseUrl이 있어야 하고, JSON으로 받아온 데이터를 객체로 변환해주는 GsonConverterFactory가 필요합니다.
         * 위에서 만든 interceptor는 client 객체의 메소드였죠? 따라서 위에서 만든 client를 해당 네트워크의 클라이언트로 설정합니다.
         */

        // TODO: retrofit 객체 만들기
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(tourBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client((client))
                .build();
        //retrofit.create(TourNetworkService.class)를 통해 새로운 NetworkService를 만들어줍니다.
        tourNetworkService = retrofit.create(TourNetworkService.class);
    }

    private void buildAwsService() {
        if (awsNetworkService == null) {
            Gson gson = new GsonBuilder()
                    .create();

            GsonConverterFactory factory = GsonConverterFactory.create(gson);  //서버에서 json 형식으로 데이터를 보내고 이를 파싱해서 받아오기 위해서 사용합니다.

            // TODO: Retrofit.Builder()를 이용해 Retrofit 객체를 생성한 후 이를 이용해 AwsnetworkService 정의
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(AwsBaseUrl)
                    .addConverterFactory(factory)
                    .build();

            awsNetworkService = retrofit.create(AwsNetworkService.class);
        }
    }

}
