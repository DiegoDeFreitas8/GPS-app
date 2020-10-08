package com.example.gpsapp;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;

public class PollyPath {
    public Polyline polyline;
    public DirectionsLeg directionsLeg;


    public PollyPath(Polyline polyline, DirectionsLeg directionsLeg) {
        this.polyline = polyline;
        this.directionsLeg = directionsLeg;
    }

    public Polyline getPolyline() {
        return polyline;
    }


    public DirectionsLeg getLeg() {
        return directionsLeg;
    }


    @Override
    public String toString() {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + directionsLeg +
                '}';
    }
}
