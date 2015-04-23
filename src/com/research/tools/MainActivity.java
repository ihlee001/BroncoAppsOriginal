package com.research.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.sax.EndElementListener;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends FragmentActivity implements SensorEventListener {
	
private static Context context;

private boolean gpsOn = false;
private boolean wifiOn = false;
private boolean testOn = false;

private LocationManager locationManager;
private DeviceListener deviceListener;

private Timer timer;
private TimerTask timerTaskOne;
private TimerTask timerTaskTwo;
private TimerTask timerTaskThree;
private TimerTask timerTaskFour;
private TimerTask timerTaskFive;

private ToggleButton gpsButton;
private ToggleButton wifiButton;
private ToggleButton cellButton;
private ToggleButton accelButton;
private ToggleButton algroButton;
private SensorManager sensorManager;
private Button testButton;
private float batteryChange;
private float timeRunning;
private int numOfUpdate;
private double velocity;
private long lastTimeMoving;

private long lastAccelUpdate;
protected static int typeOfActivity;

//Variables for the algorithm.
private boolean isWaitingForGPS;
private int updates;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		MainActivity.context = getApplicationContext();
		
		gpsButton = (ToggleButton) findViewById(R.id.gps_button);
		wifiButton = (ToggleButton) findViewById(R.id.wifi_button);
		cellButton = (ToggleButton) findViewById(R.id.cell_button);
		accelButton =  (ToggleButton) findViewById(R.id.accelerometer_button);
		algroButton = (ToggleButton) findViewById(R.id.algorithm_button);
		testButton = (Button) findViewById(R.id.test_button);
		
		
		gpsButton.setOnCheckedChangeListener(new GPSButtonListner());
		wifiButton.setOnCheckedChangeListener(new WifiButtonListner());
		cellButton.setOnCheckedChangeListener(new CellButtonListner());
		testButton.setOnClickListener(new TestButtonListener());
		accelButton.setOnCheckedChangeListener(new AccButtonListner());
		algroButton.setOnCheckedChangeListener(new AlgorithmButtonListener());
		lastAccelUpdate = System.currentTimeMillis();
	}
	public void onGPSButtonClick(){
		Log.i("b","gps");
		wifiButton.setChecked(false);
		cellButton.setChecked(false);
		accelButton.setChecked(false);
		
		
		if(!gpsOn){
			CharSequence text = "Test started, please wait " + getTimeToRun() +" minutes.";
			if(getTimeToRun() < 1){
				text = "Test started.";
			}
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		deviceListener = new DeviceListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, deviceListener);
		final Handler handler = new Handler();
		timer = new Timer(false);
		timerTaskOne = new TimerTask() {
		    @Override
		    public void run() {
		        handler.post(new Runnable() {
		            @Override
		            public void run() {
		            	TextView lat = (TextView) findViewById(R.id.latitudeTextView);
		        		TextView lon = (TextView) findViewById(R.id.longitudeTextView);
		        		TextView time = (TextView) findViewById(R.id.gpsTimeTextView);
		        		TextView batChange = (TextView) findViewById(R.id.gpsBatteryLossTextView);
		        		TextView updateCount = (TextView) findViewById(R.id.gpsUpdatesTextView);
		        		TextView firstLock = (TextView) findViewById(R.id.gpsFirstLockTextView);
		    			
		        		if(deviceListener.getLocation() != null){
		        		lat.setText(Double.toString(deviceListener.getLocation().getLatitude()));
		        		lon.setText(Double.toString(deviceListener.getLocation().getLongitude()));
		        		try {
							recordData("GPSlocation",lat.getText(), lon.getText());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        		}
		        		if(!deviceListener.getTimeTillFirstUpdate().equals("")) firstLock.setText(deviceListener.getTimeTillFirstUpdate());
		        		time.setText(deviceListener.getTimeRunning());
		        		batChange.setText(Integer.toString((int)deviceListener.getBatteryChange()) + "%");
		        		updateCount.setText(Integer.toString(deviceListener.getUpdateCount()));
		        		if(getTimeToRun() > 0 && (int)deviceListener.getTimeRunningLong() >= getTimeToRun() * 60){
		        			timer.cancel();
		        		}
		        		batteryChange= deviceListener.getBatteryChange();
		        		timeRunning= deviceListener.getTime();
		        		numOfUpdate= deviceListener.getUpdateCount();
		            }
		        });
		    }
		};
		timer.scheduleAtFixedRate(timerTaskOne, 0, 1000);
		} else {
			timer.cancel();
			locationManager.removeUpdates(deviceListener);
		}
		gpsOn = !gpsOn;
		return;
	}
	public int getTimeToRun(){
		EditText editText = (EditText) findViewById(R.id.timeToRunTextField);
		int timeToRun = -1;
        if(!editText.getText().toString().equals("")){
        	timeToRun = Integer.parseInt(editText.getText().toString());
        }
		return timeToRun;
	}
	public void startAccel(){
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL );
	}
	public void stopAccel(){
		sensorManager.unregisterListener(this);
	}
	public void onWifiButtonClick(){
		Log.i("b","wifi");
		gpsButton.setChecked(false);
		cellButton.setChecked(false);
		accelButton.setChecked(false);
		if(!wifiOn){	
			CharSequence text = "Test started, please wait " + getTimeToRun() +" minutes.";
			if(getTimeToRun() < 1){
				text = "Test started.";
			}
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		deviceListener = new DeviceListener();
		locationManager.removeUpdates(deviceListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, deviceListener);
		final Handler handler = new Handler();
		timer = new Timer(false);
		timerTaskOne = new TimerTask() {
		    @Override
		    public void run() {
		        handler.post(new Runnable() {
		            @Override
		            public void run() {
		            	TextView lat = (TextView) findViewById(R.id.latitudeTextView);
		        		TextView lon = (TextView) findViewById(R.id.longitudeTextView);
		        		TextView time = (TextView) findViewById(R.id.wifiTimeTextView);
		        		TextView batChange = (TextView) findViewById(R.id.wifiBatteryLossTextView);
		        		TextView updateCount = (TextView) findViewById(R.id.wifiUpdatesTextView);
		        		TextView firstLock = (TextView) findViewById(R.id.wifiFirstLockTextView);
		    			
		        		if(deviceListener.getLocation() != null){
		        		lat.setText(Double.toString(deviceListener.getLocation().getLatitude()));
		        		lon.setText(Double.toString(deviceListener.getLocation().getLongitude()));
		        		}
		        		if(!deviceListener.getTimeTillFirstUpdate().equals("")) firstLock.setText(deviceListener.getTimeTillFirstUpdate());
		        		time.setText(deviceListener.getTimeRunning());
		        		batChange.setText(Integer.toString((int)deviceListener.getBatteryChange()) + "%");
		        		updateCount.setText(Integer.toString(deviceListener.getUpdateCount()));
		        		if(getTimeToRun() > 0 && (int)deviceListener.getTimeRunningLong() >= getTimeToRun() * 60){
		        			timer.cancel();
		        		}
		        		batteryChange= deviceListener.getBatteryChange();
		        		timeRunning= deviceListener.getTime();
		        		numOfUpdate= deviceListener.getUpdateCount();
		            }
		        });
		    }
		};
		timer.scheduleAtFixedRate(timerTaskOne, 0, 1000);
		} else {
			locationManager.removeUpdates(deviceListener);
		}
		wifiOn = !wifiOn;
		return;
	}
	public void onTestButtonClick(){
		final String[] selections ={"Seated", "Running", "Vehicle"};
		if(!testOn){
			if(getTimeToRun() < 0){
				Context context = getApplicationContext();
				CharSequence text = "Please enter time to test.";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				return;
    		}
		Context context = getApplicationContext();
		CharSequence text = "Test started, please wait " + 5 * getTimeToRun() +" minutes.";
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		final Handler handler = new Handler();
		timer = new Timer(false);
		timerTaskOne = new TimerTask() {
		    @Override
		    public void run() {
		        handler.post(new Runnable() {
		            @Override
		            public void run() {
		            	TextView lat = (TextView) findViewById(R.id.latitudeTextView);
		        		TextView lon = (TextView) findViewById(R.id.longitudeTextView);
		        		TextView time = (TextView) findViewById(R.id.wifiTimeTextView);
		        		TextView batChange = (TextView) findViewById(R.id.wifiBatteryLossTextView);
		        		TextView updateCount = (TextView) findViewById(R.id.wifiUpdatesTextView);
		        		TextView firstLock = (TextView) findViewById(R.id.wifiFirstLockTextView);
		    			
		        		if(deviceListener.getLocation() != null){
		        		lat.setText(Double.toString(deviceListener.getLocation().getLatitude()));
		        		lon.setText(Double.toString(deviceListener.getLocation().getLongitude()));
		        		}
		        		if(!deviceListener.getTimeTillFirstUpdate().equals("")) firstLock.setText(deviceListener.getTimeTillFirstUpdate());
		        		time.setText(deviceListener.getTimeRunning());
		        		batChange.setText(Integer.toString((int)deviceListener.getBatteryChange()) + "%");
		        		updateCount.setText(Integer.toString(deviceListener.getUpdateCount()));
		        		Log.i("hi", Double.toString((double)(deviceListener.getTimeRunningLong())));
		        		if(getTimeToRun() > 0 && (int)deviceListener.getTimeRunningLong() >= getTimeToRun() * 60){
		        			timer.cancel();
		        			timer = new Timer(false);
		        			locationManager.removeUpdates(deviceListener);
		        			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, deviceListener);
		        			deviceListener.setTimeStarted();
		        			deviceListener.setBatteryChange();
		        			deviceListener.setUpdates(0);
		        			timer.scheduleAtFixedRate(timerTaskTwo, 0, 1000);
		        			CharSequence text = "GPS test started .";
							int duration = Toast.LENGTH_SHORT;
							Toast toast = Toast.makeText(getApplicationContext(), text, duration);
							toast.show();
		        			
		        		}
		            }
		        });
		        
		    }
		};
		timerTaskTwo = new TimerTask() {
		    @Override
		    public void run() {
		        handler.post(new Runnable() {
		            @Override
		            public void run() {
		            	TextView lat = (TextView) findViewById(R.id.latitudeTextView);
		        		TextView lon = (TextView) findViewById(R.id.longitudeTextView);
		        		TextView time = (TextView) findViewById(R.id.gpsTimeTextView);
		        		TextView batChange = (TextView) findViewById(R.id.gpsBatteryLossTextView);
		        		TextView updateCount = (TextView) findViewById(R.id.gpsUpdatesTextView);
		        		TextView firstLock = (TextView) findViewById(R.id.gpsFirstLockTextView);
		    			
		        		if(deviceListener.getLocation() != null){
		        		lat.setText(Double.toString(deviceListener.getLocation().getLatitude()));
		        		lon.setText(Double.toString(deviceListener.getLocation().getLongitude()));
		        		}
		        		if(!deviceListener.getTimeTillFirstUpdate().equals("")) firstLock.setText(deviceListener.getTimeTillFirstUpdate());
		        		time.setText(deviceListener.getTimeRunning());
		        		batChange.setText(Integer.toString((int)deviceListener.getBatteryChange()) + "%");
		        		updateCount.setText(Integer.toString(deviceListener.getUpdateCount()));
		        		if(getTimeToRun() > 0 && (int)deviceListener.getTimeRunningLong() >= getTimeToRun() * 60){
		        			timer.cancel();
		        			timer = new Timer(false);
		        			deviceListener.setTimeStarted();
		        			deviceListener.setBatteryChange();
		        			locationManager.removeUpdates(deviceListener);
		        			startAccel();
		        			CharSequence text = "Seated accelerometer test started .";
							int duration = Toast.LENGTH_SHORT;
							Toast toast = Toast.makeText(getApplicationContext(), text, duration);
							toast.show();
		        			timer.scheduleAtFixedRate(timerTaskThree, 0, 1000);
		        		}
		            }
		        });
		        
		    }
		};
		timerTaskThree = new TimerTask(){
			@Override
			public void run() {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						TextView time = (TextView) findViewById(R.id.time_on_acc);
		        		TextView batChange = (TextView) findViewById(R.id.acc_baterry_lost);	
		        		time.setText(deviceListener.getTimeRunning());
		        		batChange.setText(Integer.toString((int)deviceListener.getBatteryChange()) + "%");
		        		batteryChange= deviceListener.getBatteryChange();
		        		timeRunning= deviceListener.getTime();
		        		numOfUpdate= deviceListener.getUpdateCount();
		        		try {
							recordData(selections[0]);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        		if(getTimeToRun() > 0 && (int)deviceListener.getTimeRunningLong() >= getTimeToRun() * 60){
		        			timer.cancel();
		        			timer = new Timer(false);
		        			CharSequence text = "Running accelerometer test started.";
							int duration = Toast.LENGTH_SHORT;
							Toast toast = Toast.makeText(getApplicationContext(), text, duration);
							toast.show();
		        			timer.scheduleAtFixedRate(timerTaskFour, 0, 1000);
		        		}
					}
				});
			}
		};
		timerTaskFour = new TimerTask(){
			@Override
			public void run() {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						Log.i("asd", "Running accel test");
						TextView time = (TextView) findViewById(R.id.time_on_acc);
		        		TextView batChange = (TextView) findViewById(R.id.acc_baterry_lost);	
		        		time.setText(deviceListener.getTimeRunning());
		        		batChange.setText(Integer.toString((int)deviceListener.getBatteryChange()) + "%");
		        		batteryChange= deviceListener.getBatteryChange();
		        		timeRunning= deviceListener.getTime();
		        		numOfUpdate= deviceListener.getUpdateCount();
		        		try {
							recordData(selections[1]);
						} catch (IOException e) {
							Log.i("Error", "Running accel test");
						}
		        		if(getTimeToRun() > 0 && (int)deviceListener.getTimeRunningLong() >= 2 * getTimeToRun() * 60){
		        			timer.cancel();
		        			timer = new Timer(false);
		        			CharSequence text = "Driving accelerometer test started .";
							int duration = Toast.LENGTH_SHORT;
							Toast toast = Toast.makeText(getApplicationContext(), text, duration);
							toast.show();
		        			timer.scheduleAtFixedRate(timerTaskFive, 0, 1000);
		        		}
					}
				});
			}
		};
		timerTaskFive = new TimerTask(){
			@Override
			public void run() {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						TextView time = (TextView) findViewById(R.id.time_on_acc);
		        		TextView batChange = (TextView) findViewById(R.id.acc_baterry_lost);	
		        		time.setText(deviceListener.getTimeRunning());
		        		batChange.setText(Integer.toString((int)deviceListener.getBatteryChange()) + "%");
		        		batteryChange= deviceListener.getBatteryChange();
		        		timeRunning= deviceListener.getTime();
		        		try {
							recordData(selections[2]);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        		if(getTimeToRun() > 0 && (int)deviceListener.getTimeRunningLong() >= 3 * getTimeToRun() * 60){
		        			timer.cancel();
		        			stopAccel();
		        			
		        			
		        		}
					}
				});
			}
		};
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		deviceListener = new DeviceListener();
		locationManager.removeUpdates(deviceListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, deviceListener);
		timer.scheduleAtFixedRate(timerTaskOne, 0,1000);
    	text = "Wifi test started.";
		toast = Toast.makeText(getApplicationContext(), text, duration);
		toast.show();

		
		} else {
			Context context = getApplicationContext();
			CharSequence text = "Test canceled.";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			resetView();
			timer.cancel();
		}
		
		testOn = !testOn;
	}
	
	public void onCellButtonClick() throws JSONException{
		gpsButton.setChecked(false);
		wifiButton.setChecked(false);
		accelButton.setChecked(false);
		
		String[] huy = new String[2];
		Postmethod postmethod = new Postmethod(this);
		long startTime = System.currentTimeMillis();
		postmethod.execute(huy);
		
		while(true){
			
			if(postmethod.getresponseText() != null){
				long endTime = System.currentTimeMillis();
				long duration = (endTime- startTime)/ 1000;;
				String JSONResponse = postmethod.getresponseText();
				JSONObject response = new JSONObject(JSONResponse);
				
				
				double lattitude =  response.getDouble("lat");
				double longtitude = response.getDouble("lon");
				
				TextView lat = (TextView) findViewById(R.id.latitudeTextView);
				TextView lon = (TextView) findViewById(R.id.longitudeTextView);
				TextView firstLockTextView = (TextView) findViewById(R.id.cellFirstLockTextView);
				String durationText = Long.toString(duration / (60 * 60)) + ":" + Long.toString((duration / 60) % 60) + ":" + Long.toString(duration % 60 );

				firstLockTextView.setText(durationText);
				lat.setText(Double.toString(lattitude));
				lon.setText(Double.toString(longtitude));
				break;
			}
		}
		
	}
	
	public void onAccelButtonClick(){
		gpsButton.setChecked(false);
		wifiButton.setChecked(false);
		cellButton.setChecked(false);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL );
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		final String[] selections ={"Seated", "Running", "Vehicle"};
		builder.setTitle("Select the type of Activity");
		builder.setItems(selections, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.typeOfActivity = which;
			}
		});
		builder.show();
		
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, "Test Started", duration);
		toast.show();
		
		deviceListener = new DeviceListener();
		final Handler handler = new Handler();
		timer = new Timer(false);
		
		timerTaskThree = new TimerTask(){
			@Override
			public void run() {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						TextView time = (TextView) findViewById(R.id.time_on_acc);
		        		TextView batChange = (TextView) findViewById(R.id.acc_baterry_lost);	
		        		time.setText(deviceListener.getTimeRunning());
		        		batChange.setText(Integer.toString((int)deviceListener.getBatteryChange()) + "%");
		        		batteryChange= deviceListener.getBatteryChange();
		        		timeRunning= deviceListener.getTime();
		        		numOfUpdate= deviceListener.getUpdateCount();
		        		try {
							recordData(selections[typeOfActivity]);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		};
		timer.scheduleAtFixedRate(timerTaskThree, 0, 1000);
		
	}
	
	public void drawGraph(View v){
		Intent intent = new Intent(this, GraphActivity.class);
		startActivity(intent);
	}
	public void mapGraph(View v){
		Intent intent = new Intent(this, MapMssgActivity.class);
		startActivity(intent);
	}
	
	public void onSensorChanged(SensorEvent event){
		if(System.currentTimeMillis() - lastAccelUpdate < 1000) return;
		lastAccelUpdate = System.currentTimeMillis();
		if(event.sensor.getType()== Sensor.TYPE_ACCELEROMETER)
		{
			double x = (double) event.values[0];
			double y = (double) event.values[1];
			double z = (double) event.values[2];
			
			velocity = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
			TextView velocityText = (TextView) findViewById(R.id.velocityMag);
			velocityText.setText(String.valueOf(velocity));
			if(checkMoving()){
				lastTimeMoving = System.currentTimeMillis();
			}
			
		}
	}
	
	
	public static Context getContext(){
		return context;
	}
	
	class GPSButtonListner implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton button, boolean check) {
			if(!check){	
				resetView();
				if(numOfUpdate >0 && batteryChange > 2.0){
					try {
						recordData("GPS");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			onGPSButtonClick();
		}
	}
	
	class WifiButtonListner implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton button, boolean check) {
			if(!check){
				resetView();
				if(numOfUpdate>0 && batteryChange > 2.0){
					try {
						recordData("Wifi");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			onWifiButtonClick();
		}
	}
	
	class CellButtonListner implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton button, boolean check) {
			if(check)
			{	
				try {
					onCellButtonClick();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				resetView();
				//recordData("Cell");
			}
		}
	}
	
	class AccButtonListner implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton button, boolean check) {
			if(check)
			{	
				onAccelButtonClick();
			}
			else {
				resetView();
				if(batteryChange > 0.0){
					try {
						recordData("Accel");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	class AlgorithmButtonListener implements OnCheckedChangeListener{
		@Override
		public void onCheckedChanged(CompoundButton button, boolean check){
			if(check){
				onAlgorithmClick();
			}
			else{
				resetView();
				if(batteryChange >0.0){
					try{
						recordData("Algorithm");
					}catch(IOException exception){
						exception.printStackTrace();
					}
				}
			}
		}
	}
	class TestButtonListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			Log.i("b","test");
			onTestButtonClick();
		}
	}
	
	public void resetView(){
		Intent intent = getIntent();
		finish();
		overridePendingTransition(17432576 , 17432579 );
		startActivity(intent);
		if(timer != null){
			timer.cancel();
		}
		if(locationManager !=null)
		{
			locationManager.removeUpdates(deviceListener);
		}
	}
	
	public void recordData(String typeOfData) throws IOException{
		File file = null;
		if(typeOfData == "GPS"){
			file = new File("/mnt/sdcard/Download/GPSdata1111111.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			
		}else if(typeOfData =="Wifi"){
			file = new File("/mnt/sdcard/Download/Wifidata1111111.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
		}else if(typeOfData == "Accel"){
			file = new File("/mnt/sdcard/Download/Acceldata1111111.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
		}
		else if(typeOfData =="Seated"){
			file = new File("/mnt/sdcard/Download/Seateddata1111111.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
		}
		else if(typeOfData =="Running"){
			file = new File("/mnt/sdcard/Download/Runningdata1111111.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
		}
		else if(typeOfData == "Vehicle"){
			file = new File("/mnt/sdcard/Download/Vehicledata1111111.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
		}
		else{
		
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
		BufferedWriter bw = new BufferedWriter(fw);
		if(typeOfData == "Seated" || typeOfData =="Running"|| typeOfData =="Vehicle"){
			bw.write(String.valueOf(velocity));
		}else {
			bw.write(Float.toString(batteryChange/timeRunning));
		}
		bw.write("\n");
		bw.close();
		
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean getWaitingForGPS(){
		return isWaitingForGPS;
	}
	public void setWaitingForGPS(boolean b){
		isWaitingForGPS = b;
	}
	public void incrementUpdates(){
		updates++;
	}
	public int getUpdates(){
		return updates;
	}
	/**
	 * The algorithm from the paper implementation
	 * 1st: Get the vector magnitude
	 * 		From 9 to 11: this is at rest
	 * 		less than 9 or greater than 11: means moving
	 * if( moving =true)
	 * 		add the listener to the gps
	 * else
	 * 		create timer count for 1 mins
	 * 		After 1 mins if(moving = false)
	 *							remove the listener.
	 *
	 */
	public void onAlgorithmClick(){
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		deviceListener = new DeviceListener();
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL * 2);
		
		updates = 0;
		isWaitingForGPS = false;
		
		final Handler handler = new Handler();
		timer = new Timer(false);
		timerTaskOne = new TimerTask() {
		    @Override
		    public void run() {
		        handler.post(new Runnable() {
		            @Override
		            public void run() {
						if(checkMoving()){
							locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, deviceListener);
							setWaitingForGPS(true);
							
							
						}
						else
						{
							long now = System.currentTimeMillis();
							Log.i("RunningTime", Long.toString(now- lastTimeMoving));
							if(now - lastTimeMoving > 1 * 60 * 1000){
								locationManager.removeUpdates(deviceListener);
								Log.e("algorithmTimer", "deactivate the location manager");
								try {
									Thread.sleep(60*1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								lastTimeMoving = System.currentTimeMillis();
							}
							
						}
						/**
						if(deviceListener.getUpdateCount() > getUpdates()){
							incrementUpdates();
							locationManager.removeUpdates(deviceListener);
							// get lat and long, display them
						}
						*/
						
						TextView lat = (TextView) findViewById(R.id.latitudeTextView);
						TextView lon = (TextView) findViewById(R.id.longitudeTextView);
						TextView time = (TextView) findViewById(R.id.gpsTimeTextView);
						TextView batChange = (TextView) findViewById(R.id.gpsBatteryLossTextView);
						TextView updateCount = (TextView) findViewById(R.id.gpsUpdatesTextView);
						TextView firstLock = (TextView) findViewById(R.id.gpsFirstLockTextView);
		
						if(deviceListener.getLocation() != null){
						lat.setText(Double.toString(deviceListener.getLocation().getLatitude()));
						lon.setText(Double.toString(deviceListener.getLocation().getLongitude()));
						try {
							recordData("AlgorithmLocation",lat.getText(), lon.getText());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}
						if(!deviceListener.getTimeTillFirstUpdate().equals("")) firstLock.setText(deviceListener.getTimeTillFirstUpdate());
						time.setText(deviceListener.getTimeRunning());
						batChange.setText(Integer.toString((int)deviceListener.getBatteryChange()) + "%");
						updateCount.setText(Integer.toString(deviceListener.getUpdateCount()));
						setWaitingForGPS(false);
		            }
		        });
		    }
		};
		timer.scheduleAtFixedRate(timerTaskOne, 0, 1000);
		
	}
	
	public boolean checkMoving(){
		
		
		if(velocity >9 && velocity <11)
		{
			
			return false;
		}
		else
		{
			Log.i("Moving","hello");
			return true;
		}
	}
	
	public void recordData(String typeOfTechnique, CharSequence lat, CharSequence lon) throws IOException{
		File file = null;
		if(typeOfTechnique == "GPSlocation"){
			file = new File("/mnt/sdcard/Download/GPSlocation1111.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			
		}else if(typeOfTechnique =="AlgorithmLocation"){
			file = new File("/mnt/sdcard/Download/AlgorithmLocation1111.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
		}
		
		FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(lat.toString());
		bw.write("\n");
		bw.write(lon.toString());
		bw.write("\n");
		bw.close();
	}
}
