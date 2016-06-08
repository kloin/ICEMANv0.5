package com.teamunemployment.breadcrumbs.AsyncWorkers;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.json.JSONObject;

/**
 * Generic async that was written to fetch and process JSON from the server. This class is very
 * flexible however, your background tasks can do anything as long as they return a JSONObject.
 * The Post execute method recieves the processed JSONObject.
 */
public class GenericAsyncWorker extends AsyncTask<Integer, Void, JSONObject> {

    private IGenericAsync iGenericAsync;

    // Override me
    public interface IGenericAsync {
        JSONObject backgroundTasks();
        void postExecute(JSONObject jsonObject);
    }

    public GenericAsyncWorker(IGenericAsync iGenericAsync) {
        this.iGenericAsync = iGenericAsync;
    }

    @Override
    protected JSONObject doInBackground(Integer... params) {
        return iGenericAsync.backgroundTasks();
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        iGenericAsync.postExecute(jsonObject);
    }
}
