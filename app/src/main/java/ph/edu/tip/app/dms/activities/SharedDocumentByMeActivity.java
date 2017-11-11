package ph.edu.tip.app.dms.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.adapters.DocumentSharedAdapter;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentShared;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharedDocumentByMeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Realm realm;
    private User user;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private RelativeLayout noSharedDocuments;
    private LinearLayout no_result;
    private TextView search_content;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_by_me);
        realm = Realm.getDefaultInstance();
        user = realm.where(User.class).findFirst();
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        noSharedDocuments = (RelativeLayout) findViewById(R.id.no_shared_documents);
        noSharedDocuments.setVisibility(View.GONE);


        final RealmResults<DocumentShared> documentUploadedFiles = realm.where(DocumentShared.class).equalTo("userIdSharedBy", user.getUserId().trim()).findAllAsync();
        documentUploadedFiles.addChangeListener(new RealmChangeListener<RealmResults<DocumentShared>>() {
            @Override
            public void onChange(RealmResults<DocumentShared> element) {
                recyclerView.setLayoutManager(new LinearLayoutManager(SharedDocumentByMeActivity.this));
                recyclerView.setAdapter(new DocumentSharedAdapter(SharedDocumentByMeActivity.this, documentUploadedFiles, toolbar.getTitle().toString()));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                if (documentUploadedFiles.isEmpty()) {
                    noSharedDocuments.setVisibility(View.VISIBLE);
                } else {
                    noSharedDocuments.setVisibility(View.GONE);
                }
            }
        });


        getSharedDocuments(user.getUserId());
        initializeSearchView();
    }

    public void getSharedDocuments(String user_id) {
        progressDialog.show();
        progressDialog.setMessage("Getting documents...");
        ApiInterface apiInterface = App.getApiInterface();
        apiInterface.getSharedDocumentsBy(user.getU(), user.getS(), ParameterEncryption.getSharedDocumentsBy(user_id)).enqueue(new Callback<List<DocumentShared>>() {
            @Override
            public void onResponse(Call<List<DocumentShared>> call, final Response<List<DocumentShared>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    try {
                        if (!response.body().isEmpty()) {
                            final Realm realm = Realm.getDefaultInstance();
                            realm.executeTransactionAsync(
                                    new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.delete(DocumentShared.class);
                                            realm.copyToRealmOrUpdate(response.body());
                                        }
                                    }, new Realm.Transaction.OnSuccess() {
                                        @Override
                                        public void onSuccess() {
                                            realm.close();
                                        }
                                    }, new Realm.Transaction.OnError() {
                                        @Override
                                        public void onError(Throwable error) {
                                            realm.close();
                                            error.printStackTrace();
                                            Toast.makeText(SharedDocumentByMeActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                            );
                        } else {
                            progressDialog.dismiss();
                            noSharedDocuments.setVisibility(View.VISIBLE);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(SharedDocumentByMeActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(SharedDocumentByMeActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DocumentShared>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(SharedDocumentByMeActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void initializeSearchView() {
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.custom_cursor);
        searchView.setEllipsize(true);
        searchView.setHint("Search description, tags, user id...");

        search_content = (TextView) findViewById(R.id.keyword);
        no_result = (LinearLayout) findViewById(R.id.no_result);
        no_result.setVisibility(View.GONE);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
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

    public void doSearch(String text) {
        final RealmResults<DocumentShared> documentUploadedFiles = realm.where(DocumentShared.class)
                .contains("tags", text, Case.INSENSITIVE)
                .or()
                .contains("documentDescription", text, Case.INSENSITIVE)
                .or()
                .contains("userIdSharedWith", text, Case.INSENSITIVE).findAll()
                .sort("dateShared", Sort.ASCENDING);
        recyclerView.setLayoutManager(new LinearLayoutManager(SharedDocumentByMeActivity.this));
        recyclerView.setAdapter(new DocumentSharedAdapter(SharedDocumentByMeActivity.this, documentUploadedFiles, toolbar.getTitle().toString()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (documentUploadedFiles.isEmpty()) {
            no_result.setVisibility(View.VISIBLE);
            search_content.setText(text);
        } else {
            no_result.setVisibility(View.GONE);
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.action_refresh) {
            getSharedDocuments(user.getUserId());
        }
        return super.onOptionsItemSelected(item);
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
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
