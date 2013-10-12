package com.eb.client;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.eb.client.util.LogcatDumper;

public class LogcatService extends Service{

    private final IBinder mBinder = new LocalBinder();
    BroadcastReceiver  mActionReceiver  = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context cts, Intent intent) {

            String action = intent.getAction();
            if(Constants.ACTION_START.equals(action))
            {
                startLogcatDump();
            }
            else if(Constants.ACTION_STOP.equals(action))
            {
                stopLogcatDump();
            }

        }

    };

    public class LocalBinder extends Binder {
        public LogcatService getService() {
            return LogcatService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    public void startLogcatDump()
    {
        LogcatDumper.getInstance().start();
    }

    public void stopLogcatDump()
    {
        LogcatDumper.getInstance().stop();
    }

    public boolean isRunning()
    {
        return LogcatDumper.getInstance().isRunning();
    }



}
