package com.breadcrumbs.ServiceProxy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.breadcrumbs.Network.LoadBalancer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class AsyncRetrieveImage extends AsyncTask<String, Integer, Bitmap> {
	private String id;
	
	// This is the interface for the callback method to use.
	public interface RequestListener {
		public void onFinished(Bitmap result);
	}
	// Our callback instance
	private RequestListener requestListener;
	
	// Pass in the callback and the url to our constructor.
	public AsyncRetrieveImage(String id, RequestListener requestListener){
		this.id = id;
		this.requestListener = requestListener;
	}
	
	
	@Override
    protected void onPostExecute(Bitmap image) {
		requestListener.onFinished(image);
    }
	@Override
	protected Bitmap doInBackground(String... arg0) {
		URL url = null;
		try {
			url = new URL(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id+".jpg");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Bitmap bmp = null;
		try {
			bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bmp;
	}
	
}
