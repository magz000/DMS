package ph.edu.tip.app.dms.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.IOException;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.adapters.DocumentUploadedAdapter;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentUploaded;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentsActivity extends AppCompatActivity {

    private Realm realm;
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RealmResults<DocumentUploaded> documentUploadedRealmResults;
    private String folder_id;
    private String folder_name;
    private RelativeLayout no_documents;
    private String template_name;
    private MaterialSearchView searchView;
    private TextView advSearch, breadcrumb;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        realm = Realm.getDefaultInstance();
        user = realm.where(User.class).findFirst();
        apiInterface = App.getApiInterface();


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        Intent i = getIntent();
        folder_id = i.getStringExtra(Constants.FOLDER_ID_KEY);
        folder_name = i.getStringExtra(Constants.FOLDER_NAME_KEY);
        template_name = i.getStringExtra(Constants.TEMPLATE_NAME_KEY);

        Log.e("Folder ID : ", folder_id);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        // toolbar.setTitle(template_name + " / " + folder_name);
        toolbar.setTitle("Folder");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        breadcrumb = (TextView) findViewById(R.id.breadcrumb);
        breadcrumb.setText(template_name + " > " + folder_name);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllDocumentUploaded(folder_id);

            }
        });

        no_documents = (RelativeLayout) findViewById(R.id.no_documents);

        documentUploadedRealmResults = realm.where(DocumentUploaded.class).equalTo("folderId", folder_id).findAll().sort("documentDescription", Sort.ASCENDING);
        ;
        documentUploadedRealmResults.addChangeListener(new RealmChangeListener<RealmResults<DocumentUploaded>>() {
            @Override
            public void onChange(RealmResults<DocumentUploaded> element) {
                recyclerView.setLayoutManager(new LinearLayoutManager(DocumentsActivity.this));
                recyclerView.setAdapter(new DocumentUploadedAdapter(DocumentsActivity.this, documentUploadedRealmResults, breadcrumb.getText().toString(),user));
                recyclerView.setItemAnimator(new DefaultItemAnimator());


            }
        });

        getAllDocumentUploaded(folder_id);

        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DocumentsActivity.this, PickerActivity.class);
                i.putExtra(Constants.FOLDER_ID_KEY, folder_id);
                i.putExtra(Constants.FOLDER_NAME_KEY, folder_name);
                i.putExtra(Constants.TEMPLATE_NAME_KEY, template_name);//this

                startActivity(i);
            }
        });

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.custom_cursor);
        searchView.setEllipsize(true);
        searchView.setHint("Search via description and tags");

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
                advSearch.setVisibility(View.VISIBLE);
                breadcrumb.setVisibility(View.GONE);
            }

            @Override
            public void onSearchViewClosed() {
                breadcrumb.setVisibility(View.VISIBLE);
                advSearch.setVisibility(View.GONE);
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

        advSearch = (TextView) findViewById(R.id.advSearch);
        advSearch.setVisibility(View.GONE);
        advSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DocumentsActivity.this, AdvanceSearchActivity.class));

            }
        });
    }

    public void doSearch(String text) {
        RealmResults<DocumentUploaded> documentUploadedRealmResults = realm.where(DocumentUploaded.class)
                .equalTo("folderId", folder_id)
                .contains("tags", text, Case.INSENSITIVE)
                .or().contains("documentDescription", text, Case.INSENSITIVE)
                .equalTo("folderId", folder_id).findAll()
                .sort("documentDescription", Sort.ASCENDING);
        recyclerView.setLayoutManager(new LinearLayoutManager(DocumentsActivity.this));
        recyclerView.setAdapter(new DocumentUploadedAdapter(DocumentsActivity.this, documentUploadedRealmResults, breadcrumb.getText().toString(),user));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllDocumentUploaded(folder_id);
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        }

    }

    private void getAllDocumentUploaded(String id) {
        swipeRefreshLayout.setRefreshing(true);
        Call<List<DocumentUploaded>> templateResult = apiInterface.getDocumentUploaded(user.getU(),user.getS(), ParameterEncryption.getAllDocumentUploaded(id));
        templateResult.enqueue(new Callback<List<DocumentUploaded>>() {
            @Override
            public void onResponse(Call<List<DocumentUploaded>> call, final Response<List<DocumentUploaded>> response) {
                swipeRefreshLayout.setRefreshing(false);
                final Realm realm = Realm.getDefaultInstance();
                if (response.isSuccessful()) {
                    if (response.body().isEmpty()) {
                        no_documents.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        realm.executeTransactionAsync(
                                new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.delete(DocumentUploaded.class);
                                        realm.copyToRealmOrUpdate(response.body());
                                    }
                                }, new Realm.Transaction.OnSuccess() {
                                    @Override
                                    public void onSuccess() {
                                        no_documents.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                        realm.close();
                                    }
                                }, new Realm.Transaction.OnError() {
                                    @Override
                                    public void onError(Throwable error) {
                                        realm.close();
                                        error.printStackTrace();
                                        Toast.makeText(DocumentsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }

                        );
                    }

                }
            }

            @Override
            public void onFailure(Call<List<DocumentUploaded>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                if (t instanceof IOException) {
                    Toast.makeText(DocumentsActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

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
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}


