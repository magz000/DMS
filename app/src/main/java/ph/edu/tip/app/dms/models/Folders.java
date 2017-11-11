package ph.edu.tip.app.dms.models;

/**
 * Created by Mark Jansen Calderon on 11/24/2016.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmModel;
import io.realm.RealmObject;

public class Folders extends RealmObject {


    @SerializedName("folder_id")
    @Expose
    private String folderId;
    @SerializedName("folder_name")
    @Expose
    private String folderName;
    @SerializedName("parent_folder_id")
    @Expose
    private String parentFolderId;
    @SerializedName("access_level_id")
    @Expose
    private String accessLevelId;
    @SerializedName("disabled")
    @Expose
    private String disabled;
    @SerializedName("disabled_by")
    @Expose
    private String disabledBy;
    @SerializedName("date_disabled")
    @Expose
    private String dateDisabled;
    @SerializedName("logs")
    @Expose
    private String logs;

    @SerializedName("document_count")
    @Expose
    private String documentCount;

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFolderNameWithCount() {
        return folderName+" ("+getDocumentCount()+")";
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public String getAccessLevelId() {
        return accessLevelId;
    }

    public void setAccessLevelId(String accessLevelId) {
        this.accessLevelId = accessLevelId;
    }

    public String getDisabled() {
        return disabled;
    }

    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

    public String getDisabledBy() {
        return disabledBy;
    }

    public void setDisabledBy(String disabledBy) {
        this.disabledBy = disabledBy;
    }

    public String getDateDisabled() {
        return dateDisabled;
    }

    public void setDateDisabled(String dateDisabled) {
        this.dateDisabled = dateDisabled;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public String getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(String documentCount) {
        this.documentCount = documentCount;
    }
}
