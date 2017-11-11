package ph.edu.tip.app.dms.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.adapters.DocumentUploadedAdapter;
import ph.edu.tip.app.dms.adapters.FolderAdapter;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentUploaded;
import ph.edu.tip.app.dms.models.Folders;
import ph.edu.tip.app.dms.models.Templates;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.CustomSpinnerAdapter;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Realm realm;
    private ApiInterface apiInterface;
    private RecyclerView recyclerViewDocuments, recyclerViewFolders, recyclerViewSearchResults;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RealmResults<Folders> folderUploadedRealmResults;
    private RealmResults<DocumentUploaded> documentUploadedRealmResults;
    private String folder_id;
    private String template_title,template_dept_code;
    private RelativeLayout no_folders;
    private Spinner spinner_nav;
    private RealmResults<Templates> templatesRealmResults;
    private List<Templates> templatesList;
    private TextView advSearch, search_content;
    private MaterialSearchView searchView;
    private LinearLayout no_result;
    private ProgressBar progressBar;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        realm = Realm.getDefaultInstance();

        apiInterface = App.getApiInterface();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        realm = Realm.getDefaultInstance();
        apiInterface = App.getApiInterface();

        user = realm.where(User.class).findFirst();


        TextView employeeName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.employeeName);
        TextView position = (TextView) navigationView.getHeaderView(0).findViewById(R.id.position);
        final TextView dept_code = (TextView) navigationView.getHeaderView(0).findViewById(R.id.department);

        employeeName.setText(user.getEmployeeName());
        position.setText(user.getPosition());
        dept_code.setText(user.getDepartmentCode());

        recyclerViewDocuments = (RecyclerView) findViewById(R.id.recyclerViewDocuments);
        recyclerViewFolders = (RecyclerView) findViewById(R.id.recyclerViewFolders);
        recyclerViewSearchResults = (RecyclerView) findViewById(R.id.recyclerViewSearchResults);

        spinner_nav = (Spinner) findViewById(R.id.spinner_nav);
        advSearch = (TextView) findViewById(R.id.advSearch);
        advSearch.setVisibility(View.GONE);
        advSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AdvanceSearchActivity.class));

            }
        });


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllFoldersFromTemplate(template_title,template_dept_code);
                getAllDocumentUploadedFromDeptCode(user.getDepartmentCode());
                // getAllDocumentUploaded(template_title,folder_id);

            }
        });

        no_folders = (RelativeLayout) findViewById(R.id.no_folders);

        folderUploadedRealmResults = realm.where(Folders.class).findAll().sort("folderName", Sort.ASCENDING);
        folderUploadedRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Folders>>() {
            @Override
            public void onChange(RealmResults<Folders> element) {
                if (folderUploadedRealmResults.isEmpty()) {
                    no_folders.setVisibility(View.VISIBLE);
                    recyclerViewFolders.setVisibility(View.GONE);
                } else {
                    StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, 1);
                    recyclerViewFolders.setLayoutManager(staggeredGridLayoutManager);
                    recyclerViewFolders.setAdapter(new FolderAdapter(MainActivity.this, folderUploadedRealmResults, template_title));
                    recyclerViewFolders.setItemAnimator(new DefaultItemAnimator());
                    recyclerViewFolders.setVisibility(View.VISIBLE);
                    no_folders.setVisibility(View.GONE);
                }

            }
        });


        search_content = (TextView) findViewById(R.id.keyword);
        no_result = (LinearLayout) findViewById(R.id.no_result);
        no_result.setVisibility(View.GONE);
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.custom_cursor);
        searchView.setEllipsize(true);
        searchView.setHint("Search via description and tags");
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                doSearch(newText);
                return true;
            }
        });


        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
                recyclerViewSearchResults.setVisibility(View.VISIBLE);
                recyclerViewDocuments.setVisibility(View.GONE);
                recyclerViewFolders.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.GONE);
                advSearch.setVisibility(View.VISIBLE);
                getAllDocumentUploadedFromDeptCode(user.getDepartmentCode());
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
                recyclerViewSearchResults.setVisibility(View.GONE);
                recyclerViewDocuments.setVisibility(View.VISIBLE);
                recyclerViewFolders.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                advSearch.setVisibility(View.GONE);
                no_result.setVisibility(View.GONE);
            }
        });

        templatesRealmResults = realm.where(Templates.class).findAll().sort("templateName", Sort.ASCENDING);
        templatesRealmResults.addChangeListener(new RealmChangeListener<RealmResults<Templates>>() {
            @Override
            public void onChange(RealmResults<Templates> element) {
                if (templatesRealmResults.isEmpty()) {
                    //Do nothing
                } else {
                    templatesList = realm.copyFromRealm(templatesRealmResults);
                    addItemsToSpinner();
                }
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        getAllTemplate();
    }

    public void doSearch(String text) {
        Realm realm = Realm.getDefaultInstance();
        documentUploadedRealmResults = realm.where(DocumentUploaded.class).contains("tags", text, Case.INSENSITIVE).or().contains("documentDescription", text, Case.INSENSITIVE).findAll();
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerViewSearchResults.setAdapter(new DocumentUploadedAdapter(MainActivity.this, documentUploadedRealmResults, "",user));
        recyclerViewSearchResults.setItemAnimator(new DefaultItemAnimator());

        if (documentUploadedRealmResults.isEmpty()) {
            no_result.setVisibility(View.VISIBLE);
            search_content.setText(text);
        } else {
            no_result.setVisibility(View.GONE);
        }
        realm.close();

    }

    public void addItemsToSpinner() {

        ArrayList<String> list = new ArrayList<String>();
        list.clear();
        for (int x = 0; x < templatesList.size(); x++) {
            list.add(templatesList.get(x).getTemplateName());
        }


        CustomSpinnerAdapter spinAdapter = new CustomSpinnerAdapter(
                MainActivity.this, list);


        spinner_nav.setAdapter(spinAdapter);

        spinner_nav.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item
                String item = adapter.getItemAtPosition(position).toString();
                //get template
                RealmResults<Templates> templatesRealmResults = realm.where(Templates.class).equalTo("templateName", item).findAll();
                Templates templates = templatesRealmResults.first();
                folder_id = templates.getFolderId();
                template_title = templates.getTemplateName();
                template_dept_code = templates.getDeptCode();
                getAllFoldersFromTemplate(template_title,template_dept_code);
                Log.d("folder_id ", folder_id);
                Log.d("template_title ", template_title);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getAllTemplate() {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading Department Templates...");
        progressDialog.show();
        swipeRefreshLayout.setRefreshing(false);
        Call<List<Templates>> templateResult = apiInterface.getTemplates(user.getU(),user.getS(), ParameterEncryption.getAllTemplates(user.getDepartmentCode().trim()));
        templateResult.enqueue(new Callback<List<Templates>>() {
            @Override
            public void onResponse(Call<List<Templates>> call, final Response<List<Templates>> response) {
                progressDialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                final Realm realm = Realm.getDefaultInstance();
                if (response.isSuccessful() && !response.body().isEmpty()) {
                    realm.executeTransactionAsync(
                            new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.delete(Templates.class);
                                    realm.copyToRealmOrUpdate(response.body());
                                    // templatesList = realm.copyFromRealm(templatesRealmResults);
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
                                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }

                    );
                }
            }

            @Override
            public void onFailure(Call<List<Templates>> call, Throwable t) {
                progressDialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getAllDocumentUploadedFromDeptCode(String dept_code) {
        Call<List<DocumentUploaded>> templateResult = apiInterface.getDocumentUploadedViaDeptCode(user.getU(),user.getS(),ParameterEncryption.getUploadedViaDeptCode(dept_code));
        templateResult.enqueue(new Callback<List<DocumentUploaded>>() {
            @Override
            public void onResponse(Call<List<DocumentUploaded>> call, final Response<List<DocumentUploaded>> response) {
                final Realm realm = Realm.getDefaultInstance();
                if (response.isSuccessful() && !response.body().isEmpty()) {
                    realm.executeTransactionAsync(
                            new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.delete(DocumentUploaded.class);
                                    realm.copyToRealmOrUpdate(response.body());
                                    // templatesList = realm.copyFromRealm(templatesRealmResults);
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
                                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }

                    );
                } else {
                    realm.executeTransactionAsync(
                            new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.delete(DocumentUploaded.class);
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
                                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }

                    );
                }
            }

            @Override
            public void onFailure(Call<List<DocumentUploaded>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getAllFoldersFromTemplate(String template_name,String dept_code) {
        progressBar.setVisibility(View.VISIBLE);
        Call<List<Folders>> templateResult = apiInterface.getAllFoldersFromTemplate(user.getU(),user.getS(),ParameterEncryption.getAllFoldersFromTemplate(template_name,dept_code));
        templateResult.enqueue(new Callback<List<Folders>>() {
            @Override
            public void onResponse(Call<List<Folders>> call, final Response<List<Folders>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                final Realm realm = Realm.getDefaultInstance();
                if (response.isSuccessful()) {
                    if (response.body().isEmpty()) {
                        no_folders.setVisibility(View.VISIBLE);
                    } else {
                        no_folders.setVisibility(View.GONE);
                        realm.executeTransactionAsync(
                                new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.delete(Folders.class);
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
                                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }

                        );
                    }

                } else {
                    Log.d("Error message", response.message());
                    Log.d("Error code", String.valueOf(response.code()));
                    Toast.makeText(MainActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Folders>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (t instanceof IOException) {
                    //Add your code for displaying no network connection error
                    Toast.makeText(MainActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            getAllTemplate();
        } else if (id == R.id.sharedDocs) {
            startActivity(new Intent(this, SharedDocumentToMeActivity.class));
        } else if (id == R.id.mySharedDocs) {
            startActivity(new Intent(this, SharedDocumentByMeActivity.class));
        } else if (id == R.id.logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Log Out");
            builder.setMessage("Are you sure?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing but close the dialog
                    final Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.deleteAll();
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            realm.close();
                            // TODO: 12/4/2016 add flag to clear all task
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            MainActivity.this.finish();
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                            realm.close();
                        }
                    });
                    finish();
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Do nothing
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
