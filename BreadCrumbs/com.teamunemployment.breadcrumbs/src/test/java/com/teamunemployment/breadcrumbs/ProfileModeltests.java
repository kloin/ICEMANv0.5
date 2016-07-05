package com.teamunemployment.breadcrumbs;

import android.content.Context;

import com.teamunemployment.breadcrumbs.Profile.data.Presenter.Presenter;
import com.teamunemployment.breadcrumbs.Profile.data.Presenter.ProfileContract;
import com.teamunemployment.breadcrumbs.Profile.data.model.ProfileModel;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.junit.Test;
import org.mockito.Mockito;

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
