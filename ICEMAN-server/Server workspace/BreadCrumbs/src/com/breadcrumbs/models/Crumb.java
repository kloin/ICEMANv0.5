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
import com.breadcrumbs.database.DBMaster.TransactionCallback;
import com.breadcrumbs.database.DBMaster.myLabels;
import com.breadcrumbs.database.DBMaster.myRelationships;
import com.breadcrumbs.heavylifting.Compression.CompressVideo;
import com.breadcrumbs.heavylifting.Compression.CompressionContract;
import com.breadcrumbs.heavylifting.Compression.SynchronizedCompressionBacklog;
import com.breadcrumbs.heavylifting.CompressionManager;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import java.io.FileInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
public class Crumb {
	
    private DBMaster dbm;
    public String AddCrumb(String chat, String userId, String trailId, String latitude, String longitude, String icon, String extension, String placeId, String suburb, String city, String country, String timeStamp, String orientation) {
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
            keysAndItems.put("Orientation", orientation);


            dbm = DBMaster.GetAnInstanceOfDBMaster();
            int crumbId = dbm.SaveNode(keysAndItems, com.breadcrumbs.database.DBMaster.myLabels.Crumb);	
            Node crumb = dbm.RetrieveNode(crumbId);
            Node trail = dbm.RetrieveNode(Integer.parseInt(trailId));

            // Set the cover photo. This is being done automatically at the moment, but in the future we will
            // need to check if the user has set a personal cover photo first.
           // trailManager.SetCoverPhoto(trailId, Integer.toString(crumbId));
            dbm.CreateRelationship(crumb, trail, myRelationships.Part_Of);	
            return String.valueOf(crumbId);
    }
	
    public int ConvertAndSaveVideo(InputStream stream, final String crumbId) {
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
        
        // Grab our global blocking queue. Add our item to the backlog for compression.
        compressVideo(crumbId);
        
        return result;            
    }
    
    public void compressVideo(String id) {
        SynchronizedCompressionBacklog backlog = SynchronizedCompressionBacklog.GetInstance();
        BlockingQueue<String> ids = backlog.GetQueue();
        ids.add(id);
        
        // If its the first item, we need to start up the compression thread again as it would have stopped.
        if (!backlog.isCompressing()) {
            CompressionContract contract = new CompressVideo();
            CompressionManager compressionManager = new CompressionManager(contract, ids);
            new Thread(compressionManager).start();
            backlog.setCompressing(true);
        }
    }
    
    /**
     * @param inputStream
     * @param mime The mime type (".jpg" ".mp4")
     * @param crumbId The crumb id we are saving against. Used in fileName
     * @return The result (success or failure)
     */
    public int saveMedia (InputStream inputStream, String mime, String crumbId, String trailId) {
        int result = 0; // Success = 0, failure = 1;
        if (mime.equals(".mp4")) {
            result = ConvertAndSaveVideo(inputStream, crumbId);
        } else {
            result = ConvertAndSaveImage(inputStream, crumbId);
            updateCoverPhoto(trailId, crumbId);
        }
        
        return result;
    }
    
    public void updateCoverPhoto(String trailId, String coverId) {
         if (trailHasCoverPhoto(trailId)) {
                // Set trail as cover photo
            dbm.UpdateNodeWithCypherQuery(trailId, "CoverPhotoId", coverId);
            }
    }
    
    public boolean trailHasCoverPhoto(String trailId) {
        if (dbm == null) {
            dbm = DBMaster.GetAnInstanceOfDBMaster();
        }
        Node trail = dbm.RetrieveNode(Long.parseLong(trailId));
        GraphDatabaseService _db = dbm.GetDatabaseInstance();
        Transaction tx = _db.beginTx();
        try {
            if (trail.hasProperty("CoverPhotoId")) {
                tx.success();
                return true;
            }
        } catch (Exception ex) {
                System.out.println("Failed to do callback transaction.");
                ex.printStackTrace();
                tx.failure();
        } finally {
                tx.finish();
                tx.close();
        }
        return false;
    }
    
    
   
    
    
   
    
    /**
     * This is a test version of the method {@link #ConvertAndSaveImage(java.io.InputStream, java.lang.String) 
     * Used only for testing, with different save parameters.
     * @param uploadImageStream
     * @param crumbId
     * @param isTest
     * @return 
     */
    public int ConvertAndSaveImage(InputStream uploadImageStream, String crumbId, boolean isTest) {
        int result = 0;
        File targetFile = new File("C:\\Users\\jek40\\Desktop\\ICEMANv0.5\\ICEMAN-server\\Server workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\BreadCrumbs\\images\\"+crumbId+".jpg");
        byte[] buffer = new byte[1024];
        OutputStream outputStream;
        try {    
            outputStream = new FileOutputStream(targetFile);
            int length;             
            while((length = uploadImageStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);      
            }
            
            // Create a thumbnail. Note that this method closes the inputStream {@link #uploadImageStream}
            InputStream targetStream = new FileInputStream(targetFile);
            createAndSaveThumbnail(targetStream, crumbId, true);
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
    
    public int ConvertAndSaveImage(InputStream uploadImageStream, String crumbId) {
        int result = 0;
        File targetFile = new File("/var/lib/tomcat7/webapps/images/"+crumbId+".jpg");
        byte[] buffer = new byte[1024];
        OutputStream outputStream;
        try {    
            outputStream = new FileOutputStream(targetFile);
            int length;             
            while((length = uploadImageStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);      
            }
            
            // Create a thumbnail. Note that this method closes the inputStream {@link #uploadImageStream}
            InputStream targetStream = new FileInputStream(targetFile);
            createAndSaveThumbnail(targetStream, crumbId);
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
        
        // Create a thumbnail of an image.
        private void createAndSaveThumbnail(InputStream uploadInputStream, String crumbId, boolean isTest) throws IOException {
			BufferedImage img = ImageIO.read(uploadInputStream);
			Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
			BufferedImage thumbnail = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			thumbnail.createGraphics().drawImage(scaledImg,0,0,null);
			ImageIO.write(thumbnail, "jpg", new File(Statics.StaticValues.testingAddress + crumbId+"T.jpg"));      
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
    
    public String GetFrame(String frameId) {
        DBMaster dbMaster = DBMaster.GetAnInstanceOfDBMaster();
        GraphDatabaseService _db = dbMaster.GetDatabaseInstance();
        Node userNode = dbMaster.RetrieveNode(Long.parseLong(frameId));
        if (userNode == null) {
            return "500";
        }
        
        Transaction tx = _db.beginTx();
        try {
            NodeConverter nodeConverter = new NodeConverter();
            JSONObject result = nodeConverter.ConvertSingleNodeToJSON(userNode);
            return result.toString();
        } catch (Exception ex) {
            System.out.println("Failed to retrieve node");
            ex.printStackTrace();
            tx.failure();
            return "500";
        } finally {
            tx.finish();
        }
    }

    public String AddCrumbWithFloatingDescription(String chat, String posX, String posY, String userId, String trailId, String latitude, String longitude, String icon, String extension, String placeId, String suburb, String city, String country, String timeStamp) {
        Trail trailManager = new Trail();
        //create node using node controller
        Hashtable<String, Object> keysAndItems = new Hashtable<String, Object>();
        keysAndItems.put("Chat", chat);
        keysAndItems.put("DescPosX", posX);
        keysAndItems.put("DescPosY", posY);
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
	
}
