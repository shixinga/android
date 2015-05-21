package com.example.save02.ui;



import java.io.File;

import com.example.save02.R;
import com.example.save02.domain.UpdateInfo;
import com.example.save02.engine.DownloadFile;
import com.example.save02.engine.UpdateService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {

	private TextView tv_splash_version;
	private LinearLayout ll_splash_main;
	private String TAG = "SplashActivity";
	private UpdateInfo info;
	public ProgressDialog pd;
	private Handler handler=new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			if(msg.what== 1)
			{
				pd = new ProgressDialog(SplashActivity.this);
				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				pd.setMessage("��������...");
				Log.i(TAG, "���������Ի���");
				showUpdateDialog();
			} else if(msg.what == 2) {
				loadMainActivity();
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// ȡ��������,����������λ��
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		initSplash();

	}
	
	
	
	private void initSplash() {
		// TODO Auto-generated method stub
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText(getVersion());
		
		// ��ɴ����ȫ����ʾ // ȡ����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		ll_splash_main = (LinearLayout) findViewById(R.id.ll_splash_main);

		//���뵭��Ч��
		AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
		alphaAnimation.setDuration(2000);
		ll_splash_main.setAnimation(alphaAnimation);
		new Thread(new UpdateThread()).start();
	}

	private String getVersion() {
		PackageManager packageManager = getPackageManager();
		try {
			PackageInfo info = packageManager.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "�汾��û�ҵ�";
		}
	}


	private class UpdateThread implements Runnable {

		@Override
		public void run() {

			UpdateService service = new UpdateService(SplashActivity.this);
			try {
				info = service.getUpdateInfo(R.string.updateurl);
			
				if(info.getVersion().equals(getVersion())) {
					
					Log.i(TAG, "��ȡ�汾����ͬ����������");
					Thread.sleep(2000);
					handler.sendEmptyMessage(2);
				} else {
					Log.i(TAG, "��ȡ�汾��buͬ��������");
					handler.sendEmptyMessage(1);
				}
				Log.i(TAG, getVersion());
			} catch (Exception e) {
				Log.i(TAG, "��ȡ�汾��ʧ��");
				handler.sendEmptyMessage(2);
			}
		}
		
	}
	
	protected void showUpdateDialog() {
		AlertDialog.Builder buider = new Builder(this);
		buider.setIcon(R.drawable.icon5);
		buider.setTitle("��������");
		buider.setMessage(info.getDescription());
		buider.setCancelable(false); // ���û�����ȡ���Ի���
		buider.setPositiveButton("ȷ��", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				Log.i(TAG, "����apk�ļ�" + info.getUpdateurl());
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					DownLoadFileThreadTask task = new DownLoadFileThreadTask(info.getUpdateurl(), "/sdcard/new.apk");
					pd.show();
					new Thread(task).start();
				 
				}else{
					Toast.makeText(getApplicationContext(), "sd��������", 1).show();
					loadMainActivity();
				}
			}
		});
		buider.setNegativeButton("ȡ��", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "�û�ȡ���������������");
				loadMainActivity();
			}
		});

		buider.create().show();
	}


	private class DownLoadFileThreadTask implements Runnable {

		String path;
		String filepath;
		public DownLoadFileThreadTask(String path, String filepath) {
			this.path = path;
			this.filepath = filepath;
		}

		@Override
		public void run() {
			DownloadFile downloadFile = new DownloadFile();
		
			try {
				File file = downloadFile.getFile(path, filepath, pd);
				pd.dismiss();
				install(file);
			} catch (Exception e) {
				Log.i(TAG, "�����ļ�ʧ��");
				pd.dismiss();
				loadMainActivity();
			}
		}
		
	}
	protected void loadMainActivity() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, com.example.save02.MainActivity.class);
		startActivity(intent);
		finish(); // �ѵ�ǰactivity������ջ�����Ƴ�
	}



	public void install(File file) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		finish();
		startActivity(intent);
	}
}
