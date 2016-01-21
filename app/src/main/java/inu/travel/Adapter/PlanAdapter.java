package inu.travel.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.skplanetx.tmapopenmapapi.R;

import java.util.ArrayList;

/**
 * Created by JongMin on 2015-11-26.
 */
public class PlanAdapter extends BaseAdapter {

    private ArrayList<Plan> planDatas;
    LayoutInflater layoutInflater;

    public PlanAdapter(ArrayList<Plan> planDatas, Context context) {
        this.planDatas = planDatas;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return planDatas != null ? planDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (planDatas != null && (planDatas.size() > position && position >= 0) ? planDatas.get(position) : null);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_plan, parent, false);

            viewHolder.txtName_item = (TextView)convertView.findViewById(R.id.txtTitle_item);
            //viewHolder.txtId_item = (TextView)convertView.findViewById(R.id.txtAge);
            viewHolder.txtDetail_item = (TextView)convertView.findViewById(R.id.txtDescription_item);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Plan plan_temp = planDatas.get(position);
        System.out.println(plan_temp.getName()+"ffffffffffffffffffffffffffffff");
        viewHolder.txtName_item.setText(plan_temp.getName().toString());
        //viewHolder.txtId_item.setText(String.valueOf(plan_temp.getId()));
        viewHolder.txtDetail_item.setText(plan_temp.getDetail());

        return convertView;
    }
}