package ph.edu.tip.app.dms.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.ArrayList;
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
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharedDocumentFilesActivity extends AppCompatActivity implements DocumentUploadedFilesAdapter.DownloadCaller {

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
    private TextView breadcrumb;
    private String breadcrumb_title;
    private Medescope mMedescope;
    private String file_path;
    private static final String TAG = MainActivity.class.getSimpleName();
    private int selectedId;
    private RealmResults<DocumentUploadedFiles> documentUploadedRealmResults;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_document_files);
        realm = Realm.getDefaultInstance();
        user = realm.where(User.class).findFirst();
        apiInterface = App.getApiInterface();


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
            Call<List<DocumentUploaded>> getDocument = apiInterface.getSingleDocumentUploaded(user.getU(),user.getS(), ParameterEncryption.getSingleDocumentUploaded(document_id));
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

    }

    private void doSearch(String query) {
        documentUploadedRealmResults = realm.where(DocumentUploadedFiles.class).equalTo("documentId", document_id).contains("filename", query, Case.INSENSITIVE).findAll().sort("filename", Sort.ASCENDING);
        recyclerView.setLayoutManager(new LinearLayoutManager(SharedDocumentFilesActivity.this));
        recyclerView.setAdapter(new DocumentUploadedFilesAdapter(SharedDocumentFilesActivity.this, documentUploadedRealmResults, Constants.FROM_SHARED,user));
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
                                        recyclerView.setLayoutManager(new LinearLayoutManager(SharedDocumentFilesActivity.this));
                                        recyclerView.setAdapter(new DocumentUploadedFilesAdapter(SharedDocumentFilesActivity.this, documentUploadedRealmQuery, Constants.FROM_SHARED,user));
                                        recyclerView.setVisibility(View.VISIBLE);
                                        no_files.setVisibility(View.GONE);
                                        realm.close();

                                    }
                                }, new Realm.Transaction.OnError() {
                                    @Override
                                    public void onError(Throwable error) {
                                        Toast.makeText(SharedDocumentFilesActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                                        realm.close();
                                        error.printStackTrace();
                                    }
                                }

                        );
                    }
                } else {
                    Toast.makeText(SharedDocumentFilesActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<List<DocumentUploadedFiles>> call, Throwable t) {
                t.printStackTrace();
                toolbar.setTitle(document_desc);
                no_files.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SharedDocumentFilesActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.shared_files_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.action_refresh) {
            getFiles();
        }
        return super.onOptionsItemSelected(item);
    }
}

