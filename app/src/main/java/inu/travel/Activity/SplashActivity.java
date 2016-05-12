package inu.travel.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import inu.travel.R;

public class SplashActivity extends Activity {
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        Toast.makeText(getApplicationContext(), "로딩 화면입니다.", Toast.LENGTH_SHORT).show();

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(getApplication(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
