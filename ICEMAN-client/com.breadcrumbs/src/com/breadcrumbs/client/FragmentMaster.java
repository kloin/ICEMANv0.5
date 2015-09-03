package com.breadcrumbs.client;

import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;

import org.json.JSONObject;
/*
 * This is an abstract method to contain methods needed for my view classes, that are needed
 * across all pages but are not in FragmentActivity
 */
public class FragmentMaster extends FragmentActivity{

	//Notify a fragment once the file has finished its async function.
	public void Notify(JSONObject jsonResponse) {
		//ABSTRACT METHOD
	}
	
	public void LoadImage(byte[] imageBuffer) {
		//Abstract method
	}

	public void DisplayImage(Bitmap jsonResult) {
		// TODO Auto-generated method stub
		
	}

}
