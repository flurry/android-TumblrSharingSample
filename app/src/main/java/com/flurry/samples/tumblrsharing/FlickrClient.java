/*
 *  Copyright 2015 Yahoo Inc.
 *  Licensed under the terms of the zLib license. Please see LICENSE file for terms.
 */
package com.flurry.samples.tumblrsharing;

import android.location.Location;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Calendar;
import java.util.Date;

/**
 * Class that calls the Flickr APIs.
 */
public class FlickrClient {

    private final String FLICKR_API_KEY = "88cb9d8a33b050e7a083a88c05a99a55";
    private AsyncHttpClient client;
    public static final String TAG = FlickrClient.class.getSimpleName();
    public static final String PHOTO_FEED_URL = "https://api.flickr.com/services/rest/";

    /**
     * Constructor
     *
     */
    public FlickrClient() {
        this.client = new AsyncHttpClient();
    }

    /**
     * Sets request parameters and calls the flickr.photos.search API
     *
     * @param lastLocation  location of user
     * @param handler   handler for JSON response object
     */
    public void getPhotoFeed(Location lastLocation, JsonHttpResponseHandler handler) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -3);
        Date minTakenDate = cal.getTime();

        RequestParams params = new RequestParams();
        params.put("method", "flickr.photos.search");
        params.put("api_key", FLICKR_API_KEY);
        params.put("privacy_filter", 1);
        params.put("per_page", 50);
        params.put("format", "json");
        params.put("nojsoncallback", 1);
        params.put("sort", " interestingness-desc");
        params.put("has_geo", 1);
        params.put("min_taken_date", Long.valueOf(minTakenDate.getTime() / 1000));
        //default location set to San Francisco, if location not obtained from anywhere
        if (lastLocation != null) {
            params.put("lat", lastLocation.getLatitude());
            params.put("lon", lastLocation.getLongitude());
            AnalyticsHelper.logLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
        else {
            params.put("lat", "37.8197");
            params.put("lon", "122.4786");
            AnalyticsHelper.logLocation(0,0);
        }

        client.get(PHOTO_FEED_URL, params, handler);
    }

    /**
     * Sets request parameters and calls the flickr.photos.getInfo API
     *
     * @param id     photo id
     * @param secret    photo secret
     * @param handler   handler for JSON response object
     */
    public void getPhotoDetailFeed(String id, String secret, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("method", "flickr.photos.getInfo");
        params.put("api_key", FLICKR_API_KEY);
        params.put("photo_id", id);
        params.put("secret", secret);
        params.put("format", "json");
        params.put("nojsoncallback", 1);
        client.get(PHOTO_FEED_URL, params, handler);
    }
}
