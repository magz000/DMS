package ph.edu.tip.app.dms.models;

/**
 * Created by Mark Jansen Calderon on 11/25/2016.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Templates extends RealmObject{

    @PrimaryKey
    @SerializedName("template_name")
    @Expose
    private String templateName;

    @SerializedName("folder_id")
    @Expose
    private String folderId;
    @SerializedName("dept_code")
    @Expose
    private String deptCode;
    @SerializedName("logs")
    @Expose
    private String logs;

    /**
     *
     * @return
     * The templateName
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     *
     * @param templateName
     * The template_name
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     *
     * @return
     * The folderId
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     *
     * @param folderId
     * The folder_id
     */
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    /**
     *
     * @return
     * The deptCode
     */
    public String getDeptCode() {
        return deptCode;
    }

    /**
     *
     * @param deptCode
     * The dept_code
     */
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    /**
     *
     * @return
     * The logs
     */
    public String getLogs() {
        return logs;
    }

    /**
     *
     * @param logs
     * The logs
     */
    public void setLogs(String logs) {
        this.logs = logs;
    }

}