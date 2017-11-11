package ph.edu.tip.app.dms.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.models.DocumentUploadedFiles;
import ph.edu.tip.app.dms.utils.Constants;
import ph.edu.tip.app.dms.utils.TouchImageView;


public class PhotoAdapter extends RealmRecyclerViewAdapter<DocumentUploadedFiles, PhotoAdapter.PhotoViewHolder> {

    private Context context;
    private TouchImageView mImageView;
    private String string;
    private TextView numberIndicator;

    public PhotoAdapter(Context context, OrderedRealmCollection<DocumentUploadedFiles> photoFile,TouchImageView mImageView,TextView numberIndicator) {
        super(context, photoFile, true);
        this.context = context;
        this.mImageView = mImageView;
        this.numberIndicator = numberIndicator;
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_photo, parent, false);
        PhotoViewHolder photoViewHolder = new PhotoViewHolder(itemView);


        return photoViewHolder;
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder photoViewHolder, final int position) {
        final DocumentUploadedFiles obj = getData().get(position);

        Glide.with(context).load(Constants.IMAGES_URL+obj.getUploadedFilename().trim())
                .crossFade()
                .centerCrop()
                .error(R.drawable.ic_picture_o)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(photoViewHolder.imageView);

        photoViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(context).load(Constants.IMAGES_URL+obj.getUploadedFilename().trim())
                        .crossFade()
                        .error(R.drawable.ic_picture_o)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mImageView);

                numberIndicator.setText(position+1+". "+obj.getFilename());

            }
        });

        photoViewHolder.number.setText(position+1+"");






    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        LinearLayout indicator;
        TextView number;
        PhotoViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        //  indicator = (LinearLayout) itemView.findViewById(R.id.indicator);
            number = (TextView)itemView.findViewById(R.id.number);

        }
    }
}
