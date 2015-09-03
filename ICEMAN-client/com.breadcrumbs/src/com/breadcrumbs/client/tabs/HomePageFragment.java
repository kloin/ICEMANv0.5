package com.breadcrumbs.client.tabs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.breadcrumbs.Trails.MyCurrentTrailManager;
import com.breadcrumbs.client.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class HomePageFragment extends android.support.v4.app.Fragment {
    private Context context;
    private View rootView;
    private Activity activityContext;
    private MyCurrentTrailManager trailManager;
    public void onAttach(Activity activity) {
        activityContext=(Activity) activity;
        super.onAttach(activity);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {
            rootView = inflater.inflate(R.layout.home_map, container, false);
            context = rootView.getContext();
            TrailStartToggleListener();
            ActionBarListeners();
            GoogleMap map = ((MapFragment) activityContext.getFragmentManager().findFragmentById(R.id.map)).getMap();
            trailManager = new MyCurrentTrailManager(map, activityContext);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
		return rootView;
	}

    private void ActionBarListeners() {
        //ImageButton loadProfileButton = (ImageButton) rootView.findViewById(R.id.loadUserProfile);
      //  loadProfileButton.setOnClickListener(new View.OnClickListener() {
         //   @Override
         //   public void onClick(View view) {
       //         Intent save = new Intent();
      //          save.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.ProfilePageViewer");
      //          startActivity(save);
            //}
       // });
    }

    public void TrailStartToggleListener() {
        ImageButton trailStartButton = (ImageButton) rootView.findViewById(R.id.new_content);

        trailStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View baseView) {
                if (baseView.getTag().equals("0")) {
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.new_trail_dialog);
                    dialog.setTitle("Create new trail...");

                    // set the custom dialog components - text, image and button
                    //TextView text = (TextView) dialog.findViewById(R.id.text);
                    // text.setText("Android custom dialog example!");
                    //ImageView image = (ImageView) dialog.findViewById(R.id.image);
                    //image.setImageResource(R.drawable.ic_launcher);

                    ImageButton dialogButton = (ImageButton) dialog.findViewById(R.id.dialogButtonOK);
                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText titleField = (EditText) dialog.findViewById(R.id.new_trail_title);
                            baseView.setTag("1");
                            trailManager.CreateTrailAndBeginTracking(titleField.getText().toString());
                            Toast.makeText(context, "Trail started...", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                    dialog.show();

                } else {
                    baseView.setTag("0");
                    trailManager.StopTracking();
                    Toast.makeText(context, "Trail stopped", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
	
	
}
