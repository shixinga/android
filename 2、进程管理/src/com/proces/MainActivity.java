package com.proces;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private TextView tv_process_count;
	private TextView tv_mem_info;
	private TextView tv_status;
	//活动管理器
	private ActivityManager am;
	
	private ListView lv_taskmanager;
	private LinearLayout ll_loading;
	//全部进程
	private List<TaskInfo> allTaskInfos;
	//用户进程集合
	private List<TaskInfo> userTaskInfos;
	//系统进程集合
	private List<TaskInfo> sysTaskInfos;
	//正在运行的进程数量
	private int runningProcessCount;
	//可用ram内存
	private long availRam;
	//总内存
	private long totalRam;
	//Adapter
	private TaskManagerAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv_status = (TextView) findViewById(R.id.tv_status);
		lv_taskmanager = (ListView) findViewById(R.id.lv_taskmanager);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		tv_mem_info = (TextView) findViewById(R.id.tv_mem_info);
		tv_process_count = (TextView) findViewById(R.id.tv_process_count);
		runningProcessCount = SystemInfoUtils.getRunningProcessCount(this);//获取正在运行的进程
		availRam = SystemInfoUtils.getAvailRam(this);//获取可用内存
		totalRam = SystemInfoUtils.getTotalRam(this); //获取总内存
		tv_process_count.setText("运行中进程:"+runningProcessCount+"个");
		tv_mem_info.setText("剩余/总内存:"+Formatter.formatFileSize(this,availRam)+"/"+Formatter.formatFileSize(this, totalRam));
		fillData();
		//ListView滚动事件
		lv_taskmanager.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(userTaskInfos != null && sysTaskInfos != null){
					if(firstVisibleItem > userTaskInfos.size()){
						tv_status.setText("系统进程("+sysTaskInfos.size()+")");
					}else{
						tv_status.setText("用户进程("+userTaskInfos.size()+")");
					}
				}
			}
		});
		//为listview设置点击事件
		lv_taskmanager.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TaskInfo taskInfo;
				if(position == 0 || position == (userTaskInfos.size())+1){ //用户进程的标签
					return;
				}else if(position <= userTaskInfos.size()){
					taskInfo = userTaskInfos.get(position - 1);
				}else{
					taskInfo = sysTaskInfos.get(position-1-userTaskInfos.size()-1);
				}
				if(getPackageName().equals(taskInfo.getPackname())){
					return;
				}
				ViewHolder holder = (ViewHolder) view.getTag();
				if(taskInfo.isChecked()){
					taskInfo.setChecked(false);
					holder.cb_status.setChecked(false);
				}else{
					taskInfo.setChecked(true);
					holder.cb_status.setChecked(true);
				}
			}
		});
	}
	@Override
	protected void onStart() {
		super.onStart();
		setTitle();
		fillData();
	}
	static class ViewHolder{
		ImageView iv_icon;
		TextView tv_name;
		TextView tv_memsize;
		CheckBox cb_status;
	}
	/**
	 * 设置标题
	 */
	private void setTitle(){
		runningProcessCount = SystemInfoUtils.getRunningProcessCount(this);
		tv_process_count.setText("运行中进程:"+runningProcessCount+"个");
		availRam = SystemInfoUtils.getAvailRam(this);
		totalRam = SystemInfoUtils.getTotalRam(this);
		tv_mem_info.setText("剩余/总内存:"+Formatter.formatFileSize(this,availRam)+"/"+Formatter.formatFileSize(this,totalRam));
	}
	/**
	 * 填充数据
	 */
	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				allTaskInfos = TaskInfoProvider.getTaskInfos(getApplicationContext());
				userTaskInfos = new ArrayList<TaskInfo>();
				sysTaskInfos = new ArrayList<TaskInfo>();
				for (TaskInfo info : allTaskInfos) {
					if(info.isUserTask()){
						userTaskInfos.add(info);
					}else{
						sysTaskInfos.add(info);
					}
				}
				//更新设置界面
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ll_loading.setVisibility(View.INVISIBLE);
						if(adapter == null){
							adapter = new TaskManagerAdapter();
							lv_taskmanager.setAdapter(adapter);
						}else{
							adapter.notifyDataSetChanged();
						}
						setTitle();
					}
				});
			};
		}.start();
	}
	/**
	 * 选择全部
	 */
	public void selectAll(View view){
		for (TaskInfo info : allTaskInfos) {
			if(getPackageName().equals(info.getPackname())){
				continue;
			}
			info.setChecked(true);
		}
		adapter.notifyDataSetChanged();
	}
	/**
	 * 反选
	 */
	public void selectOppo(View view){
		for (TaskInfo info : allTaskInfos) {
			if(getPackageName().equals(info.getPackname())){
				continue;
			}
			info.setChecked(!info.isChecked());
		}
		adapter.notifyDataSetChanged();
	}
	/**
	 * 一键清理,记得加权限
	 */
	public void killAll(View view){
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		int count = 0;
		long savedMem = 0;
		//记录那些被杀死的条目
		List<TaskInfo> killedTaskinfos = new ArrayList<TaskInfo>();
		for (TaskInfo info : allTaskInfos) {
			if(info.isChecked()){ //杀死这些被勾选的进程
				am.killBackgroundProcesses(info.getPackname());//杀死进程
				if(info.isUserTask()){
					userTaskInfos.remove(info);
				}else{
					sysTaskInfos.remove(info);
				}
				killedTaskinfos.add(info);
				count++;
				savedMem+=info.getMemsize();
			}
		}
		allTaskInfos.removeAll(killedTaskinfos);
		adapter.notifyDataSetChanged();
		Toast.makeText(this,"杀死了"+count+"个进程,释放了"+Formatter.formatFileSize(this,savedMem)+"内存",1).show();
		runningProcessCount -= count;
		availRam += savedMem;
		tv_process_count.setText("运行中的进程:"+runningProcessCount+"个");
		tv_mem_info.setText("剩余/总内存:"+Formatter.formatFileSize(this,availRam)+"/"+Formatter.formatFileSize(this,totalRam));
	}
	/**
	 * 设置
	 */
	public void enterSetting(View view){
		Intent intent = new Intent(this,TaskSettingActivity.class);
		startActivity(intent);
	}
	/**
	 * Aadpter
	 */
	private class TaskManagerAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
			if(sp.getBoolean("showsystem",false)){
				return userTaskInfos.size() + 1 + sysTaskInfos.size() +1;
			}else{
				return userTaskInfos.size()+1;
			}
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TaskInfo taskInfo;
			if(position == 0){ //用户进程
				TextView tv = new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setText("用户进程:"+userTaskInfos.size()+"个");
				return tv;
			}else if(position == (userTaskInfos.size() + 1)){
				TextView tv = new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setText("系统进程:"+sysTaskInfos.size()+"个");
				return tv;
			}else if(position <= userTaskInfos.size()){
				taskInfo = userTaskInfos.get(position - 1);
			}else{
				taskInfo = sysTaskInfos.get(position - 1 - userTaskInfos.size() - 1);
			}
			View view;
			ViewHolder holder;
			if(convertView != null && convertView instanceof RelativeLayout){
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}else{
				view = View.inflate(getApplicationContext(),R.layout.list_task_item,null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_memsize = (TextView) view.findViewById(R.id.tv_memsize);
				holder.cb_status = (CheckBox) view.findViewById(R.id.cb);
				view.setTag(holder);
			}
			holder.iv_icon.setImageDrawable(taskInfo.getIcon());
			holder.tv_name.setText(taskInfo.getName());
			holder.tv_memsize.setText("内存占用:"+Formatter.formatFileSize(getApplicationContext(),taskInfo.getMemsize()));
			System.out.println("holder.cb_status= "+holder.cb_status);
			System.out.println("taskInfo = "+taskInfo);
			holder.cb_status.setChecked(taskInfo.isChecked());
			if(getPackageName().equals(taskInfo.getPackname())){
				holder.cb_status.setVisibility(View.INVISIBLE);
			}else{
				holder.cb_status.setVisibility(View.VISIBLE);
			}
			return view;
		}
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return 0;
		}
		
	}
}
