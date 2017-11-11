package ph.edu.tip.app.dms.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Mark Jansen Calderon on 11/14/2016.
 */

public class UserSharedWith {


    @SerializedName("user_id_shared_with")
    @Expose
    private String userIdSharedWith;

    public String getUserIdSharedWith() {
        return userIdSharedWith;
    }

    public void setUserIdSharedWith(String userIdSharedWith) {
        this.userIdSharedWith = userIdSharedWith;
    }
}
