package ph.edu.tip.app.dms.interfaces;

/**
 * Created by Mark Jansen Calderon on 8/18/2016.
 */

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import ph.edu.tip.app.dms.models.DocumentSearchedFiles;
import ph.edu.tip.app.dms.models.DocumentUploaded;
import ph.edu.tip.app.dms.models.DocumentUploadedFiles;
import ph.edu.tip.app.dms.models.DocumentShared;
import ph.edu.tip.app.dms.models.Employee;
import ph.edu.tip.app.dms.models.Folders;
import ph.edu.tip.app.dms.models.Result;
import ph.edu.tip.app.dms.models.ResultDoc;
import ph.edu.tip.app.dms.models.Templates;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.models.UserSharedWith;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface ApiInterface {


    /*
    Retrofit get annotation with our URL
    And our method that will return us the List of Contacts
    */
    final static String do_action = "dms_mobile.php?";
    @POST(do_action)
    @FormUrlEncoded
    Call<List<Templates>> getTemplates(@Field("u") String u, @Field("s") String s, @Field("p") String p);



    /* @POST(do_action+"login")
     @FormUrlEncoded
     Call<User> login(@Field("user_id") String user_id, @Field("password") String password);*/
    @POST(do_action)
    @FormUrlEncoded
    Call<User> login(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<List<Employee>> getEmployees(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<List<Folders>> getAllFoldersFromTemplate(@Field("u") String u,@Field("s") String s,@Field("p") String p);



    /*
    START OF DOCUMENT UPLOADED
    */


    @POST(do_action)
    @FormUrlEncoded
    Call<List<DocumentUploaded>> getDocumentUploaded(@Field("u") String u,@Field("s") String s,@Field("p") String p);

    @POST(do_action)
    @FormUrlEncoded
    Call<List<DocumentUploaded>> getDocumentUploadedFromTemplateName(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<List<DocumentUploaded>> getDocumentUploadedViaDeptCode(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<ResultDoc> addDocumentsUploaded(@Field("u") String u,@Field("s") String s,@Field("p") String p);

    @POST(do_action)
    @FormUrlEncoded
    Call<List<DocumentUploaded>> getSingleDocumentUploaded(@Field("u") String u,@Field("s") String s,@Field("p") String p);

    @POST(do_action)
    @FormUrlEncoded
    Call<Result> updateDocumentUploaded(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<Result> deleteDocument(@Field("u") String u,@Field("s") String s,@Field("p") String p);

    /*
     END OF DOCUMENT UPLOADED
    */

    /*
     START OF DOCUMENT UPLOADED FILES
    */

    @POST(do_action)
    @Multipart
    Call<Result> uploadFile(@Part("u") RequestBody u,
                            @Part("s") RequestBody s,
                            @Part MultipartBody.Part file,
                            @Part("p") RequestBody p);

    @POST(do_action)
    @FormUrlEncoded
    Call<List<DocumentSearchedFiles>> getFilesByContent(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<Result> deleteFile(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<Result> readDocumentFiles(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<Result> readSingleFile(@Field("u") String u,@Field("s") String s,@Field("p") String p);



    @POST(do_action)
    @FormUrlEncoded
    Call<List<DocumentUploadedFiles>> getAllDocumentUploadedFiles(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<List<DocumentShared>> getSharedDocumentsWith(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    @POST(do_action)
    @FormUrlEncoded
    Call<List<DocumentShared>> getSharedDocumentsBy(@Field("u") String u,@Field("s") String s,@Field("p") String p);

    @POST(do_action)
    @FormUrlEncoded
    Call<Result> shareDocument(@Field("u") String u,@Field("s") String s,@Field("p") String p);

    @POST(do_action)
    @FormUrlEncoded
    Call<List<UserSharedWith>> getUserSharedWithList(@Field("u") String u,@Field("s") String s,@Field("p") String p);

    @POST(do_action)
    @FormUrlEncoded
    Call<Result> unshareDocument(@Field("u") String u,@Field("s") String s,@Field("p") String p);


    /*
     END OF DOCUMENT UPLOADED FILES
    */
}