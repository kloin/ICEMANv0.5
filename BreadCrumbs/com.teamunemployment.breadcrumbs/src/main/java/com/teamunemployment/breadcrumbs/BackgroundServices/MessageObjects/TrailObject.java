package com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects;

/**
 * Simple object to pass to subscribed background method.
 * @author Josiah kendall
 */
public class TrailObject {

    public final String ServerTrail;

    public TrailObject(String localTrailId) {
        this.ServerTrail = localTrailId;
    }
}
