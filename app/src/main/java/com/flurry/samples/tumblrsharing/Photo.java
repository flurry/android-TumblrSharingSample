/*
 *  Copyright 2015 Yahoo Inc.
 *  Licensed under the terms of the zLib license. Please see LICENSE file for terms.
 */
package com.flurry.samples.tumblrsharing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO class for photo object
 */
public class Photo implements Serializable {

    private static final long serialVersionUID = -1435409505345917879L;
    private String photoId;
    private String owner;
    private String secret;
    private int server;
    private int farm;
    private String title;
    private String photoUrl;
    private String dateTaken;

    public static final String LOG_TAG = Photo.class.getSimpleName();

    /**
     * Getter method for photo id
     */
    public String getPhotoId() {
        return photoId;
    }

    /**
     * Getter method for photo owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Setter method for photo owner
     *
     * @param owner     photo owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Getter method for photo secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Getter method for photo title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter method for photo owner
     *
     * @param title     photo title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter method for photo Flickr URL
     */
    public String getPhotoUrl() { return photoUrl; }

    /**
     * Getter method for photo taken date
     */
    public String getDateTaken() {
        return dateTaken;
    }

    /**
     * Setter method for photo taken date
     *
     * @param dateTaken     photo taken date
     */
    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }


    /**
     * Deserializes JSON into Photo objects
     *
     * @param photoArray     JSONArray for photo list
     */
    public static List<Photo> fromJson(JSONArray photoArray) {
        List<Photo> photoList = new ArrayList<Photo>();
        JSONObject photoObject = null;
        try {
            // Deserialize json into object fields
            for (int i = 0; i < photoArray.length(); i++) {
                Photo p = new Photo();
                photoObject = photoArray.getJSONObject(i);

                p.owner = photoObject.getString("owner");
                p.photoId = photoObject.getString("id");
                p.secret = photoObject.getString("secret");
                p.server = photoObject.getInt("server");
                p.farm = photoObject.getInt("farm");
                p.title = photoObject.getString("title");
                p.photoUrl = "https://farm" + p.farm + ".staticflickr.com/" + p.server+ "/" + p.photoId + "_" + p.secret+ "_c.jpg";
                p.photoUrl = p.photoUrl.replace(" ", "");

                photoList.add(p);
            }

        } catch (JSONException e) {
            AnalyticsHelper.logError(LOG_TAG, "Deserialize Photo Feed JSONArray Error.", e);
            return null;
        }
        // Return new object
        return photoList;
    }
}
