package ph.edu.tip.app.dms.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Mark Jansen Calderon on 11/14/2016.
 */

public class ResultDoc {


    @SerializedName("result")
    @Expose
    private String result;


    @SerializedName("document_id")
    @Expose
    private String document_id;
    /**
     * @return The result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result The result
     */
    public void setResult(String result) {
        this.result = result;
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }
}
