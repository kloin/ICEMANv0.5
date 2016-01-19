package com.breadcrumbs.client;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.breadcrumbs.Network.LoadBalancer;
import com.breadcrumbs.R;
import com.breadcrumbs.ServiceProxy.AsyncDataRetrieval;
import com.breadcrumbs.ServiceProxy.UpdateViewElementWithProperty;
import com.breadcrumbs.caching.GlobalContainer;
import com.breadcrumbs.client.DialogWindows.DatePickerDialog;
import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.text.MessageFormat;
import java.util.prefs.Preferences;

/**
 * Created by aDirtyCanvas on 6/28/2015.
 */
public class EditTrail extends FragmentActivity implements DatePickerDialog.DatePickerDialogListener{

    private Context context;
    private TextView startDateTextView;
    private boolean setFront = false;
    private String trailId = "0";
    private String coverId = "0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trail_editor_window);
        context = this;
        setUpButtonListeners();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trailId = extras.getString("TrailId");
            coverId = extras.getString("CoverId");
            setUpFields();
        }
        setUpHeaderPhoto();


        ActionBar actionBar = getActionBar();
        //startDateTextView = (TextView) findViewById(R.id.start_date_picker);
      //  actionBar.hide();

    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
    }

    private void setUpFields() {
        UpdateViewElementWithProperty updater = new UpdateViewElementWithProperty();

        EditText trailTitleEdit = (EditText) findViewById(R.id.title_edit);
        updater.UpdateEditTextElement(trailTitleEdit, trailId, "TrailName");

        EditText about = (EditText) findViewById(R.id.countries_edit);
        updater.UpdateEditTextElement(about, trailId, "Description");
    }

    private void setUpHeaderPhoto() {
        ImageView header = (ImageView) findViewById(R.id.trail_header_image_in_edit_screen);
        ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        Double widthDouble = displaymetrics.widthPixels * 0.75;
        layoutParams.height = widthDouble.intValue();
        header.setLayoutParams(layoutParams);
        header.setBackgroundResource(R.color.ColorPrimary);
        if (coverId != null && !coverId.equals("0")) {
            Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+coverId+".jpg").centerCrop().crossFade().into(header);
        }

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // click handler here for loading up a profile selection page
            }
        });
    }
    // Handler to create the trail based on the data we have been given
    private void setUpButtonListeners() {
       TextView saveTrail = (TextView) findViewById(R.id.save_trail);
        saveTrail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trailId.equals("0")) {
                    createNewTrail();
                } else {
                    updateProperties();
                }

                finish();
            }
        });

      /*  ImageView trailCoverPhoto = (ImageView) findViewById(R.id.trail_image);
        trailCoverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageSelector = new Intent();
                imageSelector.putExtra("TrailId", "1");
                imageSelector.setClassName("com.breadcrumbs.client", "com.breadcrumbs.client.BreadCrumbsImageSelector");
                startActivity(imageSelector);
            }
        });*/

        //TextView startDateButton = (TextView) findViewById(R.id.start_date_picker);
        //startDateButton.setPaintFlags(startDateButton.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        //TextView endDateButton = (TextView) findViewById(R.id.end_date_picker);
        //endDateButton.setPaintFlags(endDateButton.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
//        startDateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openDatePicker();
//                setFront = true;
//            }
//        });
//
//        endDateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openDatePicker();
//                setFront = false;
//            }
//        });
        ImageButton backButton = (ImageButton) findViewById(R.id.exit_trail_editor);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  if (isDirty) {
                    //save();
                   // Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                //}
                finish();
            }
        });
    }

    private void updateProperties() {
        EditText title = (EditText) findViewById(R.id.title_edit);
        EditText description = (EditText) findViewById(R.id.countries_edit);

        String newTitle = title.getText().toString();
        String newDescription = description.getText().toString();

        String titleUpdateUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+trailId+"/TrailName/"+newTitle;
        String descriptionUpdateUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+trailId+"/Description/"+newDescription;

        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(titleUpdateUrl, null);
        asyncDataRetrieval.execute();
        AsyncDataRetrieval asyncDataRetrieval1 = new AsyncDataRetrieval(descriptionUpdateUrl, null);
        asyncDataRetrieval1.execute();
    }

    private void openDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog();
        datePickerDialog.show(getSupportFragmentManager(), "datepicker");
    }

    private void createNewTrail() {
        //Get the text for description etc..
        EditText trailTitleEdit = (EditText) findViewById(R.id.title_edit);
        EditText trailDescriptionEdit = (EditText) findViewById(R.id.countries_edit);

        String trailTitle = trailTitleEdit.getText().toString();
        if (trailTitle == null) {
            Toast.makeText(context, "Enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        String trailDescription = trailDescriptionEdit.getText().toString();
        // Not using preferences here seems risky but it also seems to be working. Maybe have a double check (i.e try both) if it is not working.
        String userId = GlobalContainer.GetContainerInstance().GetUserId();//PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("USERID", "-1");

        String url = MessageFormat.format("{0}/rest/login/saveTrail/{1}/{2}/{3}",
                LoadBalancer.RequestServerAddress(),
                trailTitle,
                trailDescription,
                userId);

        url = url.replaceAll(" ", "%20");
        AsyncDataRetrieval asyncDataRetrieval  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                // Not sure I want to do anything here.
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("TRAILID", result).commit();
            }
        });

        asyncDataRetrieval.execute();
        finish();
    }

    /*
        Override our method in our DatePickerDialog, so that we can process and save data.
     */
    @Override
    public void onDateClick(int day, int month, int year) {
//        TextView endDateTextView = (TextView) findViewById(R.id.end_date_picker);
//        if (setFront) {
//            startDateTextView.setText(Integer.toString(day) + "/"+Integer.toString(month) + "/" + Integer.toString(year));
//        } else {
//            endDateTextView.setText(Integer.toString(day) + "/"+Integer.toString(month) + "/" + Integer.toString(year));
//        }
    }
}
