//package com.breadcrumbs.retrieval;
//
//import java.util.Iterator;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.json.JSONObject;
//import org.json.JSONException;
//import org.neo4j.graphdb.Label;
//import org.neo4j.graphdb.Node;
//
//import com.breadcrumbs.database.DBMaster;
//import com.breadcrumbs.database.NodeController;
//
//public class UserManager extends IDataManager {
//
//	private HttpServletRequest request;
//	private DBMaster dbMaster;
//	
//	private JSONObject jsonResponse;
//	
//	public UserManager(HttpServletRequest request) {
//		this.request = request;
//		dbMaster = DBMaster.GetAnInstanceOfDBMaster();
//	}
//	
//	public UserManager() {
//		
//	}
//	
//	
//	public String GetUser(int userId) throws JSONException {
//		NodeController nc = new NodeController();
//		Node userNode = nc.FetchNode(userId);
//		
//		JSONObject userJson = new JSONObject();
//		Iterator userIterator = userNode.getLabels().iterator();
//		
//		while (userIterator.hasNext()) {
//			try {
//				String label = userIterator.next().toString();
//				userJson.put(label, userNode.getProperty(label));
//			} catch (JSONException e) {
//				//Not throwing this blatantly,just printing it because it is possible that an id could be wrong/not find user etc..
//				System.out.println("EXCEPTION THROWN FETCHING USER - ");
//				e.printStackTrace();
//			}
//		}
//		
//		System.out.println("FETCHED DATA: " + userJson.toString());
//		return userJson.toString();
//		
//	}
//	
//	
//	
//
//}
