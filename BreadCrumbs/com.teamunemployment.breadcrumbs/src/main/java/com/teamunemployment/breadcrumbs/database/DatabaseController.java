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

import com.google.android.gms.maps.model.LatLng;
import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.Models;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.NetworkConnectivityManager;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncPost;
import com.teamunemployment.breadcrumbs.Preferences.Preferences;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.Trails.TrailManagerWorker;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.caching.Utils;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsEncodedPolyline;
import com.teamunemployment.breadcrumbs.data.BreadcrumbsPolyline;
import com.teamunemployment.breadcrumbs.data.TripDetails;
import com.teamunemployment.breadcrumbs.data.TripPath;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/*
 * This is the class used to create and help with local storage. Uses SQLite
 */
public class DatabaseController extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String POLYLINES = "polylines";
	private static final String DATABASE_NAME="users";
    private static final String TRAIL_POINTS_INDEX = "trailIndexDb";
    private static final String TRIP_TABLE = "Trips_db";
    private static final String GPS_TABLE = "GPS_POINTS";
    private static final String TRAIL_SUMMARY = "trailsSummaryDb";
    private static final String CRUMBS = "crumbsDb";
    private static final String RESTZONES = "restZoneDb";
    private static final String METADATA = "metadataDb";
    private static final String WEATHER = "weatherDb";
    private static final String USERS = "users_db";
	public static final String USERID="userid";
	public static final String USERNAME="username";
	public static final String AGE="age";
	public static final String PIN="pin";
    public static final String TRAILID="trailid";
    //public static final String
    private static final String TAG = "DBC";
    private Context mContext;
	private SQLiteDatabase db;

    private PreferencesAPI mPreferencesApi;
	public DatabaseController(Context context) {
		super(context, "users", null, DATABASE_VERSION);
        this.mContext = context;
        mPreferencesApi = new PreferencesAPI(context);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Need to fix this shit
        Log.d(TAG, "Updating from version: " + oldVersion + " to version: "+ newVersion);
        // Upgade from version one to two
        if (oldVersion == 1 && newVersion == 2) {
            String upgradeQuery = "ALTER TABLE " + TRAIL_SUMMARY +" ADD COLUMN PublishPoint INTEGER";
            db.execSQL(upgradeQuery);
            oldVersion = 2;
        }

        if (oldVersion == 2 && newVersion == 3) {
            db.execSQL("CREATE TABLE " + TRIP_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "UserId INTEGER," +
                    "StartDate TEXT," +
                    "CoverPhotoId TEXT," +
                    "Description TEXT," +
                    "Id INTEGER,"+
                    "Views INTEGER);");
            oldVersion = 3;
        }
        if (oldVersion == 3 && newVersion == 4) {
            String upgradeQuery = "ALTER TABLE " + CRUMBS +" ADD COLUMN descPosX REAL";
            db.execSQL(upgradeQuery);
            upgradeQuery = "ALTER TABLE " + CRUMBS +" ADD COLUMN descPosY REAL";
            db.execSQL(upgradeQuery);
        }
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
    public void SaveCrumb(String trailId, String description, String userId, int eventId, double latitude,
                          double longitude, String mime, String timeStamp, String icon, String placeId,
                          String suburb, String city, String country, float descriptionPositionX, float descriptionPositonY) {

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
        cv.put("descPosX", descriptionPositionX);
        cv.put("descPosY", descriptionPositonY);

        SQLiteDatabase localDb = getWritableDatabase();
        long id = localDb.insert(CRUMBS, null, cv);
        localDb.close();
        // If we dont have any cover photo, we want to save one.
        if (mPreferencesApi.GetCurrentTrailCoverPhoto() == null) {
            mPreferencesApi.SetCurrentTrailCoverPhoto(Long.toString(id) + "L");
        }
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
        this.db = db;

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
                "latitude REAL," +
                "longitude REAL," +
                "descPosX REAL," +
                "descPosY REAL);");

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
                "LastUpdate TIMESTAMP," +
                "IsPublished INTEGER,"+
                "PublishPoint INTEGER);");

        db.execSQL("CREATE TABLE " + GPS_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "TrailId TEXT," +
            "CurrentActivity INTEGER," +
            "LastActivity INTEGER," +
            "Latitude REAL," +
            "Longitude REAL," +
            "Granularity INTEGER);");

        db.execSQL("CREATE TABLE " + POLYLINES + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TrailId TEXT," +
                "IsEncoded INTEGER," +
                "BaseLatitude REAL," +
                "BaseLongitude REAL," +
                "HeadLatitude REAL," +
                "HeadLongitude REAL,"+
                "Polyline TEXT);");

        db.execSQL("CREATE TABLE " + USERS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "UserId INTEGER," +
                "Web TEXT," +
                "About TEXT," +
                "Username TEXT," +
                "ProfilePicId INTEGER);");

        db.execSQL("CREATE TABLE " + TRIP_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "UserId INTEGER," +
                "StartDate TEXT," +
                "CoverPhotoId TEXT," +
                "Description TEXT," +
                "Id INTEGER,"+
                "Views INTEGER);");
    }

    public void SaveActivityPoint(int currentActivity, int pastActivity, Double latitude, Double longitude, int granularity) {
        ContentValues cv = new ContentValues();
        int currentTrailId = mPreferencesApi.GetLocalTrailId();
        if (currentTrailId == -1) {
            Log.e(TAG, "Cannot save point as we dont have a trailId.");
            return;
        }

        cv.put("TrailId", Integer.toString(currentTrailId));
        cv.put("CurrentActivity", currentActivity);
        cv.put("LastActivity", pastActivity);
        cv.put("Latitude", latitude);
        cv.put("Longitude", longitude);
        cv.put("Granularity", granularity);

        SQLiteDatabase localDb = getWritableDatabase();
        localDb.insert(GPS_TABLE, null, cv);
        Log.d(TAG, "Successfully saved an activity point to the database. current Activity: " + currentActivity);
    }

    public int SaveTrailStart(String trailId, String startDate) {
        ContentValues cv = new ContentValues();
        int trailIdInt = 1;
        if (trailId != null) {
         trailIdInt = Integer.parseInt(trailId);
            trailIdInt+= 1;
        }

        cv.put("StartDate", startDate);
        cv.put("TrailId", Integer.toString(trailIdInt));
        cv.put("LastUpdate", "");
        cv.put("CoverPhotoId", "0");
        cv.put("TrailName", "");
        cv.put("SavedIndex", 0);
        cv.put("IsPublished", 1); // 0 means published

       // cv.put("EndDate", ""); // End date is unkown - we can change this later.

        SQLiteDatabase localDb = getWritableDatabase();
        long newId = localDb.insert(TRAIL_SUMMARY, null, cv);
        mPreferencesApi.SaveCurrentLocalTrailId((int) newId);
        localDb.close();
        return (int) trailIdInt;
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

    public void updateTrailSavedPoint(String trailId, int count) {

        String query = "UPDATE "+TRAIL_SUMMARY + " SET SavedIndex="+ count+ " WHERE _id = "+trailId;
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // WHY DOES THIS MAKE IT WORK? I FUCKING HATE SQL. If you remove this line, it wont update the row.
        cursor.getCount();

        cursor.close();
    }

    public void updateTrailPublishPoint(String trailId, int index) {

        String query = "UPDATE "+TRAIL_SUMMARY + " SET PublishPoint="+ index+ " WHERE _id = "+trailId;
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // WHY DOES THIS MAKE IT WORK? I FUCKING HATE SQL. If you remove this line, it wont update the row.
        cursor.getCount();

        cursor.close();
    }

    public JSONObject GetTrailSummary(String trailId) {
        JSONObject metadata = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + TRAIL_SUMMARY + " WHERE _id =" + trailId + " ORDER BY _id",
                null);
        while (constantsCursor.moveToNext()) {
            JSONObject trailSummaryNode = new JSONObject();
            //Get all columns
            String startDate = constantsCursor.getString(constantsCursor.getColumnIndex("StartDate"));
            String lastUpdate = constantsCursor.getString(constantsCursor.getColumnIndex("LastUpdate"));
            String trailName = constantsCursor.getString(constantsCursor.getColumnIndex("TrailName"));

            // Save our single point as a single node, and add it to the overall object that is
            // going to be sent to the server.
            try {
                trailSummaryNode.put("TrailId", trailId);
                trailSummaryNode.put("TrailName", trailName);
                trailSummaryNode.put("StartDate", startDate);
                if (!lastUpdate.isEmpty()) {
                    trailSummaryNode.put("LastUpdate", lastUpdate);
                }
             ///   trailSummaryNode.put("IsPublished", isPublished == 0);

                return trailSummaryNode;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void SetLastUpdate(String trailId, String timestamp) {
        String query = "UPDATE "+TRAIL_SUMMARY + " SET LastUpdate='"+ timestamp+ "' WHERE _id = "+trailId;
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // WHY DOES THIS MAKE IT WORK? I FUCKING HATE SQL. If you remove this line, it wont update the row.
        cursor.getCount();

        cursor.close();
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
            String decXPos = constantsCursor.getString(constantsCursor.getColumnIndex("descPosX"));
            String decYPos = constantsCursor.getString(constantsCursor.getColumnIndex("descPosY"));

            try {
                node.put(Models.Crumb.EVENT_ID, eventId);
                node.put(Models.Crumb.LATITUDE, latitude);
                node.put(Models.Crumb.LONGITUDE, longitude);
                node.put(Models.Crumb.TIMESTAMP, timeStamp);
                node.put(Models.Crumb.DESCRIPTION, description);
                node.put(Models.Crumb.USER_ID, userId);
                node.put(Models.Crumb.PLACEID, placeId);
                node.put(Models.Crumb.SUBURB, suburb);
                node.put(Models.Crumb.CITY, city);
                node.put(Models.Crumb.EXTENSION, mime);
                node.put(Models.Crumb.DESC_POS_X, decXPos);
                node.put(Models.Crumb.DESC_POS_Y, decYPos);
                node.put("index", index);
                returnObject.put(Integer.toString(count), node);
                count += 1;
            } catch (JSONException e) {
                Log.e("DBC", "failed to get crumbs with media");
                e.printStackTrace();
            }

        }
        return returnObject;
    }

    public void SaveTrailName(String trailName, int trailId) {

        String query = "UPDATE "+TRAIL_SUMMARY + " SET TrailName='"+trailName + "' WHERE _id = "+Integer.toString(trailId);
        db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // WHY DOES THIS MAKE IT WORK? I FUCKING HATE SQL. If you remove this line, it wont update the row.
        cursor.getCount();
        cursor.close();
        db.close();
    }

    /**
     * Fetch all the Crumb objects (which are jsonobjects)  for a trail, in the order they were saved to the database initally. If you
     * pass null, it will give you our current trails id.
     *
     * @return A JSON object of all our selected trails ids.
     */
    public JSONObject GetAllCrumbs(String trailId) {

        // If we aren't given a trail id we grab our current trail id from the preferences.
        if (trailId == null) {
            trailId = Integer.toString(mPreferencesApi.GetLocalTrailId());
        }
        // Fetch our crumbs.
        return fetchCrumbs(trailId);
    }

    public String GetCurrentTrailName() {
        Cursor cursor = null;
        String trailName = "";
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TRAIL_SUMMARY + " ORDER BY _id", null);
            // WE just want the latest trail.
            if(cursor.getCount() > 0) {
                cursor.moveToLast();
                trailName = cursor.getString(cursor.getColumnIndex("TrailName"));
            }
            return trailName;
        } finally {
            cursor.close();
        }
    }

    private JSONObject fetchCrumbs(String trailId) {
        JSONObject returnObject = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+CRUMBS+" WHERE trailId ="+trailId+" ORDER BY _id", null);
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
            String decXPos = constantsCursor.getString(constantsCursor.getColumnIndex("descPosX"));
            String decYPos = constantsCursor.getString(constantsCursor.getColumnIndex("descPosY"));
            try {

                node.put(Models.Crumb.EVENT_ID, eventId);
                node.put(Models.Crumb.LATITUDE, latitude);
                node.put(Models.Crumb.LONGITUDE, longitude);
                node.put(Models.Crumb.TIMESTAMP, timeStamp);
                node.put(Models.Crumb.DESCRIPTION   , description);
                node.put("UserId", userId);
                node.put("Icon", icon);
                node.put(Models.Crumb.PLACEID, placeId);
                node.put(Models.Crumb.SUBURB, suburb);
                node.put(Models.Crumb.CITY, city);
                node.put(Models.Crumb.COUNTRY, "NZ");
                node.put(Models.Crumb.EXTENSION, mime);
                node.put(Models.Crumb.ID, id);
                node.put(Models.Crumb.DESC_POS_X, decXPos);
                node.put(Models.Crumb.DESC_POS_Y, decYPos);

                returnObject.put(Integer.toString(count), node);
                count += 1;
            } catch (JSONException e) {
                Log.e("DBC", "failed to get crumbs with media");
                e.printStackTrace();
            }
        }
        return returnObject;
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
        SQLiteDatabase localdb = getWritableDatabase();
        int result = localdb.delete(CRUMBS, "eventId = "+ crumbId, null);
        Log.d("DBC", "Requested deletion of crumb with id: " + crumbId + ", deleted " + result + " row(s)");
        localdb.close();
    }

    public Location GetLastSavedLocation() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM "+GPS_TABLE +" ORDER BY _id", null);
        if(cursor.getCount() > 0) {
            cursor.moveToLast();
            Double latitude = cursor.getDouble(cursor.getColumnIndex("Latitude"));
            Double longitude = cursor.getDouble(cursor.getColumnIndex("Longitude"));
            Location location = new Location("MOCK");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            return location;
        }

        return null;


    }

    /**
     * Grab all the gps points from the database that we have saved from a local database.
     * @param localTrailId
     * @return This is the jsonObject of all the activity recordings that we have made.
     */
    public JSONObject GetAllActivityData(int localTrailId) {
        JSONObject metadata = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+GPS_TABLE+" WHERE trailId ="+localTrailId +" ORDER BY _id", null);
        int currentIndex = 0;
        while (constantsCursor.moveToNext()) {
            JSONObject metadataNode = new JSONObject();

            //Get all columns
            int id = constantsCursor.getInt(constantsCursor.getColumnIndex("_id"));
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("Latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("Longitude"));
            int activity = constantsCursor.getInt(constantsCursor.getColumnIndex("LastActivity"));
            int currentActivity = constantsCursor.getInt(constantsCursor.getColumnIndex("CurrentActivity"));
            int granularity = constantsCursor.getInt(constantsCursor.getColumnIndex("Granularity"));

            // Save our single point as a single node, and add it to the overall object that is
            // going to be sent to the server.
            try {
                metadataNode.put(Models.Crumb.LATITUDE, Double.toString(latitude));
                metadataNode.put(Models.Crumb.LONGITUDE, Double.toString(longitude));
                metadataNode.put("LastActivity", Integer.toString(activity));
                metadataNode.put("CurrentActivity", Integer.toString(currentActivity));
                metadataNode.put("Granularity", Integer.toString(granularity));
                // This wraps the object so that the first point is not a gps point, which means that it wont track properly
                currentIndex += 1;
                metadata.put(Integer.toString(currentIndex), metadataNode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return metadata;
    }

    /**
     *
     * Tis is a pretty shitty code but it should be safe. This area needs a rewrite so I'll do that in the future, this is "for now"...
     * Grab all the gps points from the database that we have saved from a local database.
     * @param localTrailId
     * @param index We only are interested in data above the index, as this was the last 'Save' point
     * @return This is the jsonObject of all the activity recordings that we have made.
     */
    public JSONObject  GetAllUnsavedActivityData(int localTrailId, int index) {
        if (index > 0) {
            index -= 1; // This stops us skipping a line. pretty shitty fix but fck it.
        }

        JSONObject metadata = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+GPS_TABLE+" WHERE trailId ="+localTrailId +" ORDER BY _id", null);
        int currentIndex = 0;

        // Need reference index because the server reads from 0, but the current index will only be 0 on first use case.
        int referenceIndex = 0;
        while (constantsCursor.moveToNext()) {
            JSONObject metadataNode = new JSONObject();

            // Data to grab
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("Latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("Longitude"));
            int activity = constantsCursor.getInt(constantsCursor.getColumnIndex("LastActivity"));
            int currentActivity = constantsCursor.getInt(constantsCursor.getColumnIndex("CurrentActivity"));
            int granularity = constantsCursor.getInt(constantsCursor.getColumnIndex("Granularity"));

            if (currentIndex >= index) {
                // Save our single point as a single node, and add it to the overall object that is
                // going to be sent to the server.
                try {
                    metadataNode.put(Models.Crumb.LATITUDE, Double.toString(latitude));
                    metadataNode.put(Models.Crumb.LONGITUDE, Double.toString(longitude));
                    metadataNode.put("LastActivity", Integer.toString(activity));
                    metadataNode.put("CurrentActivity", Integer.toString(currentActivity));
                    metadataNode.put("Granularity", Integer.toString(granularity));
                    // This wraps the object so that the first point is not a gps point, which means that it wont track properly
                    metadata.put(Integer.toString(referenceIndex), metadataNode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                referenceIndex += 1;
            }
            currentIndex += 1;
        }

        updateTrailPublishPoint(Integer.toString(localTrailId), currentIndex-1);

        return metadata;
    }

    /**
     * Grab all the gps points from the database that we have saved from a local database.
     * @param localTrailId
     * @param index We only are interested in data above the index, as this was the last 'Save' point
     * @return This is the jsonObject of all the activity recordings that we have made.
     */
    public JSONObject  GetAllActivityData(int localTrailId, int index) {
        if (index > 0) {
            index -= 1; // This stops us skipping a line. pretty shitty fix but fck it.
        }

        JSONObject metadata = new JSONObject();
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+GPS_TABLE+" WHERE trailId ="+localTrailId +" ORDER BY _id", null);
        int currentIndex = 0;

        // Need reference index because the server reads from 0, but the current index will only be 0 on first use case.
        int referenceIndex = 0;
        while (constantsCursor.moveToNext()) {
            JSONObject metadataNode = new JSONObject();

            // Data to grab
            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("Latitude"));
            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("Longitude"));
            int activity = constantsCursor.getInt(constantsCursor.getColumnIndex("LastActivity"));
            int currentActivity = constantsCursor.getInt(constantsCursor.getColumnIndex("CurrentActivity"));
            int granularity = constantsCursor.getInt(constantsCursor.getColumnIndex("Granularity"));

            if (currentIndex >= index) {
                // Save our single point as a single node, and add it to the overall object that is
                // going to be sent to the server.
                try {
                    metadataNode.put(Models.Crumb.LATITUDE, Double.toString(latitude));
                    metadataNode.put(Models.Crumb.LONGITUDE, Double.toString(longitude));
                    metadataNode.put("LastActivity", Integer.toString(activity));
                    metadataNode.put("CurrentActivity", Integer.toString(currentActivity));
                    metadataNode.put("Granularity", Integer.toString(granularity));
                    // This wraps the object so that the first point is not a gps point, which means that it wont track properly
                    metadata.put(Integer.toString(referenceIndex), metadataNode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                referenceIndex += 1;
            }
            currentIndex += 1;
        }

        updateTrailSavedPoint(Integer.toString(localTrailId), currentIndex-1);

        return metadata;
    }

    private JSONObject fetchCurrentLocationNode() {
        SimpleGps simpleGps = new SimpleGps(mContext);
        Location location = simpleGps.GetInstantLocation();

        if (location != null) {
            JSONObject response = new JSONObject();

            try {
                response.put(Models.Crumb.LATITUDE, location.getLatitude());
                response.put(Models.Crumb.LONGITUDE, location.getLongitude());
                response.put("LastActivity", Integer.toString(7));
                response.put("CurrentActivity", Integer.toString(7));
                response.put("Granularity", Integer.toString(0));
                return response;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    /**
     * Get a {@link TripDetails} object for the corresponding (local) trip id.
     * @param id The id of the trip as it is stored in the database (e.g _id)
     * @return
     */
    public TripDetails GetTripDetails(String id) {
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + TRAIL_SUMMARY + " WHERE _id =" + id + " ORDER BY _id", null);
        constantsCursor.moveToFirst();

        String startDate = constantsCursor.getString(constantsCursor.getColumnIndex("StartDate"));
        String serverId = constantsCursor.getString(constantsCursor.getColumnIndex("TrailId"));
        String trailName = constantsCursor.getString(constantsCursor.getColumnIndex("TrailName"));

        return new TripDetails(serverId,trailName,startDate);
    }

    /**
     * Get the lat long points from the database. Note that this should be used for displaying a trail when no
     * network is available. I Still think this is a bad solution however, so future me should be pretty cautious about using this
     * except for in a super specific use case. (i.e
     * @param id The trail id in the database of the trail that we are getting points for.
     * @return The {@link TripPath} model with the poliyline as an arrayList of latLng points.
     */
    public TripPath GetTripPath(String id) {
//
//        ArrayList<LatLng> points = new ArrayList<>();
//        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM "+GPS_TABLE+" WHERE trailId ="+id +" ORDER BY _id",
//                null);
//        boolean second = false;
//        String polyline = "";
//        while (constantsCursor.moveToNext()) {
//            Double latitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("Latitude"));
//            Double longitude = constantsCursor.getDouble(constantsCursor.getColumnIndex("Longitude"));
//            if (second = false) {
//                polyline = latitude.toString() + "," + longitude.toString();
//                second = true;
//            } else {
//
//            }
//        }
//
//        BreadcrumbsEncodedPolyline polyline = new BreadcrumbsEncodedPolyline(false, points);
//        ArrayList<BreadcrumbsEncodedPolyline> lines = new ArrayList<>();
//        lines.add(polyline);
        //MOT WORKING YET AND ALSO IS NOT USED.
        return new TripPath(null);
    }

    /**
     * Save a {@link BreadcrumbsEncodedPolyline} to our database.
     * @param polyline
     * @param id
     */
    public void SavePolyline(BreadcrumbsEncodedPolyline polyline, String id) {
        int isEncoded = 1;
        if (polyline.isEncoded) {
            isEncoded = 0;
        }
        ContentValues cv = new ContentValues();

        cv.put("TrailId", id);
        cv.put("IsEncoded", isEncoded);
        cv.put("Polyline", polyline.polyline);
        if (polyline.isEncoded) {
            cv.put("BaseLatitude", polyline.baseLatitude);
            cv.put("BaseLongitude", polyline.baseLongitude);
            cv.put("HeadLatitude", polyline.headLatitude);
            cv.put("HeadLongitude", polyline.headLongitude);
        }

        SQLiteDatabase localdb = getWritableDatabase();
        localdb.insert(POLYLINES, null, cv);
        localdb.close();
    }

    public TripPath FetchTripPath(String id) {
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + POLYLINES + " WHERE TrailId =" + id + " ORDER BY _id", null);
        final ArrayList<BreadcrumbsEncodedPolyline> polylines = new ArrayList<>();
        while (constantsCursor.moveToNext()) {
            int isEncoded = constantsCursor.getInt(constantsCursor.getColumnIndex("IsEncoded"));
            String polylineString = constantsCursor.getString(constantsCursor.getColumnIndex("Polyline"));
            if (isEncoded == 0) {
                Double headLat = constantsCursor.getDouble(constantsCursor.getColumnIndex("HeadLatitude"));
                Double headLon = constantsCursor.getDouble(constantsCursor.getColumnIndex("HeadLongitude"));
                Double baseLat = constantsCursor.getDouble(constantsCursor.getColumnIndex("BaseLatitude"));
                Double baseLon = constantsCursor.getDouble(constantsCursor.getColumnIndex("BaseLongitude"));

                // Polyline with the head lat / long
                BreadcrumbsEncodedPolyline polyline = new BreadcrumbsEncodedPolyline( true, polylineString, baseLat, baseLon, headLat, headLon);
                polylines.add(polyline);
            } else {
                BreadcrumbsEncodedPolyline polyline = new BreadcrumbsEncodedPolyline(isEncoded== 0, polylineString);
                polylines.add(polyline);
            }
        }

        return new TripPath(polylines);
    }

    public String GetUserName(long userId) {
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + USERS + " WHERE UserId =" + userId, null);
        if(constantsCursor.moveToFirst()) {
            String userName = constantsCursor.getString(constantsCursor.getColumnIndex("Username"));
            return userName;
        }
        return null;

    }

    public String GetUserAbout(long userId) {
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + USERS + " WHERE UserId =" + userId, null);
        if (constantsCursor.moveToFirst()) {
            String about = constantsCursor.getString(constantsCursor.getColumnIndex("About"));
            return about;
        }
        return null;

    }

    public String getUserWeb(long userId) {
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + USERS + " WHERE UserId =" + userId, null);
        if (constantsCursor.moveToFirst()) {
            String web = constantsCursor.getString(constantsCursor.getColumnIndex("Web"));
            return web;
        }
        return null;
    }

    public String GetUserProfilePic(long userId) {
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + USERS + " WHERE UserId =" + userId, null);
        if(constantsCursor.moveToFirst()) {
            String picId = constantsCursor.getString(constantsCursor.getColumnIndex("ProfilePicId"));
            return picId;
        }
        return null;
    }

    public int GetPublishPoint(int trailId) {
        Cursor constantsCursor=getReadableDatabase().rawQuery("SELECT * FROM " + TRAIL_SUMMARY + " WHERE _id =" + trailId + " ORDER BY _id",
                null);
        if (constantsCursor.moveToFirst()) {
            int count = constantsCursor.getInt(constantsCursor.getColumnIndex("PublishPoint"));
            return count;
        }
        return 0;
    }

    public void SaveUserProfilePicId(long userId, String profilePicId) {

        ContentValues values = new ContentValues();
        values.put("ProfilePicId", Integer.parseInt(profilePicId));
        SQLiteDatabase lcoaldb = getWritableDatabase();
        long result = lcoaldb.update(USERS, values, "UserId" +" = ?",
                new String[] { String.valueOf(userId)});

        if(result <= 0){
            values.put("UserId", (int) userId);
            result = lcoaldb.insert(USERS, null, values);
        }
    }

    public void SaveUserName(long userId, String text) {
        ContentValues values = new ContentValues();
        values.put("Username", text);
        SQLiteDatabase db = getWritableDatabase();
        long result = db.update(USERS, values,"UserId" +" = ?",
                new String[] { String.valueOf(userId)});

        if(result <= 0){
            values.put("UserId", (int) userId);
            result = db.insert(USERS, null, values);
        }
    }

    public void SaveUserWebField(long userId, String text) {
        ContentValues values = new ContentValues();
        values.put("Web", text);
        SQLiteDatabase lcoalDb  = getWritableDatabase();
        long result = lcoalDb.update(USERS, values,"UserId" +" = ?",
                new String[] { String.valueOf(userId)});

        if(result <= 0){
            values.put("UserId", (int) userId);
            result = lcoalDb.insert(USERS, null, values);
        }
    }

    public void SaveUserAboutField(long userId, String text) {
        ContentValues values = new ContentValues();
        values.put("About", text);
        SQLiteDatabase lcoalDb  = getWritableDatabase();
        long result = lcoalDb.update(USERS, values, "UserId" +" = ?",
                new String[] { String.valueOf(userId)});

        if(result <= 0){
            values.put("UserId", (int) userId);
            result = lcoalDb.insert(USERS, null, values);
        }
    }

    public void SaveUserTrips(ArrayList<Trip> trips) {
        // For each trip
        Iterator<Trip> tripIterator = trips.iterator();
        while(tripIterator.hasNext()) {
            Trip trip = tripIterator.next();
            saveUserTrip(trip);
        }
        // try update the id with the values.
        // if it wont update, save it
    }

    private void saveUserTrip(Trip trip) {
        ContentValues values = new ContentValues();
        int userId = Integer.parseInt(trip.getUserId());
        values.put("UserId", userId);
        values.put("StartDate", trip.getStartDate());
        values.put("CoverPhotoId", trip.getCoverPhotoId());
        values.put("Description", trip.getDescription());
        values.put("Id", trip.getId());
        SQLiteDatabase lcoalDb  = getWritableDatabase();
        long result = lcoalDb.update(USERS, values, "UserId" +" = ?",
                new String[] { trip.getUserId()});
        if(result <= 0){
            result = lcoalDb.insert(USERS, null, values);
        }
        lcoalDb.close();
    }
}
