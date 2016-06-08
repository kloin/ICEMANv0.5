package com.teamunemployment.breadcrumbs.caching;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * @author Josiah Kendall
 */
public class TextCaching {

    private Context context;

    // Constructor.
    public TextCaching(Context context) {
        this.context = context;
    }

    // Write text to cache file with a file name as the Key.
    public void CacheText(String key, String textToCache) {
        String path = Utils.getExternalCacheDir(context) + "/"+key+".txt";
        try {
            if (textToCache != null && textToCache.length() > 0) {
                final BufferedWriter cacheWriter = new BufferedWriter(new FileWriter(path), textToCache.length());
                cacheWriter.write(textToCache);
                cacheWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CACHE", "Failed to cache text : " + textToCache + ". Refer to CacheText(key, text) method in TextCaching");
        }
    }

    /**
     * Fetch an object that contains the string result of the cache key, and a flag for whether we
     * should update our cache (based on the cache age).
     * @param key The key for our cache object.
     * @return The string result and a flag for whether or not we should update the cache.
     */
    public CacheResult FetchCacheObject(String key) {
        String result = FetchCachedText(key);

        // Ten minutes in millis
        long tenMinutes = 600000;
        boolean updateRequired = updateRequired(key, tenMinutes);

        return new CacheResult(result, updateRequired);
    }

    // Simple method to return whether or not a cache is older than a given age.
    private boolean updateRequired(String key, long ageInMillis) {
        String path = Utils.getExternalCacheDir(context) + "/"+key+".txt";
        File file = new File(path);

        // If our file doesnt exist, we should be fetching it.
        if (!file.exists()) {
            return true;
        }
        // Create age limit
        long limit = System.currentTimeMillis() - ageInMillis;

        // Create an age object for the last modified time of our file.
        Date date = new Date(file.lastModified());
        long lastModified = date.getTime();

        // If last modified is less than the limit, it means that it is older than our age limit.
        return lastModified < limit;
    }

    // Fetch text/JSON that we have cached.
    public String FetchCachedText(String key) {
        String cachedText = "";
        String path = Utils.getExternalCacheDir(context) + "/"+key+".txt";
        try {
            // Build up our input stream, if we have data then read it into our string
            FileInputStream fis = new FileInputStream (new File(path));  // 2nd line
            if ( fis != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                fis.close();
                cachedText = stringBuilder.toString();
                return cachedText;
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        // In this case we will need to send a network request to the server to get the data.
        return null;
    }

    public void DeleteCacheFile(String id) {
        String path = Utils.getExternalCacheDir(context) + "/"+id+".txt";
        File fdelete = new File(path);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + path);
            } else {
                System.out.println("file not Deleted :" + path);
            }
        }
    }
}
