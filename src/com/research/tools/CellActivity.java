package com.research.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.widget.TextView;

public class CellActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.cell_activity);
		String[] huy = new String[2];
		new Postmethod().execute(huy);
	}
	
	private class Postmethod extends AsyncTask<String, Void, String> {
		
		String requestText = null;
		String responseText =null;
		String token;
		String radio;
		String mcc;
		String mnc;
		String cid;
		String lac;
		
		@Override
		protected String doInBackground(String... params) {
			getInfoFromCellPhone();
			try {
				requestText = createJSONObject(token, radio, mcc, mnc, cid, lac);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				responseText = sendPost(requestText);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		@Override
        protected void onPostExecute(String result) {
			TextView request = (TextView) findViewById(R.id.request);
			TextView response = (TextView) findViewById(R.id.response);
			request.setText(requestText);
			response.setText(responseText);
		}
		
		private String createJSONObject(String token,String radio,  String mcc,  String mnc, String cid, String lac ) throws JSONException
		{
			OrderedJSONObject returnedObject = new OrderedJSONObject();
			//returnedObject.put("token","816261809");
			returnedObject.put("token",token);
			//returnedObject.put("radio","gsm");
			returnedObject.put("radio",radio);
			//returnedObject.put("mcc","310");
			returnedObject.put("mcc",mcc);
			//returnedObject.put("mnc","260");
			returnedObject.put("mnc",mnc);
			
			System.out.println(returnedObject.toString());
			
			OrderedJSONObject cells = new OrderedJSONObject();
			//cells.put("cid","20956163");
			cells.put("cid",cid);
			//cells.put("lac","14170");
			cells.put("lac",lac);
			
			JSONArray arr = new JSONArray();
			arr.put(cells);
			System.out.println(cells.toString());
			returnedObject.put("cells", arr);
			System.out.println(returnedObject.toString());
			return returnedObject.toString();
		}
		
		private String sendPost(String message) throws IOException
		{
			String url = "http://unwiredlabs.com/v2/process.php";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			con.setRequestMethod("POST");
			String urlParameters = message;
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
	 
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);
	 
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			//print result
			return response.toString();
		}
		private void getInfoFromCellPhone()
		{
			this.token = "816261809";
			TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		    String networkOperator = tel.getNetworkOperator();
		    if (networkOperator != null) {
		        int mcc = Integer.parseInt(networkOperator.substring(0, 3));
		        int mnc = Integer.parseInt(networkOperator.substring(3));
		        this.mcc = Integer.toString(mcc);
		        this.mnc = Integer.toString(mnc);
		    }
		    int phoneType = tel.getPhoneType();
		    if(phoneType == 1){
		    	this.radio = "gsm";
		    	GsmCellLocation cellLocation1 = (GsmCellLocation)tel.getCellLocation();
		    	int cid = cellLocation1.getCid();
		    	int lac = cellLocation1.getLac();
		    	this.cid = Integer.toString(cid);
		    	this.lac = Integer.toString(lac);
		    	
		    }
		    else 
		    {
		    	this.radio ="cdma";
		    	CdmaCellLocation cellLocation2 = (CdmaCellLocation)tel.getCellLocation();
		    	int cid = cellLocation2.getBaseStationId();
		    	int lac = cellLocation2.getNetworkId();
		    	this.cid = Integer.toString(cid);
		    	this.lac = Integer.toString(lac);
		    }
		    
		}
	}

}
