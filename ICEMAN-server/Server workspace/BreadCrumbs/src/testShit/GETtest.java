package testShit;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;
import javax.ws.rs.PathParam;



import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.codec.binary.*;
import org.apache.lucene.util.IOUtils;

import com.breadcrumbs.resource.RetrieveData;

public class GETtest {
	private RetrieveData retrieve;
	@Before
	public void setUp() throws Exception {
		retrieve = new RetrieveData();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void TestGetAllCrumbsReturnsData() {
		
		try {
			JSONObject jsonResponse = new JSONObject(retrieve.GetAllCrumbsForATrail("4"));
			assertEquals(jsonResponse.length() > 0, true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	@Test
	public void TestFileCanBeCreatedViaURL() {
		 HttpURLConnection connection = null;  
		    try {
		    	FileInputStream fileInputStream=null;
		    	File file = new File("C:\\Users\\Josiah\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp4\\wtpwebapps\\BreadCrumbs\\images\\media\\4.png");
		    	System.out.println(file.exists());
		    	byte[] bFile = new byte[(int) file.length()];
		    	 fileInputStream = new FileInputStream(file);
		 	    fileInputStream.read(bFile);
		 	    fileInputStream.close();
		  
		    	 String imageString = null;
		    	 //byte[] imageBytes = IOUtils.toByteArray("C:\\Users\\Josiah\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp4\\wtpwebapps\\BreadCrumbs\\images\\media\\4.png");
		    	// String base64 = Base64.encodeBase64URLSafeString(bFile);
		            
		    //	String urlParameters = URLEncoder.encode(base64, "UTF-8");
		      //Create connection
		      URL url = new URL("http://localhost:8080/breadcrumbs/rest/login/savecrumb/21");
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
	}*/
	@Test
	public void TestGetAllTrailsReturnsData() {
		
		try {
			JSONObject jsonResponse = new JSONObject(retrieve.GetAllTrailsForAUser("6"));
			assertEquals(jsonResponse.length() > 0, true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	@Test
	public void TestThatUserCanBeCreated() {
		// Warning - this will create a user in the db as it is not inside a transsaction scope.
		int userId = retrieve.CreateNewUser("Josiah", "Kendall", "7873", "23", "M");
		assertTrue(userId > 0);
		try {
			assertTrue(retrieve.GetUser(userId).length() > 0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/
	
	@Test 
	public void TestThatWeCanSaveImageToServer() {
		//Construct imput stream from image
		//Pass it to server method
		//Test image exists at location
		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 InputStream file = null;
		 try {
			file = new FileInputStream("C:\\Users\\Josiah\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp4\\wtpwebapps\\BreadCrumbs\\images\\vine.png");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// retrieve.uploadFile(file);
		 
		 
		 
		
	}
	
	@Test
	public void TestThatTrailCanBeCreated() {
		retrieve.SaveTrail("OOOHRAA", "just testing yo", "6");
	}
	
/*	@Test
	public void TestThatCrumbCanBeAddedToTrail() {
		retrieve.SaveCrumb("testing123", 7, 4, "-36.8", "174.5", "icon");
				
	}*/

}
