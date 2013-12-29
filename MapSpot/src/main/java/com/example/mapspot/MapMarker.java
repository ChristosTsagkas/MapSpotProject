package com.example.mapspot;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Nosfistis on 4/12/2013.
 */
public class MapMarker {
    private String title;
    private String description;
    private String category;
    private LatLng position;
    private long dbID;

    public MapMarker(String title, String description, String category, double latitude, double longitude, long dbID) {
        this(title, description, category, latitude, longitude);
        this.dbID = dbID;
    }

    public MapMarker(String title, String description, String category, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.position = new LatLng(latitude, longitude);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public long getDbID() {
        return dbID;
    }

    public void setDbID(long dbID) {
        this.dbID = dbID;
    }
}
