package ph.edu.tip.app.dms.utils;

import io.realm.Realm;
import ph.edu.tip.app.dms.models.User;

/**
 * Created by Mark Jansen Calderon on 2/16/2017.
 */

public class ParameterEncryption {

    private static String do_action = "do_action=";

    public static String getAllTemplates(String dept_code) {
        return Encrypt.protectString(do_action + "getAllTemplates" + "&dept_code=" + dept_code);
    }

    public static String getAllFoldersFromTemplate(String template_name,String dept_code) {
        return Encrypt.protectString(do_action + "getAllFoldersFromTemplate" + "&template_name=" + template_name +"&dept_code=" + dept_code);
    }

    public static String getAllDocumentUploaded(String folder_id) {
        return Encrypt.protectString(do_action + "getAllDocumentUploaded" + "&folder_id=" + folder_id);
    }

    public static String getAllDocumentUploadedFromTemplateName(String template_name) {
        return Encrypt.protectString(do_action + "getAllDocumentUploadedFromTemplateName" + "&template_name=" + template_name);
    }

    public static String getAllDocumentUploadedFromDeptCode(String dept_code) {
        String to_do = "getAllDocumentUploadedFromDeptCode";
        return Encrypt.protectString(do_action + to_do + "&dept_code=" + dept_code);
    }

    public static String addDocumentsUploaded(String templateName, String dept_code, String folder_id, String documentDescription, String tags, String restriction,String user_id) {
        String to_do = "addDocumentsUploaded";
        return Encrypt.protectString(do_action + to_do +
                "&template_name=" + templateName +
                "&dept_code=" + dept_code +
                "&folder_id=" + folder_id +
                "&document_description=" + documentDescription +
                "&tags=" + tags +
                "&restriction=" + restriction+
                "&user_id=" + user_id);
    }

    public static String getSingleDocumentUploaded(String document_id) {
        String to_do = "getSingleDocumentUploaded";
        return Encrypt.protectString(do_action + to_do +
                "&document_id=" + document_id);
    }

    public static String updateDocumentUploaded(String document_id, String document_description, String tags, String restriction, String user_id) {
        String to_do = "updateDocumentUploaded";
        return Encrypt.protectString(do_action + to_do +
                "&document_id=" + document_id +
                "&document_description=" + document_description +
                "&tags=" + tags +
                "&restriction=" + restriction+
                "&user_id=" + user_id);
    }

    public static String deleteDocument(String document_id) {
        String to_do = "deleteDocument";
        return Encrypt.protectString(do_action + to_do +
                "&document_id=" + document_id);
    }


    /*
     END OF DOCUMENT UPLOADED
    */

    /*
     START OF DOCUMENT UPLOADED FILES
    */

/*
    @POST(do_action+"addFile")
    @Multipart
    Call<Result> uploadFile(@Part("u") RequestBody u,
                            @Part MultipartBody.Part file,
                            @Part("document_id") RequestBody document_id,
                            @Part("folder_id") RequestBody folder_id);
*/

    public static String uploadFile(String document_id, String folder_id, String user_id) {

        return Encrypt.protectString(do_action+"addFile&document_id=" + document_id + "&folder_id=" + folder_id+
                "&user_id=" + user_id);
    }


    public static String getFilesByContent(String content, String dept_code) {
        String to_do = "getFilesByContent";
        return Encrypt.protectString(do_action + to_do + "&content=" + content + "&dept_code=" + dept_code);
    }


    public static String deleteFile(String document_file_id,String user_id) {
        String to_do = "deleteFile";
        return Encrypt.protectString(do_action + to_do + "&document_file_id=" + document_file_id+
                "&user_id=" + user_id);
    }

    public static String readDocumentFiles(String document_id) {
        String to_do = "readDocumentFiles";
        return Encrypt.protectString(do_action + to_do + "&document_id=" + document_id);
    }

    public static String readSingleFile(String document_file_id) {
        String to_do = "readSingleFile";
        return Encrypt.protectString(do_action + to_do + "&document_file_id=" + document_file_id);
    }

    public static String getAllDocumentUploadedFiles(String document_id) {
        String to_do = "getAllDocumentUploadedFiles";
        return Encrypt.protectString(do_action + to_do + "&document_id=" + document_id);
    }

    /*

        @POST(do_action+"getSharedDocumentsWith")
        @FormUrlEncoded
        Call<List<DocumentShared>> getSharedDocumentsWith(@Field("u") String u,
                                                          @Field("user_id") String user_id);*/
    public static String getSharedDocumentsWith(String user_id) {
        String to_do = "getSharedDocumentsWith";
        return Encrypt.protectString(do_action + to_do + "&user_id=" + user_id);
    }

    /*@POST(do_action + "getSharedDocumentsBy")
    @FormUrlEncoded
    Call<List<DocumentShared>> getSharedDocumentsBy(@Field("u") String u,
                                                    @Field("user_id") String user_id);*/
    public static String getSharedDocumentsBy(String user_id) {
        String to_do = "getSharedDocumentsBy";
        return Encrypt.protectString(do_action + to_do + "&user_id=" + user_id);
    }

    public static String shareDocument(String document_id,
                                       String user_id_shared_with,
                                       String user_id_shared_by) {
        String to_do = "shareDocument";
        return Encrypt.protectString(do_action + to_do +
                "&document_id=" + document_id +
                "&user_id_shared_with=" + user_id_shared_with +
                "&user_id_shared_by=" + user_id_shared_by);
    }


    public static String getUserSharedWithList(String document_id) {
        String to_do = "getUserSharedWithList";
        return Encrypt.protectString(do_action + to_do + "&document_id=" + document_id);
    }

    public static String unshareDocument(String document_id,String user_id) {
        String to_do = "unshareDocument";
        return Encrypt.protectString(do_action + to_do + "&document_id=" + document_id+"&user_id="+user_id);
    }

    public static String getUploadedViaDeptCode(String dept_code) {
        String to_do = "getAllDocumentUploadedFromDeptCode";
        return Encrypt.protectString(do_action + to_do + "&dept_code="+dept_code);
    }
}
