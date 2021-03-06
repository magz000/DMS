package ph.edu.tip.app.dms.models;

/**
 * Created by Mark Jansen Calderon on 11/28/2016.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DocumentShared extends RealmObject {

    @PrimaryKey
    @SerializedName("document_id")
    @Expose
    private String documentId;
    @SerializedName("dept_code")
    @Expose
    private String deptCode;
    @SerializedName("template_name")
    @Expose
    private String templateName;
    @SerializedName("folder_id")
    @Expose
    private String folderId;
    @SerializedName("document_description")
    @Expose
    private String documentDescription;
    @SerializedName("tags")
    @Expose
    private String tags;
    @SerializedName("last_modified")
    @Expose
    private String lastModified;
    @SerializedName("logs")
    @Expose
    private String logs;

    @SerializedName("file_count")
    @Expose
    private String fileCount;

    @SerializedName("date_shared")
    @Expose
    private String dateShared;
    @SerializedName("user_id_shared_with")
    @Expose
    private String userIdSharedWith;
    @SerializedName("user_id_shared_by")
    @Expose
    private String userIdSharedBy;

    @SerializedName("encoded_by")
    @Expose
    private String encodedBy;

    /**
     *
     * @return
     * The documentId
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     *
     * @param documentId
     * The document_id
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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
     * The documentDescription
     */
    public String getDocumentDescription() {
        return documentDescription;
    }

    /**
     *
     * @param documentDescription
     * The document_description
     */
    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
    }

    /**
     *
     * @return
     * The tags
     */
    public String getTags() {
        return tags;
    }

    /**
     *
     * @param tags
     * The tags
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     *
     * @return
     * The lastModified
     */
    public String getLastModified() {
        return lastModified;
    }

    /**
     *
     * @param lastModified
     * The last_modified
     */
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
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

    public String getFileCount() {
        return fileCount;
    }

    public void setFileCount(String fileCount) {
        this.fileCount = fileCount;
    }

    public String getUserIdSharedWith() {
        return userIdSharedWith;
    }

    public void setUserIdSharedWith(String userIdSharedWith) {
        this.userIdSharedWith = userIdSharedWith;
    }

    public String getDateShared() {
        return dateShared;
    }

    public void setDateShared(String dateShared) {
        this.dateShared = dateShared;
    }

    public String getUserIdSharedBy() {
        return userIdSharedBy;
    }

    public void setUserIdSharedBy(String userIdSharedBy) {
        this.userIdSharedBy = userIdSharedBy;
    }

    public String getEncodedBy() {
        return encodedBy;
    }

    public void setEncodedBy(String encodedBy) {
        this.encodedBy = encodedBy;
    }
}
