package ph.edu.tip.app.dms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import br.com.bemobi.medescope.Medescope;
import br.com.bemobi.medescope.constant.DownloadConstants;
import br.com.bemobi.medescope.receiver.BroadcastReceiverLogger;
import br.com.goncalves.pugnotification.notification.PugNotification;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.models.NotificationData;

import static br.com.bemobi.medescope.constant.DownloadConstants.LOG_FEATURE_DOWNLOAD;

/**
 * Created by bkosawa on 02/07/15.
 */
public class DownloadReceiver extends BroadcastReceiver {

    private static final String TAG = DownloadReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        new BroadcastReceiverLogger(TAG, LOG_FEATURE_DOWNLOAD).onReceive(context, intent);

        String action = intent.getAction();
        if (Medescope.ACTION_BROADCAST_FINISH_WITH_SUCCESS.equals(action)) {
            String downloadId = intent.getStringExtra(DownloadConstants.EXTRA_STRING_DOWNLOAD_ID);
            String filePath = intent.getStringExtra(DownloadConstants.EXTRA_STRING_FILE_PATH);

            if (!TextUtils.isEmpty(downloadId)) {
                String data = intent.getStringExtra(DownloadConstants.EXTRA_STRING_JSON_DATA);
                NotificationData nData = new Gson().fromJson(data, NotificationData.class);

                PugNotification.with(context).load()
                        .title("DMS: Download Successful")
                        .message(nData.getDesc())
                        .bigTextStyle(nData.getDesc())
                        .smallIcon(R.mipmap.ic_launcher)
                        .largeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .autoCancel(true)
                        .simple()
                        .build();


                Log.d(TAG, "File Path: " + filePath);
            }
        } else if (Medescope.ACTION_BROADCAST_FINISH_WITH_ERROR.equals(action)) {
            String downloadId = intent.getStringExtra(DownloadConstants.EXTRA_STRING_DOWNLOAD_ID);
            int errorMsg = intent.getIntExtra(DownloadConstants.EXTRA_INT_ERROR_REASON, -1);

            if (!TextUtils.isEmpty(downloadId)) {
                String data = intent.getStringExtra(DownloadConstants.EXTRA_STRING_JSON_DATA);
                NotificationData nData = new Gson().fromJson(data, NotificationData.class);

                PugNotification.with(context).load()
                        .title(nData.getTitle())
                        .message(errorMsg + " - " + nData.getDesc())
                        .bigTextStyle(nData.getDesc())
                        .smallIcon(R.mipmap.ic_launcher)
                        .largeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .autoCancel(true)
                        .simple()
                        .build();
            }
        }
    }
}
