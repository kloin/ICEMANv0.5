package com.teamunemployment.breadcrumbs;

import android.content.Context;

import com.teamunemployment.breadcrumbs.Profile.data.Presenter.Presenter;
import com.teamunemployment.breadcrumbs.Profile.data.Presenter.ProfileContract;
import com.teamunemployment.breadcrumbs.Profile.data.model.ProfileModel;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by jek40 on 2/07/2016.
 */
public class ProfileModeltests {


    private ProfileModel model;
    private ProfileContract.ViewContract view;
    private Presenter presenter;

    @Test
    public void TestThatToggleWhenInEditModeSavesTheUserAboutIfItHasChanged() {
        presenter = Mockito.mock(Presenter.class);
        view = Mockito.mock(ProfileContract.ViewContract.class);
        DatabaseController databaseController = Mockito.mock(DatabaseController.class);
        model = new ProfileModel(presenter, null, databaseController, 0);

        boolean result = model.ToggleEditReadOnlyButton();
    }

    @Test
    public void TestThatWeSetCorrectModeWhenSettingUserId() {
        presenter = Mockito.mock(Presenter.class);
        view = Mockito.mock(ProfileContract.ViewContract.class);
        DatabaseController databaseController = Mockito.mock(DatabaseController.class);
        model = new ProfileModel(presenter, null, databaseController, 12);
        model.setUserState(12);
        verify(presenter, times(1)).setUserReadOnly();
        model.setUserState(14);
        verify(presenter, times(1)).setUserEditable();
    }

    @Test
    public void TestThatToggleWhenInEditModeSavesTheUserWebIfItHasChanged() {

    }

    @Test
    public void TestThatToggleWhenInEditModeSavesTheUserProfilePicIfItHasChanged() {

    }

    @Test
    public void TestThatToggleInEditModeDoesntSaveUserAboutIfItHasNotChanged() {

    }

    @Test
    public void TestThatToggleInEditModeDoesntSaveUserWeIfItHasNotChanged() {

    }
}
