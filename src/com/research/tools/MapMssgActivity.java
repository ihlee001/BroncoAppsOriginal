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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Document;

import com.google.android.gms.drive.internal.e;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.research.tools.MyLocation.LocationResult;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.Activity;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;

public class MapMssgActivity extends FragmentActivity  {

    // Google Map
	private Handler uiCallback;
    private GoogleMap googleMap;
    private MarkerOptions markerOptions;
    private ArrayList<MarkerOptions> algorithmCoordinatesArrayList;
    private ArrayList<MarkerOptions> apiCoordinatesArrayList;
    private LatLng latLng;
    Location location;
    ArrayList<Building> buildings;
    Thread plotUserMovement;
    LatLng startingLocation;
    private ProgressDialog progress;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;
    private boolean zoomCamera= true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_mssg);

        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
        button_listener();
        button_listener2();

        //getting btn_find_location
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
        progress = new ProgressDialog(this);
       if(getIntent().getStringExtra("location") != null){
    	   String[] locationStrings = getIntent().getStringExtra("location").split("M");
    	   LatLng lattlongg = new LatLng(Double.parseDouble(locationStrings[0]),Double.parseDouble(locationStrings[1]));
    	   
    	   markerOptions = new MarkerOptions();
           markerOptions.position(lattlongg);
           markerOptions.title("here");

           googleMap.addMarker(markerOptions);
    	   googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lattlongg,
                   (float) 17.0));
    	   
       }
  
       //new PlotDirectionTask().execute("huy");
       plotUserMovement = new DynamicLocation();
       
       
       try {
		initializeBuilding();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       uiCallback = new Handler () {
    	    public void handleMessage (Message msg) {
    	    	Marker marker = googleMap.addMarker(buildings.get(msg.arg1).getMarker());
				marker.showInfoWindow();
    	    }
    	};
    	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 
       
    }

    private void initializeBuilding() throws IOException {
		buildings = new ArrayList<Building>();
		
//		buildings.add(new Building(new LatLng(34.129167, -117.441866), "Neighborhood"));
//		buildings.add(new Building(new LatLng(34.059366, -117.823947), "One"));
//		buildings.add(new Building(new LatLng(34.058784,-117.824454), "Building 8: College of Science"));
//		buildings.add(new Building(new LatLng(34.058886, -117.823274), "Building 94: University office building"));
		
		BuildingParser parser = new BuildingParser("/mnt/sdcard/Download/buildingList.txt");
		
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
		for (int i = 0; i < buildings.size(); i++){
			googleMap.addMarker(buildings.get(i).getMarker());
		}
	}

	/**
     * function to load map. If map is not created it will create it for you
     * */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	private void initilizeMap() {
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
        //centerMapOnMyLocation();
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
        initilizeMap();
        

    }

    private void centerMapOnMyLocation() {

        googleMap.setMyLocationEnabled(false);
        Location location = getLocation();
        LatLng myLocation = null;
        if (location != null) {
            myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
            markerOptions = new MarkerOptions();
            markerOptions.position(myLocation);
            markerOptions.title("you");

            googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    (float) 8.0));
        }
        else
        {
            //do nothing
        }

    }

    void button_listener() {
        Button button = (Button) findViewById(R.id.algorithm_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Open the text file and put markers in there
                 */
            	createMarkers(1);
            	
            }
        });
    }
    
    void button_listener2() {
        Button button = (Button) findViewById(R.id.api_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * open the text file and put markers in there
                 */
            	createMarkers(2);

            }
        });
    }
    
    public void createMarkers(int option)
    {
    	FileReader fr = null;
    	try{
	    	if(option == 1){
	    		fr = new FileReader("/mnt/sdcard/Download/AlgorithmLocation1111.txt");
	    	}
	    	else {
	    		fr = new FileReader("/mnt/sdcard/Download/GPSlocation1111.txt");
			}
    	}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
    	
    	BufferedReader br = null;
    	
    	try{
    		String dataString;
    		br = new BufferedReader(fr);
    		LatLng lattlongg = new LatLng(37.090240,-95.712891);
    		while((dataString = br.readLine()) !=null){
    			MarkerOptions coordinate = new MarkerOptions();
    			lattlongg = new LatLng(Double.parseDouble(dataString),Double.parseDouble(br.readLine()));
    	    	   
    			coordinate = new MarkerOptions();
    			coordinate.position(lattlongg);
    			coordinate.title("here");

    			googleMap.addMarker(coordinate);
    		}
    		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lattlongg,
                    (float) 8.0));
    	}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if(br != null){
					br.close();
				}
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
    }
    
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
                latLng = new LatLng(address.getLatitude(), address.getLongitude());

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
			PolylineOptions rectLine = new PolylineOptions().width(3).color(
	                Color.BLUE);
	        for (int i = 0; i < directionPoint.size(); i++) {
	            rectLine.add(directionPoint.get(i));
	        }
	        Polyline polylin = googleMap.addPolyline(rectLine);
	        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0),
                    (float) 15.0));
		}
    	
    }
    
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
						MapMssgActivity.this.googleMap.addMarker(marker);
						
						
						LatLng loc =new LatLng(location.getLatitude(), location.getLongitude());
						float zoom= (float) 18.00;
						float bearing= (float) 0.00;
						float tilt= (float) 90.00;
						CameraPosition cameraPosition = new CameraPosition(loc, zoom, tilt,bearing);
						if(zoomCamera == true){
							googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
					             cameraPosition));
							zoomCamera = false;
						}
						MapMssgActivity.this.location = location; 
						startingLocation = new LatLng(location.getLatitude(), location.getLongitude());
						
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
    
    public void mapDirections(){
    	plotUserMovement.run();
        
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
//		// TODO Auto-generated method stub
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
  

  