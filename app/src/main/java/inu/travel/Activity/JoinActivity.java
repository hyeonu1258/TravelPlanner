package inu.travel.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import inu.travel.R;


public class JoinActivity extends Activity {
    TextView textView1, textView2;
    EditText editID, editPass;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        initView();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editID.getText().toString();
                String pass = editPass.getText().toString();

                Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.\n"
                                                        + "회원아이디 : " + id + " 비밀번호 : " + pass,
                                                        Toast.LENGTH_SHORT).show();

                finish();
            }
        });
    }

    private void initView() {
        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        editID = (EditText)findViewById(R.id.editID);
        editPass = (EditText)findViewById(R.id.editPass);
        btnSubmit = (Button)findViewById(R.id.btnSubmit);
    }
}

