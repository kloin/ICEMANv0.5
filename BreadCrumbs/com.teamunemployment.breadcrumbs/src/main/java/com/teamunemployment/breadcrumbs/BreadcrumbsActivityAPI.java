package com.teamunemployment.breadcrumbs;

import com.teamunemployment.breadcrumbs.BackgroundServices.MessageObjects.StopActivityEvent;
import com.teamunemployment.breadcrumbs.BackgroundServices.UserActivity;

import org.greenrobot.eventbus.EventBus;

/**
 * Acess to the activity service
 */
public class BreadcrumbsActivityAPI {
    EventBus bus = EventBus.getDefault();

    public void ListenToUserActivityChanges() {
        UserActivity event = new UserActivity();
        bus.post(event);
    }


    public void StopListeningToActivityChanges() {
        StopActivityEvent stopActivityEvent = new StopActivityEvent();
        bus.post(stopActivityEvent);
    }

}
