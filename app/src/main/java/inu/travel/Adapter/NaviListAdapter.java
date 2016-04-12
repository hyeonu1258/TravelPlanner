package inu.travel.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import inu.travel.R;
import inu.travel.ViewHolder.NaviListViewHolder;

/**
 * Created by jingyu on 2016-04-12.
 */
public class NaviListAdapter extends BaseAdapter {
    private NodeList nodeList;
    LayoutInflater layoutInflater;

    public NaviListAdapter(NodeList nList, Context context) {
        this.nodeList = nList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NaviListViewHolder naviListViewHolder = new NaviListViewHolder();
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_item_navi, parent, false);
            naviListViewHolder.imgNaviMaker = (ImageView)convertView.findViewById(R.id.imgNaviMarker);
            naviListViewHolder.txtNaviDescription = (TextView)convertView.findViewById(R.id.txtNaviDesc);
            convertView.setTag(naviListViewHolder);
        }else{
            naviListViewHolder = (NaviListViewHolder) convertView.getTag();
        }

        Node nNode = nodeList.item(position);
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) nNode;
            try { //항목이 null인 것도 있음
                System.out.println("name : " + eElement.getElementsByTagName("name").item(0).getTextContent());
                System.out.println("tmap:pointIndex : " + eElement.getElementsByTagName("tmap:pointIndex").item(0).getTextContent());
                System.out.println("description : " + eElement.getElementsByTagName("description").item(0).getTextContent());
                naviListViewHolder.txtNaviDescription.setText(eElement.getElementsByTagName("description").item(0).getTextContent());
                System.out.println("styleUrl : " + eElement.getElementsByTagName("styleUrl").item(0).getTextContent());
                System.out.println("tmap:nextRoadName : " + eElement.getElementsByTagName("tmap:nextRoadName").item(0).getTextContent());
                System.out.println("tmap:nodeType : " + eElement.getElementsByTagName("tmap:nodeType").item(0).getTextContent());
                System.out.println("tmap:turnType : " + eElement.getElementsByTagName("tmap:turnType").item(0).getTextContent());
                System.out.println("tmap:pointType : " + eElement.getElementsByTagName("tmap:pointType").item(0).getTextContent());
//                    System.out.println("coordinates : " + eElement.getElementsByTagName("coordinates").item(0).getTextContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("----------------------------");
        }
        return convertView;
    }
}
