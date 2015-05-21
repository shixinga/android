package com.lock;

import android.support.v7.app.ActionBarActivity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private DevicePolicyManager mDPM;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName   who = new ComponentName(this,DeviceAdminSample.class);
		if(mDPM.isAdminActive(who)){
			mDPM.lockNow();//锁屏
			finish();
			//清除Sdcard上的数据
//			dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
			//恢复出厂设置
//			dpm.wipeData(0);
		}else{
			Toast.makeText(this, "还没有打开管理员权限", 1).show();
			return ;
		}
	}

	public void getAdmin(View view) {
		//创建一个Intent 
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		//我要激活谁
		ComponentName   mDeviceAdminSample = new ComponentName(this,DeviceAdminSample.class);
		
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
       //劝说用户开启管理员权限
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
               "哥们开启我可以一键锁屏，你的按钮就不会经常失灵");
        startActivity(intent);
	}
}
