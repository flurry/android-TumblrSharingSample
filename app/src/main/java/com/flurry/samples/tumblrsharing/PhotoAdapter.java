/*
 *  Copyright 2015 Yahoo Inc.
 *  Licensed under the terms of the zLib license. Please see LICENSE file for terms.
 */
package com.flurry.samples.tumblrsharing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that manages data for grid view
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    public static final String LOG_TAG = PhotoAdapter.class.getSimpleName();
    private ArrayList<Photo> photoList;
    private Context context;
    private static OnItemClickListener listener;

    /**
     * Constructor
     *
     * @param photoArray     Arraylist of photos
     */
    public PhotoAdapter(ArrayList<Photo> photoArray) {
        photoList = photoArray;
    }

    /**
     * Inflates a layout from XML and returns the holder
     *
     * @param parent     ViewGroup
     * @param viewType     viewType
     */
    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View photoView = inflater.inflate(R.layout.adapter_item_photo, parent, false);

        ViewHolder viewHolder = new ViewHolder(context, photoView);
        return viewHolder;
    }

    /**
     * Populating data into the item through holder
     *
     * @param viewHolder     photo view holder
     * @param position     photo position
     */
    @Override
    public void onBindViewHolder(PhotoAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Photo photo = photoList.get(position);

        // Set item views based on the data model
        ImageView photoImage = viewHolder.photoView;

        Picasso.with(context)
                .load(photo.getPhotoUrl())
                .resize(400,400)
                .centerCrop()
                .into(photoImage);
    }

    /**
     * get number of photos
     */
    @Override
    public int getItemCount() {
        return photoList.size();
    }


    /**
     * Get single photo from photo list
     *
     * @param position     photo position
     */
    public Photo getItem(int position) {
       return photoList.get(position);
    }

    /**
     * add photos to photo list
     *
     * @param photos     list of photos to be added
     */
    public void addPhotos(List<Photo> photos) {
        photoList.addAll(photos);
    }

    /**
     * Declaring listener interface along with methods
     */
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    /**
     * Setting listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * ViewHolder class
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView photoView;
        private Context context;

        public ViewHolder(Context context, final View itemView) {
            super(itemView);
            photoView = (ImageView) itemView.findViewById(R.id.photo);
            this.context = context;
            // Attach a click listener to the entire row view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null)
                        listener.onItemClick(itemView, getLayoutPosition());
                }
            });
        }
    }
}
