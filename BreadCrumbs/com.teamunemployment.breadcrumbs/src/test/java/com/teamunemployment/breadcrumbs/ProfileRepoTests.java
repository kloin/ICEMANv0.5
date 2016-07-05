package com.teamunemployment.breadcrumbs;

import com.teamunemployment.breadcrumbs.Profile.data.LocalProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.ProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.RemoteProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.RepositoryResponseContract;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Josiah Kendall
 *
 * Test class to test the behaviour of our profile repo.
 */
public class ProfileRepoTests {

    @Test
    public void TestThatICanGetUserNameFromDatabaseWhenIHaveNothingLocallySaved() {
        // Our mock contract.
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn(null).when(localProfileRepository).getUserName(1);

        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Test").when(remoteProfileRepository).getUserName(1);

        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getUserName(1, contract);

        // We should call getUserName on the local repo once, and the remote repo once.
        verify(remoteProfileRepository, times(1)).getUserName(1);
        verify(localProfileRepository, times(1)).getUserName(1);

        // Make sure we only set the username once - when the remote request is done.
        verify(contract, times(1)).setUserName("Test");

        // Test that we save the name back to the correct user in the db
        verify(localProfileRepository, times(1)).saveUserName("Test", 1);
    }

    @Test
    public void TestThatWeCanUpdateOurModelDataIfWeHaveDifferentDataOnTheServerThanTheDataThatWeHaveLocally() {
        // Our mock contract.
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn("Test1").when(localProfileRepository).getUserName(1);

        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Test2").when(remoteProfileRepository).getUserName(1);

        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getUserName(1, contract);

        verify(contract, times(2)).setUserName(any(String.class));
        verify(contract, times(1)).setUserName("Test1");
        verify(contract, times(1)).setUserName("Test2");

        verify(localProfileRepository, times(1)).getUserName(1);
        verify(remoteProfileRepository, times(1)).getUserName(1);
    }

    @Test
    public void TestThatWhenWeHaveTheSameDataWeDontBotherUpdatingIt() {
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn("Test1").when(localProfileRepository).getUserName(1);

        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Test1").when(remoteProfileRepository).getUserName(1);

        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getUserName(1, contract);

        verify(contract, times(1)).setUserName(any(String.class));
        verify(contract, times(1)).setUserName("Test1");


        verify(localProfileRepository, times(1)).getUserName(1);
        verify(remoteProfileRepository, times(1)).getUserName(1);
    }

    @Test
    public void TestThatWeCanFetchUserAboutWhenWeFailToFindAnyDataLocally() {
        // Our mock contract.
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        // Local repo cant find it
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn(null).when(localProfileRepository).getUserAbout(1);

        // Remote repo returns about
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Hi Im Joe").when(remoteProfileRepository).getUserAbout(1);

        // Do method now that mock rules are set
        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getUserAbout(1, contract);

        // Should call for service once each repo
        verify(localProfileRepository, times(1)).getUserAbout(1);
        verify(remoteProfileRepository, times(1)).getUserAbout(1);

        //Should try and set the repo exaclly once.
        verify(contract, times(1)).setAbout("Hi Im Joe");
    }

    @Test
    public void TestThatWeCanUpdateOurUserAboutModelDataIfWeHaveDifferentDataOnTheServerThanTheDataThatWeHaveLocally() {
        // Our mock contract.
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn("Test1").when(localProfileRepository).getUserAbout(1);

        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Test2").when(remoteProfileRepository).getUserAbout(1);

        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getUserAbout(1,contract);

        verify(contract, times(2)).setAbout(any(String.class));
        verify(contract, times(1)).setAbout("Test1");
        verify(contract, times(1)).setAbout("Test2");

        verify(localProfileRepository, times(1)).getUserAbout(1);
        verify(localProfileRepository, times(1)).saveUserAbout("Test2", 1);
        verify(remoteProfileRepository, times(1)).getUserAbout(1);
    }

    @Test
    public void TestThatWhenWeHaveTheSameUserAboutDataWeDontBotherUpdatingIt() {
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn("Test1").when(localProfileRepository).getUserAbout(1);

        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Test1").when(remoteProfileRepository).getUserAbout(1);

        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getUserAbout(1, contract);

        verify(contract, times(1)).setAbout(any(String.class));
        verify(contract, times(1)).setAbout("Test1");


        verify(localProfileRepository, times(1)).getUserAbout(1);
        verify(remoteProfileRepository, times(1)).getUserAbout(1);
    }

