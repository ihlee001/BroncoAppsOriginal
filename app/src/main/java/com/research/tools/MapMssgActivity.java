package com.research.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.w3c.dom.Document;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

public class MapMssgActivity extends FragmentActivity  {

    // Google Map
	private Handler uiCallback;
    private GoogleMap googleMap;
    private MarkerOptions markerOptions;
    Location location;
    Map<Integer, PolygonHolder> polygonholder_list;
    ArrayList<Building> buildings;
    ArrayList<Event> events;
    ArrayList<Marker> markers = new ArrayList<Marker>();
    Map<Integer, Marker> polygon_markers = new HashMap<Integer, Marker>();
    Thread plotUserMovement;
    LatLng startingLocation;
    private Location previous_location;
    private Polygon[] polygon_list;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_mssg);

        try {
            // Loading map
            initializeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
        button_listener();

        //Temp for events until we get things sorted out
        temp_grab_events();
        //getting btn_find_location
        plotUserMovement = new DynamicLocation();
        Button btn_find = (Button) findViewById(R.id.btn_find_location);
        Button get_current_location = (Button) findViewById(R.id.get_current_location);
        Button go_btn = (Button) findViewById(R.id.go_button);
        
        //Define the listener to btn_find_location
        OnClickListener findClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting reference to EditText to get the user input location
                EditText etLocation = (EditText) findViewById(R.id.et_location);

                // Getting user input location
                String location = etLocation.getText().toString();

                if(location!=null && !location.equals("")){
                    new GeocoderTask().execute(location);
                }
            }
        };
        OnClickListener getCurrentListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
            	centerMapOnMyLocation();
            }
        };
        OnClickListener goClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
            	mapDirections();
            }
        };
        
        btn_find.setOnClickListener(findClickListener);
        get_current_location.setOnClickListener(getCurrentListener);
        go_btn.setOnClickListener(goClickListener);
       if(getIntent().getStringExtra("location") != null){
    	   String[] locationStrings = getIntent().getStringExtra("location").split("M");
    	   startingLocation = new LatLng(Double.parseDouble(locationStrings[0]),Double.parseDouble(locationStrings[1]));
    	   
    	   markerOptions = new MarkerOptions();
           markerOptions.position(startingLocation);
           markerOptions.title("here");

           googleMap.addMarker(markerOptions);
    	   googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startingLocation,
                   (float) 17.0));
    	   
       }
  
       //new PlotDirectionTask().execute("huy");



       try {
		initializeBuilding();
        createPolygons();
        initializeEvents();


	} catch(IOException e) {
        Log.d("test", "why did it fail");
		e.printStackTrace();
	}
       uiCallback = new Handler () {
    	    public void handleMessage (Message msg) {
    	    	Marker marker = googleMap.addMarker(buildings.get(msg.arg1).getMarker());
				marker.showInfoWindow();
    	    }
    	};
 
       
    }

    private void initializeBuilding() throws IOException {
		buildings = new ArrayList<Building>();
		
//		buildings.add(new Building(new LatLng(34.129167, -117.441866), "Neighborhood"));
//		buildings.add(new Building(new LatLng(34.059366, -117.823947), "One"));
//		buildings.add(new Building(new LatLng(34.058784,-117.824454), "Building 8: College of Science"));
//		buildings.add(new Building(new LatLng(34.058886, -117.823274), "Building 94: University office building"));
		
		BuildingParser parser = new BuildingParser(Environment.getExternalStorageDirectory().getPath() + "/Download/buildingList.txt");

		//create buildings ArraysList
		buildings = parser.getBuildingArray();
		
		//This is for testing
		
		for( int i  =0 ; i < buildings.size(); i++){
			Log.i("Building", buildings.get(i).getString());
		}


		// adapt the arraylist to spinner
		ArrayList<String> buildingName = new ArrayList<String>();
		for(int i = 0; i < buildings.size(); i++){
			buildingName.add(Integer.toString(buildings.get(i).getID()) + ":"+buildings.get(i).getName());
		}
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
        (this, android.R.layout.simple_spinner_item,buildingName);
         
		dataAdapter.setDropDownViewResource
        (android.R.layout.simple_spinner_dropdown_item);
		Spinner building = (Spinner) findViewById(R.id.building);
		building.setAdapter(dataAdapter);
		
		//Add each Building marker on the map
/*		for (int i = 0; i < buildings.size(); i++){
            Log.d("size", "" + buildings.size());
			googleMap.addMarker(buildings.get(i).getMarker());
		}*/
	}

    //Create polygons
    public void createPolygons(){
        polygonholder_list = new HashMap<Integer, PolygonHolder>(10);
        polygonholder_list.put(8, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.05871267583157, -117.82521486282349),
                new LatLng(34.05891266584845, -117.8247481584549), new LatLng(34.05830380588406, -117.82436728477478),
                new LatLng(34.058094925910545, -117.82487154006958)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 8));

        polygonholder_list.put(5, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.057886045422, -117.82490104437),
                new LatLng(34.058126035726, -117.82434314489), new LatLng(34.057659387289, -117.82404541969),
                new LatLng(34.057414951368, -117.82460868359)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 5));

        polygonholder_list.put(97, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.057748272904, -117.82376646996),
                new LatLng(34.05808603739, -117.82296985388), new LatLng(34.057743828625, -117.82275795937),
                new LatLng(34.057408284923, -117.82354921103)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 97));

        polygonholder_list.put(6, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.058768228661, -117.82310664654),
                new LatLng(34.058874889993, -117.82284110785), new LatLng(34.058392690823, -117.82254606485),
                new LatLng(34.058274918259, -117.8228276968)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 6));

        polygonholder_list.put(94, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.059408194635, -117.82354384661),
                new LatLng(34.059545964455, -117.82320320606), new LatLng(34.058979329083, -117.82284110785),
                new LatLng(34.058832669898, -117.82317638397)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 94));

        polygonholder_list.put(1, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.059494856322, -117.82485276461),
                new LatLng(34.059832613848, -117.82400786877), new LatLng(34.059588184194, -117.82386034727),
                new LatLng(34.059248203594, -117.82471060753)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 1));

    }

    //Sets the event markers in the set locations
    private void initializeEvents() throws IOException{
        events = new ArrayList<Event>();

        EventParser parser = new EventParser(Environment.getExternalStorageDirectory().getPath() + "/Download/buildingList2.txt");

        events = parser.getEvents();

        for(int i = 0; i < events.size(); i++){
            Log.i("Events", events.get(i).getString());
        }

        for(int i = 0; i < events.size(); i++){
            int bn = events.get(i).building_number;
            if(bn == 0){
                Marker mark = googleMap.addMarker(events.get(i).getMarker());
                markers.add(mark);
            }
            else{
                polygonholder_list.get(bn).add_event(events.get(i));
            }
        }
        for(PolygonHolder ph : polygonholder_list.values()){
            Marker mark = googleMap.addMarker(ph.getMarker());
            polygon_markers.put(ph.id, mark);
        }
    }


	/**
     * function to load map. If map is not created it will create it for you
     * */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	private void initializeMap() {
//    	GMapV2Direction md = new GMapV2Direction();
//    	LatLng start;
//    	LatLng end;
//    	start = new LatLng(34.05755723, -117.82353688);
//    	end  = new LatLng(34.06205066,-117.82055747);
//    	Document doc = md.getDocument(start, end, GMapV2Direction.MODE_WALKING);
    	if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        centerMapOnMyLocation();
        googleMap.setMyLocationEnabled(false);
//        ArrayList<LatLng> directionPoint = md.getDirection(doc);
//        PolylineOptions rectLine = new PolylineOptions().width(3).color(
//                Color.RED);
//        for (int i = 0; i < directionPoint.size(); i++) {
//            rectLine.add(directionPoint.get(i));
//        }
//        Polyline polylin = googleMap.addPolyline(rectLine);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeMap();
        

    }

    private void centerMapOnMyLocation() {

        googleMap.setMyLocationEnabled(false);
        Location location = getLocation();
        LatLng myLocation;
        if (location != null) {
            myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
            markerOptions = new MarkerOptions();
            markerOptions.position(myLocation);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
            markerOptions.title("you");

            googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    (float) 16.0));
        }

    }

    void button_listener(){
        Button button = (Button) findViewById(R.id.refresh_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new JSONAsyncTask().execute("http://broncomaps.com/edit/events/data/");
                try{
                    initializeEvents();
                }
                catch(IOException e){
                    Log.e("intializeEvents", "failed");
                }
            }
        });
    }

    public void temp_grab_events(){
        String path = Environment.getExternalStorageDirectory().getPath() + "/Download";
        File file = new File(path, "BuildingList2.txt");
        try{
            FileOutputStream stream = new FileOutputStream(file);
            String towrite = "College of Sci event\tscience experiments\t8\tout in front\t2015-01-21\t2016-02-05\t00:00:00\t00:00:00\t-117.824643552303, 34.0587193421731\n";
            stream.write(towrite.getBytes());
            towrite = "Active Sock Fundraiser\tAmerican Marketing Association\t0\tOutdoor Spaces\t2015-01-21\t2016-02-21\t00:00:00\t00:00:00\t-117.823745012283, 34.0585926815949\n";
            stream.write(towrite.getBytes());
            Log.d("temp_events", "yay it worked");
        }
        catch(IOException e){
            Log.e("temp_events", "failed");
        }

    }


   /* class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls){
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httppost = new HttpPost("http://broncomaps.com/edit/events/data/");
            httppost.setHeader("Content-type", "application/json");

            String path = Environment.getExternalStorageDirectory().getPath() + "/Download";

            File file = new File(path, "BuildingList2.txt");

            InputStream inputStream = null;
            String result;
            JSONArray jArray;

            try {
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();
                reader.close();

                FileOutputStream stream = new FileOutputStream(file);
                jArray = new JSONArray(result);
                for (int i=0; i < jArray.length(); i++)
                {
                    try {
                        org.json.JSONObject jobject = jArray.getJSONObject(i);
                        String towrite = jobject.getString("title") + "\t" +
                                jobject.getString("description") + "\t" +
                                jobject.getString("location") + "\t" +
                                jobject.getString("location_details") + "\t" +
                                jobject.getString("start_date") + "\t" +
                                jobject.getString("end_date") + "\t" +
                                jobject.getString("start_time") + "\t" +
                                jobject.getString("end_time") + "\t" +
                                jobject.getString("Lon") + ", " +
                                jobject.getString("Lat") + "\n";
                        stream.write(towrite.getBytes());
                        Log.d("json", towrite);
                    } catch (org.json.JSONException e) {
                        // Oops
                    }
                }

                stream.close();
                Log.d("Finisher", "Try Method Complete");
            } catch (FileNotFoundException e){
                Log.e("login activiyt", "file not found");
            } catch (IOException e) {
                Log.e("login activity", "Can not read file");
            } catch (org.json.JSONException e){
                // Oops
            }
            finally {
                try{
                    if(inputStream != null) inputStream.close();
                }catch(Exception squish){}
            }
            return false;
        }
    }*/


    
    public Location getLocation(){
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Activity.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
               
                public void onLocationChanged(Location location){
                        makeUseOfNewLocation(location);
                        
                }

                @Override
                public void onProviderDisabled(String provider) {
                        // TODO Auto-generated method stub

                }
                @Override
                public void onProviderEnabled(String provider) {
                        // TODO Auto-generated method stub

                }
                @Override
                public void onStatusChanged(String provider, int status,
                                Bundle extras) {
                        // TODO Auto-generated method stub

                }
        };
	    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,40*1000,2, locationListener);
	       
	    if(location != null){
	    	return location;
	    }
	    else {
	    	return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    }
	}
	
	private void makeUseOfNewLocation(Location location){
	        this.location = location;
	        
	}

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(String... locationName) {
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(locationName[0],3);
            }catch(IOException e){
                e.printStackTrace();
            }
            return addresses;

        }

        @Override
        protected void onPostExecute(List<Address> addresses)
        {
            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map

            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                address.getCountryName());

                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);

                googleMap.addMarker(markerOptions);

                // Locate the first location
                if(i==0)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
                            (float) 8.0));
            }

        }

    }
    
    class PlotDirectionTask extends AsyncTask<LatLng, Void, ArrayList<LatLng>>{

    	
		@Override
		protected ArrayList<LatLng> doInBackground(LatLng... latlong) {
		  	GMapV2Direction md = new GMapV2Direction();
	    	LatLng start;
	    	LatLng end;
	    	
	    	start = latlong[0];
	    	
	    	end  =latlong[1];
	    	Document doc = md.getDocument(start, end, GMapV2Direction.MODE_WALKING);
	    	Log.e("Direction", doc.toString());
	    	ArrayList<LatLng> directionPoint = md.getDirection(doc);
	        
	        return directionPoint;
		}
		
		@Override
		protected void onPostExecute(ArrayList<LatLng> directionPoint) {
			PolylineOptions rectLine = new PolylineOptions().width(7).color(
	                Color.BLUE);
	        for (int i = 0; i < directionPoint.size(); i++) {
	            rectLine.add(directionPoint.get(i));
	        }
	        Polyline polylin = googleMap.addPolyline(rectLine);
	        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0),
                    (float) 15.0));
		}
    	
    }

    //This refreshes everytime
    class DynamicLocation extends Thread{
    	public void run(){
    			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    			LocationListener locationListener = new LocationListener() {
    				public void onLocationChanged(Location location){
    					updateMapUI(location);
    				}

					private void updateMapUI(Location location) {
                        MarkerOptions marker = new MarkerOptions();
                        marker.position(new LatLng(location.getLatitude(), location.getLongitude()));
                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                        float[] result = new float[1];
                        if(previous_location != null){
                            Location.distanceBetween(previous_location.getLatitude(), previous_location.getLongitude(), location.getLatitude(), location.getLongitude(), result);
                            Log.d("starting location", "it reaches here3");
                            if(result[0] > 3 ){
                                Log.d("starting location", "it reaches here");
                                MapMssgActivity.this.googleMap.addMarker(marker);
                                updateGPSCamera();
                            }
                        }
                        else{
                            previous_location = location;
                            MapMssgActivity.this.googleMap.addMarker(marker);
                            Log.d("starting location", "it reaches here2");
                            updateGPSCamera();
                        }
                        previous_location = location;


						MapMssgActivity.this.location = location;
                        //Log.d("starting location", "it reaches here");
						startingLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        for(int i = 0; i < markers.size(); i++){
                            Marker mark = markers.get(i);
                            LatLng event_loc = mark.getPosition();

                            Location.distanceBetween(event_loc.latitude, event_loc.longitude, startingLocation.latitude, startingLocation.longitude, result);
                            if(result[0] <= 40) {
                                mark.setVisible(true);
                            }
                            else {
                                mark.setVisible(false);
                            }
                        }
                        for(PolygonHolder ph : polygonholder_list.values()){
                            Location.distanceBetween(ph.center.latitude, ph.center.longitude, startingLocation.latitude, startingLocation.longitude, result);
                            if(result[0] <= 40 + ph.radius){
                                ph.polygon.setVisible(true);
                                polygon_markers.get(ph.id).setVisible(true);

                            }
                            else{
                                ph.polygon.setVisible(false);
                                polygon_markers.get(ph.id).setVisible(false);

                            }
                        }
                        //Log.d("starting location", "it creates it");

						
					}

					@Override
					public void onProviderDisabled(String paramString) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onProviderEnabled(String paramString) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onStatusChanged(String paramString,
							int paramInt, Bundle paramBundle) {
						// TODO Auto-generated method stub
						
					}
    				
    			};
    			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    	}
    }

    public void updateGPSCamera(){
        LatLng loc =new LatLng(location.getLatitude(), location.getLongitude());
        float zoom= (float) 17.00;
        //Set bearing here maybe?
        float bearing= location.getBearing();
        float tilt= (float) 90.00;
        CameraPosition cameraPosition = new CameraPosition(loc, zoom, tilt, bearing);
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                cameraPosition));

    }

    public void mapDirections(){
    	plotUserMovement.run();
        location = getLocation();
        startingLocation = new LatLng(location.getLatitude(), location.getLongitude());
        final Thread t = new Thread(){
        	@Override
        	public void run(){
        		try{
        			sleep(25000);
        			
        			
        		}catch(InterruptedException exception){
        			exception.printStackTrace();
        		}
        		if(startingLocation == null){
        			Log.e("mapdirection", "no starting location");
        			run();
    			}
    			else{
    				//progress.dismiss();
    				PlotDirectionTask task = new PlotDirectionTask();
    		    	
    		    	LatLng[] arrayLatLngs = new LatLng[2];
    		    	
    			    arrayLatLngs[0] = startingLocation;
    				arrayLatLngs[1] = getEndingLocation();
    				if(arrayLatLngs[1] == null)
    				{
    					Log.e("mapdirection", "no ending location");
    				}
    				task.execute(arrayLatLngs);
    				int period = 1*1000;
    		        
    		        Timer timer = new Timer();
    		        
    		        timer.scheduleAtFixedRate(new TimerTask(){
    		        	public void run(){
    		        		updateDistanceToUser();
    		        		updateMap();
    		        	}

    					

    					private void updateDistanceToUser() {
    						for(int i =0; i<buildings.size(); i++){
    							buildings.get(i).setDistance(calCulateDistance(startingLocation, buildings.get(i).getLocation(),'K'));
    						}
    						
    					}
    					
    					private double calCulateDistance(LatLng start, LatLng end, char unit){
    						double lat1 = start.latitude;
    						double lat2 = end.latitude;
    						double lon1 = start.longitude;
    						double lon2 = end.longitude;
    						double theta = lon1 - lon2;
    					      double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    					      dist = Math.acos(dist);
    					      dist = rad2deg(dist);
    					      dist = dist * 60 * 1.1515;
    					      if (unit == 'K') {
    					        dist = dist * 1.609344;
    					      } else if (unit == 'N') {
    					        dist = dist * 0.8684;
    					        }
    					      return (dist);
    					    }

    					    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    					    /*::  This function converts decimal degrees to radians             :*/
    					    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    					    private double deg2rad(double deg) {
    					      return (deg * Math.PI / 180.0);
    					    }

    					    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    					    /*::  This function converts radians to decimal degrees             :*/
    					    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    					    private double rad2deg(double rad) {
    					      return (rad * 180.0 / Math.PI);
    					    }
    					
    					private void updateMap() {
    						for(int i = 0; i< buildings.size(); i++){
    							if(buildings.get(i).getDistance()<0.094697 && buildings.get(i).getMarker().getSnippet()!=null){
    								Message message = new Message();
    								message.arg1 = i;
    								uiCallback.sendMessage(message);
    							}
    						}
    						
    					}
    		        }, 0, period);
    			}
        	}
        };
        
        t.start();
    	
        
		
		    
		    
    }
    
    public LatLng getEndingLocation(){
    	Spinner building = (Spinner) findViewById(R.id.building);
    	int position = building.getSelectedItemPosition();
//    	Log.e("Ending location", )
    	return buildings.get(position).getLocation();
    }



//	@Override
//	public void onAccuracyChanged(Sensor sensor, int accuracy) {
//		
//	}
//
//	@Override
//	public void onSensorChanged(SensorEvent event) {
//		if(location!= null){
//			float degree = Math.round(event.values[0]);
//			LatLng loc =new LatLng(location.getLatitude(), location.getLongitude());
//			float zoom= (float) 20.00;
//			float bearing= degree;
//			float tilt= (float) 90.00;
//			CameraPosition cameraPosition = new CameraPosition(loc, zoom, tilt,bearing);
//			googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
//		             cameraPosition));
//		}
//	}
    
   
}   
  

  