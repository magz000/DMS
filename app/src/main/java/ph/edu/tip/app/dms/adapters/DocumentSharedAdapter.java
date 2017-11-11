package ph.edu.tip.app.dms.adapters;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import me.gujun.android.taggroup.TagGroup;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.activities.DocumentUploadedFilesActivity;
import ph.edu.tip.app.dms.activities.LoginActivity;
import ph.edu.tip.app.dms.activities.MainActivity;
import ph.edu.tip.app.dms.activities.SharedDocumentFilesActivity;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentShared;
import ph.edu.tip.app.dms.models.Employee;
import ph.edu.tip.app.dms.models.Result;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.models.UserSharedWith;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DocumentSharedAdapter extends RealmRecyclerViewAdapter<DocumentShared, DocumentSharedAdapter.DocumentSharedViewHolder> {

    private Context context;
    private ApiInterface apiInterface;
    private String breadcrumb_title;
    private List<String> tagList = null;

    public DocumentSharedAdapter(Context context, OrderedRealmCollection<DocumentShared> documentShared, String breadcrumb_title) {
        super(context, documentShared, true);
        this.context = context;
        this.breadcrumb_title = breadcrumb_title;
    }


    @Override
    public DocumentSharedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_doc_shared, parent, false);
        DocumentSharedViewHolder documentSharedViewHolder = new DocumentSharedViewHolder(itemView);


        return documentSharedViewHolder;
    }

    @Override
    public void onBindViewHolder(DocumentSharedViewHolder documentSharedViewHolder, int position) {


        final DocumentShared obj = getData().get(position);
        final String tags = obj.getTags().trim();
        final String document_id = obj.getDocumentId();
        final String document_desc = obj.getDocumentDescription();
        final ApiInterface apiInterface = App.getApiInterface();


        try {
            tagList = Arrays.asList(tags.split(","));
            if (tagList.size() > 0) {
                documentSharedViewHolder.tagGroup.setTags(tagList);
            } else {
                tagList = Arrays.asList("No tags yet");
                documentSharedViewHolder.tagGroup.setTags(tagList);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            tagList = Arrays.asList("No tags yet");
            documentSharedViewHolder.tagGroup.setTags(tagList);
        }

        try {
            int doc_count = Integer.parseInt(obj.getFileCount());
            if (doc_count > 0) {
                documentSharedViewHolder.icon.setImageResource(R.drawable.ic_file_text);
                if(doc_count == 1){
                    documentSharedViewHolder.fileCount.setText(obj.getFileCount() + " file");
                }else {
                    documentSharedViewHolder.fileCount.setText(obj.getFileCount() + " files");
                }
            } else {
                documentSharedViewHolder.icon.setImageResource(R.drawable.ic_file_text_o);
                documentSharedViewHolder.fileCount.setText("No files");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            documentSharedViewHolder.icon.setImageResource(R.drawable.ic_file_text_o);
            documentSharedViewHolder.fileCount.setText("No files");
        }

        documentSharedViewHolder.tagGroup.setClickable(false);
        documentSharedViewHolder.fileDate.setText(obj.getLastModified());
        documentSharedViewHolder.fileName.setText(obj.getDocumentDescription());
        documentSharedViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, SharedDocumentFilesActivity.class);
                i.putExtra(Constants.DOCUMENT_ID, document_id);
                i.putExtra(Constants.DOCUMENT_NAME, document_desc);
                i.putExtra(Constants.BREADCRUMB_TITLE, breadcrumb_title);
                context.startActivity(i);
            }
        });
        if (breadcrumb_title.equals("My Shared Docs")) {
            documentSharedViewHolder.sharedBy.setVisibility(View.GONE);
            documentSharedViewHolder.more.setVisibility(View.VISIBLE);
            documentSharedViewHolder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialDialog.Builder(context)
                            .title(obj.getDocumentDescription())
                            .items("Unshare", "Cancel")
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(final MaterialDialog dialog, View view, int which, CharSequence text) {
                                    if (which == 0) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle("Unshare Document");
                                        builder.setMessage("Are you sure?");
                                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                            public void onClick(final DialogInterface dialog, int which) {
                                                // Do nothing but close the dialog/*
                                               /* final Realm realm = Realm.getDefaultInstance();
                                                final User user = realm.copyFromRealm(realm.where(User.class).findFirst());
                                                realm.close();*/
                                                final ProgressDialog progressDialog = new ProgressDialog(context);
                                                progressDialog.setMessage("Unsharing document...");
                                                progressDialog.show();
                                                String u ="", s="";
                                                Realm realm = Realm.getDefaultInstance();
                                                User user = realm.where(User.class).findFirst();
                                                u = user.getU();
                                                s = user.getS();
                                                realm.close();
                                                apiInterface.unshareDocument(u,s, ParameterEncryption.unshareDocument(document_id, obj.getUserIdSharedBy())).enqueue(new Callback<Result>() {
                                                    @Override
                                                    public void onResponse(Call<Result> call, Response<Result> response) {
                                                        progressDialog.dismiss();
                                                        if (response.isSuccessful()) {
                                                            if (response.body().getResult().equals(Constants.SUCCESS)) {
                                                                Toast.makeText(context, document_desc + " unshared", Toast.LENGTH_SHORT).show();
                                                                Realm realm = Realm.getDefaultInstance();
                                                                realm.executeTransaction(new Realm.Transaction() {
                                                                    @Override
                                                                    public void execute(Realm realm) {
                                                                        obj.deleteFromRealm();
                                                                    }
                                                                });
                                                                realm.close();
                                                            } else {
                                                                Toast.makeText(context, R.string.oops, Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(context, R.string.oops, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Result> call, Throwable t) {
                                                        Toast.makeText(context, R.string.oops, Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                });

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

                                }
                            })
                            .show();
                }
            });
        } else {
            documentSharedViewHolder.sharedBy.setText("Shared by: "+obj.getUserIdSharedBy());
            documentSharedViewHolder.more.setVisibility(View.INVISIBLE);
        }


    }

    class DocumentSharedViewHolder extends RecyclerView.ViewHolder {

        TextView fileName, fileDate, fileCount,sharedBy;
        RelativeLayout relativeLayout;
        TagGroup tagGroup;
        ImageView icon, more;

        DocumentSharedViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.tv_file_name);
            fileDate = (TextView) itemView.findViewById(R.id.tv_file_date);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            tagGroup = (TagGroup) itemView.findViewById(R.id.tag_group_large);
            icon = (ImageView) itemView.findViewById(R.id.iv_photo);
            fileCount = (TextView) itemView.findViewById(R.id.tv_file_count);
            more = (ImageView) itemView.findViewById(R.id.more);
            sharedBy = (TextView) itemView.findViewById(R.id.tv_shared_by);

        }
    }
}
