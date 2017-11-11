package ph.edu.tip.app.dms.adapters;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import java.util.Arrays;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import me.gujun.android.taggroup.TagGroup;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.activities.DocumentUploadedFilesActivity;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentUploaded;
import ph.edu.tip.app.dms.models.Result;
import ph.edu.tip.app.dms.models.User;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.ParameterEncryption;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DocumentUploadedAdapter extends RealmRecyclerViewAdapter<DocumentUploaded, DocumentUploadedAdapter.DocumentUploadedViewHolder> {

    private Context context;
    private ApiInterface apiInterface;
    private String breadcrumb_title;
    private List<String> tagList = null;
    private String restriction;
    private User user;
    private String [] choices = new String[]{"Download", "Cancel"};

    public DocumentUploadedAdapter(Context context, OrderedRealmCollection<DocumentUploaded> documentUploaded, String breadcrumb_title, User user) {
        super(context, documentUploaded, true);
        this.context = context;
        this.user = user;
        this.breadcrumb_title = breadcrumb_title;
    }


    @Override
    public DocumentUploadedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_doc_uploaded, parent, false);
        DocumentUploadedViewHolder templateViewHolder = new DocumentUploadedViewHolder(itemView);


        return templateViewHolder;
    }

    @Override
    public void onBindViewHolder(DocumentUploadedViewHolder fileViewHolder, int position) {


        final DocumentUploaded obj = getData().get(position);
        final String tags = obj.getTags().trim();
        final String document_id = obj.getDocumentId();
        final String document_desc = obj.getDocumentDescription();
        final String doc_rest = obj.getRestriction();


        try {
            tagList = Arrays.asList(tags.split(","));
            if (tagList.size() > 0) {
                fileViewHolder.tagGroup.setTags(tagList);
            } else {
                tagList = Arrays.asList("No tags yet");
                fileViewHolder.tagGroup.setTags(tagList);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            tagList = Arrays.asList("No tags yet");
            fileViewHolder.tagGroup.setTags(tagList);
        }

        try {
            int doc_count = Integer.parseInt(obj.getFileCount());
            if (doc_count > 0) {
                fileViewHolder.icon.setImageResource(R.drawable.ic_file_text);
                if (doc_count == 1) {
                    fileViewHolder.fileCount.setText(obj.getFileCount() + " file");
                } else {
                    fileViewHolder.fileCount.setText(obj.getFileCount() + " files");
                }
            } else {
                fileViewHolder.icon.setImageResource(R.drawable.ic_file_text_o);
                fileViewHolder.fileCount.setText("No files");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            fileViewHolder.icon.setImageResource(R.drawable.ic_file_text_o);
            fileViewHolder.fileCount.setText("No files");
        }


        fileViewHolder.tagGroup.setClickable(false);
        fileViewHolder.fileDate.setText(obj.getLastModified());
        fileViewHolder.fileName.setText(obj.getDocumentDescription());
        fileViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, DocumentUploadedFilesActivity.class);
                i.putExtra(Constants.DOCUMENT_ID, document_id);
                i.putExtra(Constants.DOCUMENT_NAME, document_desc);
                i.putExtra(Constants.BREADCRUMB_TITLE, breadcrumb_title);
                context.startActivity(i);
            }
        });

        try {
            if (obj.getEncodedBy().trim().equals(user.getUserId().trim())) {
                choices = new String[]{"Download", "Edit", "Delete", "Cancel"};
                fileViewHolder.more.setVisibility(View.VISIBLE);
            } else {
                fileViewHolder.more.setVisibility(View.INVISIBLE);
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        fileViewHolder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(context)
                        .title(obj.getDocumentDescription())
                        .items(choices)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(final MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (text.equals("Delete")) {
                                    new MaterialDialog.Builder(context)
                                            .title("Are you sure you want to delete\n" + obj.getDocumentDescription() + " ?")
                                            .positiveText("Yes")
                                            .negativeText("No")
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    Toast.makeText(context, "Deleting " + obj.getDocumentDescription() + "...", Toast.LENGTH_SHORT).show();
                                                    apiInterface = App.getApiInterface();
                                                    Realm realm = Realm.getDefaultInstance();
                                                    User user = realm.where(User.class).findFirst();
                                                    String u = user.getU();
                                                    String s = user.getS();
                                                    realm.close();
                                                    Call<Result> call = apiInterface.deleteDocument(u,s, ParameterEncryption.deleteDocument(document_id));
                                                    call.enqueue(new Callback<Result>() {
                                                        @Override
                                                        public void onResponse(Call<Result> call, final Response<Result> response) {
                                                            if (response.isSuccessful()) {
                                                                if (response.body().getResult().equals("success")) {
                                                                    Realm realm = Realm.getDefaultInstance();
                                                                    realm.executeTransaction(new Realm.Transaction() {
                                                                        @Override
                                                                        public void execute(Realm realm) {
                                                                            Toast.makeText(context, obj.getDocumentDescription() + " deleted", Toast.LENGTH_SHORT).show();
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
                                                    //     Toast.makeText(context, document_id+"FUCKK", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                } else if (text.equals("Edit")) {

                                    final Dialog dialog_document_edit = new Dialog(context);
                                    dialog_document_edit.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog_document_edit.setContentView(R.layout.dialog_document_edit);
                                    final TagGroup mTagGroup = (TagGroup) dialog_document_edit.findViewById(R.id.tagGroup);
                                    final EditText desc = (EditText) dialog_document_edit.findViewById(R.id.etDesc);
                                    final EditText rest = (EditText) dialog_document_edit.findViewById(R.id.etRest);
                                    mTagGroup.setTags(tagList);
                                    desc.setText(document_desc);
                                    String restrictionTemp = null;

                                    if (doc_rest.equals("P")) {
                                        restrictionTemp = "Public";
                                        restriction = "P";
                                    } else if (doc_rest.equals("I")) {
                                        restrictionTemp = "Internal Use";
                                        restriction = "I";
                                    } else if (doc_rest.equals("R")) {
                                        restrictionTemp = "Restricted";
                                        restriction = "R";
                                    }
                                    rest.setText(restrictionTemp);

                                    ProgressDialog progressDialog = new ProgressDialog(context);
                                    progressDialog.setIndeterminate(true);
                                    progressDialog.setMessage("Updating " + document_desc);

                                    rest.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            new MaterialDialog.Builder(context)
                                                    .title("Restriction")
                                                    .items("Public", "Internal Use", "Restricted")
                                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                                        @Override
                                                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                            rest.setText(text);
                                                            if (text.equals("Public")) {
                                                                restriction = "P";
                                                            } else if (text.equals("Internal Use")) {
                                                                restriction = "I";
                                                            } else if (text.equals("Restricted")) {
                                                                restriction = "R";
                                                            }
                                                        }
                                                    }).
                                                    show();
                                        }
                                    });
                                    rest.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        @Override
                                        public void onFocusChange(View view, boolean b) {
                                            if (b) {
                                                new MaterialDialog.Builder(context)
                                                        .title("Restriction")
                                                        .items("Public", "Internal Use", "Restricted")
                                                        .itemsCallback(new MaterialDialog.ListCallback() {
                                                            @Override
                                                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                                rest.setText(text);
                                                                if (text.equals("Public")) {
                                                                    restriction = "P";
                                                                } else if (text.equals("Internal Use")) {
                                                                    restriction = "I";
                                                                } else if (text.equals("Restricted")) {
                                                                    restriction = "R";
                                                                }
                                                            }
                                                        }).
                                                        show();
                                            }

                                        }
                                    });


                                    Button button = (Button) dialog_document_edit.findViewById(R.id.submit);
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String[] tags = mTagGroup.getTags();
                                            String joined = TextUtils.join("", tags).trim();
                                            if (desc.getText().toString().equals("") || joined.equals("") || rest.getText().toString().equals("")) {
                                                Toast.makeText(context, "Fill up all details", Toast.LENGTH_SHORT).show();
                                            } else if (joined.length() > 150) {
                                                Toast.makeText(context, "Tags must be less than 150 characters", Toast.LENGTH_SHORT).show();
                                            } else {
                                                joined = TextUtils.join(",", mTagGroup.getTags());
                                                final ApiInterface apiInterface = App.getApiInterface();
                                                final String finalJoined = joined;
                                                Realm realm = Realm.getDefaultInstance();
                                                User user = realm.where(User.class).findFirst();
                                                String u = user.getU();
                                                String s = user.getS();
                                                realm.close();
                                                apiInterface.updateDocumentUploaded(u, s,ParameterEncryption.updateDocumentUploaded(document_id, desc.getText().toString(), joined, restriction,user.getUserId())).enqueue(new Callback<Result>() {
                                                    @Override
                                                    public void onResponse(Call<Result> call, Response<Result> response) {
                                                        if (response.isSuccessful()) {
                                                            if (response.body().getResult().equals("success")) {
                                                                final Realm realm = Realm.getDefaultInstance();
                                                                realm.executeTransactionAsync(new Realm.Transaction() {
                                                                    @Override
                                                                    public void execute(Realm realm) {
                                                                        DocumentUploaded documentUploaded = realm.where(DocumentUploaded.class).equalTo("documentId", document_id).findFirst();
                                                                        documentUploaded.setDocumentDescription(desc.getText().toString());
                                                                        documentUploaded.setTags(finalJoined);
                                                                        documentUploaded.setRestriction(restriction);
                                                                        dialog_document_edit.dismiss();
                                                                    }
                                                                }, new Realm.Transaction.OnSuccess() {
                                                                    @Override
                                                                    public void onSuccess() {
                                                                        Toast.makeText(context, document_desc + " updated", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            } else {
                                                                Toast.makeText(context, R.string.oops, Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(context, R.string.oops, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<Result> call, Throwable t) {
                                                        t.printStackTrace();
                                                        Toast.makeText(context, R.string.oops, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                        }
                                    });

                                    dialog_document_edit.show();

                                } else {
                                    dialog.dismiss();
                                }
                            }
                        })
                        .show();
            }
        });

    }

    class DocumentUploadedViewHolder extends RecyclerView.ViewHolder {

        TextView fileName, fileDate, fileCount;
        RelativeLayout relativeLayout;
        TagGroup tagGroup;
        ImageView icon, more;

        DocumentUploadedViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.tv_file_name);
            fileDate = (TextView) itemView.findViewById(R.id.tv_file_date);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            tagGroup = (TagGroup) itemView.findViewById(R.id.tag_group_large);
            icon = (ImageView) itemView.findViewById(R.id.iv_photo);
            fileCount = (TextView) itemView.findViewById(R.id.tv_file_count);
            more = (ImageView) itemView.findViewById(R.id.more);

        }
    }
}
