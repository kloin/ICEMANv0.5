package testShit;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.breadcrumbs.resource.RetrieveData;

public class MediaTests {
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
	
//	@Test
//	public void TestFileCanBeCreatedViaURL() {
//		 HttpURLConnection connection = null;  
//		    try {
//		    	FileInputStream fileInputStream=null;
//		    	File file = new File("C:\\Users\\Josiah\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp4\\wtpwebapps\\BreadCrumbs\\images\\media\\4.png");
//		    	System.out.println(file.exists());
//		    	byte[] bFile = new byte[(int) file.length()];
//		    	 fileInputStream = new FileInputStream(file);
//		 	    fileInputStream.read(bFile);
//		 	    fileInputStream.close();
//		  
//		    	 String imageString = null;
//		    	// byte[] imageBytes = IOUtils.toByteArray("C:\\Users\\Josiah\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp4\\wtpwebapps\\BreadCrumbs\\images\\media\\4.png");
//		    	// String base64 = Base64.encodeBase64URLSafeString(bFile);
//		            
//		    	String urlParameters = URLEncoder.encode(base64, "UTF-8");
//		      //Create connection
//		      URL url = new URL("http://localhost:8080/breadcrumbs/rest/login/savecrumb/21");
//		      connection = (HttpURLConnection)url.openConnection();
//		      connection.setRequestMethod("POST");
//		      connection.setRequestProperty("Content-Type", 
//		           "application/json");
//					
//		      connection.setRequestProperty("Content-Length", "" + 
//		               Integer.toString(urlParameters.getBytes().length));
//		      connection.setRequestProperty("Content-Language", "en-US");  
//					
//		      connection.setUseCaches (false);
//		      connection.setDoInput(true);
//		      connection.setDoOutput(true);
//
//		      //Send request
//		      DataOutputStream wr = new DataOutputStream (
//		                  connection.getOutputStream ());
//		      wr.writeBytes (urlParameters);
//		      wr.flush ();
//		      wr.close ();
//
//		      //Get Response	
//		      InputStream is = connection.getInputStream();
//		      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//		      String line;
//		      StringBuffer response = new StringBuffer(); 
//		      while((line = rd.readLine()) != null) {
//		        response.append(line);
//		        response.append('\r');
//		      }
//		      rd.close();
//
//		    } catch (Exception e) {
//
//		      e.printStackTrace();
//
//		    } finally {
//
//		      if(connection != null) {
//		        connection.disconnect(); 
//		      }
//		    }
//	}
	
//	@Test 
//	public void TestThatWeCanSaveImageToServer() {
//		//Construct imput stream from image
//		//Pass it to server method
//		//Test image exists at location
//		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		 InputStream file = null;
//		 try {
//			file = new FileInputStream("C:\\Users\\Josiah\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp4\\wtpwebapps\\BreadCrumbs\\images\\vine.png");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		// retrieve.uploadFile(file);
//		 
//		 
//		 
//		
//	}
}
