package com.teamunemployment.breadcrumbs.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

/*
 * This is the class used to create and help with local storage. Uses SQLite
 */
public class DatabaseController extends SQLiteOpenHelper {
	private static final String DATABASE_NAME="userDb";
    private static final String TRAIL_POINTS_INDEX = "trailIndexDb";

    private static final String CRUMBS = "crumbsDb";
    private static final String RESTZONES = "restZoneDb";
    private static final String METADATA = "metadataDb";
    private static final String WEATHER = "weatherDb";

	private static final String USERID="userid";
	private static final String USERNAME="username";
	private static final String AGE="age";
	private static final String PIN="pin";
    private static final String TRAILID="trailid";

    private Context mContext;
	private SQLiteDatabase db;

	//private SQLiteDatabase db;
	public DatabaseController(Context context) {
		super(context, "users", null, 1);
        this.mContext = context;
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
	}

    public boolean CheckUserExists(String userId) {
        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("Select * from "+DATABASE_NAME+" where userid="+userId, null);
            if (cursor.getCount() > 0) {
                return true;
            }
        }catch (SQLiteException ex) {
            Log.d("DB", "Checking for user failed, most likely due to database not existing.");
            ex.printStackTrace();
        }

        return false;
    }

    /*
    a method made to delete all the saved trail points for a trail
     */
    public void clearTrails(String trailId) {
        db = getWritableDatabase();
        db.rawQuery("DELETE from trailPoints", null);
    }

    private void setTrailPointIndex(String trailId, int trailIndex) {
        // Store it in our shared preferences.
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("TrailIndex", trailIndex).commit();

        // We also want it in the DB.
        db = getWritableDatabase();
        db.rawQuery("UPDATE "+ TRAIL_POINTS_INDEX +
                    " SET trailIndex="+trailIndex +
                    " WHERE trailid="+trailId, null);
    }

    // Method to get the number of points we have currently saved, so we know the where the point needs to go in the trail.
    private int getTrailPointIndex(String trailId) {
        int trailPointIndex = PreferenceManager.getDefaultSharedPreferences(mContext).getInt("TrailIndex", 0);
        if (trailPointIndex == 0) {
            // This is incase we fail to get it out the shared preferences for whatever reason (cleared cache etc)
            db = getWritableDatabase();
            Cursor cursor = db.rawQuery("Select * from "+TRAIL_POINTS_INDEX+" where trailid="+trailId, null);
            if (cursor.getCount() > 0) {
                trailPointIndex = cursor.getInt(cursor.getColumnIndex("trailIndex"));
            } else {
                db.rawQuery("INSERT INTO "+ TRAIL_POINTS_INDEX + " (trailid, trailIndex)" +
                        " VALUES ("+trailId + ","+trailPointIndex+");", null);
            }
        }

        return trailPointIndex;
    }

    /*
        This is used to save and store data for a trail. This is because we only save trails when:
        - User is online (i.e using the app)
        - User has not saved a trail for 5/10 minutes
     */
    public void saveTrailPoint(String trailId, Location location, String userId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        String seconds = DateFormat.getDateTimeInstance().format(new Date());

        // Get our needed variables
        int trailPointIndex = getTrailPointIndex(trailId);

        Double lastLatitude = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(mContext).getString("LastLat", "-1"));
        Double lastLongitude =  Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(mContext).getString("LastLong", "-1"));

        // Here we check if the point is a satasfactory distance from the previously saved point, so that
        // we do not save shitty repeated points whilst we are at one location for a long period of time.
        // This should be done by the Fused GPS but it does not seem to be consistently working.
        //if (lastLatitude > 0 && lastLongitude > 0) {
            Location oldLocation = new Location("Old Location");
            oldLocation.setLatitude(lastLatitude);
            oldLocation.setLongitude(lastLongitude);

            float[] distances = new float[1];
            Location.distanceBetween(lastLatitude,
                    lastLongitude, location.getLatitude(),
                    location.getLongitude(), distances);

            // Check distances, if less than X meters moved, we dont want to be saving the point
            for (float distance : distances) {
                if (distance > 2000 || distance < 100) {
                    if (distance > 2000) {
                        // Update the location. THis is done so that the next point we get will probably be back to normal,
                        // which will again miss anc come through this if statement and not save. The next point however will be ok.
                        // The need for this is that if we do not record for a long time for whatever reason, this statement will stop
                        // the tracking from being permanantly disabled.
                        preferences.edit().putString("LastLat", Double.toString(location.getLatitude())).commit();
                        preferences.edit().putString("LastLong", Double.toString(location.getLongitude())).commit();
                    }
                    Log.d("GPS", "Not saving point - too close to last");
                    return;
                }
            }
        //}

        // Need to make sure I am saving these.
        String savedActivity = preferences.getString("RECORDING_ACTIVITY", null);
        String lastEventId = preferences.getString("LAST_EVENT_ID", null);
        if (savedActivity == null || lastEventId == null) {
            Log.d("DBC", "Not saving point because user activity or eventId is currently none. Hopefully it will be known in a few seconds and we can save then");
            return;
        }

        // Build up the object that we are going to save
        ContentValues cv = new ContentValues();
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("timeStamp", seconds);
        cv.put("trailId", trailId);
        cv.put("userId", userId);
        cv.put("trailIndex", trailPointIndex);
        cv.put("activity_status", savedActivity);
        cv.put("base_event_id", lastEventId);


        Log.d("GPS", "Saving new point to database");
        // Write our shit to the database.
        SQLiteDatabase localDb = getWritableDatabase();
        localDb.insert("trailPoints", null, cv);
        localDb.close();

        // Increment an index
        trailPointIndex += 1;

        // Update our index now that we have ssaved the point to the db.
        setTrailPointIndex(trailId, trailPointIndex);
        // Check whether we should save the points to the server.
        updateServer(2); // Currently saving points in twos.

        // Save the last point.
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("LastLat", Double.toString(location.getLatitude())).commit();
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("LastLong", Double.toString(location.getLongitude())).commit();

    }

    // Method that checks whether more than "pointCount" points have been saved to the db, and if so
    // we need to update the server to have
    private void updateServer(int pointCount) {
        final String trailId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("TRAILID", "-1");

        db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from trailPoints where trailId="+trailId, null);
        cursor.moveToFirst();

        // This makes us only update when we have X amount of points.
        if (cursor.getCount() < pointCount ) {
            Log.d("GPS", "Not enough new points to save to server");
            return;
        }


        // Dont want to be saving a non existent trail - I will do other shit with it first
        if (!trailId.equals("-1")) {
            JSONObject jsonObject = getAllSavedTrailPoints(trailId);
            Log.d("GPS", "Attempting to save points to server");
            // Save our trails.
            String url = LoadBalancer.RequestServerAddress() + "/rest/TrailManager/SaveTrailPoints/";
            AsyncPost post = new AsyncPost(url, new AsyncPost.RequestListener() {
                @Override
                public void onFinished(String result) {
                    Log.d("GPS", "Successfully saved points to server");
                   DeleteAllSavedTrailPoints(trailId);
                    Log.d("GPS", "Deleted all saved points.");
        }
    }, jsonObject);

            post.execute();
        }
    }


    /*
        Method to remove all trailPoints from the db. We want to do this after saving them.
     */
    public void DeleteAllSavedTrailPoints(String trailId) {
        getWritableDatabase().delete("trailPoints", "trailId=" + trailId, null);
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
            int index = constantsCursor.getInt(constantsCursor.getColumnIndex("trailIndex"));
            // Save our single point as a single node, and add it to the overall object that is
            // going to be sent to the server.
            try {
                pointJsonNode.put("latitude", latitude);
                pointJsonNode.put("longitude", longitude);
                pointJsonNode.put("userId", userId);
                pointJsonNode.put("timeStamp", timeStamp);
                pointJsonNode.put("trailId", trailId);
                pointJsonNode.put("index", index);
                pointJsonNode.put("next", index+1);
                allTrailPoints.put("Index:"+numberOfPointsIndex, pointJsonNode);
                // Also need to set last node and next node. So :
                numberOfPointsIndex += 1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        numberOfPointsIndex += 1;
        Log.i("BC.DatabaseController", "Found Trail Points: " + allTrailPoints.toString());
        return allTrailPoints;
    }
    public void saveLinkedTrail(String Id) {
        ContentValues cv = new ContentValues();
        cv.put(TRAILID, Id);
        getWritableDatabase().insert("linkedTrails", null, cv);
        getWritableDatabase().close(); // same again.
    }

    public void SaveCrumb(String trailId, String eventId, double latitude, double longitude, String mime, String timeStamp, byte[] media) {
        ContentValues cv = new ContentValues();
        cv.put("trailId", trailId);
        cv.put("eventId", eventId);
        cv.put("timeStamp", timeStamp);
        cv.put("mime", mime);
        cv.put("longitude", longitude);
        cv.put("latitude", latitude);
        cv.put("media", media);

        // Save our crumb
        SQLiteDatabase localDb = getWritableDatabase();
        localDb.insert(CRUMBS, null, cv);
        localDb.close();
        AddMetadata(eventId, timeStamp, latitude, longitude, trailId);

    }

    public void SaveRestZone(String trailId, String eventId, double latitude, double longitude, String placeId, String timeStamp) {
        ContentValues cv = new ContentValues();
        cv.put("trailId", trailId);
        cv.put("eventId", eventId);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("placeId", placeId);
        cv.put("timeStamp", timeStamp);

        SQLiteDatabase localDb = getWritableDatabase();
        localDb.insert(RESTZONES, null, cv);
        localDb.close();

        AddMetadata(eventId, timeStamp, latitude, longitude, trailId);
    }


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
        this.db = db;

        /*
            Here we build up all our databases.
         */
        db.execSQL("CREATE TABLE users (_id INTEGER PRIMARY KEY AUTOINCREMENT, userid TEXT," +
                "username TEXT, " +
                "age TEXT," +
                "pin TEXT);");

        //Create our linkedTrails table.
        db.execSQL("CREATE TABLE linkedTrails (_id INTEGER PRIMARY KEY AUTOINCREMENT, trailId TEXT);");

        // Table to hold all our indexs for all our users trails
        db.execSQL("CREATE TABLE trailPoints (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trailId TEXT," +
                "userId TEXT," +
                "latitude REAL," +
                "longitude REAL," +
                "trailIndex INTEGER," +
                "timeStamp TEXT," +
                "activity_status," +
                "base_event_id);");

        db.execSQL("CREATE TABLE " + TRAIL_POINTS_INDEX + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, trailid TEXT, trailIndex INTEGER);");

        // Database for Crumbs
        db.execSQL("CREATE TABLE " + CRUMBS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trailId TEXT, " +
                "eventId TEXT, " +
                "timeStamp TEXT, " +
                "mime TEXT," +
                "latitude REAL, " +
                "longitude REAL," +
                "media BLOB);");

        // Database for RestZones.
        db.execSQL("CREATE TABLE " + RESTZONES + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trailId TEXT, " +
                "eventId TEXT, " +
                "timeStamp TEXT, " +
                "latitude REAL, " +
                "longitude REAL);");

    // Database for metadata.
        db.execSQL("CREATE TABLE " + METADATA + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "eventId TEXT, " +
                "trailId TEXT, " +
                "timeStamp TEXT, " +
                "latitude REAL, " +
                "longitude REAL);");

        db.execSQL("CREATE TABLE " + WEATHER + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "weatherId TEXT, " +
                "travelDay TEXT," +
                "temperature TEXT," +
                "weatherDesc TEXT, " +
                "latitude REAL, " +
                "longitude REAL," +
                "city TEXT);");
    }

    public void AddWeather(String weatherId, String travelDay, String weatherDesc, Double latitude, Double longitude, String city, String temperature) {

        ContentValues cv = new ContentValues();
        cv.put("weatherId", weatherId);
        cv.put("travelDay", travelDay);
        cv.put("temperature", temperature);
        cv.put("weatherDesc", weatherDesc);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("city", city);

        SQLiteDatabase localDb = getWritableDatabase();
        localDb.insert(WEATHER, null, cv);
        localDb.close();
    }

    /*
        This is the method we use to add information about the trail to the database. For everything we do (gps, crumb etc)
        there is metadata, and that will be saved in this database.
     */
    public void AddMetadata(String eventId, String timeStamp, double latitude, double longitude, String trailId) {
        ContentValues cv = new ContentValues();
        cv.put("eventId", eventId);
        cv.put("trailId", trailId );
        cv.put("timeStamp", timeStamp);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(METADATA, null, cv);
        db.close();
    }

	public SQLiteDatabase GetDBInstance() {
		return this.db;
	}

    public JSONObject fetchMetadataFromDB(String trailId) {
        JSONObject metadata = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+METADATA+" WHERE trailId ="+trailId+" ORDER BY _id",
                null);
        int numberOfPointsIndex = 0;
        JSONObject lastNode = null;

        while (constantsCursor.moveToNext()) {
            JSONObject metadataNode = new JSONObject();

            //Get all columns
            int id = constantsCursor.getInt(constantsCursor.getColumnIndex("_id"));
            String eventId = constantsCursor.getString(constantsCursor.getColumnIndex("eventId"));
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("longitude"));
            String timeStamp = constantsCursor.getString(constantsCursor.getColumnIndex("timeStamp"));
            int index = constantsCursor.getInt(constantsCursor.getColumnIndex("trailIndex"));
            // Save our single point as a single node, and add it to the overall object that is
            // going to be sent to the server.
            try {
                metadataNode.put("latitude", latitude);
                metadataNode.put("longitude", longitude);
                metadataNode.put("timeStamp", timeStamp);
                metadataNode.put("trailId", trailId);
                metadataNode.put("eventId", eventId );
                metadataNode.put("id", id);
                // Also need to set last node and next node. So :
                numberOfPointsIndex += 1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.i("BC.DatabaseController", "Found Trail Points: " + metadata.toString());
        return null;
    }

    public JSONObject getCrumbsWithoutMedia(String trailId) {
        JSONObject returnObject = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+METADATA+" WHERE trailId ="+trailId+" ORDER BY _id",
                null);
        int numberOfPointsIndex = 0;
        JSONObject lastNode = null;

        while (constantsCursor.moveToNext()) {
            JSONObject metadataNode = new JSONObject();

            //Get all columns
            String eventId = constantsCursor.getString(constantsCursor.getColumnIndex("eventId"));
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("longitude"));
            String timeStamp = constantsCursor.getString(constantsCursor.getColumnIndex("timeStamp"));

          /*  "CREATE TABLE " + CRUMBS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "trailId TEXT, " +
                    "eventId TEXT, " +
                    "timeStamp TEXT, " +
                    "mime TEXT," +
                    "latitude REAL, " +
                    "longitude REAL," +
                    "media BLOB);");*/
        }

        return returnObject;
    }

    public JSONObject GetCrumbsWithMedia(String trailId) {
        JSONObject returnObject = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+METADATA+" WHERE trailId ="+trailId+" ORDER BY _id",
                null);
        int numberOfPointsIndex = 0;
        while (constantsCursor.moveToNext()) {
            JSONObject metadataNode = new JSONObject();

            //Get all columns
            String eventId = constantsCursor.getString(constantsCursor.getColumnIndex("eventId"));

        }

        return returnObject;
    }

}
