package com.eb.client;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.eb.client.util.FileItem;
import com.eb.client.util.LogcatUtil;

import net.youmi.android.AdManager;
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

public class MainActivity extends Activity
{

    private final static String TAG ="REJIN_Mainactivity";
    private EditText ip ;
    private Button add ;
    private String root = "/";
    private EditText name;
    private EditText pw;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AdManager.getInstance(this).init("22cf83070ae2981a","8c632fa3cb14d3f1",false);
        setContentView(R.layout.main);
        Intent intent = new Intent(this, MyFileService.class);
        startService(intent);
        LogcatUtil.getInstance(this).init();
        init();
        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        LinearLayout adLayout=(LinearLayout)findViewById(R.id.adLayout);
        adLayout.addView(adView);


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        menu.add(getString(R.string.command));
        String logcatStatus = LogcatUtil.getInstance(this).isRunning() ?
                "On" : "Off";
        menu.add(getString(R.string.logcat)+" "+logcatStatus);
        menu.add("About");
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getTitle().equals("About"))
        {
            String version ="1.0";
            try {
                version = getPackageManager()
                        .getPackageInfo(getPackageName(),
                                PackageManager.GET_CONFIGURATIONS).versionName;
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            AlertDialog.Builder about = new Builder(this);
            about.setTitle("About");
            about.setMessage("this app used to browser/install the file in LAN server, Version: "
                    +version);
            about.setCancelable(true);
            about.setPositiveButton("Ok",new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });
            about.show();

        }
        else if(item.getTitle().equals(getString(R.string.command)))
        {
            Intent intent = new Intent(Constants.ACTION_CMD);
            startActivity(intent);
        }
        else if(item.getTitle().toString().startsWith(getString(R.string.logcat)))
        {
            if(LogcatUtil.getInstance(this).isRunning())
                LogcatUtil.getInstance(this).stop();
            else
                LogcatUtil.getInstance(this).start();
        }
        return super.onOptionsItemSelected(item);
    }


    private void init()
    {
        add = (Button) findViewById(R.id.add);
        ip = (EditText) findViewById(R.id.ip);
        name = (EditText) findViewById(R.id.username);
        pw = (EditText) findViewById(R.id.pw);
        add.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String ipValue = ip.getText().toString();
                String serverip = ip.getText().toString();
                String userName = name.getText().toString();
                String password = pw.getText().toString();
                root = "smb://" + userName + ":" + password + "@" + ipValue+"/";
                FileItem fi = new FileItem(serverip,userName,password,"...", root, false);

                Intent intent = new Intent(MainActivity.this,BrowserActivity.class);
                Bundle data = new Bundle();
                data.putParcelable(Constants.INFO, fi);
                data.putString(Constants.ACTION, "browser");
                intent.putExtras(data);
                startActivity(intent);
            }
        });



    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Intent intent = new Intent(this, MyFileService.class);
        stopService(intent);
    }


}