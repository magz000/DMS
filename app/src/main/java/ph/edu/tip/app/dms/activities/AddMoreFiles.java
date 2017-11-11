package ph.edu.tip.app.dms.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import me.gujun.android.taggroup.TagGroup;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.adapters.ImageAdapter;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentUploaded;
import ph.edu.tip.app.dms.models.Files;
import ph.edu.tip.app.dms.models.Result;
import ph.edu.tip.app.dms.models.ResultDoc;
import ph.edu.tip.app.dms.models.Templates;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RuntimePermissions
public class AddMoreFiles extends AppCompatActivity {

    private int MAX_ATTACHMENT_COUNT = 10;
    private ArrayList<String> photoPaths = new ArrayList<>();
    private ArrayList<String> docPaths = new ArrayList<>();
    private List<Files> fileList;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private Realm realm;
    private RealmResults<Files> filesRealmResults;
    private LinearLayout addFiles;
    private Dialog dialog;
    private Button upload;
    private LinearLayout buttonDoc, buttonPhoto, addMoreFiles;
    private ApiInterface apiInterface;
    private List<CharSequence> templateNameList = new ArrayList<>();
    static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private String document_id, folder_id;
    private User user;

    public AddMoreFiles() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_files);
        realm = Realm.getDefaultInstance();
        user = realm.where(User.class).findFirst();

        apiInterface = App.getApiInterface();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        Intent i = getIntent();
        try {
            document_id = i.getStringExtra(Constants.DOCUMENT_ID);
            toolbar.setTitle("Add Files to " + i.getStringExtra(Constants.DOCUMENT_NAME));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        DocumentUploaded documentUploaded = realm.where(DocumentUploaded.class).equalTo("documentId", document_id).findFirst();
        folder_id = documentUploaded.getFolderId();

        progressDialog = new ProgressDialog(AddMoreFiles.this);
        progressDialog.setIndeterminate(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        addFiles = (LinearLayout) findViewById(R.id.tapToAdd);
        addMoreFiles = (LinearLayout) findViewById(R.id.addMoreFiles);

        realm.beginTransaction();
        realm.delete(Files.class);
        realm.commitTransaction();

   /*    getAllTemplate();

        etTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                templateNameList.clear();
                for (int x = 0; x < templatesList.size(); x++) {
                    templateNameList.add(templatesList.get(x).getTemplateName());
                }
                final CharSequence[] Animals = templateNameList.toArray(new String[templateNameList.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(PickerActivity.this);
                builder.setItems(Animals, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        etTemplate.setText(templateName);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });*/


        filesRealmResults = realm.where(Files.class).findAll().sort("fileName", Sort.ASCENDING);
        filesRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Files>>() {
            @Override
            public void onChange(RealmResults<Files> element) {
                fileList = realm.copyFromRealm(filesRealmResults);
                Log.d("Realm FL Size", fileList.size() + "");
                if (filesRealmResults.isEmpty()) {
                    addFiles.setVisibility(View.VISIBLE);
                    upload.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    addMoreFiles.setVisibility(View.GONE);
                } else {
                    upload.setVisibility(View.VISIBLE);
                    addFiles.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    addMoreFiles.setVisibility(View.VISIBLE);
                }
            }
        });

        upload = (Button) findViewById(R.id.uploadButton);
        upload.setVisibility(View.GONE);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addToDocuments();
            }
        });


        addFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickerDialog();
            }
        });

        addMoreFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickerDialog();
            }
        });


    }

    private void pickerDialog() {
        dialog = new Dialog(AddMoreFiles.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pick);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        buttonDoc = (LinearLayout) dialog.findViewById(R.id.linearDoc);
        buttonPhoto = (LinearLayout) dialog.findViewById(R.id.linearPhoto);
        buttonDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddMoreFilesPermissionsDispatcher.onPickDocWithCheck(AddMoreFiles.this);

                dialog.dismiss();
            }
        });

        buttonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                AddMoreFilesPermissionsDispatcher.onPickPhotoWithCheck(AddMoreFiles.this);
            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    photoPaths = new ArrayList<>();
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_PHOTOS));
                }
                break;

            case FilePickerConst.REQUEST_CODE_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    docPaths = new ArrayList<>();
                    docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                }
                break;
        }
        addThemToView(photoPaths, docPaths);

    }

    private void addThemToView(ArrayList<String> imagePaths, ArrayList<String> docPaths) {

        realm.beginTransaction();
        realm.delete(Files.class);
        realm.commitTransaction();


        if (imagePaths != null) {
            for (int x = 0; x < imagePaths.size(); x++) {
                final String path = imagePaths.get(x);
                File file = new File(path);
                // Get the number of bytes in the file
                long sizeInBytes = file.length();
                //transform in MB
                long sizeInMb = sizeInBytes / (1024 * 1024);
                if (sizeInMb > 10) {
                    Toast.makeText(this, "Can't add " + file.getName() + " file size too big", Toast.LENGTH_SHORT).show();
                } else {
                    Date lastModDate = new Date(file.lastModified());
                    Files files = new Files();// Create a new object
                    files.setFileDate(lastModDate);
                    files.setFileName(file.getName());
                    files.setFilePath(file.getPath());
                    files.setFileOwner("Jansen");
                    files.setFileUri(file.toURI().toString());
                    files.setFileSize(file.length());
                    realm.beginTransaction();
                    realm.copyToRealm(files);
                    realm.commitTransaction();
                }


            }
        }


        if (docPaths != null) {

            for (int x = 0; x < docPaths.size(); x++) {
                final String path = docPaths.get(x);
                File file = new File(path);
                // Get the number of bytes in the file
                long sizeInBytes = file.length();
                //transform in MB
                long sizeInMb = sizeInBytes / (1024 * 1024);
                if (sizeInMb > 10) {
                    Toast.makeText(this, "Can't add " + file.getName() + " file size too big", Toast.LENGTH_SHORT).show();
                } else {

                    Date lastModDate = new Date(file.lastModified());
                    Files files = new Files();// Create a new object
                    files.setFileDate(lastModDate);
                    files.setFileName(file.getName());
                    files.setFilePath(file.getPath());
                    files.setFileOwner("Jansen");
                    files.setFileUri(file.toURI().toString());
                    files.setFileSize(file.length());
                    realm.beginTransaction();
                    realm.copyToRealm(files);
                    realm.commitTransaction();
                }
            }
        }

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(AddMoreFiles.this));
            recyclerView.setAdapter(new ImageAdapter(this, filesRealmResults));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }

        //  Toast.makeText(this, "Num of files selected: " + filePaths.get(0).toString(), Toast.LENGTH_SHORT).show();
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void onPickPhoto() {
        int maxCount = MAX_ATTACHMENT_COUNT - docPaths.size();
        if ((docPaths.size() + photoPaths.size()) == MAX_ATTACHMENT_COUNT)
            Toast.makeText(this, "Cannot select more than " + MAX_ATTACHMENT_COUNT + " items", Toast.LENGTH_SHORT).show();
        else
            FilePickerBuilder.getInstance().setMaxCount(maxCount)
                    .setSelectedFiles(photoPaths)
                    .setActivityTheme(R.style.FilePickerTheme)
                    .pickPhoto(this);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void onPickDoc() {
        int maxCount = MAX_ATTACHMENT_COUNT - photoPaths.size();
        if ((docPaths.size() + photoPaths.size()) == MAX_ATTACHMENT_COUNT)
            Toast.makeText(this, "Cannot select more than " + MAX_ATTACHMENT_COUNT + " items", Toast.LENGTH_SHORT).show();
        else
            FilePickerBuilder.getInstance().setMaxCount(maxCount)
                    .setSelectedFiles(docPaths)
                    .setActivityTheme(R.style.FilePickerTheme)
                    .pickDocument(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AddMoreFilesPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pick, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort) {
            new MaterialDialog.Builder(AddMoreFiles.this)
                    .title("Sort By")
                    .items(R.array.sort)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            switch (which) {
                                case 0:
                                    filesRealmResults = realm.where(Files.class).findAll().sort("fileName", Sort.ASCENDING);
                                    break;
                                case 1:
                                    filesRealmResults = realm.where(Files.class).findAll().sort("fileDate", Sort.ASCENDING);
                                    break;
                                case 2:
                                    filesRealmResults = realm.where(Files.class).findAll().sort("fileSize", Sort.ASCENDING);
                                    break;

                            }

                            recyclerView.setAdapter(new ImageAdapter(AddMoreFiles.this, filesRealmResults));
                        }
                    })
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }


    List<Files> tempFiles;

    private void addToDocuments() {
        tempFiles = fileList;
        try {
            if (tempFiles.isEmpty())
                Toast.makeText(AddMoreFiles.this, "No File Selected", Toast.LENGTH_SHORT).show();
            else {
                // for (int x = 0; x < tempFiles.size(); x++) {
                uploadImage(0, tempFiles.size(), tempFiles.get(0), document_id, folder_id);
                //}
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(AddMoreFiles.this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(final int x, final int size, final Files files, final String document_id, final String folderId) {


        /**
         * Progressbar to Display if you need
         */

        progressDialog.show();
        progressDialog.setMessage("Uploading file " + (x + 1) + " of " + size);
        //Create Upload Server Client

        //File creating from selected filepath
        final File file = new File(files.getFilePath());
        Log.d(this.getPackageName(), "uploadImage int " + x);

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

        RequestBody p = createPartFromString(ParameterEncryption.uploadFile(document_id,folderId,user.getUserId()));
        RequestBody user_id = createPartFromString(user.getU());
        RequestBody user_campus = createPartFromString(user.getS());
        Call<Result> resultCall = apiInterface.uploadFile(user_id,user_campus, body,p);
        // finally, execute the request
        resultCall.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                // Response Success or Fail
                if (response.body().getResult().equals("success")) {
                    final Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<Files> filesRealmResults = realm.where(Files.class).findAll();
                            Files deleteFile = filesRealmResults.where().equalTo("filePath", files.getFilePath()).findFirst();
                            deleteFile.deleteFromRealm();

                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            realm.close();
                            if (x + 1 == size) {
                                App.addToTesseract(user.getU(),user.getS(),document_id);
                                finish();
                                tempFiles.clear();
                                progressDialog.dismiss();
                            } else {
                                uploadImage(x + 1, size, tempFiles.get(x + 1), document_id, folderId);
                            }
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                            error.printStackTrace();
                            realm.close();
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(AddMoreFiles.this, "Failed", Toast.LENGTH_SHORT).show();
                }


                /**
                 * Update Views
                 */
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                t.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(AddMoreFiles.this, "Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }


}
