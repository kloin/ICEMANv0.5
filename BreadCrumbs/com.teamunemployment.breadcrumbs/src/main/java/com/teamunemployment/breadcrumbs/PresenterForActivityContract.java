package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * @author Josiah Kendall.
 *
 * The contract defining the required methods an activies presenter. THis covers the base methods.
 */
public interface PresenterForActivityContract {
    void start(Context context);
    void stop();

}
