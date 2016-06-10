package com.teamunemployment.breadcrumbs._HAX;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Josiah Kendall
 * THis is a pretty hacks class that I use to catch a bug in google maps. Google maps fails when it is
 * involved in a return animation with a transition to another activity. See bug: https://code.google.com/p/gmaps-api-issues/issues/detail?id=7712
 * Has been fixed on marshmallow above but occurs
 */
public class ExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    private final String LINE_SEPARATOR = "\n";
    public static final String LOG_TAG = ExceptionHandler.class.getSimpleName();

    @SuppressWarnings("deprecation")
    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        StringBuilder errorReport = new StringBuilder();
        errorReport.append(stackTrace.toString());

        Log.e(LOG_TAG, errorReport.toString());

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
