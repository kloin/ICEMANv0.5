package com.breadcrumbs.ServiceProxy;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.client.FragmentMaster;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class AsyncImageFetch extends AsyncTask<String, Integer, String> {
		private JSONObject json = new JSONObject();
		private FragmentMaster fragment;
		private String url;
		private Bitmap bm;
		private File image;
		private String id;
		// This is the interface for the callback method to use.
		public interface RequestListener {
			public void onFinished(String result);
		}
		// Our callback instance
		private RequestListener requestListener;
		
		// Pass in the callback and the url to our constructor.
		public AsyncImageFetch(Bitmap bm, String id, RequestListener requestListener){
			this.bm = bm;
			this.id = id;
			this.requestListener = requestListener;
		}
		
		@Override
		protected String doInBackground(String... urls) {
			 PostImage();
			 return "Fuck yea";
		}
		
		@Override
	    protected void onPostExecute(String jsonResult) {
			requestListener.onFinished(jsonResult);
	    }		
		
		/*
		 * Send an image to the server
		 */
		public void PostImage() {

				
				HttpURLConnection connection = null;  
			    try {
			    	ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			    	bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object   
			    	byte[] b = baos.toByteArray(); 
			    	String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
			    	//File file = new File("C:\\Users\\Josiah\\Desktop\\test.jpeg");
			    	//System.out.println(file.exists());
			    	// String imageString = null;
			       //  ByteArrayOutputStream bos = new ByteArrayOutputStream();
			         //BufferedImage image = ImageIO.read(file);

			             //ImageIO.write(image, "jpeg", bos);
			            // byte[] imageBytes = bos.toByteArray();

			             //BASE64Encoder encoder = new BASE64Encoder();
			             //imageString = encoder.encode(imageBytes);

			           //  bos.close();
			            
			    	String urlParameters =encodedImage;
			      //Create connection
			      URL url = new URL(LoadBalancer.RequestServerAddress() + "/rest/login/savecrumb/"+ id);
			      connection = (HttpURLConnection)url.openConnection();
			      connection.setRequestMethod("POST");
			      connection.setRequestProperty("Content-Type", 
			           "application/json");
						
			      connection.setRequestProperty("Content-Length", "" + 
			               Integer.toString(urlParameters.getBytes().length));
			      connection.setRequestProperty("Content-Language", "en-US");  
						
			      connection.setUseCaches (false);
			      connection.setDoInput(true);
			      connection.setDoOutput(true);

			      //Send request
			      DataOutputStream wr = new DataOutputStream (
			                  connection.getOutputStream ());
			      wr.writeBytes (urlParameters);
			      wr.flush ();
			      wr.close ();

			      //Get Response	
			      InputStream is = connection.getInputStream();
			      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			      String line;
			      StringBuffer response = new StringBuffer(); 
			      while((line = rd.readLine()) != null) {
			        response.append(line);
			        response.append('\r');
			      }
			      rd.close();

			    } catch (Exception e) {

			      e.printStackTrace();

			    } finally {

			      if(connection != null) {
			        connection.disconnect(); 
			      }
			    }
		}	
	}
	
	
         

      

