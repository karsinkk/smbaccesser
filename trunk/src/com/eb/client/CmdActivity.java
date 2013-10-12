package com.eb.client;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CmdActivity extends Activity{


    EditText cmdInput;
    TextView cmdResult;
    Button mAction;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cmd);

        cmdInput = (EditText)findViewById(R.id.cmd_input);
        cmdInput.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == 0)
                {
                    shell();
                    cmdInput.setText("");
                    return true;
                }
                return false;
            }

        });
        cmdResult =  (TextView)findViewById(R.id.cmd_result);
        mAction = (Button)findViewById(R.id.go);
        mAction.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View arg0) {
                shell();
                cmdInput.setText("");

            }

        });

        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        LinearLayout adLayout=(LinearLayout)findViewById(R.id.adLayout);
        adLayout.addView(adView);
    }

    private void shell()
    {
        String action = cmdInput.getEditableText().toString();
        if(action == null || action.equals(""))
        {
            return;
        }

        String res = exec(action);
        cmdResult.setText(res);
    }

    private String exec(String[] args) {
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream is = null;
        try {
            process = processBuilder.start();
            is = process.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            while ((read = is.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
    private  String exec(String cmd) {
        String s = "";
        if(cmd.startsWith("adb logcat")) return s;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line + "\n";
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s;
    }

}
