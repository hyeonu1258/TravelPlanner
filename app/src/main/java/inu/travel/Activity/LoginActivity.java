package inu.travel.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    Button btnLogin, btnJoin;
    AwsNetworkService awsNetworkService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        initNetworkService();

        Toast.makeText(getApplicationContext(), "로그인 화면입니다.", Toast.LENGTH_LONG).show();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editID.getText().toString();
                String pass = editPass.getText().toString();
                boolean bool = true;


                Toast.makeText(getApplicationContext(), "로그인 버튼이 눌렸습니다.\n"
                        + "입력하신   아이디 : " + id + "\n"
                        + "입력하신 비밀번호 : " + pass, Toast.LENGTH_SHORT).show();

                Person person = new Person(id,pass);

                Call<Object> memberLogin = awsNetworkService.memberLogin(person);

                memberLogin.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Response<Object> response, Retrofit retrofit) {
                        if (response.code() == 200) {
                            Toast.makeText(getApplicationContext(), "로그인 OK", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), PlanListActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (response.code() == 503) {
                            Toast.makeText(getApplicationContext(), "로그인실패!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {

                    }
                });





                Intent intent = new Intent(getApplicationContext(), PlanListActivity.class);
                startActivity(intent);

            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
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
    }
    private void initNetworkService(){
        awsNetworkService = ApplicationController.getInstance().getAwsNetwork();
    }
}
