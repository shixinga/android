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
	//涓荤晫闈㈢殑list
	private List<Map<String, Object>> needList = new ArrayList<Map<String,Object>>();
	
	MyListViewBaseAdapter myListViewBaseAdapter;
	public GridView gridView;
	List<Map<String, Object>> list = null;
	private PackageManager packageManager;
	private SharedPreferences sharedPreferences;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == 11) {
				//寮�惎涓荤晫闈�
				myListViewBaseAdapter = new MyListViewBaseAdapter(MainActivity.this, needList, sharedPreferences);
				yijiankaiqiList.setAdapter(myListViewBaseAdapter);
			} else if(msg.what == 22) {
				//寮�惎宸﹁竟鐣岄潰
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
		//闅愯棌android绯荤粺搴曢儴鐨刴enu瀵艰埅鏍�
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		//鑾峰彇SlidingMenu涓殑GridView
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
			// 濡傛灉璇ョ▼搴忎笉鍙惎鍔紙鍍忕郴缁熻嚜甯︾殑鍖咃紝鏈夊緢澶氭槸娌℃湁鍏ュ彛鐨勶級浼氳繑鍥濶ULL
			if (intent != null)
				startActivity(intent);
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		count--;
		if (count == 1) {
			// 鍙栧埌鐐瑰嚮鐨勫寘鍚�
			Intent i1 = packageManager.getLaunchIntentForPackage((String) needList.get(1).get("packageName"));
			// 濡傛灉璇ョ▼搴忎笉鍙惎鍔紙鍍忕郴缁熻嚜甯︾殑鍖咃紝鏈夊緢澶氭槸娌℃湁鍏ュ彛鐨勶級浼氳繑鍥濶ULL
			if (i1 != null) {
				startActivity(i1);
			}

		} else if (count > 0) {
			// 鍙栧埌鐐瑰嚮鐨勫寘鍚�
			Intent intent = packageManager.getLaunchIntentForPackage((String) needList.get(count).get("packageName"));
			// 濡傛灉璇ョ▼搴忎笉鍙惎鍔紙鍍忕郴缁熻嚜甯︾殑鍖咃紝鏈夊緢澶氭槸娌℃湁鍏ュ彛鐨勶級浼氳繑鍥濶ULL
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
		//涓荤晫闈㈣繍琛岀殑绾跨▼
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
			//濡傛灉闈炵郴缁熷簲鐢紝鍒欐坊鍔犺嚦appList
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
