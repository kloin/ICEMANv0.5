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
		String message = userName + " commented on your crumb: '" + comment + "'"; 
		gcmSender.SendDownStreamCommentMessage(message, gcmId, trailId, id);
	}
	
	public void SendUserNoficationWhenFollowed(String followedUserId, String followingUserId) {
		String userName = dbMaster.GetStringPropertyFromNode(followingUserId, "Username");
		String message = userName + " just is now following you.";
		gcmSender.SendDownStreamMessage(message, followedUserId);
	}
	
	public void SendUserNotificationOfTrailBeingFollowed(String trailId, String followedUserId, String followingUserId) {
		String userName = dbMaster.GetStringPropertyFromNode(followingUserId, "Username");
		String trailname = dbMaster.GetStringPropertyFromNode(trailId, "TrailName");
		String message = userName + " just is now following your trail: " + trailname; 
	}
	
	
}
