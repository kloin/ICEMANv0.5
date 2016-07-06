package com.teamunemployment.breadcrumbs.Trips;

/**
 * @author Josiah Kendall
 */
public class TripRepo {

    private PostTripService postTripService;

    public TripRepo() {
        postTripService = new PostTripService();
    }
    public TripRepo(PostTripService postTripService) {
        this.postTripService = postTripService;
    }
    public void SaveCoverPhotoId(String trailId, String coverPhotoId) {
        postTripService.SaveCoverPhoto(coverPhotoId, trailId);
    }
}
