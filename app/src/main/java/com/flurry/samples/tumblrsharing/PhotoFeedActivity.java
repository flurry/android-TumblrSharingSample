/*
 *  Copyright 2015 Yahoo Inc.
 *  Licensed under the terms of the zLib license. Please see LICENSE file for terms.
 */
package com.flurry.samples.tumblrsharing;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Class that manages the photo grid screen.
 */
public class PhotoFeedActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private static final int REQUEST_CHECK_SETTINGS = 1;
    private FlickrClient flickrClient;
    private Location lastLocation;
    private RecyclerView photoGrid;
    private LocationRequest locationRequest;
    private PhotoAdapter photoAdapter;
    private GoogleApiClient googleApiClient;
    private LocationSettingsRequest.Builder builder;
    public static final String PHOTO_DETAIL_KEY = "photo";

    public static final String LOG_TAG = PhotoFeedActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static int UPDATE_INTERVAL = 2000; // 2 seconds
    private static int FASTEST_INTERVAL = 1; // 1 microseconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_feed);

        // First we need to check availability of play services
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        photoGrid = (RecyclerView) findViewById(R.id.photoGrid);
        ArrayList<Photo> photoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(photoList);
        photoGrid.setAdapter(photoAdapter);
        photoGrid.setLayoutManager(new GridLayoutManager(this, 2));
        photoGrid.setHasFixedSize(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsHelper.logPageViews();
        Log.i(LOG_TAG, "Logging page views");

        checkPlayServices();
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
        if (googleApiClient != null && googleApiClient.isConnected()) {
            checkLocationSettings();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /**
     * Method to display photo feed.
     * */
    private void fetchPhotoFeed() {
        flickrClient = new FlickrClient();

        HashMap<String, String> fetchPhotosEventParams = new HashMap<>(1);
        fetchPhotosEventParams.put(AnalyticsHelper.PARAM_LOCATION, String.valueOf(lastLocation));
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_FETCH_PHOTOS, fetchPhotosEventParams,true );

        flickrClient.getPhotoFeed(lastLocation, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int code, Header[] headers, JSONObject body) {

                AnalyticsHelper.endTimedEvent(AnalyticsHelper.EVENT_FETCH_PHOTOS);

                JSONObject photosObject = null;
                if (body != null) {
                    try {
                        if (body.has("photos")) {
                            photosObject = body.getJSONObject("photos");
                            JSONArray photoArray = photosObject.getJSONArray("photo");
                            List<Photo> photosList = Photo.fromJson(photoArray);
                            photoAdapter.addPhotos(photosList);
                            photoAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Flickr Api error.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        AnalyticsHelper.logError(LOG_TAG, "Photo Feed JSON Error", e);
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Flickr Api error.", Toast.LENGTH_LONG).show();
                    AnalyticsHelper.logError(LOG_TAG, "Response body is null", null);
                    finish();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(getApplicationContext(),
                        "Flickr Api error.", Toast.LENGTH_LONG).show();
                AnalyticsHelper.logError(LOG_TAG, "Failure in fetching photo feed", null);
                finish();
            }
        });
    }

    /**
     * Method to set listener on photo selected action.
     * */
    public void setupPhotoSelectedListener() {
        photoAdapter.setOnItemClickListener(new PhotoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Photo photo = photoAdapter.getItem(position);

                HashMap<String, String> itemClickEventParams = new HashMap<>(2);
                itemClickEventParams.put(AnalyticsHelper.PARAM_SELECTED_PHOTO_ID, photo.getPhotoId());
                itemClickEventParams.put(AnalyticsHelper.PARAM_SELECTED_PHOTO_SECRET, photo.getSecret());
                AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_PHOTO_SELECTED, itemClickEventParams, false);

                Log.i(LOG_TAG, "Logging event: " + AnalyticsHelper.EVENT_PHOTO_SELECTED);

                Intent i = new Intent(PhotoFeedActivity.this, PhotoDetailActivity.class);
                i.putExtra(PHOTO_DETAIL_KEY, photoAdapter.getItem(position));
                startActivity(i);
            }
        });
    }

    /**
     * Method to build Google Api Client.
     * */
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Callback Method called when GoogleApiClient is connected.
     * */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "Location services connected.");
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastLocation != null) {
            fetchPhotoFeed();
            setupPhotoSelectedListener();
        }
        createLocationRequestAndLocationSettingsRequestBuilder();
        checkLocationSettings();

    }

    /**
     * Callback Method called when GoogleApiClient connection is suspended.
     * */
    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }


    /**
     * Callback Method called when GoogleApiClient connection fails.
     * */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                AnalyticsHelper.logError(LOG_TAG, "GoogleAPIClient connection failed. Resolution Intent Exception.", e);
            }
        } else {
            AnalyticsHelper.logError(LOG_TAG, "GoogleAPIClient connection failed. No resolution.", null);
        }
    }

    /**
     * Method to create Location Request and Location Settings Request objects.
     * */
    protected void createLocationRequestAndLocationSettingsRequestBuilder() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
    }


    /**
     * Method to start location updates
     * */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }


    /**
     * Callback Method called when location changes.
     * */
    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        fetchPhotoFeed();
        setupPhotoSelectedListener();
    }

    /**
     * Method to to stop location updates.
     * */
    protected void stopLocationUpdates() {
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    /**
     * Method to verify if location tracking is enabled.
     * */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS:
                        if (googleApiClient != null && !googleApiClient.isConnected()){
                            googleApiClient.connect();
                        }
                        startLocationUpdates();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //Fix location settings by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                            status.startResolutionForResult(PhotoFeedActivity.this,REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            AnalyticsHelper.logError(LOG_TAG, "Location Settings Disabled. Resolution Intent Exception", e);
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(getApplicationContext(),
                                "Location services not enabled. To use this app, enable location services in settings.", Toast.LENGTH_LONG)
                                .show();
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (googleApiClient != null && !googleApiClient.isConnected()){
                            googleApiClient.connect();
                        }
                        startLocationUpdates();
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getApplicationContext(),
                                "Location services not enabled. To use this app, enable location services.", Toast.LENGTH_LONG)
                                .show();
                        finish();
                        break;

                    default:
                        break;
                }
                break;
        }
    }
}

