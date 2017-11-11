package ph.edu.tip.app.dms.models;

/**
 * Created by Mark Jansen Calderon on 1/23/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject{

    @PrimaryKey
    @SerializedName("employee_id")
    @Expose
    private String employeeId;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("emp_photo")
    @Expose
    private String empPhoto;
    @SerializedName("employee_name")
    @Expose
    private String employeeName;
    @SerializedName("position")
    @Expose
    private String position;
    @SerializedName("position_code")
    @Expose
    private String positionCode;
    @SerializedName("gen_position_code")
    @Expose
    private String genPositionCode;
    @SerializedName("department_code")
    @Expose
    private String departmentCode;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("u")
    @Expose
    private String u;
    @SerializedName("s")
    @Expose
    private String s;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmpPhoto() {
        return empPhoto;
    }

    public void setEmpPhoto(String empPhoto) {
        this.empPhoto = empPhoto;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPositionCode() {
        return positionCode;
    }

    public void setPositionCode(String positionCode) {
        this.positionCode = positionCode;
    }

    public String getGenPositionCode() {
        return genPositionCode;
    }

    public void setGenPositionCode(String genPositionCode) {
        this.genPositionCode = genPositionCode;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getU() {
        return u;
    }

    public void setU(String u) {
        this.u = u;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }
}
