package com.teamunemployment.breadcrumbs.AsyncWorkers;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by jek40 on 6/06/2016.
 */
public class AsyncFetchCrumbList extends AsyncTask<Integer, Void, JSONArray> {

    private IGenericAsync iGenericAsync;

    // Override me
    public interface IGenericAsync {
        JSONArray backgroundTasks();
        void postExecute(JSONArray jsonObject);
    }

    public AsyncFetchCrumbList(IGenericAsync iGenericAsync) {
        this.iGenericAsync = iGenericAsync;
    }

    @Override
    protected JSONArray doInBackground(Integer... params) {
        return iGenericAsync.backgroundTasks();
    }

    @Override
    protected void onPostExecute(JSONArray jsonObject) {
        iGenericAsync.postExecute(jsonObject);
    }
}