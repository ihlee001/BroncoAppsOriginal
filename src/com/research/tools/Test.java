package com.research.tools;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;

public class Test {
	
	final Handler handler = new Handler();
	private int currentlyUsing = -1;
	private Timer timer;
	private TimerTask updateInterface = new TimerTask() {
	    @Override
	    public void run() {
	        handler.post(new Runnable() {
	            @Override
	            public void run() {
	            	
	            }
	        });
	    }
	};
	
	public Test(int hours){
		timer.schedule(updateInterface,1000);
		timer.cancel();
		
	}
	public Test(){
		
	}
    public void SetAlarm(Context context)
    {
    	
    }
}
class Listener implements LocationListener{
private Location loc;
private char type; 
	public Location getLocation(){
		return loc;
	}
	@Override
	public void onLocationChanged(Location location) {
		loc = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	
}