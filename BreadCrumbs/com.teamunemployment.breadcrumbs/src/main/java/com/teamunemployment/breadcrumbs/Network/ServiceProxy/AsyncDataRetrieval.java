package com.teamunemployment.breadcrumbs.Network.ServiceProxy;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AsyncDataRetrieval extends AsyncTask<String, Integer, String> {
		private JSONObject json = new JSONObject();
		private String url;
		private Context mContext;
        // jsonResult was losing a variable some how, so im testing with this to see what happens.

	// This is the interface for the callback method to use.
	public interface RequestListener {
		public void onFinished(String result) throws JSONException;

	}

		// Our callback instance
		private RequestListener requestListener;
		
		// Pass in the callback and the url to our constructor.
		public AsyncDataRetrieval(String url, RequestListener requestListener, Context context){
			this.mContext = context;
			this.url = url;
			this.requestListener = requestListener;
		}
		
		@Override
		protected String doInBackground(String... urls) {
			return SendDataRequest(url);
		}
		
		@Override
	    protected void onPostExecute(String jsonResult) {
			if (requestListener != null) {
				try {
					requestListener.onFinished(jsonResult);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
	    }

		/*
		 * This is our method we use to retrieve data using get (or store data.)
		 * Remember - GET is pretty shit, I need to do more research.
		 */
		public String SendDataRequest(String url) {
			if (!NetworkConnectivityManager.IsNetworkAvailable(mContext)) {
				Log.d("NETWORK", "No network connection available.");
				return null;
			}
			OkHttpClient client = new OkHttpClient();
			try {
				Request request = new Request.Builder()
						.url(url)
						.build();
				Response response = client.newCall(request).execute();

				return response.body().string();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

        public String PostDataToServer(String urlString) {
            URL url = null;
            try {
                url = new URL(urlString);
                URLConnection connection = url.openConnection();
                //connection.
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return"";
        }
	}
	
         

      

