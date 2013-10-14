package com.eb.client;

import android.os.Environment;

public class Constants {

    public final static String INFO ="file info";
    public final static String ACTION ="action";
    public final static String ROOT = Environment.getExternalStorageDirectory().getPath() +"/SmbAccesser/";
    public final static String CACHEPATH=ROOT+"Cache/";
    public final static String COPYPATH=ROOT+"Download/";
    public final static String ACTION_CMD="com.eb.cmd";
    public final static String ACTION_START = "com.eb.logcat.start";
    public final static String ACTION_STOP = "com.eb.logcat.stop";
    public final static String LOGCATPATH = ROOT+"Logcat/";
    public static final String[] VIDEO = new String[] {
        /* video */
        ".MP4",
        ".mp4",
        ".tts",
        ".flv",
        ".avi",
        ".AVI",
        ".wmv",
        ".m4v",
        ".mpeg",
        ".mpg",
        ".MPG",
        ".ts",
        ".3gp",
        ".mkv",
        ".MKV",
        ".divx",
        ".vob",
        ".VOB",
        ".mov",
        ".MOV",
        ".qt",
        ".QT",
        ".dat",
        ".DAT",
        ".rm",
        ".RM",
        ".rmvb",
        ".RMVB",


    };

    public static final String[] AUDIO = new String[] {
        /* audio */
        ".mp3",
        ".MP3",
        ".wma",
        ".wav",
        ".ogg",
        ".m4a",
        ".aac",
        ".ac3",
    };

    public static final String[] IMAGE = new String[] {
        /* picture */
        ".jpg",
        ".jpeg",
        ".png",
        ".bmp",
        ".gif",
    };
}
