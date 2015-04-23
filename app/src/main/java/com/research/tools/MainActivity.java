package com.research.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import org.json.JSONArray;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity {
	
private static Context context;





	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		MainActivity.context = getApplicationContext();

        //Button eventButton = (Button) findViewById(R.id.event_button);

        //eventButton.setOnClickListener(new EventButtonListener());
	}

    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
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
                        /*String oneObjectsItem2 = oneObject.getString("description");*/
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
    }

    class EventButtonListener implements OnClickListener{
        @Override
        public void onClick(View v){
            new JSONAsyncTask().execute("http://broncomaps.com/edit/events/data/");
        }
    }

	public void mapGraph(View v){
		Intent intent = new Intent(this, MapMssgActivity.class);
		startActivity(intent);
	}

	public static Context getContext(){
		return context;
	}


	public boolean checkMoving(){
        double velocity = 10.00;
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

}
