package inu.travel.Model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by inhoi on 2016-02-01.
 */
public class PersonRealM extends RealmObject {

    @PrimaryKey
    private String id;
    private String pw;
    private int number;

    public PersonRealM(){

    }
    public PersonRealM(String id,String pw,int number){
        this.id=id;
        this.pw=pw;
        this.number=number;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
