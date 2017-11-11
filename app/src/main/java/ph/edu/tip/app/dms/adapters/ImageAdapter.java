package ph.edu.tip.app.dms.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.models.Files;
import ph.edu.tip.app.dms.utils.DateHelper;
import ph.edu.tip.app.dms.utils.FileSizeHelper;

/**
 * Created by droidNinja on 29/07/16.
 */
public class ImageAdapter extends RealmRecyclerViewAdapter<Files, ImageAdapter.FileViewHolder> {

    private int imageSize;
    private Context context;

    public ImageAdapter(Context context, OrderedRealmCollection<Files> files) {
        super(context, files, true);
        this.context = context;
    }


    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_layout, parent, false);
        FileViewHolder fileViewHolder = new FileViewHolder(itemView);

        return fileViewHolder;
    }

    @Override
    public void onBindViewHolder(FileViewHolder fileViewHolder, int position) {
        final Files obj = getData().get(position);
        Glide.with(context).load(Uri.parse(obj.getFileUri())).into(fileViewHolder.imageView);
        fileViewHolder.fileName.setText(obj.getFileName());
        fileViewHolder.fileSize.setText(FileSizeHelper.readableFileSize(obj.getFileSize()));
        fileViewHolder.fileDate.setText(DateHelper.TO_AM_PM_DATE(obj.getFileDate()));
        fileViewHolder.rView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new MaterialDialog.Builder(context)
                        .title(obj.getFileName())
                        .items(R.array.choices)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which == 0) {
                                    new MaterialDialog.Builder(context)
                                            .title("Are you sure you want to delete\n" + obj.getFileName() +" ?")
                                            .positiveText("Yes")
                                            .negativeText("No")
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    Realm realm = Realm.getDefaultInstance();
                                                    realm.executeTransaction(new Realm.Transaction() {
                                                        @Override
                                                        public void execute(Realm realm) {
                                                            obj.deleteFromRealm();
                                                        }
                                                    });
                                                    realm.close();
                                                }
                                            })
                                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        })
                        .show();
                return false;
            }
        });





    }


    class FileViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView fileName, fileDate, fileSize;
        RelativeLayout rView;

        FileViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.iv_photo);
            fileName = (TextView) itemView.findViewById(R.id.tv_file_name);
            fileDate = (TextView) itemView.findViewById(R.id.tv_file_date);
            fileSize = (TextView) itemView.findViewById(R.id.tv_file_size);
            rView = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);

        }
    }
}
