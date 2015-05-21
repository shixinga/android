package com.our5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ListView;

import com.our5.adapter.MyListViewBaseAdapter;
import com.our5.adapter.MyMenuBaseAdapter;
import com.our5.selfview.SlidingMenu;

public class MainActivity extends ActionBarActivity {

	private SlidingMenu mLeftMenu ; 
	private boolean isNeedShowDialog = true;
	
	private ListView yijiankaiqiList;
	//主界面的list
	private List<Map<String, Object>> needList = new ArrayList<Map<String,Object>>();
	
	MyListViewBaseAdapter myListViewBaseAdapter;
	public GridView gridView;
	List<Map<String, Object>> list = null;
	private PackageManager packageManager;
	private SharedPreferences sharedPreferences;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == 11) {
				//开启主界面
				myListViewBaseAdapter = new MyListViewBaseAdapter(MainActivity.this, needList, sharedPreferences);
				yijiankaiqiList.setAdapter(myListViewBaseAdapter);
			} else if(msg.what == 22) {
				//开启左边界面
				refreshListItems();
				
			}
		}
	};
	int count = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		init();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void init() {
		mLeftMenu = (SlidingMenu) findViewById(R.id.id_menu);
		mLeftMenu.mainActivity = this;
		//隐藏android系统底部的menu导航栏
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		//获取SlidingMenu中的GridView
		gridView = (GridView) findViewById(R.id.list_home);
		sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
		
		yijiankaiqiList = (ListView) findViewById(R.id.yijiankaiqiList);
		packageManager = getPackageManager();
		startRight();
	}
	
	public void toggleMenu(View view)
	{
		mLeftMenu.toggle();
	}
	
	public void gogoClick(View view) {
		count = needList.size();
		if (count != 0) {
			Intent intent = packageManager.getLaunchIntentForPackage((String) needList.get(0).get("packageName"));
			// 如果该程序不可启动（像系统自带的包，有很多是没有入口的）会返回NULL
			if (intent != null)
				startActivity(intent);
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		count--;
		if (count == 1) {
			// 取到点击的包名
			Intent i1 = packageManager.getLaunchIntentForPackage((String) needList.get(1).get("packageName"));
			// 如果该程序不可启动（像系统自带的包，有很多是没有入口的）会返回NULL
			if (i1 != null) {
				startActivity(i1);
			}

		} else if (count > 0) {
			// 取到点击的包名
			Intent intent = packageManager.getLaunchIntentForPackage((String) needList.get(count).get("packageName"));
			// 如果该程序不可启动（像系统自带的包，有很多是没有入口的）会返回NULL
			if (intent != null) {
				startActivity(intent);
				new Thread() {

					@Override
					public void run() {
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						onStop();
					}

				}.start();
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (count > 0) {
			onRestart();
		}
	}
	
	public void startLeft() {
		new Thread() {
			@Override
			public void run() {
				fetch_installed_apps();
				handler.sendEmptyMessage(22);
			}
		}.start();
	}
	
	
	public void startRight() {
		//主界面运行的线程
		new Thread() {
			@Override
			public void run() {
				list = fetch_installed_apps();
				needList.removeAll(needList);
				for(int i = 0; i < list.size(); ++i)
				if(sharedPreferences.getBoolean((String) list.get(i).get("packageName"), false)) {
					needList.add(list.get(i));
				}
				handler.sendEmptyMessage(11);
			}
			
		}.start();
	}


	
	
	private void refreshListItems() {
		list = fetch_installed_apps();
		MyMenuBaseAdapter myAdapter = new MyMenuBaseAdapter(this, list, sharedPreferences);
		gridView.setAdapter(myAdapter);
	}

	public List fetch_installed_apps() {
		List<ApplicationInfo> packages = getPackageManager()
				.getInstalledApplications(0);
		list = new ArrayList<Map<String, Object>>(packages.size());
		Iterator<ApplicationInfo> l = packages.iterator();
		
		while (l.hasNext()) {
			ApplicationInfo app = (ApplicationInfo) l.next();
			//如果非系统应用，则添加至appList
			if((app.flags&ApplicationInfo.FLAG_SYSTEM)==0) {
				String packageName = app.packageName;
				Map<String, Object> map = new HashMap<String, Object>();
				Drawable drawable = app.loadIcon(packageManager);
				map = new HashMap<String, Object>();
				map.put("ico", drawable);
				map.put("packageName", packageName);
				list.add(map);
			}
		}
		return list;
	}
	
}
