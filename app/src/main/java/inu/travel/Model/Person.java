package inu.travel.Model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Person extends RealmObject {

    @PrimaryKey
    private String personId;

    private String personPassword;
    private String personEmail;
    public Person(){

    }
    public Person(String personId,String personPassword){
        this.personId = personId;
        this.personPassword = personPassword;
    }

    public Person(String personId,String personPassword,String personEmail){
        this.personId=personId;
        this.personPassword=personPassword;
        this.personEmail=personEmail;
    }

    public String getPersonEmail() {
        return personEmail;
    }

    public void setPersonEmail(String personEmail) {
        this.personEmail = personEmail;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonPassword() {
        return personPassword;
    }

    public void setPersonPassword(String personPassword) {
        this.personPassword = personPassword;
    }
}