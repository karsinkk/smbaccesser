package com.eb.client.util;

import android.os.Parcel;
import android.os.Parcelable;

public class FileItem implements Parcelable
{
    private String name = "";
    private String path = "/";
    private boolean isFile = false;

    private String mServerIp;
    private String mUserName;
    private String mUserPW;


    public static final Parcelable.Creator<FileItem> CREATOR = new Parcelable.Creator<FileItem>() {

        @Override
        public FileItem createFromParcel(Parcel source) {
            // TODO Auto-generated method stub

            String ip = source.readString();
            String userName = source.readString();
            String password = source.readString();
            String name = source.readString();
            String patch = source.readString();
            boolean[] res = new boolean[1];
            source.readBooleanArray(res);
            return new FileItem(ip,userName,password,name,patch,res[0]);
        }

        @Override
        public FileItem[] newArray(int size) {
            // TODO Auto-generated method stub
            return new FileItem[size];
        }

    };

    public FileItem()
    {
        super();
    }


    public FileItem(String name, String path, boolean isFile)
    {
        super();
        this.name = name;
        this.path = path;
        this.isFile = isFile;
    }

    public FileItem(String ip,String uName,String pw,String name, String path, boolean isFile)
    {

        this(name,path,isFile);
        mServerIp = ip;
        mUserName = uName;
        mUserPW = pw;

    }

    public FileItem cloneByPath(String path)
    {
        FileItem clone = new FileItem();
        clone.path = path;
        clone.isFile = false;
        clone.mServerIp = this.mServerIp;
        clone.mUserName = this.mUserName;
        clone.mUserPW = this.mUserPW;
        return clone;

    }

    public String getServerIp()
    {
        return mServerIp;
    }

    public String getUserName()
    {
        return mUserName;
    }


    public String getPassword()
    {
        return mUserPW;
    }

    public String getName()
    {
        return name;
    }

    public String getPath()
    {
        return path;
    }

    public boolean isFile()
    {
        return isFile;
    }

    @Override
    public String toString()
    {
        return name;
    }


    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public void writeToParcel(Parcel object, int arg1) {
        object.writeString(this.mServerIp);
        object.writeString(this.mUserName);
        object.writeString(this.mUserPW);
        object.writeString(this.name);
        object.writeString(this.path);
        boolean[] res = new boolean[1];
        res[0] = this.isFile;
        object.writeBooleanArray(res);

    }

}
