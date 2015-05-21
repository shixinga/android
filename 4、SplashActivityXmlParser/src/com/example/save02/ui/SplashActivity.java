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
				pd.setMessage("正在下载...");
				Log.i(TAG, "弹出升级对话框");
				showUpdateDialog();
			} else if(msg.what == 2) {
				loadMainActivity();
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 取消标题栏,必须放在这个位置
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		initSplash();

	}
	
	
	
	private void initSplash() {
		// TODO Auto-generated method stub
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText(getVersion());
		
		// 完成窗体的全屏显示 // 取消掉状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		ll_splash_main = (LinearLayout) findViewById(R.id.ll_splash_main);

		//淡入淡出效果
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
			return "版本号没找到";
		}
	}


	private class UpdateThread implements Runnable {

		@Override
		public void run() {

			UpdateService service = new UpdateService(SplashActivity.this);
			try {
				info = service.getUpdateInfo(R.string.updateurl);
			
				if(info.getVersion().equals(getVersion())) {
					
					Log.i(TAG, "获取版本号相同，不用下载");
					Thread.sleep(2000);
					handler.sendEmptyMessage(2);
				} else {
					Log.i(TAG, "获取版本号bu同，用下载");
					handler.sendEmptyMessage(1);
				}
				Log.i(TAG, getVersion());
			} catch (Exception e) {
				Log.i(TAG, "获取版本号失败");
				handler.sendEmptyMessage(2);
			}
		}
		
	}
	
	protected void showUpdateDialog() {
		AlertDialog.Builder buider = new Builder(this);
		buider.setIcon(R.drawable.icon5);
		buider.setTitle("升级提醒");
		buider.setMessage(info.getDescription());
		buider.setCancelable(false); // 让用户不能取消对话框
		buider.setPositiveButton("确定", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				Log.i(TAG, "下载apk文件" + info.getUpdateurl());
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					DownLoadFileThreadTask task = new DownLoadFileThreadTask(info.getUpdateurl(), "/sdcard/new.apk");
					pd.show();
					new Thread(task).start();
				 
				}else{
					Toast.makeText(getApplicationContext(), "sd卡不可用", 1).show();
					loadMainActivity();
				}
			}
		});
		buider.setNegativeButton("取消", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "用户取消进入程序主界面");
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
				Log.i(TAG, "下载文件失败");
				pd.dismiss();
				loadMainActivity();
			}
		}
		
	}
	protected void loadMainActivity() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, com.example.save02.MainActivity.class);
		startActivity(intent);
		finish(); // 把当前activity从任务栈里面移除
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
