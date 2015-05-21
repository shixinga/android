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
			mDPM.lockNow();//����
			finish();
			//���Sdcard�ϵ�����
//			dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
			//�ָ���������
//			dpm.wipeData(0);
		}else{
			Toast.makeText(this, "��û�д򿪹���ԱȨ��", 1).show();
			return ;
		}
	}

	public void getAdmin(View view) {
		//����һ��Intent 
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		//��Ҫ����˭
		ComponentName   mDeviceAdminSample = new ComponentName(this,DeviceAdminSample.class);
		
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
       //Ȱ˵�û���������ԱȨ��
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
               "���ǿ����ҿ���һ����������İ�ť�Ͳ��ᾭ��ʧ��");
        startActivity(intent);
	}
}
