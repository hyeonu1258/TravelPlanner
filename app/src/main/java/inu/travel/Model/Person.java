package inu.travel.Model;

import io.realm.annotations.PrimaryKey;

//public class Person extends RealmObject {
public class Person  {

    @PrimaryKey
    private String id;
    private String pw;
    private String email;

    public Person(){

    }
    public Person(String personId,String personPassword){
        this.id = personId;
        this.pw = personPassword;
    }

    public Person(String personId,String personPassword,String personEmail){
        this.id = personId;
        this.pw = personPassword;
        this.email = personEmail;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}