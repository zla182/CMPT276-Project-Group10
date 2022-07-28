package com.example.project276.Model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;

public class ClusterItem implements com.google.maps.android.clustering.ClusterItem {
    private LatLng mPosition;
    private String mTitle;
    private BitmapDescriptor mHazard;

    public ClusterItem(double lat, double lng, String mTitle, BitmapDescriptor mHazard) {
        this.mPosition = new LatLng(lat, lng);
        this.mTitle = mTitle;
        this.mHazard = mHazard;
    }

    public ClusterItem(double lat, double lng, String hazardIcon) {
        mPosition = new LatLng(lat, lng);
    }


    public LatLng getPosition() {
        return mPosition;
    }


    public String getTitle() {
        return mTitle;
    }

    public BitmapDescriptor getHazard() {
        return mHazard;
    }

    public String getSnippet() {
        return "";
    }
}

