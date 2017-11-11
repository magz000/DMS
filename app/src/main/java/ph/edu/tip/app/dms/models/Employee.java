package ph.edu.tip.app.dms.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Mark Jansen Calderon on 2/7/2017.
 */

public class Employee extends RealmObject{
    @SerializedName("emp_id")
    @Expose
    private String empId;
    @SerializedName("name")
    @Expose
    private String name;
    @PrimaryKey
    @SerializedName("user_id")
    @Expose
    private String userId;

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
