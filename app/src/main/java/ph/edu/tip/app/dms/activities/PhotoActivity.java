package ph.edu.tip.app.dms.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.adapters.PhotoAdapter;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentSearchedFiles;
import ph.edu.tip.app.dms.models.DocumentShared;
import ph.edu.tip.app.dms.models.DocumentUploaded;
import ph.edu.tip.app.dms.models.DocumentUploadedFiles;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import ph.edu.tip.app.dms.utils.TouchImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoActivity extends AppCompatActivity {

    private Realm realm;
    private List<DocumentUploadedFiles> Photos;
    private RealmQuery<DocumentUploadedFiles> documentUploadedFilesRealmQuery;
    private TouchImageView imageView;
    private DocumentUploadedFiles documentUploadedFiles;
    private RealmResults<DocumentUploadedFiles> documentUploadedFilesRealmResults;
    private TextView numberIndicator, docDesc;
    private ImageView back;
    private ApiInterface apiInterface;
    private DocumentUploaded documentUploaded;
    private String document_desc;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        realm = Realm.getDefaultInstance();
        apiInterface = App.getApiInterface();
        user = realm.where(User.class).findFirst();
        Intent i = getIntent();
        String documentFileId = i.getStringExtra(Constants.DOCUMENT_FILE_ID);
        String fileName = i.getStringExtra(Constants.DOCUMENT_FILE_NAME);

        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });




        ImageView menu = (ImageView) findViewById(R.id.menu);
        imageView = (TouchImageView) findViewById(R.id.imageView);

        docDesc = (TextView) findViewById(R.id.docDesc);
     //   DocumentUploaded documentUploadedTemp = realm.where(DocumentUploaded.class).equalTo("documentId", document_id).findFirst();
        String from = i.getStringExtra(Constants.FROM_KEY);
        if (from.equals(Constants.FROM_SEARCH)) {
            DocumentSearchedFiles documentSearchedFiles = realm.where(DocumentSearchedFiles.class).equalTo("documentFileId", documentFileId).findFirst();
            final String document_id = documentSearchedFiles.getDocumentId();
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(Constants.IMAGES_URL +documentSearchedFiles.getUploadedFilename().trim())
                    .asBitmap()
                    .skipMemoryCache(true)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            progressBar.setVisibility(View.GONE);
                            imageView.setImageBitmap(resource);
                        }
                    });
            getDocumentInfo(document_id);
            menu.setVisibility(View.VISIBLE);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialDialog.Builder(PhotoActivity.this)
                            .items("Open Parent Document", "Cancel")
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    if (which == 0) {
                                        Intent i = new Intent(PhotoActivity.this, DocumentUploadedFilesActivity.class);
                                        i.putExtra(Constants.DOCUMENT_ID, document_id);
                                        i.putExtra(Constants.IS_FROM_SEARCH, true);
                                        startActivity(i);
                                    } else {
                                        dialog.dismiss();
                                    }
                                }
                            })
                            .show();
                }
            });
        }else if(from.equals(Constants.FROM_SHARED)){
            DocumentUploadedFiles documentUploaded = realm.where(DocumentUploadedFiles.class).equalTo("documentFileId", documentFileId).findFirst();
            menu.setVisibility(View.GONE);

            documentUploadedFiles = realm.where(DocumentUploadedFiles.class).equalTo("documentFileId", documentFileId).findFirst();

            String document_id = documentUploaded.getDocumentId();

            documentUploadedFilesRealmQuery = realm.where(DocumentUploadedFiles.class)
                    .contains("uploadedFilename", "jpg")
                    .equalTo("documentId", documentUploadedFiles.getDocumentId())
                    .or()
                    .contains("uploadedFilename", "png")
                    .equalTo("documentId", documentUploadedFiles.getDocumentId());

            documentUploadedFilesRealmResults = documentUploadedFilesRealmQuery.findAll().sort("filename", Sort.ASCENDING);

            DocumentShared documentUploadedTemp = realm.where(DocumentShared.class).equalTo("documentId", document_id).findFirst();
            Photos = documentUploadedFilesRealmResults;
            numberIndicator = (TextView) findViewById(R.id.number);

            numberIndicator.setText(fileName);

            docDesc.setText(documentUploadedTemp.getDocumentDescription());
            //document_id = documentUploadedFiles.getDocumentId();
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(Constants.IMAGES_URL + documentUploadedFiles.getUploadedFilename().trim())
                    .asBitmap()
                    .skipMemoryCache(true)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            progressBar.setVisibility(View.GONE);
                            imageView.setImageBitmap(resource);
                        }
                    });


            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setAdapter(new PhotoAdapter(this, documentUploadedFilesRealmResults, imageView, numberIndicator));
        }else {
            DocumentUploadedFiles documentUploaded = realm.where(DocumentUploadedFiles.class).equalTo("documentFileId", documentFileId).findFirst();
            menu.setVisibility(View.GONE);

            documentUploadedFiles = realm.where(DocumentUploadedFiles.class).equalTo("documentFileId", documentFileId).findFirst();

            String document_id = documentUploaded.getDocumentId();

            documentUploadedFilesRealmQuery = realm.where(DocumentUploadedFiles.class)
                    .contains("uploadedFilename", "jpg")
                    .equalTo("documentId", documentUploadedFiles.getDocumentId())
                    .or()
                    .contains("uploadedFilename", "png")
                    .equalTo("documentId", documentUploadedFiles.getDocumentId());

            documentUploadedFilesRealmResults = documentUploadedFilesRealmQuery.findAll().sort("filename", Sort.ASCENDING);

            DocumentUploaded documentUploadedTemp = realm.where(DocumentUploaded.class).equalTo("documentId", document_id).findFirst();
            Photos = documentUploadedFilesRealmResults;
            numberIndicator = (TextView) findViewById(R.id.number);

            numberIndicator.setText(fileName);

            docDesc.setText(documentUploadedTemp.getDocumentDescription());
            //document_id = documentUploadedFiles.getDocumentId();
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(Constants.IMAGES_URL + documentUploadedFiles.getUploadedFilename().trim())
                    .asBitmap()
                    .skipMemoryCache(true)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            progressBar.setVisibility(View.GONE);
                            imageView.setImageBitmap(resource);
                        }
                    });


            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setAdapter(new PhotoAdapter(this, documentUploadedFilesRealmResults, imageView, numberIndicator));
        }




    }

    private void getDocumentInfo(final String document_id) {
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
                            docDesc.setText(document_desc);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
