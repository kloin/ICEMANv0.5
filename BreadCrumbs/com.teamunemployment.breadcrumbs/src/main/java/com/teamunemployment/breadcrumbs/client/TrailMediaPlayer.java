package com.teamunemployment.breadcrumbs.client;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;



/**
 * Created by jek40 on 25/04/2016.
 */
public class TrailMediaPlayer extends Activity {

  //  @State
    ArrayList<String> ids;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
   //     Icepick.saveInstanceState(this, outState);
    }


}
