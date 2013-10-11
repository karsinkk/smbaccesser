package com.eb.client.util;


public class FileUtil
{
    private static String type = "*/*";
    public static String ip = "127.0.0.1";
    public static int port = 0;


    public static String getFileType(String uri)
    {
        if (uri == null)
        {
            return type;
        }
        else if (uri.endsWith(".mp3"))
        {
            return "audio/mpeg";
        }
        else if (uri.endsWith(".mp4"))
        {
            return "video/mp4";
        }
        else if(uri.endsWith(".apk"))
        {
            return "application/vnd.android.package-archive";
        }

        return type;
    }




}
