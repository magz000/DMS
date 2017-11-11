package ph.edu.tip.app.dms.models;

/**
 * Created by Mark Jansen Calderon on 11/28/2016.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DocumentUploadedFiles extends RealmObject {

    @PrimaryKey
    @SerializedName("document_file_id")
    @Expose
    public String documentFileId;
    @SerializedName("document_id")
    @Expose
    public String documentId;
    @SerializedName("folder_id")
    @Expose
    public String folderId;
    @SerializedName("filename")
    @Expose
    public String filename;
    @SerializedName("uploaded_filename")
    @Expose
    public String uploadedFilename;
    @SerializedName("logs")
    @Expose
    public String logs;
    @SerializedName("restriction")
    @Expose
    private String restriction;
    @SerializedName("encoded_by")
    @Expose
    private String encodedBy;

    public String getDocumentFileId() {
        return documentFileId;
    }

    public void setDocumentFileId(String documentFileId) {
        this.documentFileId = documentFileId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUploadedFilename() {
        return uploadedFilename;
    }

    public void setUploadedFilename(String uploadedFilename) {
        this.uploadedFilename = uploadedFilename;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public String getRestriction() {
        return restriction;
    }

    public void setRestriction(String restriction) {
        this.restriction = restriction;
    }

    public String getEncodedBy() {
        return encodedBy;
    }

    public void setEncodedBy(String encodedBy) {
        this.encodedBy = encodedBy;
    }
}
