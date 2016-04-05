package com.teamunemployment.breadcrumbs;

import android.content.ContentValues;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.test.suitebuilder.annotation.SmallTest;

import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Created by jek40 on 29/03/2016.
 */
//@RunWith(AndroidJUnit4.class)
//@SmallTest
//public class DatabaseTest extends AndroidTestCase {
//    private DatabaseController db;
//
//    @Override
//    public void setUp() throws Exception {
//        super.setUp();
//        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
//        db = new DatabaseController(context);
//    }
//
//    @Override
//    public void tearDown() throws Exception {
//        db.close();
//        super.tearDown();
//    }
//
//    //According to Zainodis annotation only for legacy and not valid with gradle>1.1:
//    //@Test
//    public void testAddEntry(){
//        // Here i have my new database which is not connected to the standard database of the App
//        db.SaveUser("0", "Josiah", 24, "0123", null);
//        db.CheckUserExists("0");
//    }
//}
