package com.teamunemployment.breadcrumbs.client;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.teamunemployment.breadcrumbs.R;

/**
 * @author Josiah Kendall
 */
public class TrailDetailsViewer extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trail_details_layout);
        setTrailName();
        loadTrailDetails();
        setUpShareableLink();
    }

    /**
     * Set up the trail name for this trail (if applicable
     */
    private void setTrailName() {

    }

    /**
     * Load the details for a trail.
     */
    private void loadTrailDetails() {

    }

    /**
     * Set up the sharing capabilities so that a user can share to the places they want
     */
    private void setUpShareableLink() {

    }
}
