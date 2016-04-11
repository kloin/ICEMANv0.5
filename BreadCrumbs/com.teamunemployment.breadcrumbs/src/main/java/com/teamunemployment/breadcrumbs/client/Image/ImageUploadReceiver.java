package com.teamunemployment.breadcrumbs.client.Image;

import android.util.Log;

import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

/**
 * Created by jek40 on 9/04/2016.
 */
public class ImageUploadReceiver extends UploadServiceBroadcastReceiver {
        private final static String TAG = "ImageUploadReciever";
                // you can override this progress method if you want to get
                // the completion progress in percent (0 to 100)
                // or if you need to know exactly how many bytes have been transferred
                // override the method below this one
                @Override
                public void onProgress(String uploadId, int progress) {
                    Log.i(TAG, "The progress of the upload with ID "
                            + uploadId + " is: " + progress);
                }

                @Override
                public void onProgress(final String uploadId,
                                       final long uploadedBytes,
                                       final long totalBytes) {
                    Log.i(TAG, "Upload with ID " + uploadId +
                            " uploaded bytes: " + uploadedBytes
                            + ", total: " + totalBytes);
                }

                @Override
                public void onError(String uploadId, Exception exception) {
                    Log.e(TAG, "Error in upload with ID: " + uploadId + ". "
                            + exception.getLocalizedMessage(), exception);
                }

                @Override
                public void onCompleted(String uploadId,
                                        int serverResponseCode,
                                        byte[] serverResponseBody) {
                    // At this point, the serverResponseBody has been completely downloaded
                    // and is cached in memory, so no NetworkOnMainThread could happen here
                    Log.i(TAG, "Upload with ID " + uploadId
                            + " has been completed with HTTP " + serverResponseCode
                            + ". Response from server: "
                            + new String(serverResponseBody));

                    //If your server responds with a JSON, you can parse it
                    //from serverResponseBody using a library
                    //such as org.json (embedded in Android) or Google's gson
                }

                @Override
                public void onCancelled(String uploadId) {
                    Log.i(TAG, "Upload with ID " + uploadId
                            + " has been cancelled by the user");
                }

}
