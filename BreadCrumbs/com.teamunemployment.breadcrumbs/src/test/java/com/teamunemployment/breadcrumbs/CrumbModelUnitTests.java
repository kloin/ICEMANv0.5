package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.telecom.Call;
import android.test.AndroidTestCase;
import android.view.View;

import com.teamunemployment.breadcrumbs.Location.SimpleGps;
import com.teamunemployment.breadcrumbs.Map.MapContract;
import com.teamunemployment.breadcrumbs.SaveCrumb.CrumbToSaveDetails;
import com.teamunemployment.breadcrumbs.SaveCrumb.SaveCrumbActivityContract;
import com.teamunemployment.breadcrumbs.SaveCrumb.SaveCrumbModel;
import com.teamunemployment.breadcrumbs.SaveCrumb.SaveCrumbPresenter;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Locale;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jek40 on 21/06/2016.
 */
public class CrumbModelUnitTests{

    SaveCrumbModel model;

    @Mock
    SimpleGps simpleGps;

    @Mock
    SaveCrumbPresenter presenter;


    private void setUp() {
        simpleGps = Mockito.mock(SimpleGps.class);
        presenter = Mockito.mock(SaveCrumbPresenter.class);
        Mockito.doReturn(getSimpleMockLocation()).when(simpleGps).GetInstantLocation();
        model = new SaveCrumbModel(simpleGps, new CrumbToSaveDetails(false, "test_123", true), presenter);
    }

    private Location getSimpleMockLocation() {
        Location location = Mockito.mock(Location.class);
        when(location.toString()).thenReturn("BC_MOCK");
        when(location.getProvider()).thenReturn("BC_MOCK");
        return location;
    }


    @Test
    public void TestThatSettingUpSaveCrumbScreenDoesNotCrashIfLocationWasNull() {
        simpleGps = Mockito.mock(SimpleGps.class);
        presenter = Mockito.mock(SaveCrumbPresenter.class);

        // Return a null when we are saving a crumb.
        Mockito.doReturn(null).when(simpleGps).GetInstantLocation();
        model = new SaveCrumbModel(simpleGps, new CrumbToSaveDetails(false, "TEST", true), presenter);
        model.load(null);
    }

    @Test
    public void TestThatSavingCrumbFetchesNewLocationIfAgeOfInstantLocationIsGreaterThanOneMinutes() {
        setUp();
        Mockito.doReturn(getOldLocation()).when(simpleGps).GetInstantLocation();
        model.load(null);
        verify(simpleGps, times(1)).FetchFineLocation(any(SimpleGps.Callback.class));
    }

    @Test
    public void TestValidatingLocationWorks() {
        Location location = getOldLocation();
        setUp();
        Assert.assertFalse(model.validateLocation(location));
    }

    //
    private Location getOldLocation() {
        Location location = Mockito.mock (Location.class);
        location.setTime(System.currentTimeMillis() - 509000);
        return location;
    }

    @Test
    public void TestThatLoadingCrumbModelDoesNotFailIfWeHaveNoAddress() {

    }

    @Test
    public void TestThatCreatingTheCrumbModelDoesNotFailIfOurAddressReturnsANullWhenFetchingLocalties() {
        simpleGps = Mockito.mock(SimpleGps.class);
        presenter = Mockito.mock(SaveCrumbPresenter.class);

        // Return a null when we are saving a crumb.
        Mockito.doReturn(getSimpleMockLocation()).when(simpleGps).GetInstantLocation();
        Mockito.doReturn(null).when(simpleGps).getSuburb(new Address(new Locale("")));
        model = new SaveCrumbModel(simpleGps, new CrumbToSaveDetails(false, "TEST", true), presenter);
        model.load(null);
    }

    // Simple mock address to mock the scenario when our locations address cannot find a suburb.
    private Address getMockAddress() {
        Address address = Mockito.mock(Address.class);
        Mockito.doReturn(null).when(address).getSubLocality();
        return address;
    }


    @Test
    public void TestThatWeCanAdd140CharactersToDescription() {
        setUp();
        String onFortyChars = "this is 140 characters. This is a reasonable amount of letters I dont know if we want to be able to add this many. This is now 140 characte";
        String description = model.getDescription();
        Assert.assertTrue(description == null);
        model.setDescription(onFortyChars);
        description = model.getDescription();
        Assert.assertTrue(description.equals(onFortyChars));
    }

