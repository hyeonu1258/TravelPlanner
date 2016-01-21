package inu.travel.Model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PlanList extends RealmObject {

    @PrimaryKey
    private long planNum;

    private String planName;
    private String planDescription;

    public long getPlanNum() {
        return planNum;
    }

    public void setPlanNum(long planNum) {
        this.planNum = planNum;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getPlanDescription() {
        return planDescription;
    }

    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }
}
