package ph.edu.tip.app.dms.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.PermissionChecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.Result;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Mark Jansen Calderon on 11/11/2016.
 */

public class App extends Application {

    private static final String ROOT_URL = "http://dms.tip.edu.ph/dms/";
    private static int targetSdkVersion;

    public App() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

    }

    ;

    /**
     * Get Retro Client
     *
     * @return JSON Object
     */
    private static Retrofit getRetroClient() {
        final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        final Gson gson = new GsonBuilder()
                .setLenient()
                .create();


        return new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static ApiInterface getApiInterface() {
        return getRetroClient().create(ApiInterface.class);
    }

    public static void addToTesseract(String u,String s, String document_id) {

        ApiInterface apiInterface = getApiInterface();
        Call<Result> resultCall = apiInterface.readDocumentFiles(u,s, ParameterEncryption.readDocumentFiles(document_id));
        resultCall.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                // Response Success or Fail
                if (response.body().getResult().equals("success")) {
                    //do nothing
                } else {
                    // Toast.makeText(PickerActivity.this, "Tesseract Failed", Toast.LENGTH_SHORT).show();
                }


                /**
                 * Update Views
                 */
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                t.printStackTrace();
                //  progressDialog.dismiss();
                // Toast.makeText(PickerActivity.this, "Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
