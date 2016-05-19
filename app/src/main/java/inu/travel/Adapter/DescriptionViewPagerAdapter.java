package inu.travel.Adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import inu.travel.R;

/**
 * Created by kimjongmin on 16. 5. 12..
 */
public class DescriptionViewPagerAdapter extends PagerAdapter {
    LayoutInflater inflater;

    public DescriptionViewPagerAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        //이미지 개수 리턴
        return 5;
    }

    // ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
    // 쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
    // 첫번째 파라미터 : ViewPager
    // 두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;

        // 새로운 View 객체를 Layoutinflater를 이용해서 생성
        // 만들어질 View의 설계는 res폴더>>layout폴더>>viewpater_childview.xml 레이아웃 파일 사용
        view = inflater.inflate(R.layout.viewpager_description, null);

        // 만들어진 View안에 있는 ImageView 객체 참조
        // 위에서 inflated 되어 만들어진 view로부터 findViewById()를 해야 하는 것에 주의.
        ImageView img = (ImageView) view.findViewById(R.id.imgViewpager);

        // ImageView에 현재 position 번째에 해당하는 이미지를 보여주기 위한 작업
        // 현재 position에 해당하는 이미지를 setting
        img.setImageResource(R.mipmap.description_01 + position);
//        img.setImageResource(R.drawable.ic_launcher);
        //ViewPager에 만들어 낸 View 추가
        container.addView(view);

        //Image가 세팅된 View를 리턴
        return view;
    }

    // 화면에 보이지 않은 View는 파괴를 해서 메모리를 관리함.
    // 첫번째 파라미터 : ViewPager
    // 두번째 파라미터 : 파괴될 View의 인덱스 (가장 처음부터 0,1,2,3...)
    // 세번째 파라미터 : 파괴될 객체 (더 이상 보이지 않은 View 객체)
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //ViewPager에서 보이지 않는 View는 제거
        //세번째 파라미터가 View 객체 이지만 데이터 타입이 Object여서 형변환 실시
        container.removeView((View) object);
    }

    // instantiateItem() 메소드에서 리턴된 Ojbect가 View가  맞는지 확인하는 메소드
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
