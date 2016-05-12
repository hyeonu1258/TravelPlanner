package inu.travel.Model;

/**
 * Created by inhoi on 2016-02-01.
 */
public class Plan {
    String id;
    String name;
    String description;
    public String alltime;
    public String alldistance;

    public Plan() {
    }

    public Plan(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
}
