package ph.edu.tip.app.dms.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Mark Jansen Calderon on 11/14/2016.
 */

public class Files extends RealmObject {


    @PrimaryKey
    private String fileName;
    private Date fileDate;
    private String fileOwner;
    private String filePath;
    private String fileUri;
    private Long fileSize;

    public Files() {
    }

    public Files(String fileName, Date fileDate, String fileOwner, String filePath, String fileUri,Long fileSize) {
        this.fileName = fileName;
        this.fileDate = fileDate;
        this.fileOwner = fileOwner;
        this.filePath = filePath;
        this.fileUri = fileUri;
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getFileDate() {
        return fileDate;
    }

    public void setFileDate(Date fileDate) {
        this.fileDate = fileDate;
    }

    public String getFileOwner() {
        return fileOwner;
    }

    public void setFileOwner(String fileOwner) {
        this.fileOwner = fileOwner;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
