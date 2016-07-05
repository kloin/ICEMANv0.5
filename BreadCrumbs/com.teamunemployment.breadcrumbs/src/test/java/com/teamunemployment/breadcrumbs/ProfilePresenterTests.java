package com.teamunemployment.breadcrumbs;

import com.teamunemployment.breadcrumbs.Profile.data.Presenter.Presenter;
import com.teamunemployment.breadcrumbs.Profile.data.Presenter.ProfileContract;
import com.teamunemployment.breadcrumbs.Profile.data.model.ProfileModel;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Josiah Kendall.
 */
public class ProfilePresenterTests {

    @Mock
    private ProfileModel model;

    private Presenter presenter;

    @Mock
    private ProfileContract.ViewContract viewContract;
    private void setUpEditToReadOnlyUseCase() {
        viewContract = Mockito.mock(ProfileContract.ViewContract.class);

        model = Mockito.mock(ProfileModel.class);
        DatabaseController databaseController = Mockito.mock(DatabaseController.class);

        presenter = new Presenter(viewContract, databaseController, null, 0);

        when(model.ToggleEditReadOnlyButton()).thenReturn(false);
        presenter.HandleEditToggle();
    }

    private void setUpReadOnlyToEditUseCase() {
        viewContract = Mockito.mock(ProfileContract.ViewContract.class);

        model = Mockito.mock(ProfileModel.class);
        DatabaseController databaseController = Mockito.mock(DatabaseController.class);

        presenter = new Presenter(viewContract, databaseController, null, 0);

        when(model.ToggleEditReadOnlyButton()).thenReturn(true);
        presenter.HandleEditToggle();
    }

    @Test
    public void TestThatToggleWhenInEditModeSetsTheFabBackToWhite() {

        setUpEditToReadOnlyUseCase();

        verify(viewContract, times(1)).setFabAsWhite();
    }



    @Test
    public void TestThatToggleWhenInEditModeSetsTheIconBackToAnEditIcon() {
        setUpEditToReadOnlyUseCase();

        verify(viewContract, times(1)).setFabIconAsEdit();
    }

    @Test
    public void TestThatToggleWhenInEditModeSetsTheAboutBackToReadOnlyState() {
        setUpEditToReadOnlyUseCase();

        verify(viewContract, times(1)).setUserAboutAsReadOnly();
    }

    @Test
    public void TestThatToggleWhenInEditModeSetsTheWebBackToReadOnlyState() {
        setUpEditToReadOnlyUseCase();
        verify(viewContract, times(1)).setUserWebsiteAsReadOnly();
    }

    @Test
    public void TestThatTripCountGetsSetByLoadTripsMethodCall() {

    }

    @Test
    public void TestThatToggleInReadOnlyModeSetsClickPromptAsVisible() {
        setUpReadOnlyToEditUseCase();
        verify(viewContract, times(1)).setProfileClickPromptAsVisible();
    }

    @Test
    public void TestThatToggleInReadOnlyModeSetsFabToBeGreen() {
        setUpReadOnlyToEditUseCase();
        verify(viewContract, times(1)).setFabAsGreen();
    }

    @Test
    public void TestThatToggleInReadOnlyModeSetsAboutSectionAsEditable() {
        setUpReadOnlyToEditUseCase();
        verify(viewContract, times(1)).setUserAboutAsEditable();
    }

    @Test
    public void TestThatToggleInReadOnlyModeSetsWebsiteAsEditable() {
        setUpReadOnlyToEditUseCase();
        verify(viewContract, times(1)).setUserWebsiteAsEditable();
    }
}
