package ph.edu.tip.app.dms.activities;

import android.accounts.NetworkErrorException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.bemobi.medescope.Medescope;
import br.com.bemobi.medescope.exception.DirectoryNotMountedException;
import br.com.bemobi.medescope.exception.PathNotFoundException;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import me.gujun.android.taggroup.TagGroup;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.adapters.DocumentUploadedFilesAdapter;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentShared;
import ph.edu.tip.app.dms.models.DocumentUploaded;
import ph.edu.tip.app.dms.models.DocumentUploadedFiles;
import ph.edu.tip.app.dms.models.Employee;
import ph.edu.tip.app.dms.models.NotificationData;
import ph.edu.tip.app.dms.models.Result;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.models.UserSharedWith;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentUploadedFilesActivity extends AppCompatActivity implements DocumentUploadedFilesAdapter.DownloadCaller {

    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private DocumentUploadedFilesAdapter documentUploadedFilesAdapter;
    private ProgressBar progressBar;
    private RelativeLayout no_files;
    private String document_id, document_desc;
    private Toolbar toolbar;
    private MaterialSearchView searchView;
    private Realm realm;
    private DocumentUploaded documentUploaded;
    private LinearLayout linearLayout;
    private TextView breadcrumb;
    private String breadcrumb_title;
    private Medescope mMedescope;
    private String file_path;
    private static final String TAG = MainActivity.class.getSimpleName();
    private int selectedId;
    private RealmResults<DocumentUploadedFiles> documentUploadedRealmResults;
    private User user;
    private List<String> tempUsers;
    private List<UserSharedWith> userSharedWithList = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_uploaded_files);
        realm = Realm.getDefaultInstance();
        apiInterface = App.getApiInterface();
        user = realm.where(User.class).findFirst();
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewFiles);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        no_files = (RelativeLayout) findViewById(R.id.no_file_yet);

        breadcrumb = (TextView) findViewById(R.id.breadcrumb);

        recyclerView.setVisibility(View.GONE);
        no_files.setVisibility(View.GONE);


        Intent i = getIntent();

        document_id = i.getStringExtra(Constants.DOCUMENT_ID);
        document_desc = i.getStringExtra(Constants.DOCUMENT_NAME);

        breadcrumb_title = i.getStringExtra(Constants.BREADCRUMB_TITLE);
        try {
            if (!breadcrumb_title.equals(""))
                breadcrumb.setText(breadcrumb_title + " > " + document_desc);
            else
                breadcrumb.setVisibility(View.GONE);
        } catch (NullPointerException e) {
            e.printStackTrace();
            breadcrumb.setVisibility(View.GONE);
        }


        if (i.getBooleanExtra(Constants.IS_FROM_SEARCH, false)) {
            progressBar.setVisibility(View.VISIBLE);
            Call<List<DocumentUploaded>> getDocument = apiInterface.getSingleDocumentUploaded(user.getU(),user.getS(),ParameterEncryption.getSingleDocumentUploaded(document_id));
            getDocument.enqueue(new Callback<List<DocumentUploaded>>() {
                @Override
                public void onResponse(Call<List<DocumentUploaded>> call, final Response<List<DocumentUploaded>> response) {
                    if (response.isSuccessful()) {
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(response.body());
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                documentUploaded = realm.where(DocumentUploaded.class).equalTo("documentId", document_id).findFirst();
                                document_desc = documentUploaded.getDocumentDescription();
                                toolbar.setTitle(document_desc);
                                realm.close();
                            }
                        }, new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {
                                realm.close();
                                error.printStackTrace();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<DocumentUploaded>> call, Throwable t) {

                }
            });
        } else {
            toolbar.setTitle(document_desc);
        }

        linearLayout = (LinearLayout) findViewById(R.id.addMoreFiles);
        linearLayout.setVisibility(View.GONE);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DocumentUploadedFilesActivity.this, AddMoreFiles.class);
                i.putExtra(Constants.DOCUMENT_ID, document_id);
                i.putExtra(Constants.DOCUMENT_NAME, document_desc);
                startActivity(i);
            }
        });

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.custom_cursor);
        searchView.setEllipsize(true);
        searchView.setHint("Search via Filename");

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
                //  advSearch.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
                // advSearch.setVisibility(View.GONE);
            }
        });
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText);
                return true;
            }
        });

        progressDialog = new ProgressDialog(this);

    }

    private void doSearch(String query) {
        documentUploadedRealmResults = realm.where(DocumentUploadedFiles.class).equalTo("documentId", document_id).contains("filename", query, Case.INSENSITIVE).findAll().sort("filename", Sort.ASCENDING);
        recyclerView.setLayoutManager(new LinearLayoutManager(DocumentUploadedFilesActivity.this));
        recyclerView.setAdapter(new DocumentUploadedFilesAdapter(DocumentUploadedFilesActivity.this, documentUploadedRealmResults, Constants.FROM_NORMAL,user));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    protected void onResume() {
        super.onResume();
        getFiles();
        mMedescope = Medescope.getInstance(this);
        mMedescope.setApplicationName(getString(R.string.app_name));

    }


    @Override
    public void CallDownload(DocumentUploadedFiles obj) {
        NotificationData data = new NotificationData(obj.getDocumentFileId(), getString(R.string.app_name), obj.getFilename());
        selectedId = Integer.parseInt(obj.getDocumentFileId());
        file_path = obj.getFilename();
        mMedescope.enqueue(obj.getDocumentFileId(),
                Constants.FILES_URL + obj.getUploadedFilename().trim(),
                obj.getFilename(),
                obj.getFilename(),
                data.toJson());
    }

    @Override
    public void checkFilePath(String filePath) {
        String originalFilePath = "";
        File file = new File(filePath);
        String strFileName = file.getName();
        try {
            originalFilePath = mMedescope.getDownloadDirectoryToRead(file_path);
            Log.d(TAG, "Original path: " + originalFilePath);
        } catch (DirectoryNotMountedException | PathNotFoundException e) {
            e.printStackTrace();
        }

        if (filePath.equals(originalFilePath)) {
            File source = new File(originalFilePath);
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DMS");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DMS/" + strFileName;
                File destination = new File(destinationPath);
                try {
                    FileUtils.copyFile(source, destination);
                    Toast.makeText(this, "Downloaded to" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/DMS/" + strFileName, Toast.LENGTH_SHORT).show();
                    source.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DMS/" + strFileName;
                File destination = new File(destinationPath);
                try {
                    FileUtils.copyFile(source, destination);
                    Toast.makeText(this, "Downloaded to" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/DMS/" + strFileName, Toast.LENGTH_SHORT).show();
                    source.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void getFiles() {
        progressBar.setVisibility(View.VISIBLE);
        Call<List<DocumentUploadedFiles>> getData = apiInterface.getAllDocumentUploadedFiles(user.getU(),user.getS(),ParameterEncryption.getAllDocumentUploadedFiles(document_id));
        getData.enqueue(new Callback<List<DocumentUploadedFiles>>() {
            @Override
            public void onResponse(Call<List<DocumentUploadedFiles>> call, final Response<List<DocumentUploadedFiles>> response) {
                final Realm realm = Realm.getDefaultInstance();
                progressBar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                if (response.isSuccessful()) {
                    if (response.body().isEmpty()) {
                        toolbar.setTitle(document_desc);
                        no_files.setVisibility(View.VISIBLE);
                    } else {
                        realm.executeTransactionAsync(
                                new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.copyToRealmOrUpdate(response.body());
                                    }
                                }, new Realm.Transaction.OnSuccess() {
                                    @Override
                                    public void onSuccess() {
                                        RealmResults<DocumentUploadedFiles> documentUploadedRealmQuery = realm.where(DocumentUploadedFiles.class).equalTo("documentId", document_id).findAll().sort("filename", Sort.ASCENDING);
                                        toolbar.setTitle(document_desc + " (" + documentUploadedRealmQuery.size() + ")");
                                        recyclerView.setLayoutManager(new LinearLayoutManager(DocumentUploadedFilesActivity.this));
                                        recyclerView.setAdapter(new DocumentUploadedFilesAdapter(DocumentUploadedFilesActivity.this, documentUploadedRealmQuery, Constants.FROM_NORMAL,user));
                                        recyclerView.setVisibility(View.VISIBLE);
                                        no_files.setVisibility(View.GONE);
                                        realm.close();

                                    }
                                }, new Realm.Transaction.OnError() {
                                    @Override
                                    public void onError(Throwable error) {
                                        Toast.makeText(DocumentUploadedFilesActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                                        realm.close();
                                        error.printStackTrace();
                                    }
                                }

                        );
                    }
                } else {
                    Toast.makeText(DocumentUploadedFilesActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<List<DocumentUploadedFiles>> call, Throwable t) {
                t.printStackTrace();
                toolbar.setTitle(document_desc);
                no_files.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DocumentUploadedFilesActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            Medescope.getInstance(this).unsubscribeStatus(this);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
   /*     Medescope.getInstance(this).unsubscribeStatus(this);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.files_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.action_refresh) {
            getFiles();
        } else if (i == R.id.action_share) {
            doShare();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doShare() {
        final Dialog dialog = new Dialog(this);
        final List<String> strings = new ArrayList<>();
        final List<String> userIdStrings = new ArrayList<>();
        final TagGroup tagGroup,tagGroupShared;
        Button submit, cancel;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_share);
        dialog.setCancelable(false);
        tagGroup = (TagGroup) dialog.findViewById(R.id.tagGroup);
        tagGroupShared = (TagGroup) dialog.findViewById(R.id.tagGroupShared);
        strings.clear();
        tagGroup.setOnTagChangeListener(new TagGroup.OnTagChangeListener() {
            @Override
            public void onAppend(TagGroup tagGroup, String tag) {
                Employee employee = realm.where(Employee.class).equalTo("userId", tag.trim()).findFirst();
                if (employee == null) {
                    if (!tag.trim().equals("")) {
                        strings.add(tag.trim());
                        Toast.makeText(DocumentUploadedFilesActivity.this, strings.get(strings.size() - 1) + " does not exists", Toast.LENGTH_SHORT).show();
                        tagGroup.removeViewAt(strings.size() - 1);
                        strings.remove(strings.size() - 1);
                    } else {
                        strings.add(tag.trim() + "FILL UP!");
                        Toast.makeText(DocumentUploadedFilesActivity.this, strings.get(strings.size() - 1), Toast.LENGTH_SHORT).show();
                        tagGroup.removeViewAt(strings.size() - 1);
                        strings.remove(strings.size() - 1);
                    }

                } else {
                    if (strings.contains(tag.trim())) {
                        tagGroup.removeViewAt(strings.size());
                        Toast.makeText(DocumentUploadedFilesActivity.this, tag.trim() + " is already added", Toast.LENGTH_SHORT).show();
                    } else if (tag.trim().equals(user.getUserId().trim())) {
                        tagGroup.removeViewAt(strings.size());
                        Toast.makeText(DocumentUploadedFilesActivity.this, "You cant share document to yourself", Toast.LENGTH_SHORT).show();
                    } else if (userIdStrings.contains(tag.trim())) {
                        tagGroup.removeViewAt(strings.size());
                        Toast.makeText(DocumentUploadedFilesActivity.this, "Document is already shared with " + tag.trim(), Toast.LENGTH_SHORT).show();
                    } else {
                        strings.add(tag.trim());
                    }
                }

            }

            @Override
            public void onDelete(TagGroup tagGroup, String tag) {
                for (int i = 0; i < strings.size(); i++) {
                    Log.d("STRINGS ID", +i + " " + strings.get(i));
                    if (strings.get(i).equals(tag.trim())) {
                        Toast.makeText(DocumentUploadedFilesActivity.this, strings.get(i) + " deleted", Toast.LENGTH_SHORT).show();
                        strings.remove(i);
                    }
                }
            }
        });


        submit = (Button) dialog.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempUsers = strings;
                if (!tempUsers.isEmpty()) {
                    share(0, tempUsers.size(), tempUsers.get(0));
                    dialog.dismiss();
                } else {
                    Toast.makeText(DocumentUploadedFilesActivity.this, "No Users Selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancel = (Button) dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        progressDialog.show();
        progressDialog.setMessage("Getting sharing info...");
        apiInterface.getUserSharedWithList(user.getU(),user.getS(), ParameterEncryption.getUserSharedWithList(document_id)).enqueue(new Callback<List<UserSharedWith>>() {
            @Override
            public void onResponse(Call<List<UserSharedWith>> call, Response<List<UserSharedWith>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    if (!response.body().isEmpty()) {
                        userSharedWithList = response.body();
                        for (int i = 0; i < userSharedWithList.size(); i++) {
                            userIdStrings.add(i, userSharedWithList.get(i).getUserIdSharedWith());
                        }
                        tagGroupShared.setTags(userIdStrings);
                    }else {
                        tagGroupShared.setTags("None");
                    }
                    dialog.show();
                } else {

                    Toast.makeText(DocumentUploadedFilesActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<List<UserSharedWith>> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
    public void share(final int x, final int size, final String user_id_shared_with) {
        progressDialog.show();
        progressDialog.setMessage("Sharing to " + (x + 1) + " of " + size);
        apiInterface.shareDocument(user.getU(),user.getS(),ParameterEncryption.shareDocument(document_id, user_id_shared_with, user.getUserId())).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (response.isSuccessful()) {
                    if (response.body().getResult().equals(Constants.SUCCESS)) {
                        if (x + 1 == size) {
                            Toast.makeText(DocumentUploadedFilesActivity.this, document_desc + " is shared successfully", Toast.LENGTH_SHORT).show();
                            tempUsers.clear();
                            progressDialog.dismiss();
                        } else {
                            share(x + 1, size, tempUsers.get(x + 1));
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(DocumentUploadedFilesActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(DocumentUploadedFilesActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                t.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(DocumentUploadedFilesActivity.this, R.string.request_failed, Toast.LENGTH_SHORT).show();

            }

        });
    }

}
