package com.teamunemployment.breadcrumbs.client.tabs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncRetrieveImage;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.database.DatabaseController;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Iterator;

/*
    Man this class is brutal as fuck bro
 */
public class SubscriptionManagerTab extends android.support.v4.app.Fragment {
    public View rootView;
    private Activity myContext;
    private GlobalContainer globalContainer;
    private AsyncDataRetrieval clientRequestProxy;
    private LinearLayout storyBoard;
    private HTTPRequestHandler requestHandler;

    private Location thisLocationTest = null;
    @Override // Used to Grab an activity for using GetFragmentActivity later.
    public void onAttach(Activity activity) {
        myContext=(Activity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // This is our base view - the subscription manager with the bar and the container frame.
        rootView = inflater.inflate(R.layout.pinned_tab, container, false);
        // Create the objects here so these screens will persist between swipes.
        // Set up db, caching gps stuff for later use.
        DatabaseController databaseController = new DatabaseController(rootView.getContext());
        globalContainer = GlobalContainer.GetContainerInstance();
       // setTrailListeners();
        loadTrails();
        return rootView;
    }

    // Need this code but for now I am going to comment it out.
    public void loadTrails() {
        String userId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("USERID", "-1");
        /*String url = MessageFormat.format("{0}/rest/User/GetAllPinnedTrailsForAUser/{1}",
                LoadBalancer.RequestServerAddress(),
                userId);*/
        String url = LoadBalancer.RequestServerAddress() + "/rest/login/GetAllTrails";
       // mMap = ((MapFragment) myContext.getFragmentManager().findFragmentById(R.id.map)).getMap();
        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            @Override
            public void onFinished(String result) {
                //Initialise our object, and attempt to construct it from the string.
                JSONObject jsonobj = null;
                try {
                    jsonobj = new JSONObject(result);
                   // globalContainer.CacheTrails(jsonobj);
                    LoadTrailsAndCrumbs(jsonobj);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NullPointerException e) {

                }
                System.out.println("App has been given this object: " + jsonobj);
                storyBoard = (LinearLayout) rootView.findViewById(R.id.storyBoard);

            }
        });

        clientRequestProxy.execute();
        System.out.println("Sending save request to : " + url);
    }
    /*
        This is the class we use when we want to just display the trails and not load any crumbs -
        i.e for a search.
     */
    public void DisplayTrailsOnSubScriptionManagerLinkedTab(JSONObject trails) {
        //mMap = ((MapFragment) myContext.getFragmentManager().findFragmentById(R.id.map)).getMap();
        //lobalContainer.SetMapInstance(mMap);
        try {
            Iterator iterator = trails.keys();
            int i = 0;
            while (iterator.hasNext()) {
                JSONObject node = (JSONObject) trails.get(iterator.next().toString());
                String desc = "Description";
                String tit = node.getString("TrailName");
                String trailId = node.getString("Id");
                //createTrailSearchObject(i, tit, desc, trailId);
            }
        } catch(Exception ex) {
            //exception occured creating data
            System.out.println("Exception occured creating data: ");
            ex.printStackTrace();
        }
    }
    //Load up all the crumbs
    public void LoadTrailsAndCrumbs(JSONObject result) {
        //lets do it
        //mMap = ((MapFragment) myContext.getFragmentManager().findFragmentById(R.id.map)).getMap();
       // globalContainer.SetMapInstance(mMap);

        try {
            Iterator iterator = result.keys();
            int i = 0;
            while (iterator.hasNext()) {
                JSONObject node = (JSONObject) result.get(iterator.next().toString());
                String desc = "Description";
                String tit = node.getString("TrailName");
                String trailId = node.getString("Id"); // wont work until reset of db
                createStoryObject(i, tit, desc, trailId);
                //MyCurrentTrailManager manager = new MyCurrentTrailManager(mMap, getActivity());
               // manager.DisplayTrailAndCrumbs(trailId);
                //loadCrumbsForTrail(trailId); //Set to trailId
            }
        } catch(Exception ex) {
            //exception occured creating data
            System.out.println("Exception occured creating data: ");
            ex.printStackTrace();
        }
    }


