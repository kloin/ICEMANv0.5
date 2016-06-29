package com.teamunemployment.breadcrumbs;

import android.location.Address;
import android.location.Location;

import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.SaveCrumb.CrumbToSaveDetails;
import com.teamunemployment.breadcrumbs.SaveCrumb.SaveCrumbModel;
import com.teamunemployment.breadcrumbs.SaveCrumb.SaveCrumbPresenter;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

/**
 * Created by jek40 on 27/06/2016.
 */
public class SimpleGPSUnitTests {

    @Mock
    private SimpleGps simpleGps;

    @Mock
    Address address;
    private void setUp() {
        simpleGps = Mockito.mock(SimpleGps.class);
        Mockito.doReturn(getSimpleMockLocation()).when(simpleGps).GetInstantLocation();
    }

    private Location getSimpleMockLocation() {
        Location location = Mockito.mock(Location.class);
        when(location.toString()).thenReturn("BC_MOCK");
        when(location.getProvider()).thenReturn("BC_MOCK");
        return location;
    }

    @Test
    public void TestThatFetchingPlaceNameDoesntFailWhenSuburbIsNull() {
        setUp();
        String mockCity = "Auckland";
        Address address1 = Mockito.mock(Address.class);
        when(address1.getSubLocality()).thenReturn(mockCity);
        String result = simpleGps.FetchPlaceNameForLocation(address1);
        Assert.assertTrue(result.equals(mockCity));
    }
}
