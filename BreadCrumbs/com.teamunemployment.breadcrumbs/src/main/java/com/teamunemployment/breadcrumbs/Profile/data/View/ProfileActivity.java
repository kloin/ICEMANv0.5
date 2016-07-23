package com.teamunemployment.breadcrumbs.Profile.data.View;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.teamunemployment.breadcrumbs.R;

import butterknife.Bind;

/**
 * @author Josiah Kendall
 */
public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_fragment_holder);
    }
}
