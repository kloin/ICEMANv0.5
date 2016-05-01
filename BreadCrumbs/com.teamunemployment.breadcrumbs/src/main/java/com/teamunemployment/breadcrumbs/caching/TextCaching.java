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

/**
 * Written by Josiah Kendall, 2015.
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
