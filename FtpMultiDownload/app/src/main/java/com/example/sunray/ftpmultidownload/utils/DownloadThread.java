package com.example.sunray.ftpmultidownload.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.example.sunray.ftpmultidownload.MainActivity;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sunray on 2017-9-25.
 */

public class DownloadThread implements Runnable{

    FTPClient mFtpClient;
    String mRemoteFilePath,mLocalFilePath;

    public DownloadThread(FTPClient mFtpClient, String mRemoteFilePath, String mLocalFilePath) {
        this.mFtpClient = mFtpClient;
        this.mRemoteFilePath = mRemoteFilePath;
        this.mLocalFilePath = mLocalFilePath;
    }

    private boolean mIsdownloading = true;

    public void setmIsdownloading(boolean mIsdownloading) {
        this.mIsdownloading = mIsdownloading;
    }


    public DownloadStatus download(FTPClient ftpClient, String remoteFilePath, String localFilePath) throws IOException {
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE); //设置以二进制方式传输
        DownloadStatus result;

        //检查远程文件是否存在
        FTPFile files[] = ftpClient.listFiles(new String(remoteFilePath.getBytes("GBK"),"iso-8859-1"));
        if (files.length != 1) {
            Log.d(MainActivity.TAG, "download: 文件不存在");
            return DownloadStatus.Remote_File_Noexist;
        }

        long remoteFileSize = files[0].getSize();
        File localFile = new File(localFilePath);
        //本地存在文件爱你，需要进行断点下载
        if (localFile.exists()) {
            long localFileSize = localFile.length();
            //判断本地文件是否大于远程文件大小（即判断是否下载完成）
            if (localFileSize >= remoteFileSize) {
                Log.d(MainActivity.TAG, "download: 本地文件>=远程文件，所以无需下载");
                return DownloadStatus.Local_Bigger_Remote;
            }

            //进行断点续传，并记录状态
            FileOutputStream fileOutputStream = new FileOutputStream(localFile,true); //true represents append
            ftpClient.setRestartOffset(localFileSize);
            InputStream inputStream = ftpClient.retrieveFileStream(new String(remoteFilePath.getBytes("GBK"),"iso-8859-1"));
            byte[] buffer = new byte[1024];
            long step = remoteFileSize / 100;
            long process = localFileSize / step;
            int length;
            while((length = inputStream.read(buffer)) != -1 && mIsdownloading) {
                fileOutputStream.write(buffer,0,length);
                localFileSize += length;
                long currentProcess = localFileSize / step;
                if (currentProcess > process) {
                    process = currentProcess;
                    if (process % 10 == 0) {
                        //更新文件下载进度，值存放在process变量中
                        Log.d(MainActivity.TAG, "download process: " + process);
                    }
                }
            }

            inputStream.close();
            fileOutputStream.close();
            boolean isDone = ftpClient.completePendingCommand(); //是否成功完成下载
            if (isDone) {
                result = DownloadStatus.Download_From_Break_Success;
                Log.d(MainActivity.TAG, "download: 断点下载成功");
            } else {
                result = DownloadStatus.Download_From_Break_Failed;
                Log.d(MainActivity.TAG, "download: 断点下载失败");

            }


        } else {
            FileOutputStream fileOutputStream = new FileOutputStream(localFile);
            InputStream inputStream = ftpClient.retrieveFileStream(new String(remoteFilePath.getBytes("GBK"),"iso-8859-1"));
            byte buffer[] = new byte[1024];
            long step = remoteFileSize / 100;
            long process = 0;
            long localFileSize = 0;
            int length;
            while ((length = inputStream.read(buffer)) != -1 && mIsdownloading) {
                fileOutputStream.write(buffer,0,length);
                localFileSize += length;
                long currentProcess = localFileSize / step;
                if (currentProcess > process) {
                    process = currentProcess;
                    if (process % 10 == 0) {
                        //更新文件下载进度，值存放在process变量中
                        Log.d(MainActivity.TAG, "download process: " + process);
                    }
                }
            }
            inputStream.close();
            fileOutputStream.close();
            boolean downloadNewStatus = ftpClient.completePendingCommand();
            if (downloadNewStatus) {
                Log.d(MainActivity.TAG, "download new file succeed");
                result = DownloadStatus.Download_New_Success;
            } else {
                Log.d(MainActivity.TAG, "download new file failed");
                result = DownloadStatus.Download_New_Failed;
            }
        }

        return result;
    }

    @Override
    public void run() {
        try {
            DownloadStatus state = download(mFtpClient,mRemoteFilePath,mLocalFilePath);
            Log.d(MainActivity.TAG, "run: downloadStatus=" + state);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(MainActivity.TAG, "run: download failed");
        }
    }
}
