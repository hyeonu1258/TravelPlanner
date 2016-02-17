package inu.travel.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    EditText editID, editPass, editEmail;
    Button btnSubmit;
    AwsNetworkService awsNetworkService;
//    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        initView();
        initNetworkService();
        //initRealM();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editID.getText().toString();
                String pass = editPass.getText().toString();
                String email = editEmail.getText().toString();

                Person person = new Person(id, pass, email);
                Call<Object> memberJoin = awsNetworkService.memberJoin(person);
                memberJoin.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Response<Object> response, Retrofit retrofit) {
                        if (response.code() == 200) {
                            Toast.makeText(getApplicationContext(), "회원등록 OK", Toast.LENGTH_SHORT).show();
                            /*
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            */
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

                    }
                });

                /*
                Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.\n"
                                + "회원아이디 : " + id + " 비밀번호 : " + pass,
                        Toast.LENGTH_SHORT).show();
                */

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

//    private void initRealM() {
//        realm = Realm.getInstance(new RealmConfiguration.Builder(getApplicationContext())
//                .name("PlanList.Realm5").build());
//    }

    private void savePerson() {

        /*
        realm.beginTransaction(); //데이터 변경을 알리는 코드
        realm.copyToRealm(); //기존 realm의 Person 테이블에 방금 새로 만든 Person 객체의 데이터를 추가
        realm.commitTransaction(); //데이터 변경사항을 저장하는 코드
        */
    }
}
