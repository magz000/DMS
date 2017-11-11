package ph.edu.tip.app.dms.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.activities.DocumentsActivity;
import ph.edu.tip.app.dms.models.Folders;
import ph.edu.tip.app.dms.utils.Constants;


public class FolderAdapter extends RealmRecyclerViewAdapter<Folders, FolderAdapter.FolderViewHolder> {

    private Context context;
    private String title;

    public FolderAdapter(Context context, OrderedRealmCollection<Folders> files, String title) {
        super(context, files, true);
        this.context = context;
        this.title = title;
    }


    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_folder, parent, false);
        FolderViewHolder folderViewHolder = new FolderViewHolder(itemView);

        return folderViewHolder;
    }

    @Override
    public void onBindViewHolder(FolderViewHolder folderViewHolder, int position) {
        final Folders obj = getData().get(position);

        folderViewHolder.folderName.setText(obj.getFolderName());
        folderViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, DocumentsActivity.class);
                i.putExtra(Constants.FOLDER_ID_KEY, obj.getFolderId());
                i.putExtra(Constants.FOLDER_NAME_KEY, obj.getFolderName());
                i.putExtra(Constants.TEMPLATE_NAME_KEY, title.trim());
                context.startActivity(i);
            }
        });
        try {
            int doc_count = Integer.parseInt(obj.getDocumentCount());
            if (doc_count > 0) {
                folderViewHolder.icon.setImageResource(R.drawable.ic_folder);
            } else {
                folderViewHolder.icon.setImageResource(R.drawable.ic_folder_o);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            folderViewHolder.icon.setImageResource(R.drawable.ic_folder_o);
        }


    }


    class FolderViewHolder extends RecyclerView.ViewHolder {

        TextView folderName;
        LinearLayout linearLayout;
        ImageView icon;

        FolderViewHolder(View itemView) {
            super(itemView);
            folderName = (TextView) itemView.findViewById(R.id.tv_folder_name);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            icon = (ImageView) itemView.findViewById(R.id.icon);

        }
    }


}
