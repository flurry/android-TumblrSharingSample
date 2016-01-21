[![Build Status](https://travis-ci.org/flurry/android-TumblrSharingSample.svg?branch=master)](https://travis-ci.org/flurry/android-TumblrSharingSample)

# Flurry Tumblr In-App Sharing Sample App - Android

This project showcases how Tumblr Sharing can be integrated into your Android App.  This is a simple Nearby Photos app which uses the user’s location to display a feed of public Flickr photos that have been taken in the same area. On clicking on a photo, it will show the user a detailed view of the photo. Here on clicking on the Tumblr icon, you can share the Flickr photo to your Tumblr feed.

## Features

* Integration of Tumblr Sharing -  Demonstrates integration of Flurry Tumblr in-app Sharing

## Prerequisites

- Android Studio
- Android SDK Platform Tools r21 or later
- Android SDK Build Tools r21 or later
- Runtime of Android 4.0.3 (API 15) or later
- Flickr API key - To get this API key, follow instructions 
[here](https://www.flickr.com/services/apps/create/).
- Flurry Analytics and Ads SDK (6.0.0 and up)
- Flurry API key - To get this API key, follow instructions 
[here](https://developer.yahoo.com/flurry/docs/analytics/gettingstarted/android/#get-your-api-keys).
- Tumblr API key and secret - To get this API key, first register your app. Follow instructions [here](https://www.tumblr.com/oauth/apps).
- This app uses two third party libraries - 
  * [Picasso](http://square.github.io/picasso/) - For remote image loading
  * [Android AsyncHTTPClient](http://loopj.com/android-async-http/) - For asynchronous network requests

## Project Setup

1. Clone your repository and open the project in Android Studio. 
2. Add your Flickr API key in FlickrClient.java
3. Add your Flurry API key in FlurryAnalyticsSampleAndroidApp.java
4. Add your Tumblr API key and secret in PhotoDetailActivity.java
5. Log into www.flurry.com and navigate to YourApplication > Manage. Select App Info. In the Application Info panel, click Enable content sharing.
6. Build project and launch on emulator or device. 

Note - If using an emulator, make sure to enable GPS.

For more info on getting started with Flurry for Android, see [here](https://developer.yahoo.com/flurry/docs/analytics/gettingstarted/android/).

For more info on enabling Tumblr In-App Sharing, see [here](https://developer.yahoo.com/flurry/docs/tumblrsharing/android/)

## Copyright

    Copyright 2015 Yahoo Inc.
    Licensed under the terms of the zLib license. Please see LICENSE file for terms.
