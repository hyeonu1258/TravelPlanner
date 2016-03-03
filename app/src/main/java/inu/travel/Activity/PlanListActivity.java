package inu.travel.Activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import inu.travel.Adapter.PlanAdapter;
import inu.travel.Component.ApplicationController;
import inu.travel.Model.PlanList;
import inu.travel.Network.AwsNetworkService;
import inu.travel.R;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class PlanListActivity extends Activity {

    private List<PlanList> planDatas = new ArrayList<>();

    EditText editDetail_d;
    GridView gridView;
    PlanAdapter adapter;
    EditText editName_d;
    AwsNetworkService awsNetworkService;
    SharedPreferences pref;
    int PlanListLengh = 0;               //플랜 총 개수
    String user_id;                      //사용자 아이디





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        initView();
        makeList();
        makePlusButton();       // +버튼 만들기
        initNetworkService();   //Network 서버 연결
        initSharedPre();        //SharedPreferences 초기화, 유저 아이디 얻어온다.
                                //플랜 만들 때, 삭제할 때 유저 아이디가 필요


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final PlanList Plan_temp = (PlanList) adapter.getItem(position);
                String temp_str = Plan_temp.getName();
                String temp1_str = Plan_temp.getDescription();
                long PlanID = Plan_temp.getNum();
                // gridview 상에서 클릭한 플랜의 이름, 설명, 번호를 받아온다.

                Toast.makeText(PlanListActivity.this, "" + position + temp_str + temp1_str + PlanID, Toast.LENGTH_SHORT).show();

                if (PlanID == PlanListLengh) {                      // +버튼을 클릭했을 때
                    makePlan();
                } else {                                            // 플랜을 선택했을 때
                    Intent intent = new Intent(PlanListActivity.this, SearchPlaceActivity.class);
                    intent.putExtra("Userid", user_id);
                    intent.putExtra("PlanName",temp_str);
                    //김진규한테 플랜이름, 아이디 보냄
                    startActivity(intent);
                }
            }
        });// gridView.setOnItemClickListener close



        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            String temp_name;
            //삭제할 플랜이름 저장 변수

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final PlanList Plan_temp = (PlanList) adapter.getItem(position);
                long PlanID = Plan_temp.getNum();               // 클릭한 gridview 상의 번호를 받아옴
                temp_name = Plan_temp.getName();                //클릭한 플랜의 이름 받아옴

                if (PlanID != PlanListLengh) {                  // + 이 아닐 경우 삭제가능
                    LayoutInflater layoutInflater = (LayoutInflater) getLayoutInflater(); //LayoutInflater를 가져오기 위한 다른 방법입니다. LayoutInflater는 Layout을 View의 형태로 변형해주는 역할이라고 3차 세미나 때 배웠었죠?
                    View dialogLayout = layoutInflater.inflate(R.layout.dialog_remove_plan, null);//dialog_layout이라는 레이아웃을 만듭니다. 이를 뷰의 형태로 다이얼로그에 띄우기 위해 인플레이트 해줍니다.
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlanListActivity.this);
                    builder.setTitle("삭제");//다이얼로그의 상단에 표시되는 텍스트인 Title을 정해줍니다.
                    builder.setView(dialogLayout); //layout(위에서 layoutInflater를 통해 인플레이트한)을 다이얼로그가 뷰의 형태로 가져옵니다.

                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            PlanList temp_planList = new PlanList(user_id, temp_name);
                            //삭제할 PlanList의 객체 생성 아이디, 플랜이름
                            Toast.makeText(PlanListActivity.this, "" + user_id + temp_name, Toast.LENGTH_SHORT).show();

                            Call<Object> removePlanList = awsNetworkService.removePlanList(temp_planList);
                            removePlanList.enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(Response<Object> response, Retrofit retrofit) {
                                    if (response.code() == 200) {
                                        onResume();
                                        Toast.makeText(PlanListActivity.this, "삭제성공", Toast.LENGTH_SHORT).show();
                                    } else if (response.code() == 503) {
                                        Toast.makeText(PlanListActivity.this, "삭제실패", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    Toast.makeText(getApplicationContext(), "삭제실패오류. ", Toast.LENGTH_LONG).show();
                                    Log.i("MyTag", "에러내용 : " + t.getMessage());
                                }
                            });

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
        });//gridView.setOnItemLongClickListener close

    }//protected void onCreate close

    private void loadServer() {                         //서버로부터 플랜목록 받아옴

        Toast.makeText(PlanListActivity.this, user_id, Toast.LENGTH_SHORT).show();

        final Call<List<PlanList>> getPlanList = awsNetworkService.getPlanList(user_id);

        getPlanList.enqueue(new Callback<List<PlanList>>() {
            @Override
            public void onResponse(Response<List<PlanList>> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    planDatas = response.body();
                    Toast.makeText(PlanListActivity.this, "" + planDatas.size(), Toast.LENGTH_SHORT).show();
                    PlanListLengh = planDatas.size();
                    makePlusButton();
                    adapter.setSource(planDatas);
                } else if (response.code() == 503) {
                    int statusCode = response.code();
                    Log.i("MyTag", "응답코드 : " + statusCode);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to load thumbnails", Toast.LENGTH_LONG).show();
                Log.i("MyTag", "에러내용 : " + t.getMessage());
            }
        });


}


    @Override
    protected void onResume() {
        super.onResume();
        loadServer();               //서버로부터 플랜목록을 받아옴
        makeList();
    }

    private void makeList() {
        adapter = new PlanAdapter(planDatas, getApplicationContext());
        gridView.setAdapter(adapter);
    }

    private void makePlusButton() {                         // + 버튼 만드는 함수
            PlanList plan = new PlanList();
            plan.setNum(PlanListLengh);                     //PlanListLengh 번호를 통해 마지막에 생기게 함
            plan.setName("+");
            planDatas.add(plan);
            adapter.notifyDataSetChanged();
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

                String name = editName_d.getText().toString();
                String description = editDetail_d.getText().toString();

                PlanList temp_planList = new PlanList(user_id, name, description);
                // PlanLIst 객체 생성 아이디, 플랜이름, 플랜 설명
                Call<Object> makePlanList = awsNetworkService.makePlanList(temp_planList);

                makePlanList.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Response<Object> response, Retrofit retrofit) {
                        if (response.code() == 200) {
                            Toast.makeText(getApplicationContext(), "등록 OK", Toast.LENGTH_SHORT).show();
                            editName_d.setText("");
                            editDetail_d.setText("");
                            onResume();

                        } else if (response.code() == 405) {
                            Toast.makeText(getApplicationContext(), "등록실패!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(getApplicationContext(), "플랜등록오류", Toast.LENGTH_LONG).show();
                    }
                });

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


    private void initNetworkService(){
        awsNetworkService = ApplicationController.getInstance().getAwsNetwork();
    }

    private void initView() {
        gridView = (GridView) findViewById(R.id.gridView);
        editName_d = (EditText) findViewById(R.id.editName_d);
        editDetail_d = (EditText) findViewById(R.id.editDetail_d);
    }

    private void initSharedPre(){
        pref = getSharedPreferences("login",0);
        user_id = pref.getString("id", "");                  //SharedPreferences을 통해 id를 받아온다.
        Toast.makeText(PlanListActivity.this, ""+user_id, Toast.LENGTH_SHORT).show();

    }

}
