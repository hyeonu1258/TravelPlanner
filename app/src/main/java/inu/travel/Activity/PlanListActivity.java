package inu.travel.Activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;


import java.util.ArrayList;

import inu.travel.Adapter.PlanAdapter;
import inu.travel.Model.PlanList;
import inu.travel.R;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class PlanListActivity extends Activity {

    private ArrayList<PlanList> planDatas = null;

    EditText editDetail_d;
    GridView gridView;
    private Realm realm;
    PlanAdapter adapter;
    static long count;
    EditText editName_d;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);
        gridView = (GridView) findViewById(R.id.gridView);
        editName_d = (EditText) findViewById(R.id.editName_d);
        editDetail_d = (EditText) findViewById(R.id.editDetail_d);
        realm = Realm.getInstance(new RealmConfiguration.Builder(getApplicationContext())
                .name("PlanList.Realm6").build());
        makebutton();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final PlanList Plan_temp = (PlanList) adapter.getItem(position);
                String temp_str = Plan_temp.getPlanName();
                String temp1_str = Plan_temp.getPlanDescription();
                long PlanID = Plan_temp.getPlanNum();

                Toast.makeText(PlanListActivity.this, "" + temp_str + temp1_str + PlanID, Toast.LENGTH_SHORT).show();

                if (PlanID == 0) {
                    makePlan();
                }
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            long temp_id;

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final PlanList Plan_temp = (PlanList) adapter.getItem(position);
                long PlanID = Plan_temp.getPlanNum();
                temp_id = PlanID;

                if (PlanID != 0) {
                    LayoutInflater layoutInflater = (LayoutInflater) getLayoutInflater(); //LayoutInflater를 가져오기 위한 다른 방법입니다. LayoutInflater는 Layout을 View의 형태로 변형해주는 역할이라고 3차 세미나 때 배웠었죠?
                    View dialogLayout = layoutInflater.inflate(R.layout.dialog_remove_plan, null);//dialog_layout이라는 레이아웃을 만듭니다. 이를 뷰의 형태로 다이얼로그에 띄우기 위해 인플레이트 해줍니다.
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlanListActivity.this);
                    builder.setTitle("삭제");//다이얼로그의 상단에 표시되는 텍스트인 Title을 정해줍니다.
                    builder.setView(dialogLayout); //layout(위에서 layoutInflater를 통해 인플레이트한)을 다이얼로그가 뷰의 형태로 가져옵니다.

                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            minusId(temp_id);       //그리드뷰를 하나씩 땡긴다.
                            final PlanList planList = realm.where(PlanList.class).equalTo("planNum", count - 1).findFirst();
                            realm.beginTransaction(); //데이터 변경을 알리는 코드
                            planList.removeFromRealm();    // 마지막 그리드뷰 삭제
                            realm.commitTransaction(); //데이터 변경사항을 저장하는 코드
                            adapter.notifyDataSetChanged();
                            onResume();
                        }
                    });


                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                        }

                    });


                    AlertDialog alertDialog = builder.create(); //만들어놓은 AlertDialog.Builder인 builder를 이용해서 새로운 AlertDialog를 만듭니다.
                    alertDialog.show(); //다이얼로그를 띄웁니다.
                }
                return true;
            }
        });
    }

    private ArrayList<PlanList> loadDB() {

        ArrayList<PlanList> planDatas = new ArrayList<>();
        count = realm.getTable(PlanList.class).size();

        Toast.makeText(PlanListActivity.this, count + "", Toast.LENGTH_SHORT).show();
        for (int j = 1; j <= count + 1; j++) {
            if (j == count + 1)
                j = 0;
            RealmResults<PlanList> results_ID = realm.where(PlanList.class).equalTo("planNum", j).findAll();
            for (int i = 0; i < results_ID.size(); i++) {
                PlanList plan_temp = new PlanList();
                plan_temp.setPlanNum(results_ID.get(i).getPlanNum());
                plan_temp.setPlanName(results_ID.get(i).getPlanName());
                plan_temp.setPlanDescription(results_ID.get(i).getPlanDescription());
                planDatas.add(plan_temp);
            }
            if (j == 0) {
                j = (int) count + 2;
            }
        }

        return planDatas;
    }


    @Override
    protected void onResume() {
        super.onResume();
        planDatas = loadDB();
        makeList();
        adapter.notifyDataSetChanged();

    }

    private void makeList() {
        adapter = new PlanAdapter(planDatas, getApplicationContext());
        gridView.setAdapter(adapter);
    }

    private void makebutton() {
        count = realm.getTable(PlanList.class).size();
        Toast.makeText(PlanListActivity.this, "count 개수" + count, Toast.LENGTH_SHORT).show();
        if (count == 0) {
            PlanList plan = new PlanList();
            plan.setPlanNum(count++);
            plan.setPlanName("+");
            realm.beginTransaction(); //데이터 변경을 알리는 코드
            realm.copyToRealm(plan); //기존 realm의 Person 테이블에 방금 새로 만든 Person 객체의 데이터를 추가
            realm.commitTransaction(); //데이터 변경사항을 저장하는 코드
        }

    }

    void makePlan() {
        LayoutInflater layoutInflater = (LayoutInflater) getLayoutInflater(); //LayoutInflater를 가져오기 위한 다른 방법입니다. LayoutInflater는 Layout을 View의 형태로 변형해주는 역할이라고 3차 세미나 때 배웠었죠?
        View dialogLayout = layoutInflater.inflate(R.layout.dialog_add_plan, null);//dialog_layout이라는 레이아웃을 만듭니다. 이를 뷰의 형태로 다이얼로그에 띄우기 위해 인플레이트 해줍니다.
        AlertDialog.Builder builder = new AlertDialog.Builder(PlanListActivity.this);
        builder.setTitle("여행추가");//다이얼로그의 상단에 표시되는 텍스트인 Title을 정해줍니다.
        builder.setView(dialogLayout); //layout(위에서 layoutInflater를 통해 인플레이트한)을 다이얼로그가 뷰의 형태로 가져옵니다.

        editName_d = (EditText) dialogLayout.findViewById(R.id.editName_d);
        editDetail_d = (EditText) dialogLayout.findViewById(R.id.editDetail_d);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                PlanList plan = new PlanList(); //Person 인스턴스 생성
                plan.setPlanNum(count++); //id 필드에 데이터 추가

                plan.setPlanName(editName_d.getText().toString()); //name 필드에 데이터 추가
                plan.setPlanDescription(editDetail_d.getText().toString()); //age 필드에 데이터 추가

                realm.beginTransaction(); //데이터 변경을 알리는 코드
                realm.copyToRealm(plan); //기존 realm의 Person 테이블에 방금 새로 만든 Person 객체의 데이터를 추가
                realm.commitTransaction(); //데이터 변경사항을 저장하는 코드

                Toast.makeText(PlanListActivity.this, plan.getPlanNum() + "", Toast.LENGTH_SHORT).show();
                editName_d.setText("");
                editDetail_d.setText("");
                Intent intent = new Intent(PlanListActivity.this, SearchPlaceActivity.class);
                startActivity(intent);


            }
        });


        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
            }

        });

        AlertDialog alertDialog = builder.create(); //만들어놓은 AlertDialog.Builder인 builder를 이용해서 새로운 AlertDialog를 만듭니다.
        alertDialog.show(); //다이얼로그를 띄웁니다.
    }

    void minusId(long id) {
        //삭제하는 그리드뷰를 중심으로 ID값을 하나씩 감소시킨다.
        for (int j = (int) id + 1; j < count; j++) {
            long temp_id;

            RealmResults<PlanList> results_ID = realm.where(PlanList.class).equalTo("planNum", j).findAll();
            temp_id = (long) j - 1;
            PlanList temp_plan = new PlanList();
            realm.beginTransaction(); //데이터 변경을 알리는 코드
            temp_plan.setPlanNum(temp_id);
            temp_plan.setPlanName(results_ID.get(0).getPlanName());
            temp_plan.setPlanDescription(results_ID.get(0).getPlanDescription());
            realm.copyToRealmOrUpdate(temp_plan);
            realm.commitTransaction(); //데이터 변경사항을 저장하는 코드
        }
    }


}
