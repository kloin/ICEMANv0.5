package com.teamunemployment.breadcrumbs.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncPost;
import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.caching.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/*
 * This is the class used to create and help with local storage. Uses SQLite
 */
public class DatabaseController extends SQLiteOpenHelper {
	private static final String DATABASE_NAME="users";
    private static final String TRAIL_POINTS_INDEX = "trailIndexDb";

    private static final String TRAIL_SUMMARY = "trailsSummaryDb";
    private static final String CRUMBS = "crumbsDb";
    private static final String RESTZONES = "restZoneDb";
    private static final String METADATA = "metadataDb";
    private static final String WEATHER = "weatherDb";

	public static final String USERID="userid";
	public static final String USERNAME="username";
	public static final String AGE="age";
	public static final String PIN="pin";
    public static final String TRAILID="trailid";

    private Context mContext;
	private SQLiteDatabase db;

    private PreferencesAPI mPreferencesApi;
	public DatabaseController(Context context) {
		super(context, "users", null, 1);
        this.mContext = context;
        mPreferencesApi = new PreferencesAPI(context);
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
        SQLiteDatabase localdb = getWritableDatabase();
        localdb.insert("users", null, cv);
        localdb.close();
	}

    public boolean CheckUserExists(String userId) {
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + DATABASE_NAME + " where userid=" + userId, null);
        try {

            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
        }catch (SQLiteException ex) {
            Log.d("DB", "Checking for user failed, most likely due to database not existing.");
            ex.printStackTrace();
        }
        cursor.close();
        return false;
    }

    /*
        a method made to delete all the saved trail points for a trail
     */
    public void clearTrails(String trailId) {
        db = getWritableDatabase();
        db.rawQuery("DELETE from trailPoints", null);
        db.rawQuery("DELETE from " + CRUMBS, null);
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

    @Deprecated
    // Method that checks whether more than "pointCount" points have been saved to the db, and if so
    // we need to update the server to have//
    // *************************************************************
    // NOT USED ANYMORE BECAUSE WE DONT DO LIVE UPDATES. MAY BE USED SOMETIME IN THE FUTURE IF WE DO LIVE UPDATES.
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

    // Store our crumbs and details to the database until we are ready to save.
    public void SaveCrumb(String trailId, String description, String userId, int eventId, double latitude, double longitude, String mime, String timeStamp, String icon, String placeId, String suburb, String city, String country) {

        ContentValues cv = new ContentValues();
        cv.put("trailId", trailId);
        cv.put("eventId", eventId);
        cv.put("description", description);
        cv.put("userId", userId);
        cv.put("icon", icon);
        cv.put("placeId", placeId);
        cv.put("suburb", suburb);
        cv.put("city", city);
        cv.put("country", country);
        cv.put("timeStamp", timeStamp);
        cv.put("mime", mime);
        cv.put("longitude", longitude);
        cv.put("latitude", latitude);

        SQLiteDatabase localDb = getWritableDatabase();
        localDb.insert(CRUMBS, null, cv);
        localDb.close();
        AddMetadata(eventId, timeStamp, latitude, longitude, trailId, TrailManagerWorker.CRUMB, mPreferencesApi.GetTransportMethod());
    }

    public void SaveVideoCrumb(String trailId, String userId, int eventId, double latitude, double longitude, String mime, String timeStamp, String icon,String placeId, String suburb, String city,
                               String country) {
        ContentValues cv = new ContentValues();
        cv.put("trailId", trailId);
        cv.put("eventId", eventId);
        cv.put("description", " ");
        cv.put("userId", userId);
        cv.put("icon", icon);
        cv.put("placeId", placeId);
        cv.put("suburb", suburb);
        cv.put("city", city);
        cv.put("country", country);
        cv.put("timeStamp", timeStamp);
        cv.put("mime", mime);
        cv.put("longitude", longitude);
        cv.put("latitude", latitude);


    }

    private void saveVideoToLocalFile(int eventId, byte[] media) {
        String path = Utils.getExternalCacheDir(mContext) + "/"+eventId+".bcv";

        // Use this to save the file as bcv (breadcrumbsvideo - just so that people cant open it.
        try {
            Utils.SaveVideo(path, media);
        } catch (IOException e) {
            Log.d("BC/DBC", "Saving video failed. Stack trace follows.");
            e.printStackTrace();
        }
    }

    private void saveMediaToLocalFile(int eventId, byte[] media) {
        // Convert to base64

    }

    public void SaveRestZone(String trailId, int eventId, double latitude, double longitude, String placeId, String timeStamp) {
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

        AddMetadata(eventId, timeStamp, latitude, longitude, trailId, TrailManagerWorker.REST_ZONE, mPreferencesApi.GetTransportMethod());
    }

    public JSONObject GetAllRestZonesForATrail(String trailId) {
        JSONObject metadata = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+RESTZONES+" WHERE trailId ="+trailId+" ORDER BY _id",
                null);
        int count = 0;

        while (constantsCursor.moveToNext()) {
            JSONObject metadataNode = new JSONObject();

            //Get all columns
            int id = constantsCursor.getInt(constantsCursor.getColumnIndex("_id"));
            String eventId = constantsCursor.getString(constantsCursor.getColumnIndex("eventId"));
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("longitude"));
            String timeStamp = constantsCursor.getString(constantsCursor.getColumnIndex("timeStamp"));
            //int index = constantsCursor.getInt(constantsCursor.getColumnIndex("trailIndex"));
            // Save our single point as a single node, and add it to the overall object that is
            // going to be sent to the server.
            try {
                metadataNode.put("latitude", latitude);
                metadataNode.put("longitude", longitude);
                metadataNode.put("timeStamp", timeStamp);
                metadataNode.put("trailId", trailId);
                metadataNode.put("eventId", eventId );
                metadataNode.put("id", id);
                metadata.put(Integer.toString(count), metadataNode);
                // Also need to set last node and next node. So :
                count += 1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.i("BC.DatabaseController", "Found Trail Points: " + metadata.toString());
        return metadata;
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
                "activity_status TEXT," +
                "base_event_id INTEGER);");

        db.execSQL("CREATE TABLE " + TRAIL_POINTS_INDEX + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, trailid TEXT, trailIndex INTEGER);");

        // Database for Crumbs
        db.execSQL("CREATE TABLE " + CRUMBS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trailId TEXT, " +
                "eventId INTEGER, " +
                "description TEXT," +
                "userId TEXT," +
                "icon TEXT," +
                "placeId TEXT," +
                "suburb TEXT," +
                "city TEXT," +
                "country TEXT," +
                "timeStamp TEXT, " +
                "mime TEXT," +
                "latitude REAL, " +
                "longitude REAL);");

        // Database for RestZones.
        db.execSQL("CREATE TABLE " + RESTZONES + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trailId TEXT, " +
                "eventId INTEGER, " +
                "timeStamp TEXT, " +
                "placeId TEXT," +
                "latitude REAL, " +
                "longitude REAL);");

    // Database for metadata.
        db.execSQL("CREATE TABLE " + METADATA + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "eventId TEXT, " +
                "trailId TEXT, " +
                "timeStamp TEXT, " +
                "latitude REAL, " +
                "longitude REAL," +
                "transportMethod INTEGER,"+
                "type INTEGER);");

        db.execSQL("CREATE TABLE " + WEATHER + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "weatherId TEXT, " +
                "travelDay TEXT," +
                "temperature TEXT," +
                "weatherDesc TEXT, " +
                "latitude REAL, " +
                "longitude REAL," +
                "city TEXT);");

        db.execSQL("CREATE TABLE " + TRAIL_SUMMARY + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TrailId TEXT," +
                "TrailName TEXT," +
                "SavedIndex INTEGER," +
                "CoverPhotoId TEXT," +
                "StartDate TIMESTAMP," +
                "IsPublished INTEGER);");
    }

    public int SaveTrailStart(String trailId, String startDate) {
        ContentValues cv = new ContentValues();
        cv.put("StartDate", startDate);
        cv.put("TrailId", "0");
        cv.put("CoverPhotoId", "0");
        cv.put("TrailName", "");
        cv.put("SavedIndex", 0);
        cv.put("IsPublished", 1); // 0 means published

       // cv.put("EndDate", ""); // End date is unkown - we can change this later.

        SQLiteDatabase localDb = getWritableDatabase();
        long localId = localDb.insert(TRAIL_SUMMARY, null, cv);
        mPreferencesApi.SaveCurrentLocalTrailId((int) localId);
        localDb.close();
        return (int) localId;
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

        Just a note on the transport method - its the transport method up until you stopped. SO if we start driving,
        and then dont register any changes until an hour later when we are walking, the transport method for that rest stop
        where we are walking will be TrailManagerWorker.DRIVING.
     */
    public void AddMetadata(int eventId, String timeStamp, double latitude, double longitude, String trailId, int type, int transportMethod) {
        ContentValues cv = new ContentValues();
        cv.put("eventId", eventId);
        cv.put("trailId", trailId );
        cv.put("timeStamp", timeStamp);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        cv.put("type", type);
        cv.put("transportMethod", transportMethod);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(METADATA, null, cv);
        db.close();
    }

	public SQLiteDatabase GetDBInstance() {
		return this.db;
	}

    public int GetSavedIndexForTrail(String trailId) {
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + TRAIL_SUMMARY + " WHERE _id =" + trailId + " ORDER BY _id",
                null);
        if (constantsCursor.moveToFirst()) {
            int count = constantsCursor.getInt(constantsCursor.getColumnIndex("SavedIndex"));
            return count;
        }
        return 0;
    }

    /*
        Fetchs the metadata from the database. Metadata is the info that describes events, but no data of
        actual events
     */
    public JSONObject fetchMetadataFromDB(String trailId, boolean incrementFlag) {
        // We generally want to save the first point as a rest zone every time, even if it is not.
        boolean firstPoint = true;
        int currentIndex = mPreferencesApi.GetCurrentIndex();

        JSONObject metadata = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+METADATA+" WHERE trailId ="+trailId+" AND _id >"+ currentIndex + " ORDER BY _id",
                null);
        JSONObject lastNode = null;

        while (constantsCursor.moveToNext()) {
            JSONObject metadataNode = new JSONObject();

            //Get all columns
            int id = constantsCursor.getInt(constantsCursor.getColumnIndex("_id"));
            String eventId = constantsCursor.getString(constantsCursor.getColumnIndex("eventId"));
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("longitude"));
            String timeStamp = constantsCursor.getString(constantsCursor.getColumnIndex("timeStamp"));
            String type = constantsCursor.getString(constantsCursor.getColumnIndex("type"));
            int transportMethod = constantsCursor.getInt(constantsCursor.getColumnIndex("transportMethod"));

            // Save our single point as a single node, and add it to the overall object that is
            // going to be sent to the server.
            try {
                metadataNode.put("latitude", Double.toString(latitude));
                metadataNode.put("longitude", Double.toString(longitude));
                metadataNode.put("timeStamp", timeStamp);
                metadataNode.put("trailId", trailId);
                metadataNode.put("eventId", eventId);
                // This wraps the object so that the first point is not a gps point, which means that it wont track properly
                if (firstPoint) {
                    metadataNode.put("type", "3");
                    firstPoint = false;
                } else {
                    metadataNode.put("type", type);
                }
                metadataNode.put("id", id);
                metadataNode.put("driving_method", Integer.toString(transportMethod));
                metadata.put(Integer.toString(currentIndex), metadataNode);
                lastNode = metadataNode;
                // Also need to set last node and next node. So :
                currentIndex += 1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // This is ugly im sorry im pushed for time
        // We do this here to wrap the trail up, so that we dont get wierd stuff at the end of the trail.
        try {
            int id = lastNode.getInt("id") + 1;
            String latitude = lastNode.getString("latitude");
            String longitude = lastNode.getString("longitude");
            int eventId = Integer.parseInt(lastNode.getString("eventId")) + 1;
            String eventIdString = Integer.toString(eventId);
            String timeStamp = lastNode.getString("timeStamp");
            int transportMethod = lastNode.getInt("driving_method");

            JSONObject finalNode = new JSONObject();
            finalNode.put("latitude", latitude);
            finalNode.put("longitude", longitude);
            finalNode.put("timeStamp", timeStamp);
            finalNode.put("trailId", trailId);
            finalNode.put("eventId", eventIdString);
            finalNode.put("type", "1"); // Trail end is one
            finalNode.put("id", id);
            finalNode.put("driving_method", Integer.toString(transportMethod));
            metadata.put(Integer.toString(currentIndex), finalNode);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException nullPointer) {
            nullPointer.printStackTrace();
        }

        if (incrementFlag) {
            // Update our current "save point" in the database.
            updateTrailSavedPoint(trailId, currentIndex);
        }

        Log.i("BC.DatabaseController", "Found Trail Points: " + metadata.toString());
        return metadata;
    }

    private void updateTrailSavedPoint(String trailId, int count) {
        ContentValues cv = new ContentValues();
        cv.put("SavedIndex", count); //These Fields should be your String values of actual column names

        SQLiteDatabase localDb = getWritableDatabase();
        localDb.update(TRAIL_SUMMARY, cv, "TrailId=" + trailId, null);
        localDb.close();

        mPreferencesApi.SetCurrentIndex(count);
    }

    public JSONObject GetTrailSummary(String trailId) {
        JSONObject metadata = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + TRAIL_SUMMARY + " WHERE _id =" + trailId + " ORDER BY _id",
                null);
        while (constantsCursor.moveToNext()) {
            JSONObject trailSummaryNode = new JSONObject();

            //Get all columns
            String startDate = constantsCursor.getString(constantsCursor.getColumnIndex("StartDate"));
           /// int isPublished = constantsCursor.getInt(constantsCursor.getColumnIndex("IsPublished"));
            String trailName = constantsCursor.getString(constantsCursor.getColumnIndex("TrailName"));
            //int index = constantsCursor.getInt(constantsCursor.getColumnIndex("trailIndex"));
            // Save our single point as a single node, and add it to the overall object that is
            // going to be sent to the server.
            try {
                trailSummaryNode.put("TrailId", trailId);
             ///   trailSummaryNode.put("TrailName", trailName);
                trailSummaryNode.put("StartDate", startDate);
             ///   trailSummaryNode.put("IsPublished", isPublished == 0);
                return trailSummaryNode;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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

    public JSONObject GetCrumbsWithMedia(String trailId, int aboveIndex) {
        JSONObject returnObject = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+CRUMBS+" WHERE trailId ="+trailId+" AND _id > "+aboveIndex+" ORDER BY _id",
                null);
        int count = 0;
        while (constantsCursor.moveToNext()) {
            JSONObject node = new JSONObject();
            //Get all columns
            String eventId = constantsCursor.getString(constantsCursor.getColumnIndex("eventId"));
            String description = constantsCursor.getString(constantsCursor.getColumnIndex("description"));
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("longitude"));
            String timeStamp = constantsCursor.getString(constantsCursor.getColumnIndex("timeStamp"));
            String userId = constantsCursor.getString(constantsCursor.getColumnIndex("userId"));
            String icon = constantsCursor.getString(constantsCursor.getColumnIndex("icon"));
            String placeId = constantsCursor.getString(constantsCursor.getColumnIndex("placeId"));
            String suburb = constantsCursor.getString(constantsCursor.getColumnIndex("suburb"));
            String city = constantsCursor.getString(constantsCursor.getColumnIndex("city"));
            String mime = constantsCursor.getString(constantsCursor.getColumnIndex("mime"));
            int index = constantsCursor.getInt(constantsCursor.getColumnIndex("_id"));

            try {
                TextCaching caching = new TextCaching(mContext);
                String base64String = caching.FetchCachedText(eventId);

                Log.d("Base64", "base64 retrieved");
                node.put("eventId", eventId);
                node.put("latitude", latitude);
                node.put("longitude", longitude);
                node.put("timeStamp", timeStamp);
                node.put("description", description);
                node.put("userId", userId);
                node.put("icon", icon);
                node.put("media", base64String);
                node.put("placeId", placeId);
                node.put("suburb", suburb);
                node.put("city", city);
                node.put("mime", mime);
                node.put("index", index);
                returnObject.put(Integer.toString(count), node);
                count += 1;
            } catch (JSONException e) {
                Log.e("DBC", "failed to get crumbs with media");
                e.printStackTrace();
            }
            int finalIndex = index + count;
            mPreferencesApi.SetLastSavedMediaCrumbIndex(finalIndex);
        }
        return returnObject;
    }

    public JSONObject GetAllCrumbs(String trailId) {
        JSONObject returnObject = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+CRUMBS+" WHERE trailId ="+trailId+" ORDER BY _id",
                null);
        int count = 0;
        while (constantsCursor.moveToNext()) {
            JSONObject node = new JSONObject();

            //Get all columns
            String id = constantsCursor.getString(constantsCursor.getColumnIndex("_id"));
            String eventId = constantsCursor.getString(constantsCursor.getColumnIndex("eventId"));
            String description = constantsCursor.getString(constantsCursor.getColumnIndex("description"));
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("longitude"));
            String timeStamp = constantsCursor.getString(constantsCursor.getColumnIndex("timeStamp"));
            String userId = constantsCursor.getString(constantsCursor.getColumnIndex("userId"));
            String icon = constantsCursor.getString(constantsCursor.getColumnIndex("icon"));
            String placeId = constantsCursor.getString(constantsCursor.getColumnIndex("placeId"));
            String suburb = constantsCursor.getString(constantsCursor.getColumnIndex("suburb"));
            String city = constantsCursor.getString(constantsCursor.getColumnIndex("city"));
            String mime = constantsCursor.getString(constantsCursor.getColumnIndex("mime"));
            try {

                TextCaching textCaching = new TextCaching(mContext);
                String base64String = textCaching.FetchCachedText(eventId);
                Log.d("Base64", "base64 retrieved");
                node.put("eventId", eventId);
                node.put("latitude", latitude);
                node.put("longitude", longitude);
                node.put("timeStamp", timeStamp);
                node.put("description", description);
                node.put("userId", userId);
                node.put("icon", icon);
                node.put("media", base64String);
                node.put("placeId", placeId);
                node.put("suburb", suburb);
                node.put("city", city);
                node.put("mime", mime);
                node.put("id", id);

                returnObject.put(Integer.toString(count), node);
                count += 1;
            } catch (JSONException e) {
                Log.e("DBC", "failed to get crumbs with media");
                e.printStackTrace();
            }
        }
        return returnObject;
    }

    public String GetCurrentTrailName() {
        Cursor cursor = null;
        String trailName = "";
        try{
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TRAIL_SUMMARY + " ORDER BY _id", null);
            // WE just want the latest trail.
            if(cursor.getCount() > 0) {
                cursor.moveToLast();
                trailName = cursor.getString(cursor.getColumnIndex("TrailName"));
            }
            return trailName;
        }finally {
            cursor.close();
        }
    }

    public void SetCurrentTrailName(String trailName) {
        Cursor cursor = null;
        int id = -1;
        try{
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TRAIL_SUMMARY + " ORDER BY _id", null);
            // WE just want the latest trail.
            if(cursor.getCount() > 0) {
                cursor.moveToLast();
               id = cursor.getInt(cursor.getColumnIndex("_id"));
            }
        }finally {
            cursor.close();
        }

        getWritableDatabase().rawQuery("UPDATE " + TRAIL_SUMMARY + " SET TrailName = '" + trailName + "' WHERE _id="+id, null);
    }

    public String GetStartDateForCurrentTrail() {
        Cursor cursor = null;
        String date = "";
        try{
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TRAIL_SUMMARY + " ORDER BY _id", null);
            if(cursor.getCount() > 0) {
                cursor.moveToLast();
                date = cursor.getString(cursor.getColumnIndex("StartDate"));
            }
            return date;
        }finally {
            cursor.close();
        }
    }

    /*
    =================================================================================================
    =+++++++++++++++++++++++++++++ DELETE METHODS ++++++++++++++++++++++++++++++++++++++++++++++++++=
    =================================================================================================
     */

    /*
        Simple Delete sql script to delete from a database
     */
    public void DeleteCrumb(String crumbId) {
        int target = Integer.parseInt(crumbId);
        int result = getWritableDatabase().delete(CRUMBS, "trailId=" + target, null);
        Log.d("DBC", "Requested deletion of crumb with id: " + crumbId + ", deleted " + result + " row(s)");
//getWritableDatabase().rawQuery("DELETE FROM " + CRUMBS + " WHERE eventId=" + crumbId, null)
    }
}
