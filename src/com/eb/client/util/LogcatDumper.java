package com.eb.client.util;

import android.util.Log;

import com.eb.client.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogcatDumper {


    public final static String TAG = "LogcatDumper";
    private static LogcatDumper mInstance;
    LogDumper mTask;

    public static LogcatDumper getInstance()
    {
        if(mInstance == null)
            mInstance = new LogcatDumper();
        return mInstance;
    }


    public void start() {
        Log.d(TAG, "------start:  mTask:"+mTask);
        if(mTask==null){
            mTask = new LogDumper(String.valueOf(android.os.Process.myPid()),Constants.LOGCATPATH);
            mTask.start();
        }

    }

    public void stop()
    {

        Log.d(TAG, "------stop:  mTask:"+mTask);
        if(mTask!=null){
            mTask.stopLogs();
            mTask = null;
        }

    }

    public boolean isRunning()
    {

        Log.d(TAG, "------isRunning:  mTask:"+mTask);
        if(mTask == null)
            return false;

        return mTask.isRunning();
    }


    class LogDumper extends Thread{

        String fileName;
        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = false;
        String cmds=null;
        private final String mPID;
        private FileOutputStream out = null;
        private final List<String> logsMessage = new ArrayList<String>();
        private String logFileName;
        public LogDumper(String pid,String file) {
            mPID = String.valueOf(pid);
            fileName = file;

            File folder = new File(fileName);
            if(!folder.exists())
            {
                folder.mkdir();
            }
            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date now=new Date();
            String date = sfd.format(now);
            date = date.replace(" ", "-");
            date = date.replace(":", "-");
            String logpath = fileName+"/"+date+".log";
            File mFile = new File(logpath);
            Log.d(TAG, "------logPath:"+logpath);
            if(!mFile.exists()){
                try {
                    mFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                logFileName = mFile.toString();
                out = new FileOutputStream(mFile,true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /**
             * 日志等级：*:v  , *:d  , *:w , *:e , *:f  , *:s
             * 显示当前mPID程序的 E和W等级的日志.
             * */
            //cmds ="logcat *:e *:w | grep \"("+mPID+")\"";
            cmds = "logcat";
        }

        public String getLogFileName()
        {
            return logFileName;
        }

        public void stopLogs() {
            Log.d(TAG, "------stopLogs");
            mRunning = false;
        }

        public boolean isRunning()
        {

            Log.d(TAG, "------mRunning:"+mRunning);
            return mRunning;
        }

        private boolean checkFileMaxSize(String file){
            File sizefile = new File(file);
            if(sizefile.exists()){
                //1.5MB
                if(sizefile.length()>1572864){
                    return true;
                }
                else {
                    return false;
                }
            }else {
                return false;
            }
        }

        @Override
        public void run() {
            Log.d(TAG, "------run----------------------start");
            mRunning = true;
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {

                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    synchronized (out) {
                        if (out != null) {
                            /*boolean maxSize = checkFileMaxSize(getLogFileName());
                            if(maxSize){
                            }*/
                            if(logsMessage.size()>0){
                                for(String _log:logsMessage){
                                    out.write(_log.getBytes());
                                }
                                logsMessage.clear();
                            }
                            /**
                             * 再次过滤日志，筛选当前日志中有 mPID 则是当前程序的日志.
                             * */
                            /*if(line.contains(mPID))*/{
                                out.write(line.getBytes());
                                out.write("\n".getBytes());
                            }
                        }
                    }
                }


                Log.d(TAG, "------run----------------------end");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(out!=null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }

}
