package com.teamunemployment.breadcrumbs.Profile.data;

import android.util.Log;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;
import com.teamunemployment.breadcrumbs.Trails.Trip;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by jek40 on 30/06/2016.
 */
public class RemoteProfileRepository implements ProfileRepositoryContract {

    private static final String TAG = "RemoteRepo_Profile";
    private NodeService nodeService;
    public RemoteProfileRepository(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    public String getUserName(long userId) {
        if (userId == -1) {
            Log.d("RemoteRepo_Profile", "User Id is -1. Serious issue");
            return null;
        }

        Call<ResponseBody> call = nodeService.getStringProperty(Long.toString(userId), "Username");
        try {
            Response<ResponseBody> response = call.execute();
            if (response.code() == 200) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getUserAbout(long userId) {
        if (userId == -1) {
            Log.d("RemoteRepo_Profile", "User Id is -1. Serious issue");
            return null;
        }

        Call<ResponseBody> call = nodeService.getStringProperty(Long.toString(userId), "About");
        try {
            Response<ResponseBody> response = call.execute();
            if (response.code() == 200) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getUserWeb(long userId) {
        if (userId == -1) {
            Log.d("RemoteRepo_Profile", "User Id is -1. Serious issue");
            return null;
        }
        Call<ResponseBody> call = nodeService.getStringProperty(Long.toString(userId), "Web");
        try {
            Response<ResponseBody> response = call.execute();
            if (response.body() == null) {
                return null;
            }
            if (response.code() == 200) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getProfilePictureId(long userId) {

        if (userId == -1) {
            Log.d("RemoteRepo_Profile", "User Id is -1. Serious issue");
            return null;
        }

        Call<ResponseBody> call = nodeService.getStringProperty(Long.toString(userId), "ProfilePicId");
        try {
            Response<ResponseBody> response = call.execute();
            if (response.body() == null) {
                return null;
            }
            if (response.code() == 200) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Trip> getUserTrips(long userId) {
        if(userId == -1) {
            Log.d(TAG, "User id is -1. Serious issue");
        }

        try {
            Call<ArrayList<Trip>> call = nodeService.getTopThreeTripsForAUser(Long.toString(userId));
            Response<ArrayList<Trip>> tripsResponse = call.execute();

            if (tripsResponse.code() == 200) {
                return tripsResponse.body();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void saveUserName(String username, long userId) {
        if (userId == -1) {
            Log.d(TAG, "User Id is -1. Serious issue");
            return;
        }
        // Send request to server to update users profile pic.
        try {
            Call<ResponseBody> call = nodeService.setStringProperty(Long.toString(userId), "Username", username);
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void saveUserAbout(String about, long userId) {
        if (userId == -1) {
            Log.d(TAG, "User Id is -1. Serious issue");
            return;
        }
        // Send request to server to update users profile pic.
        try {
            Call<ResponseBody> call = nodeService.setStringProperty(Long.toString(userId), "About", about);
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveUserWeb(String website, long userId) {
        if (userId == -1) {
            Log.d("RemoteRepo_Profile", "User Id is -1. Serious issue");
            return;
        }

        try {
            Call<ResponseBody> call = nodeService.setStringProperty(Long.toString(userId), "Web", website);
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveProfilePictureId(String profilePicId, long userId) {
        if (userId == -1) {
                Log.d("RemoteRepo_Profile", "User Id is -1. Serious issue");
                return;
            }
            try {
                Call<ResponseBody> call = nodeService.setStringProperty(Long.toString(userId), "ProfilePicId", profilePicId);
                call.execute();
            } catch (IOException e) {
                e.printStackTrace();
        }
    }

    @Override
    public void saveUserTrips(ArrayList<Trip> trips) {

    }

    public void setUserFolliwingAnotherUser(long broadcastUserId, long followingUserId) {
        Log.d(TAG, "User " + followingUserId + " now following other user: " +broadcastUserId);
        OkHttpClient client = new OkHttpClient();
        String url = LoadBalancer.RequestServerAddress() + "/rest/User/PinUserForUser/"+followingUserId+"/"+broadcastUserId;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            // Should we retry here?
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUserNotFollowingAnotherUser(long broadcastUserId, long followingUserId) {
        Log.d(TAG, "User " + followingUserId + " is no longer following other user: " +broadcastUserId);
        OkHttpClient client = new OkHttpClient();
        String url = LoadBalancer.RequestServerAddress() + "/rest/User/UnPinUserForUser/"+followingUserId+"/"+broadcastUserId;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            // Should we retry here?
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public  boolean isUserFollowingOtherUser(long userId, long visitorId) {
        Log.d(TAG, "checking if user " + visitorId + " is following user: " +userId);
        OkHttpClient client = new OkHttpClient();
        String url = LoadBalancer.RequestServerAddress() + "/rest/User/IsUserFollowingOtherUser/"+visitorId+"/"+userId;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            String stringBool = response.body().string();
            return Boolean.parseBoolean(stringBool);
            // Should we retry here?
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "User following check failed");
        }
        return false;
    }


}
