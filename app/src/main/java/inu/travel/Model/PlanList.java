package inu.travel.Model;

public class PlanList  {


    String id;
    String name;
    String description;
    int num;


    public PlanList(String id,String name,String description){
        this.id=id;
        this.name=name;
        this.description=description;
        this.num=1;
    }
    public PlanList(String id,String name){
        this.id=id;
        this.name=name;
    }
    public PlanList(){

    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }





}
