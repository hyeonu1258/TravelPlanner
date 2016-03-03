package inu.travel.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        initView();                 //View 초기화
        initNetworkService();       //Network,서버  연결
        initSharedPre();        //SharedPreferences 초기화




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
                String temp = testMD5(pass);            //MD5를 통해서 비밀번호 암호화
                //temp 임시 암호화 변수
                Toast.makeText(JoinActivity.this, ""+testMD5(pass), Toast.LENGTH_SHORT).show();

                //Person 객체 생성해서 서버로 보냄
                Person person = new Person(id, temp, email);
                Call<Object> memberJoin = awsNetworkService.memberJoin(person);
                memberJoin.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Response<Object> response, Retrofit retrofit) {
                        if (response.code() == 200) {
                            Toast.makeText(getApplicationContext(), "회원등록 OK", Toast.LENGTH_SHORT).show();
                            edit.putString("id", id);
                            edit.putString("pass",pass);
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
        });
    }

    private void initView() {
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        editID = (EditText) findViewById(R.id.editID);
        editPass = (EditText) findViewById(R.id.editPass);
        editEmail = (EditText) findViewById(R.id.editEmail);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
    }

    private void initNetworkService() {
        awsNetworkService = ApplicationController.getInstance().getAwsNetwork();
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
}
