package com.teamunemployment.breadcrumbs;

/**
 * @author Josiah Kendall
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
