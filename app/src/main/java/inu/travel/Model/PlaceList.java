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


    public PlaceList() {
        item = new ArrayList<>();
    }

    public PlaceList(String id, String pname){
        this.id = id;
        this.pname = pname;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public ArrayList<Place> getItem(){
        return item;
    }

    public void setItem(ArrayList<Place> item) {
        this.item = item;
    }

}
