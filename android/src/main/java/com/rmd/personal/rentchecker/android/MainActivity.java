package com.rmd.personal.rentchecker.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import com.rmd.personal.rentchecker.common.Connector;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class MainActivity extends Activity {

    private static final String USERNAME = "replaceUsername";
    private static final String PASSWORD = "replacePassword";

    private ProgressDialog progressDialog;

    private ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    private TextView textView;


    private TextView getTextView() {
        return textView;
    }

    private void setTextView(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.setTextView((TextView) this.findViewById(R.id.name));
        this.progressDialog = ProgressDialog.show(MainActivity.this, "Getting rent due... ", "");
        new FetchRecordsAsyncTask(USERNAME, PASSWORD).execute();
    }

    private class FetchRecordsAsyncTask extends AsyncTask<String, String, Float> {

        private String username;
        private String password;

        public FetchRecordsAsyncTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Float doInBackground(String... params) {

            Connector connector = new Connector();

            this.publishProgress("Creating initial HTTP entity... ");
            HttpEntity<?> entity = connector.createInitialHttpEntity(username, password);

            this.publishProgress("Performing login... ");
            ResponseEntity<String> pageEntity = connector.performLogin(entity);

            this.publishProgress("Adding login cookie and 'referer' to HTTP entity... ");
            HttpHeaders headers = connector.addRefererToCopyOfHttpHeaders(entity.getHeaders());
            entity = connector.addCookiesToPageEntityWithCopyOfHttpHeaders(pageEntity, headers);

            this.publishProgress("Getting homepage... ");
            pageEntity = connector.getHomepage(entity);

            this.publishProgress("Scanning html for rent... ");
            return connector.scrapePageEntityForRent(pageEntity);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            getProgressDialog().setMessage(progress[0]);
        }

        @Override
        protected void onPostExecute(Float result) {
            getProgressDialog().dismiss();
            final String amountOwed = "$" + result;
            getTextView().setText(amountOwed);
        }
    }
}
