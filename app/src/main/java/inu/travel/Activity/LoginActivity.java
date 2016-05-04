package inu.travel.Activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import inu.travel.Component.ApplicationController;
import inu.travel.Model.Person;
import inu.travel.Network.AwsNetworkService;
import inu.travel.R;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


public class LoginActivity extends Activity {
    EditText editID, editPass;
    Button btnLogin, btnJoin;                       //로그인버튼, 회원가입 버튼
    SharedPreferences pref;
    SharedPreferences.Editor edit;
    AwsNetworkService awsNetworkService;
    String Userid;                      //사용자 아이디
    String Userpass;                    //사용자 비밀번호
    ProgressBar progressBar; // 로딩화면을 위한 변수
    TextView loginText;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        //Font.setGlobalFont(this, getWindow().getDecorView());

        initView();                 //view 초기화
        initNetworkService();       //Network 서버 연결
        initSharedPre();    //SharedPreferences 초기화
        loginTest(); //로그인 된적이 있는지를 검사하여서 바로 로그인 시킴
        limitInput();




        Toast.makeText(getApplicationContext(), "로그인 화면입니다.", Toast.LENGTH_LONG).show();


        editPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            //비밀번호를 입력하고 Enter 입력 했을 때 이벤트 처리
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Pass창에서 Enterkey 입력시
                switch (actionId) {
                    //IME_ACTION_DONE 가 Enterkey 처리
                    case EditorInfo.IME_ACTION_DONE:
                        btnLogin.performClick();         //btnLogin 클릭
                        break;
                }
                return false;
            }
        });





        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {               //로그인 버튼 클릭


                final String id = editID.getText().toString();
                final String pass = editPass.getText().toString();

                Toast.makeText(getApplicationContext(), "로그인 버튼이 눌렸습니다.\n"
                        + "입력하신   아이디 : " + id + "\n"
                        + "입력하신 비밀번호 : " + pass, Toast.LENGTH_SHORT).show();

                String Temp = testMD5(pass);
                //temp 임시 암호화 변수
                final Person person = new Person(id,Temp);
                //Person 객체에 아이디, 비밀번호 넣어서 객체를 서버에 전송
                Call<Object> memberLogin = awsNetworkService.memberLogin(person);

                memberLogin.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Response<Object> response, Retrofit retrofit) {
                        if (response.code() == 200) {
                            ActivateProgressbar();      //로딩화면 활성화
                            Toast.makeText(getApplicationContext(), "로그인 OK", Toast.LENGTH_SHORT).show();
                            edit.putString("id", id);
                            edit.putString("pass", pass);
                            edit.commit();
                            //SharedPreferences 아이디 비밀번호 저장
                            Intent intent = new Intent(getApplicationContext(), PlanListActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (response.code() == 503) {
                            Toast.makeText(getApplicationContext(), "로그인실패!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(getApplicationContext(), "완전 오류!", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

        /* 잠시만
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "회원가입 버튼이 눌렸습니다.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplication(), JoinActivity.class);
                startActivity(intent);
            }
        });
        */
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "회원가입 버튼이 눌렸습니다.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplication(), JoinActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        editID = (EditText) findViewById(R.id.editID);
        editPass = (EditText) findViewById(R.id.editPass);
        btnJoin = (Button) findViewById(R.id.btnJoin);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        loginText = (TextView) findViewById(R.id.loginText);
    }
    private void initNetworkService(){
        awsNetworkService = ApplicationController.getInstance().getAwsNetwork();
    }
    private void initSharedPre(){
            pref = getSharedPreferences("login",0);
            edit = pref.edit();
    }


    private void loginTest(){                               //로그인한적이 있는 검사하는 함수
        Userid = pref.getString("id", "null");              //SharedPreferences에서 아이디 가져옴
        Userpass = pref.getString("pass", "null");          //SharedPreferences에서 비밀번호 가져옴

        if(!(Userid.equals("null"))){                       //null이 아니라면
            Toast.makeText(LoginActivity.this, "로그인 기록이 있다."+ Userid + Userpass, Toast.LENGTH_SHORT).show();
            editID.setText(Userid);                         //아이디 설정
            editPass.setText(Userpass);                     //비밀번호 설정
            Toast.makeText(LoginActivity.this, ""+btnLogin.getText()+editID.getText()+editPass.getText(), Toast.LENGTH_SHORT).show();

            btnLogin.post(new Runnable() {
                @Override
                public void run() {
                    btnLogin.performClick();            //스레드 로그인 버튼 클릭
                }
            });

        } else {
            Toast.makeText(LoginActivity.this, "로그인 기록이 없다.", Toast.LENGTH_SHORT).show();
        }

    }

    private void ActivateProgressbar(){                     //로그인시 화면 생성 함수
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "progress", 0, 500); // see this max value coming back here, we animale towards that value
        animation.setDuration (5000); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();
    }

    public String testMD5(String str){              //암호화 함수
        String MD5 = "";
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            MD5 = sb.toString();

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            MD5 = null;
        }
        return MD5;
    }


    protected void limitInput(){                    //editText 한글 제한 함수
        InputFilter filterAlphaNum = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[a-zA-Z0-9]*$");
                if (!ps.matcher(source).matches()) {
                    return "";
                }
                return null;
            }
        };

        editID.setFilters(new InputFilter[]{filterAlphaNum}); // 영문+숫자 설정
        editPass.setFilters(new InputFilter[]{filterAlphaNum}); // 영문+숫자 설정
        editID.setPrivateImeOptions("defaultInputmode=english;"); //기본 키포드 영어설정
        editPass.setPrivateImeOptions("defaultInputmode=english;"); //기본 키포드 영어설정

    }
    public void joinClick(View v){
        Toast.makeText(getApplicationContext(), "회원가입 버튼이 눌렸습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplication(), JoinActivity.class);
        startActivity(intent);
    }



}
