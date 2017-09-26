package com.example.sunray.ftpmultidownload;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sunray.ftpmultidownload.utils.Constants;
import com.example.sunray.ftpmultidownload.utils.DownloadThread;
import com.example.sunray.ftpmultidownload.utils.FtpUtil;

import org.apache.commons.net.ftp.FTPClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * download
 */
public class DownloadActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity1";


    ListView mListView;
    List<String> mList = new ArrayList<>();
    int count = 80;
    MyBaseAdapter mMyBaseAdapter;
//    DownloadThread mDownloadThread;
    Map<Integer,DownloadThread> mHashMap = new HashMap<>();
    FTPClient mFtpClient = new FTPClient();
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {

                Toast.makeText(DownloadActivity.this,"connect succeed",Toast.LENGTH_SHORT).show();
            } else if (msg.what == 0){
                Toast.makeText(DownloadActivity.this,"connect failed",Toast.LENGTH_SHORT).show();

            }  else if (msg.what == 3) {
                Toast.makeText(DownloadActivity.this,"download succeed",Toast.LENGTH_SHORT).show();
            } else if (msg.what == 4) {
                Toast.makeText(DownloadActivity.this,"download failed", Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.lv);
//        for (int i = 0; i < 70; ++i) {
//            mList.add("item:" + i);
//        }
        mMyBaseAdapter = new MyBaseAdapter(mList,this);
        MyBaseAdapter.IListener iListener = new MyBaseAdapter.IListener() {
            @Override
            public void startListener(int position) {
                Toast.makeText(DownloadActivity.this,"start点击了:" + position,Toast.LENGTH_SHORT).show();
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                        //注意：必须确保本地的文件夹存在！！！！！！！！！！！！！！！！！！！！！！！
                            File dir = new File(Constants.LOCALPATH);
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            DownloadStatus state = FtpUtil.download(mFtpClient,Constants.REMOTEPATH + "test1.pdf",Constants.LOCALPATH + "1.pdf");
                            Log.d(TAG, "run: downloadStatus=" + state);
                            Message message = Message.obtain();
                            message.what = 3;
                            mHandler.sendMessage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "run: download failed");
                            Message message = Message.obtain();
                            message.what = 4;
                            mHandler.sendMessage(message);
                        }
                    }
                }).start();*/

                DownloadThread downloadThread = new DownloadThread(mFtpClient,
                        Constants.REMOTEPATH + "test" + position + ".pdf",Constants.LOCALPATH + position + ".pdf");
                new Thread(downloadThread).start();
                mHashMap.put(position,downloadThread);
            }

            @Override
            public void stopListener(int position) {
                DownloadThread downloadThread = mHashMap.get(position);
                if (downloadThread != null) {
                    downloadThread.setmIsdownloading(false);
                }
                Toast.makeText(DownloadActivity.this,"stop点击了:" + position,Toast.LENGTH_SHORT).show();
            }
        };
        mMyBaseAdapter.setmListener(iListener);
        mListView.setAdapter(mMyBaseAdapter);
        if (mFtpClient.isConnected()) {
            Toast.makeText(this,"has been connected",Toast.LENGTH_SHORT).show();
        } else {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean flag = FtpUtil.login(mFtpClient, Constants.REMOTE_URL,Constants.REMOTE_PORT,Constants.REMOTE_USER,Constants.REMOTE_PASSWORD);
                    Log.d(TAG, "run: " + (flag? "connected":"connect failed"));
                    Message msg = Message.obtain();
                    msg.what = flag ? 1: 0;
                    mHandler.sendMessage(msg);
                }
            }).start();
        }


    }

    public void add(View view) {
        mList.add("ahha:" + count++);
        mMyBaseAdapter.notifyDataSetChanged();
    }


}
