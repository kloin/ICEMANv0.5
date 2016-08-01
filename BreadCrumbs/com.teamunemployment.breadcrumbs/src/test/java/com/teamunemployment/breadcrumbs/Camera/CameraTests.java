package com.teamunemployment.breadcrumbs.Camera;

import android.app.Activity;
import android.content.Context;
import android.view.TextureView;

import com.teamunemployment.breadcrumbs.AppComponent;
import com.teamunemployment.breadcrumbs.Explore.Presenter;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Josiah Kendall.
 */
public class CameraTests {

    @Test
    public void TestThatWeCreateCameraOnPresenterStart() {

        CameraModel model = Mockito.mock(CameraModel.class);
        CameraViewObjectContract cameraViewObjectContract = Mockito.mock(CameraViewObjectContract.class);
        CameraPresenter presenter = new CameraPresenter(model);
        Mockito.when(model.CreateCameraSurface(any(Context.class))).thenReturn(Mockito.mock(TextureView.class));
        presenter.SetView(cameraViewObjectContract);
        presenter.start(Mockito.mock(Context.class));
        verify(model, times(1)).CreateCameraSurface(any(Context.class));
        verify(cameraViewObjectContract, times(1)).attachCameraSurface(any(TextureView.class));
    }

    @Test
    public void TestThatWeDestroyAndReleaseCameraOnActivityDestroy() {
        CameraView cameraView = Mockito.mock(CameraView.class);
        CameraModel model = Mockito.mock(CameraModel.class);
        CameraPresenter presenter = new CameraPresenter(model);
        presenter.SetView(cameraView);

        presenter.stop();
    }

    @Test
    public void TestThatWeCan1080ParametersInOurParameterList() {

    }

    @Test
    public void TestThatWeCanGrabLargestPreviewSizeIfWeFailToFind1080p() {

    }

    /**
     * We want aspect ratioif we can. If we cant find the correct aspect ratio we resort to just
     * showing the biggest with it centercropped.
     */
    @Test
    public void TestThatWeCanGetCameraWithBestAspectRatio() {

    }

    /**
     * I have a bad feeling some android phones may not have the correct aspect ratio for their screens.
     * This might not matter for the {@link android.view.TextureView}.
     */
    @Test
    public void TestThatWeGet1080pWithNoCorrectAspectRatioAvailable() {

    }

    @Test
    public void TestThatWeCanRequestPermissionsIfTheyDontExist() {

    }

    @Test
    public void TestThatWeCanAskOnlyPermissionsThatExist() {

    }

    @Test
    public void TestThatWeCanDisplayVideoMode() {

    }

    @Test
    public void TestThatWeCanDisplayPhotoMode() {

    }

    @Test
    public void TestThatWeCannotClickPhotoWhileVideoRecording() {

    }

    @Test
    public void TestVideoRecordingToggle1() {

    }

    @Test
    public void TestVideoRecordingToggle2() {

    }

    @Test
    public void TestPhotoVideoToggle1() {

    }

    @Test
    public void TestPhotoVideoToggle2() {

    }

    @Test
    public void TestThatWeCanLaunchViewerAfterRecording20Seconds() {

    }

    @Test
    public void TestThatWeCannotStartRecordingAfter20Seconds() {

    }

    @Test
    public void TestThatweStopRecordingAfter20Seconds() {

    }

    @Test
    public void TestThatWeCanLaunchSaverWhenPhotoTaken() {

    }

    @Test
    public void TestThatPhotoCanBeTaken1() {

    }

    @Test
    public void TestThatPhotoCanBeTaken2() {

    }

    @Test
    public void TestThatWeCanFindCorrectCameraPhotoSize() {

    }

    @Test
    public void TestThatCameraPhotoSizeCanMatchScreenSize() {

    }

    @Test
    public void TestThatCameraPhotoSizeSelectorCanHandleNoCorrectSize() {

    }

    @Test
    public void TestThatCameraPhotoSizeSelectorCanHandleSmallerThan1080p() {

    }

    @Test
    public void TestThatCameraPicks1080IfAvailable() {

    }

    @Test
    public void TestThatWeCanPickLargestOfAvailablePreviewsIf1080IsNotAvailable() {

    }

    @Test
    public void TestThatPreviewSizeIs1080pWhereAvailable() {

    }

    @Test
    public void TestThatPreviewSizeSelectorCanHandleLessThan1080p() {

    }

    @Test
    public void EnsureThatCameraDoesNotCrashIfItCanNotFindACorrectPreviewSize() {

    }

    @Test
    public void TestThatBackButtonWorks() {

    }

    @Test
    public void TestThatVideoLocationCanBeSetCorrectly() {

    }

    @Test
    public void TestThatCorrectPhotoLocationCanBeSet() {

    }

    @Test
    public void TestThatWeCanHandleVariousOrientationsInCamera() {

    }

    @Test
    public void TestThatWeCanSetCorrectOrientationForVideoCamera() {

    }

    @Test
    public void TestThatWeCanSetParameters1() {

    }

    @Test
    public void TestThatWeCanSetParameters2() {}

    @Test
    public void TestThatWeCanSetParameters3() {}

    @Test
    public void TestThatWeCanSetParameters4() {}

    @Test
    public void TestThatWeCanSetParameters5() {}

    @Test
    public void TestThatWeCanSetParameters6() {}


}
