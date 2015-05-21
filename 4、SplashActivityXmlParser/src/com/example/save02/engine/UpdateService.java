package com.example.save02.engine;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.example.save02.domain.UpdateInfo;

import android.content.Context;

public class UpdateService {

	private Context context;

	public UpdateService(Context context) {
		this.context = context;
	}
	
	public UpdateInfo getUpdateInfo(int urlid) throws Exception {
		String path = context.getResources().getString(urlid);
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		InputStream is = conn.getInputStream();
		return  UpdateParser.getUpdateInfo(is);
	}
	
}
