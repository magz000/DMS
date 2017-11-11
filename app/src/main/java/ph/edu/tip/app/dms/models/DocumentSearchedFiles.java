package ph.edu.tip.app.dms.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Mark Jansen Calderon on 1/9/2017.
 */

public class DocumentSearchedFiles extends RealmObject {
    @PrimaryKey
    @SerializedName("document_file_id")
    @Expose
    private String documentFileId;
    @SerializedName("document_id")
    @Expose
    private String documentId;
    @SerializedName("document_description")
    @Expose
    private String documentDescription;
    @SerializedName("folder_id")
    @Expose
    private String folderId;
    @SerializedName("filename")
    @Expose
    private String filename;
    @SerializedName("uploaded_filename")
    @Expose
    private String uploadedFilename;
    @SerializedName("logs")
    @Expose
    private String logs;
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

    public String getDocumentDescription() {
        return documentDescription;
    }

    public void setDocumentDescription(String documentDescription) {
        this.documentDescription = documentDescription;
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

    public String getEncodedBy() {
        return encodedBy;
    }

    public void setEncodedBy(String encodedBy) {
        this.encodedBy = encodedBy;
    }
}
