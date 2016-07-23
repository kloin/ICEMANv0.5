package com.teamunemployment.breadcrumbs;

import android.view.View;
import android.widget.LinearLayout;

import com.teamunemployment.breadcrumbs.Explore.Data.ExploreLocalRepository;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreRemoteRepository;
import com.teamunemployment.breadcrumbs.Explore.ExploreCardModel;
import com.teamunemployment.breadcrumbs.Explore.Model;
import com.teamunemployment.breadcrumbs.Explore.RecyclerViewAdapterContract;
import com.teamunemployment.breadcrumbs.Profile.data.Presenter.Presenter;
import com.teamunemployment.breadcrumbs.Profile.data.Presenter.ProfileContract;
import com.teamunemployment.breadcrumbs.Profile.data.model.ProfileModel;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Josiah Kendall
 */
public class ExploreModelTests {

    @Test
    public void TestThatWeCanDisplayTripFromLocalAndNotRemoteWhenBothExistAndAreTheSame() {
        ExploreRemoteRepository remoteRepository = Mockito.mock(ExploreRemoteRepository.class);
        ExploreLocalRepository localRepository = Mockito.mock(ExploreLocalRepository.class);

        RecyclerViewAdapterContract view = Mockito.mock(RecyclerViewAdapterContract.class);
        Model model = new Model(localRepository, remoteRepository);
        when(localRepository.LoadTrip(any(Long.class))).thenReturn(getTrip1());
        when(remoteRepository.LoadTrip(any(Long.class))).thenReturn(getTrip1());
        ExploreCardModel exploreCardModel = new ExploreCardModel(ExploreCardModel.FOLLOWING_CARD, "1");
        model.LoadSingleTrip(exploreCardModel, Mockito.mock(LinearLayout.class), view);

        verify(view, times(1)).bindTripToCard(any(Trip.class), any(View.class), any(ExploreCardModel.class));
    }

    private Trip getTrip1() {
        Trip trip = new Trip();
        trip.setDescription("test");
        trip.setViews("0");
        trip.setDistance("0");
        trip.setId("0");
        trip.setCoverPhotoId("01");
        trip.setTrailName("test name");
        trip.setStartDate("now");

        return trip;
    }
    private Trip getTrip2() {
        Trip trip = new Trip();
        trip.setDescription("test2");
        trip.setViews("1");
        trip.setDistance("1");
        trip.setId("1");
        trip.setCoverPhotoId("1");
        trip.setTrailName("test name 3");
        trip.setStartDate("now");

        return trip;
    }


    @Test
    public void TestThatWeCanDisplayTripFromLocalAndThenRemoteWhenTheyDiffer() {
        ExploreRemoteRepository remoteRepository = Mockito.mock(ExploreRemoteRepository.class);
        ExploreLocalRepository localRepository = Mockito.mock(ExploreLocalRepository.class);

        RecyclerViewAdapterContract view = Mockito.mock(RecyclerViewAdapterContract.class);
        Model model = new Model(localRepository, remoteRepository);

        ExploreCardModel exploreCardModel = new ExploreCardModel(ExploreCardModel.FOLLOWING_CARD, "1");
        when(localRepository.LoadTrip(any(Long.class))).thenReturn(getTrip1());
        when(remoteRepository.LoadTrip(any(Long.class))).thenReturn(getTrip2());
        model.LoadSingleTrip(exploreCardModel, null, view);

        verify(view, times(2)).bindTripToCard(any(Trip.class), any(View.class), any(ExploreCardModel.class));
    }

    @Test
    public void TestThatLoadAndDisplayTripWorksFineIfWeDontHaveLocal() {
        ExploreRemoteRepository remoteRepository = Mockito.mock(ExploreRemoteRepository.class);
        ExploreLocalRepository localRepository = Mockito.mock(ExploreLocalRepository.class);

        RecyclerViewAdapterContract view = Mockito.mock(RecyclerViewAdapterContract.class);
        Model model = new Model(localRepository, remoteRepository);

        ExploreCardModel exploreCardModel = new ExploreCardModel(ExploreCardModel.FOLLOWING_CARD, "1");
        when(localRepository.LoadTrip(any(Long.class))).thenReturn(null);
        when(remoteRepository.LoadTrip(any(Long.class))).thenReturn(getTrip2());
        model.LoadSingleTrip(exploreCardModel, null, view);

        verify(view, times(1)).bindTripToCard(any(Trip.class), any(View.class), any(ExploreCardModel.class));
    }

    @Test
    public void TestThatSaveTripToLocalGetsCalledIfNoTripExists() {
        ExploreRemoteRepository remoteRepository = Mockito.mock(ExploreRemoteRepository.class);
        ExploreLocalRepository localRepository = Mockito.mock(ExploreLocalRepository.class);

        RecyclerViewAdapterContract view = Mockito.mock(RecyclerViewAdapterContract.class);
        Model model = new Model(localRepository, remoteRepository);

        when(localRepository.LoadTrip(any(Long.class))).thenReturn(null);
        when(remoteRepository.LoadTrip(any(Long.class))).thenReturn(getTrip2());

        ExploreCardModel exploreCardModel = new ExploreCardModel(ExploreCardModel.FOLLOWING_CARD, "1");
        model.LoadSingleTrip(exploreCardModel, null, view);

        verify(localRepository, times(1)).SaveTrip(any(Trip.class), any(Long.class));
    }

    @Test
    public void TestThatSaveTripToLocalGetsCalledIfTripExistsLocallyButDiffersFromTheRemoteVersion() {
        ExploreRemoteRepository remoteRepository = Mockito.mock(ExploreRemoteRepository.class);
        ExploreLocalRepository localRepository = Mockito.mock(ExploreLocalRepository.class);

        RecyclerViewAdapterContract view = Mockito.mock(RecyclerViewAdapterContract.class);
        Model model = new Model(localRepository, remoteRepository);

        ExploreCardModel exploreCardModel = new ExploreCardModel(ExploreCardModel.FOLLOWING_CARD, "1");

        when(localRepository.LoadTrip(any(Long.class))).thenReturn(getTrip1());
        when(remoteRepository.LoadTrip(any(Long.class))).thenReturn(getTrip2());
        model.LoadSingleTrip(exploreCardModel, null, view);
        verify(localRepository, times(1)).SaveTrip(any(Trip.class), any(Long.class));
    }

    @Test
    public void TestThatSaveTripToLocalIsNotCalledIfItExistsAndBothAreTheSame() {
        ExploreRemoteRepository remoteRepository = Mockito.mock(ExploreRemoteRepository.class);
        ExploreLocalRepository localRepository = Mockito.mock(ExploreLocalRepository.class);

        RecyclerViewAdapterContract view = Mockito.mock(RecyclerViewAdapterContract.class);
        Model model = new Model(localRepository, remoteRepository);

        ExploreCardModel exploreCardModel = new ExploreCardModel(ExploreCardModel.FOLLOWING_CARD, "1");
        when(localRepository.LoadTrip(any(Long.class))).thenReturn(getTrip1());
        when(remoteRepository.LoadTrip(any(Long.class))).thenReturn(getTrip1());

        model.LoadSingleTrip(exploreCardModel, null, view);
        verify(localRepository, times(0)).SaveTrip(any(Trip.class), any(Long.class));
    }
}
