package inu.travel.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import inu.travel.R;

public class LoginActivity extends Activity {
    EditText editID, editPass;
    Button btnLogin, btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

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
}

