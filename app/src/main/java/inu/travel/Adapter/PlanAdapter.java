package inu.travel.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;

import inu.travel.Model.PlanList;
import inu.travel.R;
import inu.travel.ViewHolder.PlanViewHolder;

/**
 * Created by JongMin on 2015-11-26.
 */
public class PlanAdapter extends BaseAdapter {

    private ArrayList<PlanList> planDatas;
    LayoutInflater layoutInflater;

    public PlanAdapter(ArrayList<PlanList> planDatas, Context context) {
        this.planDatas = planDatas;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

       // PlanViewHolder viewHolder = new PlanViewHolder();
        PlanViewHolder viewHolder = new PlanViewHolder();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_plan
                    , parent, false);

            viewHolder.txtNameItem = (TextView) convertView.findViewById(R.id.txtTitle_item);
            //viewHolder.txtId_item = (TextView)convertView.findViewById(R.id.txtAge);
            viewHolder.txtDescriptionItem = (TextView) convertView.findViewById(R.id.txtDescription_item);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PlanViewHolder) convertView.getTag();
        }

        PlanList plan_temp = planDatas.get(position);
        viewHolder.txtNameItem.setText(plan_temp.getPlanName().toString());
        //viewHolder.txtId_item.setText(String.valueOf(plan_temp.getId()));
        viewHolder.txtDescriptionItem.setText(plan_temp.getPlanDescription());

        return convertView;
    }
}