package com.teamunemployment.breadcrumbs;

/**
 * Created by jek40 on 22/06/2016.
 */
public class BasicThreadPoolTest {

    private static BasicThreadPoolTest myThreadPoolInstance;

    private BasicThreadPoolTest() {
        myThreadPoolInstance = new BasicThreadPoolTest();
    }

    // Singleton access point
    public static BasicThreadPoolTest GetInstance() {
        if (myThreadPoolInstance == null) {
            myThreadPoolInstance = new BasicThreadPoolTest();
        }

        return myThreadPoolInstance;
    }


}
