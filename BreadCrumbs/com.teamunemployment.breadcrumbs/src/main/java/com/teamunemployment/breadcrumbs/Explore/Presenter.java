package com.teamunemployment.breadcrumbs.Explore;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.teamunemployment.breadcrumbs.Explore.Data.ExploreLocalRepository;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreRemoteRepository;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;
import com.teamunemployment.breadcrumbs.RESTApi.UserService;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Josiah Kendall
 */
public class Presenter {

    private Model model;

    private Context context;
    private ViewContract viewContract;
    private ArrayList<ExploreCardModel> dataArray = new ArrayList<>();
    private ExploreRecyclerViewAdapter adapter;

    public Presenter(Context context, ViewContract view) {
        Retrofit retrofit = new Retrofit.Builder()
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .build();

        UserService userService = retrofit.create(UserService.class);
        NodeService nodeService = retrofit.create(NodeService.class);
        DatabaseController databaseController = new DatabaseController(context);
        ExploreRemoteRepository remoteRepository = new ExploreRemoteRepository(userService, nodeService);

        // Do we need this??
        ExploreLocalRepository localRepository = new ExploreLocalRepository(databaseController);
        model = new Model(localRepository, remoteRepository);
        this.context = context;
        this.viewContract = view;
    }

    public void Start(final long userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start(userId);
            }
        }).start();
    }

    private void start(final long userId) {
        dataArray = model.LoadModels(userId);
        adapter = new ExploreRecyclerViewAdapter(dataArray, model, context);
        viewContract.SetRecyclerViewAdapter(adapter);
    }


}
