package inu.travel.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jingyu on 2016-03-02.
 */
public class PlaceList {
    String id;
    String pname;
    ArrayList<Place> item;

    public PlaceList(String id, String pname, ArrayList<Place> item){
        this.id = id;
        this.pname = pname;
        this.item = item;
    }
}
