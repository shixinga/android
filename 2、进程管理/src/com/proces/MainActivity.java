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
	//�������
	private ActivityManager am;
	
	private ListView lv_taskmanager;
	private LinearLayout ll_loading;
	//ȫ������
	private List<TaskInfo> allTaskInfos;
	//�û����̼���
	private List<TaskInfo> userTaskInfos;
	//ϵͳ���̼���
	private List<TaskInfo> sysTaskInfos;
	//�������еĽ�������
	private int runningProcessCount;
	//����ram�ڴ�
	private long availRam;
	//���ڴ�
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
		runningProcessCount = SystemInfoUtils.getRunningProcessCount(this);//��ȡ�������еĽ���
		availRam = SystemInfoUtils.getAvailRam(this);//��ȡ�����ڴ�
		totalRam = SystemInfoUtils.getTotalRam(this); //��ȡ���ڴ�
		tv_process_count.setText("�����н���:"+runningProcessCount+"��");
		tv_mem_info.setText("ʣ��/���ڴ�:"+Formatter.formatFileSize(this,availRam)+"/"+Formatter.formatFileSize(this, totalRam));
		fillData();
		//ListView�����¼�
		lv_taskmanager.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(userTaskInfos != null && sysTaskInfos != null){
					if(firstVisibleItem > userTaskInfos.size()){
						tv_status.setText("ϵͳ����("+sysTaskInfos.size()+")");
					}else{
						tv_status.setText("�û�����("+userTaskInfos.size()+")");
					}
				}
			}
		});
		//Ϊlistview���õ���¼�
		lv_taskmanager.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TaskInfo taskInfo;
				if(position == 0 || position == (userTaskInfos.size())+1){ //�û����̵ı�ǩ
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
	 * ���ñ���
	 */
	private void setTitle(){
		runningProcessCount = SystemInfoUtils.getRunningProcessCount(this);
		tv_process_count.setText("�����н���:"+runningProcessCount+"��");
		availRam = SystemInfoUtils.getAvailRam(this);
		totalRam = SystemInfoUtils.getTotalRam(this);
		tv_mem_info.setText("ʣ��/���ڴ�:"+Formatter.formatFileSize(this,availRam)+"/"+Formatter.formatFileSize(this,totalRam));
	}
	/**
	 * �������
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
				//�������ý���
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
	 * ѡ��ȫ��
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
	 * ��ѡ
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
	 * һ������,�ǵü�Ȩ��
	 */
	public void killAll(View view){
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		int count = 0;
		long savedMem = 0;
		//��¼��Щ��ɱ������Ŀ
		List<TaskInfo> killedTaskinfos = new ArrayList<TaskInfo>();
		for (TaskInfo info : allTaskInfos) {
			if(info.isChecked()){ //ɱ����Щ����ѡ�Ľ���
				am.killBackgroundProcesses(info.getPackname());//ɱ������
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
		Toast.makeText(this,"ɱ����"+count+"������,�ͷ���"+Formatter.formatFileSize(this,savedMem)+"�ڴ�",1).show();
		runningProcessCount -= count;
		availRam += savedMem;
		tv_process_count.setText("�����еĽ���:"+runningProcessCount+"��");
		tv_mem_info.setText("ʣ��/���ڴ�:"+Formatter.formatFileSize(this,availRam)+"/"+Formatter.formatFileSize(this,totalRam));
	}
	/**
	 * ����
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
			if(position == 0){ //�û�����
				TextView tv = new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setText("�û�����:"+userTaskInfos.size()+"��");
				return tv;
			}else if(position == (userTaskInfos.size() + 1)){
				TextView tv = new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setText("ϵͳ����:"+sysTaskInfos.size()+"��");
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
			holder.tv_memsize.setText("�ڴ�ռ��:"+Formatter.formatFileSize(getApplicationContext(),taskInfo.getMemsize()));
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
