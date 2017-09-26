package com.example.sunray.ftpmultidownload;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sunray.ftpmultidownload.utils.Constants;
import com.example.sunray.ftpmultidownload.utils.DownloadStatus;
import com.example.sunray.ftpmultidownload.utils.DownloadThread;
import com.example.sunray.ftpmultidownload.utils.FtpUtil;
import com.example.sunray.ftpmultidownload.utils.UploadThread;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * upload
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity1";


    ListView mListView;
    List<String> mList = new ArrayList<>();
    int count = 80;
    MyBaseAdapter mMyBaseAdapter;
    FTPClient mFtpClient = new FTPClient();
    Map<Integer,UploadThread> mMap = new HashMap<>();

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {

                Toast.makeText(MainActivity.this,"connect succeed",Toast.LENGTH_SHORT).show();
            } else if (msg.what == 0){
                Toast.makeText(MainActivity.this,"connect failed",Toast.LENGTH_SHORT).show();

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
                Toast.makeText(MainActivity.this,"start点击了:" + position,Toast.LENGTH_SHORT).show();
                UploadThread uploadThread = new UploadThread(mFtpClient,Constants.REMOTEPATH + position + ".pdf",Constants.LOCALPATH + position + ".pdf");
                new Thread(uploadThread).start();
                mMap.put(position,uploadThread);

            }

            @Override
            public void stopListener(int position) {
                Toast.makeText(MainActivity.this,"stop点击了:" + position,Toast.LENGTH_SHORT).show();
                UploadThread uploadThread = mMap.get(position);
                if (uploadThread != null) {
                    uploadThread.setUploading(false);
                }
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
