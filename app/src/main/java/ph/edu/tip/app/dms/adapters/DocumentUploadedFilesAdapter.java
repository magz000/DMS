package ph.edu.tip.app.dms.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import br.com.bemobi.medescope.Medescope;
import br.com.bemobi.medescope.callback.DownloadStatusCallback;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.activities.DocViewerActivity;
import ph.edu.tip.app.dms.activities.DocumentUploadedFilesActivity;
import ph.edu.tip.app.dms.activities.PhotoActivity;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentUploadedFiles;
import ph.edu.tip.app.dms.models.Result;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DocumentUploadedFilesAdapter extends RealmRecyclerViewAdapter<DocumentUploadedFiles, DocumentUploadedFilesAdapter.DocumentUploadedFilesViewHolder> {

    private Context context;
    private ApiInterface apiInterface;
    private String from;
    private DownloadCaller downloadCaller;
    private User user;

    public DocumentUploadedFilesAdapter(Context context, OrderedRealmCollection<DocumentUploadedFiles> documentUploadedFiles, String from, User user) {
        super(context, documentUploadedFiles, true);
        this.context = context;
        this.from = from;
        this.user = user;
        try {
            this.downloadCaller = ((DownloadCaller) context);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }


    @Override
    public DocumentUploadedFilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_file_uploaded, parent, false);


        return new DocumentUploadedFilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DocumentUploadedFilesViewHolder fileViewHolder, final int position) {
        final DocumentUploadedFiles obj = getData().get(position);
        final String document_id = obj.documentId;
        String[] choices = new String[0];


        fileViewHolder.fileName.setText(obj.getFilename());
        fileViewHolder.fileDate.setText(obj.getLogs());

        if (obj.getUploadedFilename().contains(".pdf") || obj.getUploadedFilename().contains(".PDF")) {
            Glide.with(context).load(R.drawable.ic_pdf).asBitmap()
                    .skipMemoryCache(true).into(fileViewHolder.imageView);
        } else if (obj.getUploadedFilename().contains(".docx") || obj.getUploadedFilename().contains(".doc")) {
            Glide.with(context).load(R.drawable.ic_doc).asBitmap()
                    .skipMemoryCache(true).into(fileViewHolder.imageView);

        } else if (obj.getUploadedFilename().contains(".ppt")) {
            Glide.with(context).load(R.drawable.ic_ppt).asBitmap()
                    .skipMemoryCache(true).into(fileViewHolder.imageView);
        } else if (obj.getUploadedFilename().contains(".xls")) {
            Glide.with(context).load(R.drawable.ic_xls).asBitmap()
                    .skipMemoryCache(true).into(fileViewHolder.imageView);
        } else if (obj.getUploadedFilename().contains(".txt")) {
            Glide.with(context).load(R.drawable.ic_txt).asBitmap()
                    .skipMemoryCache(true).into(fileViewHolder.imageView);
        } else if (obj.getUploadedFilename().contains(".jpg") || obj.getUploadedFilename().contains(".png")) {
            Glide.with(context).load(Constants.IMAGES_URL + obj.getUploadedFilename().trim())
                    .crossFade()
                    .placeholder(R.drawable.ic_picture_o)
                    .centerCrop()
                    .error(R.drawable.ic_picture_o)
                    .skipMemoryCache(true)
                    .into(fileViewHolder.imageView);


        } else {
            Glide.with(context).load(R.drawable.ic_picture_o).asBitmap()
                    .placeholder(R.drawable.ic_picture_o)
                    .skipMemoryCache(true).into(fileViewHolder.imageView);
        }
        if (from.equals(Constants.FROM_SHARED)) {
            choices = new String[]{"Download", "Cancel"};
        } else {
            if (obj.getEncodedBy().trim().equals(user.getUserId().trim())){
                choices = new String[]{"Download", "Delete", "Cancel"};
            }else {
                choices = new String[]{"Download", "Cancel"};
            }
        }
        final String[] finalChoices = choices;
        fileViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                                                           @Override
                                                           public void onClick(View view) {
                                                               if (obj.getUploadedFilename().contains(".jpg") || obj.getUploadedFilename().contains(".png")) {
                                                                   Intent i = new Intent(context, PhotoActivity.class);
                                                                   i.putExtra(Constants.DOCUMENT_FILE_ID, obj.documentFileId);
                                                                   i.putExtra(Constants.DOCUMENT_FILE_NAME, position + 1 + ". " + obj.getFilename());
                                                                   if (from.equals(Constants.FROM_SEARCH)) {
                                                                       i.putExtra(Constants.FROM_KEY,Constants.FROM_SEARCH);
                                                                   }else if(from.equals(Constants.FROM_SHARED)){
                                                                       i.putExtra(Constants.FROM_KEY,Constants.FROM_SHARED);
                                                                   }else {
                                                                       i.putExtra(Constants.FROM_KEY,Constants.FROM_NORMAL);
                                                                   }
                                                                   context.startActivity(i);
                                                               } else {
                                                                   Intent i = new Intent(context, DocViewerActivity.class);
                                                                   i.putExtra(Constants.DOCUMENT_FILE_ID, obj.documentFileId);
                                                                   i.putExtra(Constants.DOCUMENT_FILE_NAME, position + 1 + ". " + obj.getFilename());
                                                                   if (from.equals(Constants.FROM_SEARCH)) {
                                                                       i.putExtra(Constants.FROM_KEY,Constants.FROM_SEARCH);
                                                                   }else if(from.equals(Constants.FROM_SHARED)){
                                                                       i.putExtra(Constants.FROM_KEY,Constants.FROM_SHARED);
                                                                   }else {
                                                                       i.putExtra(Constants.FROM_KEY,Constants.FROM_NORMAL);
                                                                   }
                                                                   context.startActivity(i);
                                                               }
                                                           }
                                                       }

        );
        fileViewHolder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(context)
                        .title(obj.getFilename())
                        .items((CharSequence[]) finalChoices)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                                           @Override
                                           public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                               if (text.equals("Delete")) {
                                                   new MaterialDialog.Builder(context)
                                                           .title("Are you sure you want to delete\n" + obj.getFilename() + " ?")
                                                           .positiveText("Yes")
                                                           .negativeText("No")
                                                           .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                               @Override
                                                               public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                   apiInterface = App.getApiInterface();
                                                                   Realm realm = Realm.getDefaultInstance();
                                                                   User user = realm.where(User.class).findFirst();
                                                                   String u = user.getU();
                                                                   String s = user.getS();
                                                                   realm.close();
                                                                   Call<Result> call = apiInterface.deleteFile(u,s, ParameterEncryption.deleteFile(obj.documentFileId,user.getUserId()));
                                                                   call.enqueue(new Callback<Result>() {
                                                                       @Override
                                                                       public void onResponse(Call<Result> call, final Response<Result> response) {

                                                                           if (response.isSuccessful()) {
                                                                               if (response.body().getResult().equals("success")) {
                                                                                   Realm realm = Realm.getDefaultInstance();
                                                                                   realm.executeTransaction(new Realm.Transaction() {
                                                                                       @Override
                                                                                       public void execute(Realm realm) {
                                                                                           Toast.makeText(context, obj.getFilename() + " deleted", Toast.LENGTH_SHORT).show();
                                                                                           obj.deleteFromRealm();
                                                                                       }
                                                                                   });
                                                                                   realm.close();
                                                                               } else {
                                                                                   Toast.makeText(context, R.string.oops, Toast.LENGTH_SHORT).show();
                                                                               }
                                                                           }
                                                                       }

                                                                       @Override
                                                                       public void onFailure(Call<Result> call, Throwable t) {
                                                                           Toast.makeText(context, R.string.oops, Toast.LENGTH_SHORT).show();
                                                                       }
                                                                   });

                                                               }
                                                           })
                                                           .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                               @Override
                                                               public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                   dialog.dismiss();
                                                               }
                                                           })
                                                           .show();
                                               } else if (text.equals("Open in PhotoViewer")) {
                                                   Intent i = new Intent(context, PhotoActivity.class);
                                                   i.putExtra(Constants.DOCUMENT_FILE_ID, obj.documentFileId);
                                                   i.putExtra(Constants.DOCUMENT_FILE_NAME, position + 1 + ". " + obj.getFilename());
                                                   if (from.equals(Constants.FROM_SEARCH)) {
                                                       i.putExtra(Constants.IS_FROM_SEARCH, true);
                                                   }
                                                   context.startActivity(i);
                                               } else if (text.equals("Download")) {
                                                   downloadCaller.CallDownload(obj);
                                                   Activity activity = (Activity) context;
                                                   Toast.makeText(context, "Downloading " + obj.getFilename() + " ...", Toast.LENGTH_SHORT).show();
                                                   Medescope.getInstance(context).subscribeStatus(activity, "" + obj.getDocumentFileId(), new DownloadStatusCallback() {
                                                       @Override
                                                       public void onDownloadNotEnqueued(String downloadId) {
                                                           Log.d("DMS Download", "ACTION NOT ENQUEUED");
                                                       }

                                                       @Override
                                                       public void onDownloadPaused(String downloadId, int reason) {
                                                           Log.d("DMS Download", "ACTION PAUSED");
                                                       }

                                                       @Override
                                                       public void onDownloadInProgress(String downloadId, int progress) {
                                                           Log.d("DMS Download", "ACTION PROGRESS");
                                                       }

                                                       @Override
                                                       public void onDownloadOnFinishedWithError(String downloadId, int reason, String data) {
                                                           Log.d("DMS Download", "ACTION ERROR");
                                                           Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
                                                       }

                                                       @Override
                                                       public void onDownloadOnFinishedWithSuccess(String downloadId, String filePath, String data) {
                                                           Log.d("DMS Download", "SUCCESS" + filePath);
                                                           downloadCaller.checkFilePath(filePath);
                                                       }

                                                       @Override
                                                       public void onDownloadCancelled(String downloadId) {
                                                           Log.d("DMS Download", "CANCEL");
                                                       }
                                                   });

                                               } else if (text.equals("Open in DocViewer")) {
                                                   Intent i = new Intent(context, DocViewerActivity.class);
                                                   i.putExtra(Constants.DOCUMENT_FILE_ID, obj.documentFileId);
                                                   i.putExtra(Constants.DOCUMENT_FILE_NAME, position + 1 + ". " + obj.getFilename());
                                                   if (from.equals(Constants.FROM_SEARCH)) {
                                                       i.putExtra(Constants.IS_FROM_SEARCH, true);
                                                   }
                                                   context.startActivity(i);
                                               } else if (text.equals("Open Parent Document"))

                                               {
                                                   Intent i = new Intent(context, DocumentUploadedFilesActivity.class);
                                                   i.putExtra(Constants.DOCUMENT_ID, document_id);
                                                   i.putExtra(Constants.IS_FROM_SEARCH, true);
                                                   context.startActivity(i);
                                               } else

                                               {
                                                   dialog.dismiss();
                                               }
                                           }
                                       }

                        ).

                        show();
            }
        });

    }

    public static interface DownloadCaller {
        void CallDownload(DocumentUploadedFiles documentUploadedFiles);

        void checkFilePath(String filePath);
    }


    class DocumentUploadedFilesViewHolder extends RecyclerView.ViewHolder {

        TextView fileName, fileDate;
        ImageView imageView, more;
        RelativeLayout linearLayout;

        DocumentUploadedFilesViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.tv_file_name);
            fileDate = (TextView) itemView.findViewById(R.id.tv_file_date);
            imageView = (ImageView) itemView.findViewById(R.id.imageView_fileType);
            linearLayout = (RelativeLayout) itemView.findViewById(R.id.linearLayout);
            more = (ImageView) itemView.findViewById(R.id.more);

        }
    }
}
