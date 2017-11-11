package ph.edu.tip.app.dms.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.activities.DocViewerActivity;
import ph.edu.tip.app.dms.activities.DocumentUploadedFilesActivity;
import ph.edu.tip.app.dms.activities.PhotoActivity;
import ph.edu.tip.app.dms.app.App;
import ph.edu.tip.app.dms.interfaces.ApiInterface;
import ph.edu.tip.app.dms.models.DocumentSearchedFiles;
import ph.edu.tip.app.dms.models.DocumentUploadedFiles;
import ph.edu.tip.app.dms.models.Result;
import ph.edu.tip.app.dms.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DocumentSearchedFilesAdapter extends RealmRecyclerViewAdapter<DocumentSearchedFiles, DocumentSearchedFilesAdapter.DocumentUploadedFilesViewHolder> {

    private Context context;
    private ApiInterface apiInterface;
    private Boolean isAdvSearch;

    public DocumentSearchedFilesAdapter(Context context, OrderedRealmCollection<DocumentSearchedFiles> documentUploadedFiles, Boolean isAdvSearch) {
        super(context, documentUploadedFiles, true);
        this.context = context;
        this.isAdvSearch = isAdvSearch;
    }


    @Override
    public DocumentUploadedFilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_file_searched, parent, false);


        return new DocumentUploadedFilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DocumentUploadedFilesViewHolder fileViewHolder, final int position) {
        final DocumentSearchedFiles obj = getData().get(position);
        final String document_id = obj.getDocumentId();
        String[] choices = new String[0];


        fileViewHolder.fileName.setText(obj.getFilename());

        if (obj.getUploadedFilename().contains(".pdf") ||obj.getUploadedFilename().contains(".PDF") ) {
            Glide.with(context).load(R.drawable.ic_pdf).asBitmap().skipMemoryCache(true).into(fileViewHolder.imageView);
        } else if (obj.getUploadedFilename().contains(".docx") || obj.getUploadedFilename().contains(".doc")) {
            Glide.with(context).load(R.drawable.ic_doc).asBitmap().skipMemoryCache(true).into(fileViewHolder.imageView);
        } else if (obj.getUploadedFilename().contains(".ppt")) {
            Glide.with(context).load(R.drawable.ic_ppt).asBitmap().skipMemoryCache(true).into(fileViewHolder.imageView);
        } else if (obj.getUploadedFilename().contains(".xls")) {
            Glide.with(context).load(R.drawable.ic_xls).asBitmap().skipMemoryCache(true).into(fileViewHolder.imageView);
        } else if (obj.getUploadedFilename().contains(".txt")) {
            Glide.with(context).load(R.drawable.ic_txt).asBitmap().skipMemoryCache(true).into(fileViewHolder.imageView);
        } else if (obj.getUploadedFilename().contains(".jpg") || obj.getUploadedFilename().contains(".png")) {
            Glide.with(context).load(Constants.IMAGES_URL + obj.getUploadedFilename().trim())
                    .crossFade()
                    .centerCrop()
                    .skipMemoryCache(true)
                    .error(R.drawable.ic_picture_o)
                    .into(fileViewHolder.imageView);
            choices= new String[] { "Open in PhotoViewer","Download","Delete", "Cancel" };

        } else {
            Glide.with(context).load(R.drawable.ic_picture_o).asBitmap().into(fileViewHolder.imageView);
        }

        fileViewHolder.parent.setText("Parent Doc: "+ obj.getDocumentDescription());
        fileViewHolder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, DocumentUploadedFilesActivity.class);
                i.putExtra(Constants.DOCUMENT_ID,document_id);
                i.putExtra(Constants.DOCUMENT_NAME,obj.getDocumentDescription());
                context.startActivity(i);
            }
        });

        fileViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (obj.getUploadedFilename().contains(".jpg") || obj.getUploadedFilename().contains(".png")) {
                    Intent i = new Intent(context, PhotoActivity.class);
                    i.putExtra(Constants.DOCUMENT_FILE_ID, obj.getDocumentFileId());
                    i.putExtra(Constants.DOCUMENT_FILE_NAME, position + 1 + ". " + obj.getFilename());
                    if(isAdvSearch){
                        i.putExtra(Constants.FROM_KEY, Constants.FROM_SEARCH);
                    }
                    context.startActivity(i);
                }else{
                    Intent i = new Intent(context, DocViewerActivity.class);
                    i.putExtra(Constants.DOCUMENT_FILE_ID, obj.getDocumentFileId());
                    i.putExtra(Constants.DOCUMENT_FILE_NAME, position + 1 + ". " + obj.getFilename());
                    if(isAdvSearch){
                        i.putExtra(Constants.FROM_KEY, Constants.FROM_SEARCH);
                    }
                    context.startActivity(i);
                }
            }
        });

    }

    class DocumentUploadedFilesViewHolder extends RecyclerView.ViewHolder {

        TextView fileName, fileDate;
        ImageView imageView;
        RelativeLayout linearLayout;
        TextView parent;

        DocumentUploadedFilesViewHolder(View itemView) {
            super(itemView);
            fileName = (TextView) itemView.findViewById(R.id.tv_file_name);
            fileDate = (TextView) itemView.findViewById(R.id.tv_file_date);
            imageView = (ImageView) itemView.findViewById(R.id.imageView_fileType);
            linearLayout = (RelativeLayout) itemView.findViewById(R.id.layout);
            parent = (TextView) itemView.findViewById(R.id.parentDocument);

        }
    }
}
