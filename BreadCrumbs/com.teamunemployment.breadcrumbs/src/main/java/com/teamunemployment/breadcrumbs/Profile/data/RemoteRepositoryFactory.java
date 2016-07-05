package com.teamunemployment.breadcrumbs.Profile.data;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jek40 on 3/07/2016.
 */
public class RemoteRepositoryFactory {

    public static RemoteProfileRepository GetRemoteProfileRepository() {
        // Build our retrofit client.
        Retrofit retrofit = new Retrofit.Builder()
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                 .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .build();
        NodeService nodeService = retrofit.create(NodeService.class);

        // Build our repo.
        RemoteProfileRepository remoteProfileRepository = new RemoteProfileRepository(nodeService);
        return remoteProfileRepository;
    }
}
