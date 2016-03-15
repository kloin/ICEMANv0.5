package com.breadcrumbs.gcm;

import org.neo4j.graphdb.Node;

import com.breadcrumbs.database.DBMaster;

public class GcmMessages {
	private DBMaster dbMaster;
	private GcmSender gcmSender;
	
	public GcmMessages() {
		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
		gcmSender = new GcmSender();
	}
	
	public void SendUserNotificationOfComment(String id, String comment, String commenterId) {
		String userId = dbMaster.GetStringPropertyFromNode(id, "UserId");
		String trailId = dbMaster.GetStringPropertyFromNode(id, "TrailId");
		String gcmId = dbMaster.GetStringPropertyFromNode(userId, "GcmId");
		String userName = dbMaster.GetStringPropertyFromNode(commenterId, "Username");
		String message = userName + " commented on your crumb " + comment; 
		gcmSender.SendDownStreamCommentMessage(message, gcmId, trailId, id);
	}
	
	
}