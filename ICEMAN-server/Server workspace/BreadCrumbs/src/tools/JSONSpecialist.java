package tools;

import java.util.Iterator;

import org.json.JSONObject;

public class JSONSpecialist {

	/*
	 * Simple helper class to append 2 json objects of the SAME TYPE together. 
	 * Generally should be only used when they are both <String, String>
	 */
	public JSONObject JoinTwoJsonObjects(JSONObject json1, JSONObject json2) {
		Iterator<String> keys = json2.keys();
		while (keys.hasNext()) {
			String next = keys.next();
			JSONObject nextJson = new JSONObject(json2.get(next).toString());
			json1.put(next, nextJson.get("Id"));
		}
		return json1;
	}
	
	public JSONObject JoinTwoJsonObjects1(JSONObject json1, JSONObject json2) {
		Iterator<String> keys = json2.keys();
		while (keys.hasNext()) {
			String next = keys.next();
			JSONObject json = new JSONObject(json2.get(next));
			String id = json.getString("Id");
			json1.append(next,id);
		}
		
		return json1;
	}
}