    @Test
    public void TestThatWeCannotAddMoreThan140CharactersToADescription() {
        setUp();
        String onFortyChars = "this is 140 characters. This is a reasonable amount of letters I dont know if we want to be able to add this many. This is now 140 characte";
        String description = model.getDescription();
        Assert.assertTrue(description == null);
        model.setDescription(onFortyChars);
        model.setDescription(onFortyChars + "rs.");

        // When we get the description is should not have set the text because it is too long.
        description = model.getDescription();
        Assert.assertTrue(description.equals(onFortyChars));
    }

    @Test
    public void TestThatMessageIsShownIfWeAttemptToAddMoreThan140Characters() {
        setUp();
        String onFortyChars = "this is 140 characters. This is a reasonable amount of letters I dont know if we want to be able to add this many. This is now 140 characte";
        String description = model.getDescription();
        Assert.assertTrue(description == null);
        model.setDescription(onFortyChars);
        model.setDescription(onFortyChars + "rs.");

        verify(presenter, times(1)).showMessage("Descriptions are limited to 140 characters");
    }


    @Test
    public void EnsureThatWeCanShowTextViewWhenWeClickOnImageAndEditTextIsVisible() {

    }

    // For the fragment case
    @Test
    public void TestWeShowPromptWhenDeleting() {

    }

    // For the fragmet case
    @Test
    public void TestWeDeleteFromLocalDatabaseWhenDeleting() {

    }

    // For the fragment case
    @Test
    public void TestWeDeleteFromServerDatabaseWhenDeletingSavedCrumb() {

    }

    /**
     * Test that when we fetch a location
     */
    @Test
    public void TestThatWeLoadLocationOnLoad() {
        setUp();
        model.load(null);
        verify(simpleGps, times(1)).GetInstantLocation();
    }

    /**
     * Test that when we call loadLocation we set
     */
    @Test
    public void TestThatLoadLocationSetsLocationName() {
        simpleGps = Mockito.mock(SimpleGps.class);
        presenter = Mockito.mock(SaveCrumbPresenter.class);
        Mockito.doReturn(getSimpleMockLocation()).when(simpleGps).GetInstantLocation();

        model = new SaveCrumbModel(simpleGps, Mockito.mock(CrumbToSaveDetails.class), presenter);
        model.load(null);
        verify(presenter, times(1)).setLocation("MOCK");
    }


    @Test
    public void TestThatWeCanSetBitmapWhenLoading() {
        simpleGps = Mockito.mock(SimpleGps.class);
        presenter = Mockito.mock(SaveCrumbPresenter.class);
        Mockito.doReturn(getSimpleMockLocation()).when(simpleGps).GetInstantLocation();
        model = new SaveCrumbModel(simpleGps, Mockito.mock(CrumbToSaveDetails.class), presenter);

        model.load(null);

        verify(presenter, times(1)).setBitmapDisplay(null);
    }

    private Bitmap getMockBitmap() {
        return Mockito.mock(Bitmap.class);
    }

    @Test
    public void TestThatSetDescriptionDoesNotBreakWhenEmpty() {
        model = getModel();
        model.setDescription("");
        Assert.assertTrue(model.getDescription().equals(""));

        model.setDescription(null);
        Assert.assertTrue(model.getDescription() == null);
    }

    @Test
    public void TestThatErrorToastShowWhenBitmapNull() {

    }

    @Test
    public void TestThatLocationDoesntBreakWWhenNull() {

        model.load(null);
        verify(presenter, times(1)).setLocation("");
        verify(presenter, times(1)).showMessage("Failed to find location");
    }

    @Test
    public void TestThatSaveCrumbGetsCalledWhenSaving() {

    }

    private SaveCrumbModel getModel() {
        simpleGps = Mockito.mock(SimpleGps.class);
        presenter = Mockito.mock(SaveCrumbPresenter.class);
        Mockito.doReturn(null).when(simpleGps).GetInstantLocation();

        model = new SaveCrumbModel(simpleGps, Mockito.mock(CrumbToSaveDetails.class), presenter);
        return model;
    }
}
