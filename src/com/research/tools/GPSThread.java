package com.research.tools;

import android.util.Log;

public class GPSThread extends Thread{
	@Override
	public void run() {
		while(!Thread.interrupted()){
			Log.i("hi","hi");
		}
	}

}
