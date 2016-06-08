package com.teamunemployment.breadcrumbs.BackgroundServices;

import android.app.IntentService;
import android.content.Intent;

import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;

/**
 * Created by jek40 on 31/05/2016.
 */
public class UploadTrailService  extends IntentService {

    public UploadTrailService() {
        super("UploadTrailService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UploadTrailService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Do save here.
        PreferencesAPI preferencesAPI = new PreferencesAPI(this);
        int serverTrailId = preferencesAPI.GetServerTrailId();
        TrailManagerWorker worker = new TrailManagerWorker(this);
        worker.SaveEntireTrail(Integer.toString(serverTrailId));
    }
}
