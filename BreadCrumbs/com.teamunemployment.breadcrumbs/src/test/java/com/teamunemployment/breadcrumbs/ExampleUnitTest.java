package com.teamunemployment.breadcrumbs;

import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    private DatabaseController mDbc;
    @Before
    public void setup() {
        Context context = Mockito.mock(Context.class);
        mDbc = new DatabaseController(context);
    }
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(true,true);
    }

    @Test
    public void TestDecoding() {
        List<LatLng> latLongList = PolyUtil.decode("_p~iF~ps|U");
        LatLng listitem = latLongList.get(0);
        Assert.assertTrue(listitem.latitude == 38.5);
        Assert.assertTrue(listitem.longitude == -120.2);
    }




}