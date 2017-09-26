package com.example.sunray.ftpmultidownload.utils;

import android.os.Environment;

/**
 * Created by sunray on 2017-9-25.
 */

public class Constants {
    /**
     *
     * login params for ftp
     *
     */
    public static final String SEARCH_4_FILE = "SEARCH_4_FILE";

    public static final String REMOTE_URL = "192.168.0.199";

    public static final String REMOTE_USER = "sunray";

    public static final String REMOTE_PASSWORD = "121212";

    public static final int REMOTE_PORT = 2121;

    public static String REMOTEPATH = "/LocalUser/";
    public static String LOCALPATH = Environment.getExternalStorageDirectory()+"/csx/";
}
