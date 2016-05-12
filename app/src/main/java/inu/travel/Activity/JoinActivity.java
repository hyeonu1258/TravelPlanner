package inu.travel.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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


public class JoinActivity extends Activity {
    TextView textView1, textView2;
    EditText editID, editPass, editEmail; // id, pass, email 칸
    Button btnSubmit;                     //회원가입 버튼
    SharedPreferences pref;
    SharedPreferences.Editor edit;
    AwsNetworkService awsNetworkService;
    Typeface typefaceRegular;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join2);


      //  Font.setGlobalFont(this, getWindow().getDecorView());

        initView();                 //View 초기화
        initNetworkService();       //Network,서버  연결
        initSharedPre();        //SharedPreferences 초기화
        limitInput();           //edittext 한글 제한




        editEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            //이메일을 입력하고 Enter 입력 했을 때 이벤트 처리
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    //Email 에서 Enterkey 눌렀을 때
                switch (actionId) {
                    //IME_ACTION_DONE 이게 Enterkey 이벤트
                    case EditorInfo.IME_ACTION_DONE:
                        btnSubmit.callOnClick();            //btnSubmit 클릭됨.
                        break;

                }
                return false;
            }
        });




        btnSubmit.setOnClickListener(new View.OnClickListener() {       //회원가입 완료 버튼 클릭
            String id;
            String pass;
            String email;

            @Override
            public void onClick(View v) {
                id = editID.getText().toString();
                pass = editPass.getText().toString();
                email = editEmail.getText().toString();
                if (!checkEmailForm(email)) {               //이메일 형시 검사
//                    Toast.makeText(JoinActivity.this, "email형식이 틀려", Toast.LENGTH_SHORT).show();
                } else {
                    String temp = testMD5(pass);            //MD5를 통해서 비밀번호 암호화
                    //temp 임시 암호화 변수
//                    Toast.makeText(JoinActivity.this, "" + testMD5(pass), Toast.LENGTH_SHORT).show();

                    //Person 객체 생성해서 서버로 보냄
                    Person person = new Person(id, temp, email);
                    Call<Object> memberJoin = awsNetworkService.memberJoin(person);
                    memberJoin.enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Response<Object> response, Retrofit retrofit) {
                            if (response.code() == 200) {
//                                Toast.makeText(getApplicationContext(), "회원등록 OK", Toast.LENGTH_SHORT).show();
                                edit.putString("id", id);
                                edit.putString("pass", pass);
                                edit.commit();
                                //id, pass SharedPreferences 저장한다.
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (response.code() == 406) {
                                Toast.makeText(getApplicationContext(), "존재하는 email 입니다!", Toast.LENGTH_LONG).show();
                            } else if (response.code() == 405) {
                                Toast.makeText(getApplicationContext(), "존재하는 ID 입니다!", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            // debug
                            Log.i("Test", "실패");
                            Toast.makeText(JoinActivity.this, "회원가입 오류", Toast.LENGTH_SHORT).show();

                        }
                    });


                }
            }
        });
    }

    private void initView() {
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        editID = (EditText) findViewById(R.id.editID);
        editPass = (EditText) findViewById(R.id.editPass);
        editEmail = (EditText) findViewById(R.id.editEmail);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        changeFont();           //font
    }

    private void initNetworkService() {
        awsNetworkService = ApplicationController.getInstance().getAwsNetwork();
    }
    private void changeFont(){
        typefaceRegular = Typeface.createFromAsset(getAssets(),"NanumGothic.ttf");          //font
        editID.setTypeface(typefaceRegular);
        editPass.setTypeface(typefaceRegular);
        editEmail.setTypeface(typefaceRegular);
    }

    private void initSharedPre() {
        pref = getSharedPreferences("login",0);
        edit = pref.edit();
        //SharedPreferences 초기화
    }


    public String testMD5(String str){                  //암호화 함수
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


    protected void limitInput() {               //Edittext 한글 제한
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
        editID.setPrivateImeOptions("defaultInputmode=english;"); //기본 키포드 영어 설정
        editPass.setPrivateImeOptions("defaultInputmode=english;"); //기본 키보드 영어 설정

    }

    public boolean checkEmailForm(String src){              //이메일 형식검사
        String emailRegex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        return Pattern.matches(emailRegex, src);
    }
}
