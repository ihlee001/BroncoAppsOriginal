package com.research.tools;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class DeviceListener implements LocationListener{
	
	private int updates = 0;
	
	private Location location; 
	
	private float initialBattery;
	
	private String timeTillFirstUpdate = "";
	private long timeStarted;
	public DeviceListener(){
		setBatteryChange();
		setTimeStarted();
	}
	@Override
	public void onLocationChanged(Location location) {
		if(timeTillFirstUpdate.equals("")) timeTillFirstUpdate = getTimeRunning() + "s";
		updates++;
		Log.i("ping","ping");
		this.location = location;
		
	}
	public void setTimeStarted(){
		this.timeStarted = System.currentTimeMillis();
	}
	public void setUpdates(int i){
		this.updates = i;
	}
	public Location getLocation(){
		return location;
	}
	@Override
	public void onProviderDisabled(String provider) {
		
	}
	public float getCurrentBatteryLevel(){
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = MainActivity.getContext().registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		return  level;
		
	}
	public long getTimeRunningLong(){
		return (System.currentTimeMillis() - timeStarted) / 1000;
	}
	public void setBatteryChange(){
		this.initialBattery = getCurrentBatteryLevel();
	}
	@Override
	public void onProviderEnabled(String provider) {
		return;
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
	public String getTimeTillFirstUpdate(){
		return timeTillFirstUpdate;
	}
	public int getUpdateCount(){
		return updates;
	}
	public String getTimeRunning(){ 
		long temp = (System.currentTimeMillis() - timeStarted) / 1000;
		String ret = Long.toString(temp / (60 * 60)) + ":" + Long.toString((temp / 60) % 60) + ":" + Long.toString(temp % 60 );
		return ret;
	}
	public float getBatteryChange(){
		return initialBattery - getCurrentBatteryLevel();
	}
	public float getTime(){
		return System.currentTimeMillis() - timeStarted;
	}
}