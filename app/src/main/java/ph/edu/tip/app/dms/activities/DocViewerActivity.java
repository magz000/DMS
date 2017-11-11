package ph.edu.tip.app.dms.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import io.realm.Realm;
import ph.edu.tip.app.dms.R;
import ph.edu.tip.app.dms.models.DocumentSearchedFiles;
import ph.edu.tip.app.dms.models.DocumentUploadedFiles;
import ph.edu.tip.app.dms.utils.Constants;

public class DocViewerActivity extends AppCompatActivity {

    String url;
    private Realm realm;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_viewer);
        realm = Realm.getDefaultInstance();

        Intent i = getIntent();

        progressBar =(ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        
        /*DocumentUploadedFiles documentUploadedFiles = realm.where(DocumentUploadedFiles.class).equalTo("documentFileId", i.getStringExtra(Constants.DOCUMENT_FILE_ID)).findFirst();
        try {
            url = Constants.FILES_URL + documentUploadedFiles.getUploadedFilename();
        }catch (NullPointerException e){
            DocumentSearchedFiles documentSearchedFiles = realm.where(DocumentSearchedFiles.class).equalTo("documentFileId", i.getStringExtra(Constants.DOCUMENT_FILE_ID)).findFirst();
            url = Constants.FILES_URL + documentSearchedFiles.getUploadedFilename();
        }
*/
        String from = i.getStringExtra(Constants.FROM_KEY);
        if (from.equals(Constants.FROM_SEARCH)) {
            DocumentUploadedFiles documentUploadedFiles = realm.where(DocumentUploadedFiles.class).equalTo("documentFileId", i.getStringExtra(Constants.DOCUMENT_FILE_ID)).findFirst();
            url = Constants.FILES_URL + documentUploadedFiles.getUploadedFilename();
        }else if(from.equals(Constants.FROM_SHARED)){
            DocumentUploadedFiles documentUploadedFiles = realm.where(DocumentUploadedFiles.class).equalTo("documentFileId", i.getStringExtra(Constants.DOCUMENT_FILE_ID)).findFirst();
            url = Constants.FILES_URL + documentUploadedFiles.getUploadedFilename();
        }else {
            DocumentUploadedFiles documentUploadedFiles = realm.where(DocumentUploadedFiles.class).equalTo("documentFileId", i.getStringExtra(Constants.DOCUMENT_FILE_ID)).findFirst();
            url = Constants.FILES_URL + documentUploadedFiles.getUploadedFilename();
        }

        String doc="http://docs.google.com/viewer?url="+url.trim()+"&embedded=true";


       // String doc="<iframe src='http://docs.google.com/viewer?url=http://www.example.com/yourfile.pdf&embedded=true' width='100%' height='100%'  style='border: none;'></iframe>";


        WebView  wv = (WebView)findViewById(R.id.webView);
        wv.setWebViewClient(new WebViewClientDemo());
        wv.setVisibility(WebView.VISIBLE);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setAllowFileAccess(true);
        wv.getSettings().setPluginState(WebSettings.PluginState.ON);
        wv.loadUrl(doc);
    }

    private class WebViewClientDemo extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.toString());
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(100);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
