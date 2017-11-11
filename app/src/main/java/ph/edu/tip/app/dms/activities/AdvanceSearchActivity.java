package ph.edu.tip.app.dms.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.adapters.DocumentSearchedFilesAdapter;
import ph.edu.tip.app.dms.adapters.DocumentUploadedFilesAdapter;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentSearchedFiles;
import ph.edu.tip.app.dms.models.DocumentUploaded;
import ph.edu.tip.app.dms.models.DocumentUploadedFiles;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdvanceSearchActivity extends AppCompatActivity {

    private Button search;
    private RecyclerView documentsRecyclerView;
    private Realm realm;
    private ApiInterface apiInterface;
    private RealmResults<DocumentSearchedFiles> documentUploadedFiles;
    private LinearLayout no_result;
    private TextView search_content;
    private ProgressBar progressBar;
    private EditText content;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance_search);
        realm = Realm.getDefaultInstance();
        user = realm.where(User.class).findFirst();
        apiInterface = App.getApiInterface();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        content = (EditText) findViewById(R.id.editText2);
        documentsRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewDocuments);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        search_content = (TextView) findViewById(R.id.keyword);
        no_result = (LinearLayout) findViewById(R.id.no_result);
        no_result.setVisibility(View.GONE);
        documentsRecyclerView.setVisibility(View.GONE);
        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String c = content.getText().toString();
                if (c.equals("")) {
                    Toast.makeText(AdvanceSearchActivity.this, "Fill up fields", Toast.LENGTH_SHORT).show();
                } else {
                    getDocuments(c);
                }


            }
        });

        content.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    search.performClick();
                    return true;
                }
                return false;
            }
        });

        documentsRecyclerView.setVisibility(View.GONE);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(DocumentSearchedFiles.class);
            }
        });
        documentUploadedFiles = realm.where(DocumentSearchedFiles.class).findAllAsync().sort("documentFileId", Sort.DESCENDING);
        documentUploadedFiles.addChangeListener(new RealmChangeListener<RealmResults<DocumentSearchedFiles>>() {
            @Override
            public void onChange(RealmResults<DocumentSearchedFiles> element) {
                if (!documentUploadedFiles.isEmpty()) {
                    documentsRecyclerView.setLayoutManager(new LinearLayoutManager(AdvanceSearchActivity.this));
                    documentsRecyclerView.setAdapter(new DocumentSearchedFilesAdapter(AdvanceSearchActivity.this, documentUploadedFiles, true));
                    documentsRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    documentsRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    documentsRecyclerView.setVisibility(View.GONE);
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

    public void getDocuments(final String content) {
        progressBar.setVisibility(View.VISIBLE);
        Call<List<DocumentSearchedFiles>> templateResult = apiInterface.getFilesByContent(user.getU(),user.getS(), ParameterEncryption.getFilesByContent(content,user.getDepartmentCode().trim()));
        templateResult.enqueue(new Callback<List<DocumentSearchedFiles>>() {
            @Override
            public void onResponse(Call<List<DocumentSearchedFiles>> call, final Response<List<DocumentSearchedFiles>> response) {
                final Realm realm = Realm.getDefaultInstance();
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    if (response.body().isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        no_result.setVisibility(View.VISIBLE);
                        search_content.setText(content);
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
                                        Toast.makeText(AdvanceSearchActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }

                        );
                    } else {
                        realm.executeTransactionAsync(
                                new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.delete(DocumentSearchedFiles.class);
                                        realm.copyToRealmOrUpdate(response.body());
                                    }
                                }, new Realm.Transaction.OnSuccess() {
                                    @Override
                                    public void onSuccess() {
                                        no_result.setVisibility(View.GONE);
                                        documentsRecyclerView.setVisibility(View.VISIBLE);
                                        realm.close();
                                    }
                                }, new Realm.Transaction.OnError() {
                                    @Override
                                    public void onError(Throwable error) {
                                        realm.close();
                                        error.printStackTrace();
                                        Toast.makeText(AdvanceSearchActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }

                        );
                    }

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
                                    Toast.makeText(AdvanceSearchActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }

                    );
                }
            }

            @Override
            public void onFailure(Call<List<DocumentSearchedFiles>> call, Throwable t) {
                t.printStackTrace();
                final Realm realm = Realm.getDefaultInstance();
                progressBar.setVisibility(View.GONE);
                no_result.setVisibility(View.VISIBLE);
                search_content.setText(content);
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
                                Toast.makeText(AdvanceSearchActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
