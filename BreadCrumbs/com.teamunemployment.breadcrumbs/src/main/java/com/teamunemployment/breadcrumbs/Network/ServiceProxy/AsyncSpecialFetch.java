package com.teamunemployment.breadcrumbs.Network.ServiceProxy;
/*
 * redundant shit.
 */

import android.os.AsyncTask;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.client.FragmentMaster;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class AsyncSpecialFetch  {
	private URL url;
	private static FragmentMaster fragment;
	private class FetchData extends AsyncTask<Integer, Integer, byte[]> {
		@Override
		protected byte[] doInBackground(Integer... id) {
			// TODO Auto-generated method stub
			 OutputStream outStream = null;
		        URLConnection  uCon = null;
		        byte[] buf = null;

	    	    InputStream is = null;
		        try {
		            
		            int byteRead;
		            int byteWritten=0;
		            uCon = url.openConnection();
		            is = uCon.getInputStream();
		            is.read(buf);
		            System.out.println(is.toString());
		        }
		        catch (Exception e) {
		            e.printStackTrace();
		        }
		        
		        return buf;
		}
		
		@Override
		protected void onPostExecute(byte[] result) {
			fragment.LoadImage(result);
		}
	}

	
}
