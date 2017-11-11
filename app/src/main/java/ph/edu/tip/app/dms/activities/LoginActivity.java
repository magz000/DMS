package ph.edu.tip.app.dms.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import br.com.bemobi.medescope.Medescope;
import br.com.bemobi.medescope.callback.DownloadStatusCallback;
import io.realm.Realm;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.Employee;
import ph.edu.tip.app.dms.models.Result;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.Encrypt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    // UI references.

    private EditText mPasswordView, mEmailView, mCampus;
    private Realm realm;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        realm = Realm.getDefaultInstance();
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mCampus = (EditText) findViewById(R.id.campus);


        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login(mEmailView.getText().toString(), mPasswordView.getText().toString(),mCampus.getText().toString());
            }
        });

        User user = realm.where(User.class).findFirst();
        if (user != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Logging in...");

        mCampus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(LoginActivity.this)
                        .title("Campus")
                        .items("Manila", "Quezon City")
                        .itemsCallback(new MaterialDialog.ListCallback() {
                                           @Override
                                           public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                               mCampus.setText(text);
                                           }
                                       }).
                        show();
            }
        });

    }

    private void Login(String email, String password, String campus) {
        if (email.equals("") || password.equals("") || campus.equals("")) {
            Toast.makeText(this, "Fill up details", Toast.LENGTH_SHORT).show();
        } else {
            String encrypt = "";
            final String u = email.substring(0, 1);
            String campusParameter = "";
            if(campus.equals("Manila")){
                campusParameter = "M";
            }else if(campus.equals("Quezon City")) {
                campusParameter = "Q";
            }
            if (u.equals("0")) {
                encrypt = Encrypt.protectString("do_action=login&user_id=" + email.trim().substring(1) + "&password=" + password.trim());
                Log.e("LOGIN","do_action=login&user_id=" + email.trim().substring(1) + "&password=" + password.trim() +"&s="+campusParameter);
            } else {
                encrypt = Encrypt.protectString("do_action=login&user_id=" + email.trim() + "&password=" + password.trim());
            }
            progressDialog.setMessage("Getting user info...");
            progressDialog.show();
            ApiInterface apiInterface = App.getApiInterface();
            final String finalCampusParameter = campusParameter;
            apiInterface.login(u,campusParameter,encrypt).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, final Response<User> response) {
                    if (response.isSuccessful()) {
                        try {
                            if (response.body().getMessage().equals("Login successful")) {
                                final Realm realm = Realm.getDefaultInstance();
                                realm.executeTransactionAsync(
                                        new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                realm.copyToRealmOrUpdate(response.body());
                                            }
                                        }, new Realm.Transaction.OnSuccess() {
                                            @Override
                                            public void onSuccess() {
                                                realm.close();
                                                getEmployees(u, finalCampusParameter);

                                            }
                                        }, new Realm.Transaction.OnError() {
                                            @Override
                                            public void onError(Throwable error) {
                                                realm.close();
                                                error.printStackTrace();
                                                progressDialog.dismiss();
                                                Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                );
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "No Record Found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void getEmployees(String u,String s) {
        String encrypt = Encrypt.protectString("do_action=getEmployees");
        progressDialog.show();
        progressDialog.setMessage("Getting employees...");
        ApiInterface apiInterface = App.getApiInterface();
        apiInterface.getEmployees(u,s,encrypt).enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, final Response<List<Employee>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    if (!response.body().isEmpty()) {
                        final Realm realm = Realm.getDefaultInstance();
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealmOrUpdate(response.body());
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                                realm.close();
                            }
                        }, new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {
                                realm.close();
                                realm.deleteAll();
                                error.printStackTrace();
                                Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    realm.deleteAll();
                }

            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                progressDialog.dismiss();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.deleteAll();
                    }
                });
                Toast.makeText(LoginActivity.this, R.string.oops, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}

