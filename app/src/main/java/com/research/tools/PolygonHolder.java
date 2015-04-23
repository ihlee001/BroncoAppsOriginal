package com.research.tools;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Iain on 3/4/2015.
 */
public class PolygonHolder {
    Polygon polygon;
    ArrayList<Event> event_list = new ArrayList<Event>();
    MarkerOptions marker;
    LatLng center;
    float radius;
    String snippet = "";
    int id;

    public PolygonHolder(Polygon polygon, int id){
        this.id = id;
        this.polygon = polygon;
        this.center = centroid(polygon.getPoints());
        this.radius = largest_distance(polygon.getPoints());
        marker = new MarkerOptions();
        marker.visible(true);
        marker.position(center);
        marker.title("Building " + id);
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
    }

    public void add_event(Event event){
        event_list.add(event);
        snippet += event.getTitle() + ": " + event.getDescription() + "\n";
        marker.snippet(snippet);
    }

    public LatLng centroid(List<LatLng> points) {
        double[] centroid = { 0.0, 0.0 };

        for (int i = 0; i < points.size(); i++) {
            centroid[0] += points.get(i).latitude;
            centroid[1] += points.get(i).longitude;
        }

        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        center = new LatLng(centroid[0], centroid[1]);
        return center;
    }

    public float largest_distance(List<LatLng> points){
        float longest = 0;
        float[] result = new float[1];
        for(int i = 0; i < points.size(); i++){
            Location.distanceBetween(center.latitude, center.longitude, points.get(i).latitude, points.get(i).longitude, result);
            if(result[0] > longest) longest = result[0];
        }
        return longest;
    }

    public MarkerOptions getMarker(){return marker;}
}
