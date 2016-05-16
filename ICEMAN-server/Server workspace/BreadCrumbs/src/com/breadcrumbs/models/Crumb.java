package com.breadcrumbs.models;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.NClob;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;

import com.breadcrumbs.database.*;
import com.breadcrumbs.database.DBMaster.myLabels;
import com.breadcrumbs.database.DBMaster.myRelationships;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
public class Crumb {
	
	private DBMaster dbm;
	public String AddCrumb(String chat, String userId, String trailId, String latitude, String longitude, String icon, String extension, String placeId, String suburb, String city, String country, String timeStamp) {
		Trail trailManager = new Trail();
		//create node using node controller
		Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
		keysAndItems.put("Chat", chat);
		keysAndItems.put("UserId", userId);
		keysAndItems.put("TrailId", trailId);
		keysAndItems.put("Latitude", latitude);
		keysAndItems.put("Longitude", longitude);
		keysAndItems.put("Icon", icon);
		keysAndItems.put("Extension", extension);
		keysAndItems.put("PlaceId", placeId);
		keysAndItems.put("Suburb", suburb);
		keysAndItems.put("City", city);
		keysAndItems.put("Country", country);
		keysAndItems.put("TimeStamp", timeStamp);

		dbm = DBMaster.GetAnInstanceOfDBMaster();
		int crumbId = dbm.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.Crumb);	
		Node crumb = dbm.RetrieveNode(crumbId);
		Node trail = dbm.RetrieveNode(Integer.parseInt(trailId));

		// Set the cover photo. This is being done automatically at the moment, but in the future we will
                // need to check if the user has set a personal cover photo first.
                trailManager.SetCoverPhoto(trailId, Integer.toString(crumbId));
		dbm.CreateRelationship(crumb, trail, myRelationships.Part_Of);	
		return String.valueOf(crumbId);
	}
	
    public int ConvertAndSaveVideo(InputStream stream, String crumbId) {
        int result = 0;             
        File targetFile = new File("/var/lib/tomcat7/webapps/images/"+crumbId+".mp4");
        byte[] buffer = new byte[1024];
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(targetFile);
            int length;             
            while((length = stream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);      
            }
            outputStream.close();
        } catch (FileNotFoundException e) {
            result = 1;
            DeleteFile("/var/lib/tomcat7/webapps/images/"+crumbId+".mp4");
            e.printStackTrace();
        } catch (IOException e) {
            result = 1;
            DeleteFile("/var/lib/tomcat7/webapps/images/"+crumbId+".mp4");
            e.printStackTrace();
        }
        return result;            
    }
    
    /**
     * @param inputStream
     * @param mime The mime type (".jpg" ".mp4")
     * @param crumbId The crumb id we are saving against. Used in fileName
     * @return The result (success or failure)
     */
    public int saveMedia (InputStream inputStream, String mime, String crumbId) {
        int result = 0; // Success = 0, failure = 1;
        if (mime.equals(".mp4")) {
            result = ConvertAndSaveVideo(inputStream, crumbId);
        } else {
            result = ConvertAndSaveImage(inputStream, crumbId);
        }
        
        return result;
    }
    
    public int ConvertAndSaveImage(InputStream uploadImageStream, String crumbId) {
        int result = 0;
        File targetFile = new File("/var/lib/tomcat7/webapps/images/"+crumbId+".jpg");
        byte[] buffer = new byte[1024];
        OutputStream outputStream;
        try {    
            // Create a thumbnail.
            createAndSaveThumbnail(uploadImageStream, crumbId);
            outputStream = new FileOutputStream(targetFile);
            int length;             
            while((length = uploadImageStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);      
            }
            outputStream.close(); 
        } catch (FileNotFoundException e) {
                result = 1;
                // Delete what we had tried to save.
                DeleteFile("/var/lib/tomcat7/webapps/images/"+crumbId+".jpg");
                e.printStackTrace();
        } catch (IOException e) {
                result = 1;
                DeleteFile("/var/lib/tomcat7/webapps/images/"+crumbId+".jpg");
                e.printStackTrace();
        }
        return result;
    }
    
    
    /**
     * Delete a file from our local file system
     * @param fileName The path of the file to delete.
     */
    public void DeleteFile(String fileName) {
        try {
            File file = new File(fileName);
            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
            }else{
                System.out.println("Delete operation is failed.");
            }
    	} catch(Exception e){
            e.printStackTrace();
    	}
    }
        
        // Create a thumbnail of an image.
        private void createAndSaveThumbnail(InputStream uploadInputStream, String crumbId) throws IOException {
			BufferedImage img = ImageIO.read(uploadInputStream);
			Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
			BufferedImage thumbnail = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			thumbnail.createGraphics().drawImage(scaledImg,0,0,null);
			ImageIO.write(thumbnail, "jpg", new File(Statics.StaticValues.serverAddress + crumbId+"T.jpg"));      

        }

	public String GetLatitudeAndLongitude(String crumbId) {
		dbm = DBMaster.GetAnInstanceOfDBMaster();
		String latitude = dbm.GetStringPropertyFromNode(crumbId, "Latitude");
		String longitude = dbm.GetStringPropertyFromNode(crumbId, "Longitude");
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("Latitude", latitude);
		jsonResponse.put("Longitude", longitude);
		return jsonResponse.toString();
	}
	
	public void UserLikesCrumb(String userId, String crumbId) {
		dbm = DBMaster.GetAnInstanceOfDBMaster();	
		try {
			Node userNode = dbm.RetrieveNode(Integer.parseInt(userId));
			Node crumbNode = dbm.RetrieveNode(Integer.parseInt(crumbId));
			//MATCH (a),(b)
			//WHERE a.name = 'Peter' AND b.name = 'World' and (a)-[:TEST2]->(b) // Test for if relationship exists
			//		RETURN a, b
			dbm.CreateRelationship(userNode, crumbNode, myRelationships.Likes);	
		} catch(NullPointerException nullPointerException) {
			nullPointerException.printStackTrace();
		}
	}

	public String GetNumberOfLikesForACrumb(String crumbId) {
		Trail trail = new Trail();
		return trail.GetNumberOfLikesForAnEntity(crumbId);		 
	}
	
	/* removes a users like on a crumb */
	public void RemoveLike(String userId, String crumbId) {
		String cypherDelete = "START user=node(*) MATCH user-[rel:LIKES]->crumb WHERE id(user)="+userId+" AND id(crumb)= "+crumbId+" DELETE rel";
		dbm = DBMaster.GetAnInstanceOfDBMaster();
		dbm.ExecuteCypherQueryNoReturn(cypherDelete);
	}
	
}