//    private TextWatcher textWatcher = new TextWatcher() {
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            if (s.length() > 0) {
//                sendTrailSearchRequest(s.toString());
//            } else {
//                searchStoryBoard = (LinearLayout) rootView.findViewById(R.id.search_story_board);
//
//                // Remove all trails (if there are any) and re display our saved trails.
//                if(searchStoryBoard.getChildCount() > 0) {
//                    searchStoryBoard.removeAllViews();
//                }
//                //DisplayTrailsOnSubScriptionManagerLinkedTab(globalContainer.GetTrails());
//            }
//
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count,
//                                      int after) {
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//
//        }
//    };

    /*
    This method seems somewhat inefficient at the moment. TODO a refactor - this has been changed somewhat but could do with some extra loving
     */
//    private JSONObject sendTrailSearchRequest(final String searchText) {
//        String url = MessageFormat.format("{0}/rest/Search/TrailSearch/{1}",
//                LoadBalancer.RequestServerAddress(),
//                searchText);
//
//        url = url.replaceAll(" ", "%20");
//        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
//
//            @Override
//            public void onFinished(String result) {
//                //Initialise our object, and attempt to construct it from the string.
//                JSONObject jsonobj = null;
//                try {
//                    jsonobj = new JSONObject(result.toString());
//                } catch (JSONException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                // Grab our storyboard and start editing
//                searchStoryBoard  = (LinearLayout) backView.findViewById(R.id.search_story_board);
//                // We want to display our search objects unless we
//                if(searchStoryBoard.getChildCount() > 0) {
//                    searchStoryBoard.removeAllViews();
//                }
//                DisplayTrailsOnSubScriptionManagerLinkedTab(jsonobj);
//            }
//        });
//
//        clientRequestProxy.execute();
//        return null;
//    }

//    private void addClickListeners() {
//
//        EditText searchField = (EditText) backView.findViewById(R.id.sub_manager_search_text);
//        searchField.addTextChangedListener(textWatcher);
//    }
    private void setMapListener() {
        if (this.thisLocationTest == null) {
           //globalContainer.CacheLatestPoint(thisLocationTest.getLongitude(), thisLocationTest.getLongitude());
            return;
        } //else if (this.thisLocationTest != )
    }
    // Super messy code that maps a point onto  the google map.
    private void loadCrumbsForTrail(String TrailId) {

    }


    private void createImage(String id) {
        final ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        AsyncRetrieveImage asyncFetch = new AsyncRetrieveImage(id, new AsyncRetrieveImage.RequestListener() {

            @Override
            public void onFinished(Bitmap result) {
                photo.setImageBitmap(result);
            }
        });

        asyncFetch.execute();
    }

    // Create an item (e.g a crumb or a trail) Called sbo for ease of typing
    private void createStoryObject(int index, String storyTitle, String storyDescription, final String trailId) {

        //Infate that shit
        LayoutInflater inflater = myContext.getLayoutInflater();
        requestHandler = new HTTPRequestHandler();

        //Create a base object, which we will attatch to the storyboard, and later manipulate
        final View sbo = inflater.inflate(R.layout.story_board_item_light, null);
        final TextView title = (TextView) sbo.findViewById(R.id.Title);
        title.setText(storyTitle);
        title.setTag(trailId);

        clientRequestProxy  = new AsyncDataRetrieval(LoadBalancer.RequestServerAddress() +"/rest/TrailManager/GetTrailViews/"+trailId, new AsyncDataRetrieval.RequestListener() {

            @Override
            public void onFinished(String result) {
                //Initialise our object, and attempt to construct it from the string.
                if (result != null) {
                    TextView views = (TextView) sbo.findViewById(R.id.trail_views);
                    views.setText(result);
                }

            }
        });

        clientRequestProxy.execute();

        sbo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send view to the server to update views.
                //Launch intent to load that trail
                Intent TrailViewer = new Intent();
                TrailViewer.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.Maps.MapViewer");
                Bundle extras = new Bundle();
                extras.putString("TrailId", trailId);
                TrailViewer.putExtras(extras);
                myContext.startActivity(TrailViewer);
            }
        });
        storyBoard = (LinearLayout) rootView.findViewById(R.id.storyBoard);
        storyBoard.addView(sbo);

        // Handler for removing an item from the storyboard, and also removing association from the database.
        /*ImageView removeButton = (ImageView) sbo.findViewById(R.id.remove_tag);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = MessageFormat.format("{0}/rest/User/UnpintrailForUser/{1}/{2}",
                        LoadBalancer.RequestServerAddress(),
                        globalContainer.GetUserId(),
                        trailId);
                // Send request to server to unpin
                HTTPRequestHandler requestHandler = new HTTPRequestHandler();
                requestHandler.SendSimpleHttpRequest(url);

                //Now delete item from sbo
                storyBoard.removeView(sbo);
            }
        });*/





        // Add listener for the remove trail button.
    }

//    //Create an item (e.g a crumb or a trail) Called sbo for ease of typing
//    private void createTrailSearchObject(final int index, final String storyTitle, final String storyDescription, final String trailId) {
//
//        //Infate that shit
//        LayoutInflater inflater = myContext.getLayoutInflater();
//        //Create a base object, which we will attatch to the storyboard, and later manipulate
//        final View sbo = inflater.inflate(R.layout.story_board_item_light_add, null);
//
//        final TextView title = (TextView) sbo.findViewById(R.id.Title);
//        title.setText(storyTitle);
//        title.setTag(trailId);
//
//        searchStoryBoard.addView(sbo);
//
//        //We want to add the item to our cached trails so we show it on our storyboard.
//        final ImageView add = (ImageView) sbo.findViewById(R.id.add_tag);
//        add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                JSONObject trailToAdd = new JSONObject();
//
//                try {
//                    // These variables look dodgy. If bugs happen come here.
//                    trailToAdd.put("TrailName", storyTitle);
//                    trailToAdd.put("Description", storyDescription);
//                    trailToAdd.put("TrailId", trailId); // Not sure what of these two is used
//                    trailToAdd.put("Id", trailId);
//                    JSONObject cache = globalContainer.GetTrails();
//
//                    // addTrail is a reallyt bad name.
//                    cache.put("Node"+ trailId, trailToAdd);
//                    View view = backView.findViewWithTag(trailId);
//                    searchStoryBoard.removeView(sbo);
//                    SaveNewLinkedTrail(trailId);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    private void SaveNewLinkedTrail(String TrailId) {
        String url = MessageFormat.format("{0}/rest/User/PinTrailForUser/{1}/{2}",
                LoadBalancer.RequestServerAddress(),
                globalContainer.GetUserId(),
                TrailId);

        url = url.replaceAll(" ", "%20");
        clientRequestProxy  = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
                //Initialise our object, and attempt to construct it from the string.
                JSONObject jsonobj = null;
                try {
                    jsonobj = new JSONObject(result);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        clientRequestProxy.execute();
    }
}