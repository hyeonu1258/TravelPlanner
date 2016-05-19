package inu.travel.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import inu.travel.Adapter.DescriptionViewPagerAdapter;
import inu.travel.R;

public class DescriptionActivity extends AppCompatActivity {
    private ViewPager pager;
    private DescriptionViewPagerAdapter adapter;

    private LinearLayout pagerIndicator;
    private int dotsCount;
    private ImageView[] dots;

    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        initView();
        initAdapter();
        setUiPageViewController();
        pagerEvent();
        btnClickEvent();
    }

    private void initView() {
        pager = (ViewPager) findViewById(R.id.pager);
        pagerIndicator = (LinearLayout) findViewById(R.id.viewPagerCountDots);
        btnStart = (Button) findViewById(R.id.btnStart);
    }

    private void initAdapter() {
        // ViewPager에 설정할 Adapter 객체 생성
        // ListView에서 사용하는 Adapter와 같은 역할.
        // 다만. ViewPager로 스크롤 될 수 있도록 되어 있다는 것이 다름
        // PagerAdapter를 상속받은 DescriptionViewPagerAdapter 객체 생성
        // DescriptionViewPagerAdapter에 LayoutInflater 객체 전달
        adapter = new DescriptionViewPagerAdapter(getLayoutInflater());

        // ViewPager에 Adapter 설정
        pager.setAdapter(adapter);
    }

    private void setUiPageViewController() {

        dotsCount = adapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(getResources().getDrawable(R.mipmap.dot_nonselecteditem));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            pagerIndicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.mipmap.dot_selecteditem));
    }

    public void pagerEvent() {
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < dotsCount; i++) {
                    dots[i].setImageDrawable(getResources().getDrawable(R.mipmap.dot_nonselecteditem));
                }

                dots[position].setImageDrawable(getResources().getDrawable(R.mipmap.dot_selecteditem));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void btnClickEvent() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PlanListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
