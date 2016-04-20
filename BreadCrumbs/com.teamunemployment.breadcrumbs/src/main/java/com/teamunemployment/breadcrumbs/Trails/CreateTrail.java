//package com.teamunemployment.breadcrumbs.Trails;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.support.design.widget.CoordinatorLayout;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.teamunemployment.breadcrumbs.BreadcrumbsLocationAPI;
//import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
//import com.teamunemployment.breadcrumbs.PreferencesAPI;
//import com.teamunemployment.breadcrumbs.R;
//import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
//import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
//import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UpdateViewElementWithProperty;
//import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardAdapter;
//import com.teamunemployment.breadcrumbs.client.Cards.CrumbCardDataObject;
//import com.teamunemployment.breadcrumbs.client.DialogWindows.DatePickerDialog;
//import com.bumptech.glide.Glide;
//
//import java.text.MessageFormat;
//import java.util.ArrayList;
//
///**
// * Created by aDirtyCanvas on 6/28/2015.
// */
//public class CreateTrail extends AppCompatActivity implements DatePickerDialog.DatePickerDialogListener{
//
//    private Context context;
//    private TextView startDateTextView;
//    private boolean setFront = false;
//    private String trailId = "0";
//    private String coverId = "0";
//    private RecyclerView mRecyclerView;
//    private LinearLayoutManager mLayoutManager;
//    private ArrayList<CrumbCardDataObject> crumbsArray;
//    private CrumbCardAdapter mAdapter;
//    static final int PICK_PROFILE_REQUEST = 1;
//    private final String TAG = "CREATE_TRAIL";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.create_trail);
//        context = this;
//        setUpButtonListeners();
//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {
//            trailId = extras.getString("TrailId");
//            coverId = extras.getString("CoverId");
//            setUpFields();
//        }
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // This is the check for when we return with no data. Usually when the user hits the back button
//        if (requestCode == 1) {
//            if(resultCode == Activity.RESULT_OK){
//                // Load our new image
//                final ImageView header = (ImageView) findViewById(R.id.headerPicture);
//                String id = PreferenceManager.getDefaultSharedPreferences(context).getString("TRAILCOVERPHOTO", "-1");
//                Glide.with(context).load(LoadBalancer.RequestCurrentDataAddress() + "/images/"+id + ".jpg").centerCrop().crossFade().into(header);
//                String saveUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+ trailId + "/CoverPhotoId/" + id;
//                HTTPRequestHandler simpleHttpRequest = new HTTPRequestHandler();
//                simpleHttpRequest.SendSimpleHttpRequest(saveUrl, context);
//            }
//            if (resultCode == Activity.RESULT_CANCELED) {
//                //Write your code if there's no result
//            }
//        }
//    }
//
//    // Show a snackbar message to the user
//    private void displayMessage(String string) {
//
//        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
//        Snackbar snackbar = Snackbar.make(coordinatorLayout, string, Snackbar.LENGTH_LONG).setAction("UNDO", null);
//
//        // Set text color
//        snackbar.setActionTextColor(Color.WHITE);
//
//        // Grab actual snackbar and set its color
//        View snackbarView = snackbar.getView();
//        snackbarView.setBackgroundColor(getResources().getColor(R.color.ColorPrimary));
//
//        // Grab our text view
//        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
//        textView.setTextColor(getResources().getColor(R.color.white));
//
//        // Show our work
//        snackbar.show();
//    }
//
//    @Override
//    public void onResume() {  // After a pause OR at startup
//        super.onResume();
//    }
//
//    private void setUpFields() {
//        UpdateViewElementWithProperty updater = new UpdateViewElementWithProperty();
//
//        EditText trailTitleEdit = (EditText) findViewById(R.id.title_edit);
//        updater.UpdateEditTextElement(trailTitleEdit, trailId, "TrailName", context);
//
//        // Not using description at the moment
//      //  EditText about = (EditText) findViewById(R.id.countries_edit);
//        //updater.UpdateEditTextElement(about, trailId, "Description");
//    }
//
//    // Handler to create the trail based on the data we have been given
//            private void setUpButtonListeners() {
//                TextView saveTrail = (TextView) findViewById(R.id.save_trail_button);
//                saveTrail.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (trailId.equals("0")) {
//                            createNewTrail();
//                        } else {
//                            updateProperties();
//                        }
//            }
//        });
//
//
//        ImageView backButton = (ImageView) findViewById(R.id.back_button);
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//              //  if (isDirty) {
//                    //save();
//                   // Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
//                //}
//                finish();
//            }
//        });
//    }
//
//    private void updateProperties() {
//        EditText title = (EditText) findViewById(R.id.title_edit);
//        EditText description = (EditText) findViewById(R.id.countries_edit);
//
//        String newTitle = title.getText().toString();
//        String newDescription = description.getText().toString();
//
//        String titleUpdateUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+trailId+"/TrailName/"+newTitle;
//        String descriptionUpdateUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+trailId+"/Description/"+newDescription;
//
//        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(titleUpdateUrl, null, context);
//        asyncDataRetrieval.execute();
//        AsyncDataRetrieval asyncDataRetrieval1 = new AsyncDataRetrieval(descriptionUpdateUrl, null, context);
//        asyncDataRetrieval1.execute();
//        finish();
//    }
//
//    private void openDatePicker() {
//        DatePickerDialog datePickerDialog = new DatePickerDialog();
//        datePickerDialog.show(getSupportFragmentManager(), "datepicker");
//    }
//
//    private void createNewTrail() {
//        final ConnectivityManager cm =
//                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        boolean isConnected = activeNetwork != null &&
//                activeNetwork.isConnectedOrConnecting();
//        if (!isConnected) {
//            Toast.makeText(context, "No internet connection available.", Toast.LENGTH_LONG);
//            return;
//        }
//        //Get the text for description etc..
//        EditText trailTitleEdit = (EditText) findViewById(R.id.title_edit);
//        EditText trailDescriptionEdit = (EditText) findViewById(R.id.countries_edit);
//
//        String trailTitle = trailTitleEdit.getText().toString();
//        if (trailTitle.isEmpty()) {
//            displayMessage("Title is required");
//            return;
//        }
//
//        String trailDescription = " ";
//        // Not using preferences here seems risky but it also seems to be working. Maybe have a double check (i.e try both) if it is not working.
//        final String userId = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("USERID", "-1");//PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("USERID", "-1");
//
//        // This resets the Event Id because we dont want dirty data from the last trail the user may have had.
//        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putInt("EVENTID", 0).commit();
//        String url = MessageFormat.format("{0}/rest/login/saveTrail/{1}/{2}/{3}",
//                LoadBalancer.RequestServerAddress(),
//                trailTitle,
//                trailDescription,
//                userId);
//        Log.d(TAG, "Sending url request: " + url);
//        url = url.replaceAll(" ", "%20");
//        Toast.makeText(context, "Creating trail....", Toast.LENGTH_LONG).show();
//        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
//
//            /*
//             * Override for the
//             */
//            @Override
//            public void onFinished(String result) {
//                // Not sure I want to do anything here.
//                Log.d(TAG, "Url request sent successfully, recieved response : " + result);
//                PreferencesAPI.GetInstance(context).SaveCurrentServerTrailId(Integer.parseInt(result));
//
//
//
//                // This should be being done server side when trail is created.
//                String updateActiveTrailUrl = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+userId+"/ActiveTrail/"+result;
//                AsyncDataRetrieval updateActiveTrail = new AsyncDataRetrieval(updateActiveTrailUrl, new AsyncDataRetrieval.RequestListener() {
//                    @Override
//                    public void onFinished(String result) {
//                        // Dont actually need to do anything here.
//                    }
//                }, context);
//                updateActiveTrail.execute();
//
//                // Begin tracking
//                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("TRACKING", true).commit();
//               // BreadCrumbsFusedLocationProvider breadCrumbsFusedLocationProvider = new BreadCrumbsFusedLocationProvider(context);
//               // breadCrumbsFusedLocationProvider.StartBackgroundGPSService();
//
//                BreadcrumbsLocationAPI locationAPI = new BreadcrumbsLocationAPI();
//                locationAPI.StartLocationService();
//               // GlobalContainer.GetContainerInstance().SetBreadCrumbsFusedLocationProvider(breadCrumbsFusedLocationProvider);
//                finish();
//            }
//        }, context);
//        asyncDataRetrieval.execute();
//
//    }
//
//
//
//    /*
//        Override our method in our DatePickerDialog, so that we can process and save data.
//     */
//    @Override
//    public void onDateClick(int day, int month, int year) {
////        TextView endDateTextView = (TextView) findViewById(R.id.end_date_picker);
////        if (setFront) {
////            startDateTextView.setText(Integer.toString(day) + "/"+Integer.toString(month) + "/" + Integer.toString(year));
////        } else {
////            endDateTextView.setText(Integer.toString(day) + "/"+Integer.toString(month) + "/" + Integer.toString(year));
////        }
//    }
//}
