/*
 *  Copyright 2015 Yahoo Inc.
 *  Licensed under the terms of the zLib license. Please see LICENSE file for terms.
 */
package com.flurry.samples.tumblrsharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.tumblr.PhotoPost;
import com.flurry.android.tumblr.PostListener;
import com.flurry.android.tumblr.TextPost;
import com.flurry.android.tumblr.TumblrShare;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;


/**
 * Class that manages the photo detail screen.
 */
public class PhotoDetailActivity extends Activity {

    private Photo photo;
    private ImageView photoImage;
    private TextView title;
    private ImageView tumblrIcon;
    private FlickrClient flickrClient;
    public static final String LOG_TAG = PhotoDetailActivity.class.getSimpleName();

    private static final String TUMBLR_API_KEY = "j8f6rocxuAOZUSmczCtjoTGqpYkFIalcLvHAk1xnNkQiDDNLtH";
    private static final String TUMBLR_API_SECRET = "pxPfnQnR5HX7z2wjXKcVgqYDbSsS3OykaYSTn6MbKXAUna1Tfb";

    /**
     * OnCreate Activity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        photoImage = (ImageView) findViewById(R.id.detail_page_photo);
        title = (TextView) findViewById(R.id.title);
        tumblrIcon = (ImageView)findViewById(R.id.tumblr_icon);

        photo = (Photo) getIntent().getSerializableExtra(com.flurry.samples.tumblrsharing.PhotoFeedActivity.PHOTO_DETAIL_KEY);
        loadPhotoDetails(photo);

        setupTumblrSharing();
    }

    /**
     * Setup Tumblr Sharing for the Flickr Photo
     *
     */
    private void setupTumblrSharing() {

        TumblrShare.setOAuthConfig(TUMBLR_API_KEY, TUMBLR_API_SECRET);

        Bitmap tumblrButtonImg = TumblrShare.getTumblrImage();
        tumblrIcon.setImageBitmap(tumblrButtonImg);

        Log.i(LOG_TAG, "Setting up listeners on the tumblr icon");

        tumblrIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (photo.getPhotoUrl() != null) {

                    PhotoPost postImage = new PhotoPost(photo.getPhotoUrl());
                    postImage.setCaption(photo.getTitle());
                    postImage.setAndroidDeeplink("http://www.yahoo.com");
                    postImage.setIOSDeepLink("http://www.yahoo.com");
                    postImage.setWebLink("http://www.yahoo.com");
                    postImage.setPostListener(postListener);
                    TumblrShare.post(PhotoDetailActivity.this, postImage);

                } else {
                    TextPost postText = new TextPost(photo.getTitle());
                    postText.setTitle(photo.getTitle());
                    postText.setAndroidDeeplink("http://www.yahoo.com");
                    postText.setIOSDeepLink("http://www.yahoo.com");
                    postText.setWebLink("http://www.yahoo.com");
                    postText.setPostListener(postListener);
                    TumblrShare.post(PhotoDetailActivity.this, postText);
                }
            }
        });
    }

    /**
     * OnResume Activity
     */
    @Override
    protected void onResume() {
        super.onResume();

        AnalyticsHelper.logPageViews();
        Log.i(LOG_TAG, "Logging page views");
    }

    /**
     * Load Photo Details into View. Load photo from Picasso into View.
     *
     * @param photo    Photo object
     */
    public void loadPhotoDetails(final Photo photo) {

        flickrClient = new FlickrClient();

        HashMap<String, String> fetchPhotoStatEventParams = new HashMap<>(2);
        fetchPhotoStatEventParams.put(AnalyticsHelper.PARAM_FETCH_PHOTO_ID, String.valueOf(photo.getPhotoId()));
        fetchPhotoStatEventParams.put(AnalyticsHelper.PARAM_FETCH_PHOTO_SECRET, String.valueOf(photo.getSecret()));
        AnalyticsHelper.logEvent(AnalyticsHelper.EVENT_FETCH_PHOTO_STATS, fetchPhotoStatEventParams, true);

        Log.i(LOG_TAG, "Logging event: " + AnalyticsHelper.EVENT_FETCH_PHOTO_STATS);

        flickrClient.getPhotoDetailFeed(photo.getPhotoId(), photo.getSecret(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int code, Header[] headers, JSONObject body) {

                AnalyticsHelper.endTimedEvent(AnalyticsHelper.EVENT_FETCH_PHOTO_STATS);

                if (body != null) {
                    try {
                        photo.setOwner(body.getJSONObject("photo").getJSONObject("owner").getString("realname"));
                        photo.setDateTaken(body.getJSONObject("photo").getJSONObject("dates").getString("taken"));

                        long datePosted = Long.parseLong(body.getJSONObject("photo").getJSONObject("dates").getString("posted"));
                        Date date = new Date(datePosted * 1000L);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));

                    } catch (JSONException e) {
                        AnalyticsHelper.logError(LOG_TAG, "Deserialize photo detail JSONObject error.", e);
                    }

                } else {
                    AnalyticsHelper.logError(LOG_TAG, "Response body is null", null);
                }

                if (photo.getTitle() != null && !photo.getTitle().trim().equals("")) {
                    title.setText(photo.getTitle());
                }

                Picasso.with(PhotoDetailActivity.this)
                        .load(photo.getPhotoUrl())
                        .error(R.drawable.noimage)
                        .placeholder(R.drawable.noimage)
                        .into(photoImage);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                AnalyticsHelper.logError(LOG_TAG, "Failure in fetching photo details", null);
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    void displayAlert(String title, String msg) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(PhotoDetailActivity.this);
        alertBox.setTitle(title).setMessage(msg)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setCancelable(true).show();
    }

    PostListener postListener = new PostListener() {

        @Override
        public void onPostSuccess(Long postId) {
            Log.i("PostListener", "onPostSuccess" + postId);
            displayAlert("Post success", "Post Id: " + postId);
        }

        @Override
        public void onPostFailure(String errorMessage) {
            Log.i("PostListener", "onPostFailure" + errorMessage);
            displayAlert("Post failure", errorMessage);
        }
    };
}
