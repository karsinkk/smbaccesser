package com.eb.client.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.eb.client.LogcatService;

public class LogcatUtil {

    public final static String TAG ="LogcatUtil";
    private static LogcatUtil mInstance;
    LogcatService mBoundService;
    Context mContext;
    boolean mIsBound = false;
    private LogcatUtil(Context ctx)
    {
        mContext = ctx;

    }

    public void init()
    {
        mContext.bindService(new Intent(mContext,
                LogcatService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public static LogcatUtil getInstance(Context ctx)
    {

        if(mInstance == null)
            mInstance = new LogcatUtil(ctx);
        return mInstance;
    }


    public void start()
    {
        if(mBoundService!= null)
            mBoundService.startLogcatDump();
    }

    public void stop()
    {

        if(mBoundService!= null)
            mBoundService.stopLogcatDump();
    }

    public boolean isRunning()
    {
        if(mBoundService == null)
            return false;
        Log.d(TAG, "------isRunning:"+mBoundService.isRunning());
        return mBoundService.isRunning();
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.    Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((LogcatService.LocalBinder)service).getService();
            mIsBound = true;
            // Tell the user about this for our demo.
            Toast.makeText(mContext, "Logcat service is started!",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            mIsBound = false;
            Toast.makeText(mContext, "Logcat service is stoped!",
                    Toast.LENGTH_SHORT).show();
        }
    };


}
