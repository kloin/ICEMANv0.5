package com.breadcrumbs.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * This is the class used to create and help with local storage. Uses SQLite
 */
public class DatabaseController extends SQLiteOpenHelper {
	private static final String DATABASE_NAME="userDb";
	static final String USERID="userid";
	static final String USERNAME="username";
	static final String AGE="age";
	static final String PIN="pin";
	static final String TRAILID="trailid";

    private Context mContext;
	
	private SQLiteDatabase db;
	
	//private SQLiteDatabase db;
	public DatabaseController(Context context) {
		super(context, "users", null, 1);
        mContext = context;
		//SQLiteCursor cursor = new SQLiteCursor(db, driver, editTable, query)
	
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub		
	}
	
	public void SaveUser(String userId, String userName, int age, String pin) {
		ContentValues cv = new ContentValues();
		cv.put(USERID, userId);
		cv.put(USERNAME, userName);
		cv.put(AGE, age);
		cv.put(PIN, pin);
		
		getWritableDatabase().insert("users", null, cv);
		
		//Cursor cursor = db.rawQuery("SELECT * FROM userDb",
				//null);
		//cursor.getColumnNames();
	}
    /*
    a method made to delete all the saved trail points for a trail
     */
    public void clearTrails(String trailId) {
        db = getWritableDatabase();
        db.rawQuery("DELETE from trailPoints", null);
    }
    /*
    This is used to save and store data for a trail. This is because we only save trails when:
        - User is online (i.e using the app)
        - User has not saved a trail for 5/10 minutes
     */
    public void saveTrailPoint(String trailId, Location location, String userId) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        String seconds = DateFormat.getDateTimeInstance().format(new Date());

        //Values to save
        ContentValues cv = new ContentValues();
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("timeStamp", seconds);
        cv.put("trailId", trailId);
        cv.put("userId", userId);

        // Write our shit to the database.
        SQLiteDatabase localDb = getWritableDatabase();
        localDb.insert("trailPoints", null, cv);
        localDb.close();
    }
    /*
    Method to remove all trailPoints from the db. We want to do this after saving them.
     */
    public void DeleteAllSavedTrailPoints(String trailId) {
        db = getWritableDatabase();
        Cursor constantsCursor=db.rawQuery("DELETE FROM trailPoints Where trailId =" +trailId,
                null);
    }
    public JSONObject getAllSavedTrailPoints(String trailId) {
        JSONObject allTrailPoints = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM trailPoints WHERE trailId ="+trailId+" ORDER BY timeStamp",
                null);
        int numberOfPointsIndex = 0;
        JSONObject lastNode = null;
        while (constantsCursor.moveToNext()) {
            JSONObject pointJsonNode = new JSONObject();
            //Get all columns
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("longitude"));
            String userId = constantsCursor.getString(constantsCursor.getColumnIndex("userId"));
            String timeStamp = constantsCursor.getString(constantsCursor.getColumnIndex("timeStamp"));
            // Save our single point as a single node, and add it to the overall object that is
            // going to be sent to the server.
            try {
                pointJsonNode.put("latitude", latitude);
                pointJsonNode.put("longitude", longitude);
                pointJsonNode.put("userId", userId);
                pointJsonNode.put("timeStamp", timeStamp);
                pointJsonNode.put("trailId", trailId);
                allTrailPoints.put("Index:"+numberOfPointsIndex, pointJsonNode);
                if (lastNode != null) {
                    // set next node.
                    lastNode.put("next", numberOfPointsIndex);
                }
                lastNode = pointJsonNode;
                // Also need to set last node and next node. So :
                numberOfPointsIndex += 1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("BC.DatabaseController", "Found Trail Points: " + allTrailPoints.toString());
        return allTrailPoints;
    }
    public void saveLinkedTrail(String Id) {
        ContentValues cv = new ContentValues();
        cv.put(TRAILID, Id);
        getWritableDatabase().insert("linkedTrails", null, cv);
        getWritableDatabase().close(); // same again.
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
        this.db = db;

        db.execSQL("CREATE TABLE users (_id INTEGER PRIMARY KEY AUTOINCREMENT, userid TEXT," +
                "username TEXT, " +
                "age TEXT," +
                "pin TEXT);");

        //Create our linkedTrails table.
        db.execSQL("CREATE TABLE linkedTrails (_id INTEGER PRIMARY KEY AUTOINCREMENT, trailId TEXT);");

        db.execSQL("CREATE TABLE trailPoints (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trailId TEXT," +
                "userId TEXT," +
                "latitude REAL," +
                "longitude REAL," +
                "timeStamp TEXT);");
	}
	
	public SQLiteDatabase GetDBInstance() {
		return this.db;
	}
	
	public void LoadDataFromDatabaseTest() {
		Cursor constantsCursor=db.rawQuery("SELECT _ID, title, value "+
				"FROM users ORDER BY title",
				null);
	}
	
}
