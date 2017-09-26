package com.example.sunray.ftpmultidownload.utils;

import android.util.Log;

import com.example.sunray.ftpmultidownload.MainActivity;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

/**
 * Created by sunray on 2017-9-25.
 */

public class UploadThread implements Runnable {
    FTPClient mFtpClient;
    String mRemoteFilePath,mLocalFilePath;
    boolean isUploading = true;

    public void setUploading(boolean uploading) {
        isUploading = uploading;
    }

    public UploadThread(FTPClient mFtpClient, String mRemoteFilePath, String mLocalFilePath) {
        this.mFtpClient = mFtpClient;
        this.mRemoteFilePath = mRemoteFilePath;
        this.mLocalFilePath = mLocalFilePath;
    }


    public UploadStatus upload(FTPClient ftpClient,String localFilePath,String  remoteFilePath) throws IOException {
        //设置passiveMoode传输
        ftpClient.enterLocalPassiveMode();
        //设置以二进制流的方式传输
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setControlEncoding("GBK");
        UploadStatus result;
        //对远程目录的处理
        String remoteFileName;
        if (remoteFilePath != null && remoteFilePath.contains("/")) {
            remoteFileName = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
            //创建并进入server远程目录结构，创建并且进入失败直接放回
            if (createDirectory(remoteFilePath,ftpClient) == UploadStatus.Create_Directory_Fail) {
                Log.d(MainActivity.TAG, "upload: create remote directory failed");
                return UploadStatus.Create_Directory_Fail;
            }
        } else {
            Log.d(MainActivity.TAG, "upload:remote file path has some problem! ");
            return null;
        }

        //检查远程是否存在文件
        FTPFile files[] = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"),"iso-8859-1"));
        if (files.length == 1) { //远程server的该文件存在
            long remoteFileSize = files[0].getSize();
            File localFile = new File(localFilePath);
            long localFileSize = localFile.length();
            if (remoteFileSize == localFileSize) {
                return UploadStatus.File_Exits;
            } else if (remoteFileSize > localFileSize) {
                return UploadStatus.Remote_Bigger_Local;
            }

            //尝试移动文件内读取指针，实现断点续传
            result = uploadFile(ftpClient,remoteFileName,localFile,remoteFileSize);
            Log.d(MainActivity.TAG, "upload: 断点续传,result=" + result);

            //如果断点续传没有成功，则删除server上的文件，重新上传
            if (result == UploadStatus.Upload_From_Break_Failed) {
                if (!ftpClient.deleteFile(remoteFileName)) {
                    Log.d(MainActivity.TAG, "upload: 如果断点续传不成功，但是delete该文件失败");
                    return UploadStatus.Delete_Remote_Faild;
                }

                result = uploadFile(ftpClient,remoteFileName,localFile,0);
                Log.d(MainActivity.TAG, "upload:重新上传的result= " + result);
            }
        } else {
            //新文件上传
             result = uploadFile(ftpClient,remoteFileName,new File(localFilePath),0);
            Log.d(MainActivity.TAG, "upload:新文件上传成功，result= " + result);
        }
        return result;

    }

    /**
     * 上传文件到server：新的文件上传或者旧文件的断点续传
     * @param ftpClient
     * @param remoteFileName 远程的文件名，在上传之前，已经切换到server上的指定目录
     * @param localFile  本地文件的File句柄，绝对路径
     * @param remoteFileSize
     * @return
     */
    private UploadStatus uploadFile(FTPClient ftpClient, String remoteFileName, File localFile, long remoteFileSize) throws IOException {

        UploadStatus result;
        //显示文件上传的进度
        long step = localFile.length() / 100;
        long process = 0;
        long localReadBytes = 0;
        RandomAccessFile raf = new RandomAccessFile(localFile,"r");
        //如果是断点续传
        if (remoteFileSize > 0) {
            ftpClient.setRestartOffset(remoteFileSize);
            raf.seek(remoteFileSize);
            process = remoteFileSize / step;
            localReadBytes = remoteFileSize;
        }
        OutputStream outputStream = ftpClient.appendFileStream(new String(remoteFileName.getBytes("GBK"),"iso-8859-1"));
        byte[] buffer = new byte[2014];
        int length;
        while((length = raf.read(buffer)) != -1 && isUploading) {
            outputStream.write(buffer,0,length);
            localReadBytes += length;
            process = localReadBytes / step;
            Log.d(MainActivity.TAG, "uploadFile: 上传进度为:" + process);
        }
        outputStream.flush();
        outputStream.close();
        raf.close();
        boolean isCompleted = ftpClient.completePendingCommand();
        if (remoteFileSize > 0) {
            result = isCompleted ? UploadStatus.Upload_From_Break_Success : UploadStatus.Upload_From_Break_Failed;
        } else {
            result = isCompleted ? UploadStatus.Upload_New_File_Success : UploadStatus.Upload_New_File_Failed;

        }
        if (!isUploading) {
            Log.d(MainActivity.TAG, "uploadFile:暂停续传 isCompleted=" + isCompleted);
            return null;
        }
        return result;
    }

    /**
     * 递归创建远程server目录
     * @param remoteFilePath 远程server的文件绝对路径
     * @param ftpClient
     * @return 目录创建是否成功
     */
    private UploadStatus createDirectory(String remoteFilePath, FTPClient ftpClient) throws IOException {
        UploadStatus result = UploadStatus.Create_Directory_Success;
        String needMakeDirectory = remoteFilePath.substring(0,remoteFilePath.lastIndexOf("/") + 1);
        if (!needMakeDirectory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(new String(needMakeDirectory.getBytes("GBK"),"iso-8859-1"))) {
            //如果远程目录不存在，则递归创建远程server的目录
            int start = 0;
            int end = 0;
            if (needMakeDirectory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }

            end = needMakeDirectory.indexOf("/",start);
            while(true) {
                String subDirectory = new String(remoteFilePath.substring(start,end).getBytes("GBK"),"iso-8859-1");
                //创建目录并进入该目录
                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                    if (ftpClient.makeDirectory(subDirectory)) {
                      ftpClient.changeWorkingDirectory(subDirectory);
                    } else {
                        Log.d(MainActivity.TAG, "createDirectory failed ");
                        return UploadStatus.Create_Directory_Fail;
                    }
                }

                start = end + 1;
                end = needMakeDirectory.indexOf("/",start);
                if ( end <= start) { //检查所有目录是否成功创建并且进入该目录
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void run() {

        try {
            UploadStatus result = upload(mFtpClient,mLocalFilePath,mRemoteFilePath);
            Log.d(MainActivity.TAG, "run: upload sucess!!!!!!!");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(MainActivity.TAG, "run: upload failed!!!!!!!");
        }
    }
}