    @Test
    public void TestThatWeCanFetchUserWebWhenWeFailToFindAnyDataLocally() {
        // Our mock contract.
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        // Local repo cant find it
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn(null).when(localProfileRepository).getUserWeb(1);

        // Remote repo returns about
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Hi Im Joe").when(remoteProfileRepository).getUserWeb(1);

        // Do method now that mock rules are set
        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getUserWeb(1, contract);

        // Should call for service once each repo
        verify(localProfileRepository, times(1)).getUserWeb(1);
        verify(remoteProfileRepository, times(1)).getUserWeb(1);

        //Should try and set the repo exaclly once.
        verify(contract, times(1)).setUserWeb("Hi Im Joe");
    }

    @Test
    public void TestThatWeCanUpdateOurUserWebModelDataIfWeHaveDifferentDataOnTheServerThanTheDataThatWeHaveLocally() {
        // Our mock contract.
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn("Test1").when(localProfileRepository).getUserWeb(1);

        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Test2").when(remoteProfileRepository).getUserWeb(1);

        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getUserWeb(1, contract);

        verify(contract, times(2)).setUserWeb(any(String.class));
        verify(contract, times(1)).setUserWeb("Test1");
        verify(contract, times(1)).setUserWeb("Test2");

        verify(localProfileRepository, times(1)).getUserWeb(1);
        verify(localProfileRepository, times(1)).saveUserWeb("Test2", 1);
        verify(remoteProfileRepository, times(1)).getUserWeb(1);
    }

    @Test
    public void TestThatWhenWeHaveTheSameUserWebDataWeDontBotherUpdatingIt() {
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn("Test1").when(localProfileRepository).getUserWeb(1);

        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Test1").when(remoteProfileRepository).getUserWeb(1);

        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getUserWeb(1, contract);

        verify(contract, times(1)).setUserWeb(any(String.class));
        verify(contract, times(1)).setUserWeb("Test1");


        verify(localProfileRepository, times(1)).getUserWeb(1);
        verify(remoteProfileRepository, times(1)).getUserWeb(1);
    }

    @Test
    public void TestThatWeCanFetchUserProfileIdWhenWeFailToFindAnyDataLocally() {
        // Our mock contract.
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        // Local repo cant find it
        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn(null).when(localProfileRepository).getProfilePictureId(1);

        // Remote repo returns about
        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Hi Im Joe").when(remoteProfileRepository).getProfilePictureId(1);

        // Do method now that mock rules are set
        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getProfilePictureId(1, contract);

        // Should call for service once each repo
        verify(localProfileRepository, times(1)).getProfilePictureId(1);
        verify(remoteProfileRepository, times(1)).getProfilePictureId(1);

        //Should try and set the repo exaclly once.
        verify(contract, times(1)).setUserProfilePicId("Hi Im Joe");
    }

    @Test
    public void TestThatWeCanUpdateOurUserProfileIdModelDataIfWeHaveDifferentDataOnTheServerThanTheDataThatWeHaveLocally() {
        // Our mock contract.
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn("Test1").when(localProfileRepository).getProfilePictureId(1);

        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Test2").when(remoteProfileRepository).getProfilePictureId(1);

        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getProfilePictureId(1, contract);

        verify(contract, times(2)).setUserProfilePicId(any(String.class));
        verify(contract, times(1)).setUserProfilePicId("Test1");
        verify(contract, times(1)).setUserProfilePicId("Test2");

        verify(localProfileRepository, times(1)).getProfilePictureId(1);
        verify(localProfileRepository, times(1)).saveProfilePictureId("Test2", 1);
        verify(remoteProfileRepository, times(1)).getProfilePictureId(1);
    }

    @Test
    public void TestThatWhenWeHaveTheSameUserProfileIdDataWeDontBotherUpdatingIt() {
        RepositoryResponseContract contract = Mockito.mock(RepositoryResponseContract.class);

        LocalProfileRepository localProfileRepository = Mockito.mock(LocalProfileRepository.class);
        Mockito.doReturn("Test1").when(localProfileRepository).getProfilePictureId(1);

        RemoteProfileRepository remoteProfileRepository = Mockito.mock(RemoteProfileRepository.class);
        Mockito.doReturn("Test1").when(remoteProfileRepository).getProfilePictureId(1);

        ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
        profileRepository.getProfilePictureId(1, contract);

        verify(contract, times(1)).setUserProfilePicId(any(String.class));
        verify(contract, times(1)).setUserProfilePicId("Test1");


        verify(localProfileRepository, times(1)).getProfilePictureId(1);
        verify(remoteProfileRepository, times(1)).getProfilePictureId(1);
    }
}
