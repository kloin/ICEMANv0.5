package com.teamunemployment.breadcrumbs.Explore;

import android.content.Context;

import com.teamunemployment.breadcrumbs.Explore.Data.ExploreLocalRepository;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreRemoteRepository;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;
import com.teamunemployment.breadcrumbs.RESTApi.UserService;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Josiah Kendall
 *
 * The presenter class for the "Explore" or "Feed" page
 */
public class Presenter {

    private Model model;
    private Context context;
    private ViewContract viewContract;
    private ArrayList<ExploreCardModel> dataArray = new ArrayList<>();
    private ExploreRecyclerViewAdapter adapter;

    public Presenter(Context context, ViewContract view) {

        // Dependencies needed.
        this.context = context;
        this.viewContract = view;

        // Create our api interface.
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .build();

        // Build dependencies for our model
        UserService userService = retrofit.create(UserService.class);
        NodeService nodeService = retrofit.create(NodeService.class);
        DatabaseController databaseController = new DatabaseController(context);
        ExploreRemoteRepository remoteRepository = new ExploreRemoteRepository(userService, nodeService);
        ExploreLocalRepository localRepository = new ExploreLocalRepository(databaseController);

        // Construct our model
        model = new Model(localRepository, remoteRepository);
    }

    // Public access to private thread.
    public void Start(final long userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start(userId);
            }
        }).start();
    }

    // Start loading data for the explore page.
    private void start(final long userId) {

        // Fetch the ids for all the data that we show on the recyclerview.
        dataArray = model.LoadIdsForAllTheAlbumsWeWantToDisplay(userId);

        // Build an adapter with the data that we recieved.
        adapter = new ExploreRecyclerViewAdapter(dataArray, model, context);

        // Set our adapter using the contract on the view.
        viewContract.SetRecyclerViewAdapter(adapter);
    }


}
