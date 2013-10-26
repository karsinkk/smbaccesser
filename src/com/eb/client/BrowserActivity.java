package com.eb.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eb.client.util.FileItem;
import com.eb.client.util.LogcatUtil;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

@SuppressLint("NewApi")
public class BrowserActivity extends Activity
implements OnItemClickListener,OnItemLongClickListener{

    public final static String TAG = "REJIN_BROWSER";

    ListView mList;
    ProgressDialog mLoadingPD;
    ProgressDialog mCopyPD;
    String mAction;
    FileItem root;
    copyTask mCT;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        FileItem fi = data.getParcelable(Constants.INFO);
        if(fi == null)
        {
            Log.d(TAG, "FileItem is null!");
            Toast.makeText(getApplicationContext(),
                    "there is no valid File selected!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAction = data.getString(Constants.ACTION);
        File path = new File(Constants.CACHEPATH);

        if(!path.exists())
            path.mkdir();

        setTitle(fi.getPath());
        root = fi;
        init();

        if(mAction.equals("browser"))
            browser(root);
        else
            install(root);

        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        LinearLayout adLayout=(LinearLayout)findViewById(R.id.adLayout);
        adLayout.addView(adView);
    }

    @Override
    public void onBackPressed ()
    {
        AppAdapter adapter =(AppAdapter)mList.getAdapter();
        if(adapter.isEditMode())
        {
            adapter.setEditMode(false);
            return;

        }

        if(!returnToParent(root))
            super.onBackPressed();
    }

    private void browser(FileItem fi)
    {
        setTitle(fi.getPath());
        root = fi;
        Log.d(TAG, "browser fileName: " + fi.getName() + " ,Path" + fi.getPath());
        mLoadingPD = new ProgressDialog(this);
        mLoadingPD.setMessage("Loading...");
        mLoadingPD.setCanceledOnTouchOutside(false);
        mLoadingPD.setCancelable(false);
        new SearchTask().execute(fi.getPath());
    }

    private void install(ArrayList<FileItem> files)
    {
        mCopyPD = new ProgressDialog(this);
        mCopyPD.setTitle("SmbAccesser");
        mCopyPD.setMessage("Loading...");
        mCopyPD.setCanceledOnTouchOutside(false);
        mCopyPD.setCancelable(false);
        copyTaskEx task = new copyTaskEx(true);
        task.execute(files);
    }

    private void copyFromRemote(ArrayList<FileItem> files)
    {
        mCopyPD = new ProgressDialog(this);
        mCopyPD.setTitle("SmbAccesser-Copy");
        mCopyPD.setMessage("Loading...");
        mCopyPD.setCanceledOnTouchOutside(false);
        mCopyPD.setCancelable(false);
        copyTaskEx task = new copyTaskEx(false);
        task.execute(files);
    }
    private void install(final FileItem fi)
    {
        mCopyPD = new ProgressDialog(this);
        mCopyPD.setTitle("SmbAccesser");
        mCopyPD.setMessage("Loading...");
        mCopyPD.setCanceledOnTouchOutside(false);
        mCopyPD.setCancelable(false);

        if(mAction.equals("browser"))
        {
            AlertDialog.Builder builder = new Builder(this);
            builder.setMessage("install this file/folder(install all APK in this folder)");
            builder.setTitle("SmbAccesser");
            builder.setCancelable(true);
            builder.setPositiveButton("ok", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mCT = new copyTask();
                    mCT.execute(fi);
                }
            });
            builder.setNegativeButton("no", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
        else
        {
            mCT = new copyTask();
            mCT.execute(fi);
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        menu.add(getString(R.string.editmode));
        menu.add(getString(R.string.command));
        String logcatStatus = LogcatUtil.getInstance(this).isRunning() ?
                "On" : "Off";
        menu.add(getString(R.string.logcat)+" "+logcatStatus);
        menu.add(getString(R.string.exit));
        AppAdapter adapter =(AppAdapter)mList.getAdapter();
        if(adapter.isEditMode())
        {
            MenuItem editFlag = menu.add(4, 3, 0, getString(R.string.editmode));
            editFlag.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            MenuItem install =  menu.add(4, 3, 1,getString(R.string.install));
            install.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            MenuItem copy =  menu.add(4, 3, 2,getString(R.string.copy));
            copy.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        }
        else
        {
            menu.removeItem(4);
        }
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getTitle().equals(getString(R.string.editmode)))
        {
            AppAdapter adapter =(AppAdapter)mList.getAdapter();
            adapter.setEditMode(!adapter.isEditMode());
        }
        else if(item.getTitle().equals(getString(R.string.install)))
        {
            AppAdapter adapter =(AppAdapter)mList.getAdapter();
            ArrayList<FileItem> selectedList = adapter.getSelectedFiles();
            if(selectedList.size() == 0)
                Toast.makeText(this, getString(R.string.noselected), Toast.LENGTH_SHORT).show();
            else
                install(selectedList);
        }
        else if(item.getTitle().equals(getString(R.string.copy)))
        {
            AppAdapter adapter =(AppAdapter)mList.getAdapter();
            ArrayList<FileItem> selectedList = adapter.getSelectedFiles();
            if(selectedList.size() == 0)
                Toast.makeText(this, getString(R.string.noselected), Toast.LENGTH_SHORT).show();
            else
                copyFromRemote(selectedList);
        }
        else if(item.getTitle().equals(getString(R.string.command)))
        {
            Intent intent = new Intent(Constants.ACTION_CMD);
            startActivity(intent);
        }
        else if(item.getTitle().equals(getString(R.string.exit)))
        {
            finish();
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

    private boolean returnToParent(FileItem fi)
    {
        if(fi == null) return false;

        String remoteFilepath =  fi.getPath();
        Pattern p=Pattern.compile("/");
        Matcher m=p.matcher(remoteFilepath);
        int count = 0;
        while(m.find())
        {
            count++;
        }

        if(count <=3) return false;

        SmbFile smbFile = null;
        try {
            smbFile = new SmbFile(remoteFilepath);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FileItem parent = fi.cloneByPath(smbFile.getParent());
        browser(parent);
        return true;


    }
    private void install(String path)
    {
        File folder = new File(path);
        if(!folder.exists()) return;

        File[] files = folder.listFiles();

        for(File f : files)
        {
            if(f.getName().endsWith(".apk") || f.getName().endsWith(".APK"))
            {
                try {
                    Runtime.getRuntime() .exec("adb install "+f.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //packageManager need time to update
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void init()
    {
        mList = (ListView)findViewById(R.id.list);
        mList.setOnItemClickListener(this);
        mList.setOnItemLongClickListener(this);
        mList.setAdapter(new AppAdapter());
    }

    private void cleanCache(String path)
    {
        File cache = new File(path);

        if(!cache.exists()) return;

        File[] files = cache.listFiles();

        if(files == null || files.length ==0 )
            return;

        for(File f : files)
        {
            f.delete();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

        AppAdapter adapter =(AppAdapter)mList.getAdapter();
        if(adapter.isEditMode()) return true;
        FileItem fi = (FileItem)mList.getItemAtPosition(position);
        install(fi);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        FileItem fi = (FileItem)mList.getItemAtPosition(position);
        AppAdapter adapter =(AppAdapter)mList.getAdapter();
        if(adapter.isEditMode())
        {
            //if(!fi.isFile()) return ;
            adapter.selectItem(position, !adapter.isSelected(position));
        }
        else
        {
            if(!fi.isFile())
            {
                browser(fi);
            }
            else
            {
                if((fi.getName().endsWith(".apk")||fi.getName().endsWith(".APK")))
                    install(fi);
                else
                    Toast.makeText(this, "this is file,can't browser!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    class copyTask extends AsyncTask<FileItem, Integer, Void>
    {

        String message;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mCopyPD.show();

        }
        @Override
        protected Void doInBackground(FileItem... params) {
            // TODO Auto-generated method stub
            //clean cache before copy and install tasks
            cleanCache(Constants.CACHEPATH);
            FileItem fi = params[0];
            message = "target file/folder is :" +fi.getName();
            publishProgress(0);
            //getRemoteFile(this,fi.getUserName(),fi.getPassword(),fi.getPath(),CACHEPATH);
            String remoteFilepath = fi.getPath();
            String localDirectory  = Constants.CACHEPATH;
            String remoteUsername = fi.getUserName();
            String remotePassword = fi.getPassword();
            if(remoteFilepath.startsWith("/") || remoteFilepath.startsWith("\\")){
                Toast.makeText(BrowserActivity.this,
                        "path format is wrong!", Toast.LENGTH_SHORT).show();
                return null;
            }
            if(!(localDirectory.endsWith("/") || localDirectory.endsWith("\\"))){
                Toast.makeText(BrowserActivity.this,
                        "path format is wrong!", Toast.LENGTH_SHORT).show();
                return null;
            }

            Log.d(TAG,"getRemoteFile  remoteUsername:"
                    +remoteUsername+",remotePassword:"+remotePassword
                    +",remoteFilepath:"+remoteFilepath);
            try {
                SmbFile smbFile = new SmbFile(remoteFilepath);
                if(smbFile.isDirectory()){
                    SmbFile[] files = smbFile.listFiles();
                    for(SmbFile file : files){
                        Log.d(TAG,"file:"+file.getName());

                        if(file.getName().endsWith(".apk") || file.getName().endsWith(".APK"))
                        {
                            message = "copy file:" +file.getName()+"...";
                            publishProgress(0);
                            copyRemoteFile(file, localDirectory);

                        }
                    }
                }else if(smbFile.isFile()){
                    if(smbFile.getName().endsWith(".apk") || smbFile.getName().endsWith(".APK"))
                    {
                        message = "copy file:" +smbFile.getName()+"...";
                        publishProgress(0);
                        copyRemoteFile(smbFile, localDirectory);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            message = "installing...";
            publishProgress(0);
            //install(CACHEPATH);

            //begin install ...
            File folder = new File(Constants.CACHEPATH);
            if(!folder.exists()) return null;

            File[] files = folder.listFiles();

            int counter = 0;
            for(File f : files)
            {
                if(f.getName().endsWith(".apk") || f.getName().endsWith(".APK"))
                {
                    counter++;
                    message = "installing file:" +f.getName()+"...";
                    publishProgress(0);
                    try {
                        Runtime.getRuntime() .exec("adb install "+f.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //packageManager need time to update
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            //cleanCache(CACHEPATH);
            message = "total "+counter+" apps be installed!";
            publishProgress(0);
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate();
            mCopyPD.setMessage(message);
        }
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            mCopyPD.dismiss();
            BrowserActivity.this.finish();
        }
    }
    class copyTaskEx extends AsyncTask<ArrayList<FileItem>, Integer, Void>
    {
        String message;
        boolean isInstallTask =true;

        public copyTaskEx(boolean flag)
        {
            isInstallTask = flag;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mCopyPD.show();
        }
        @Override
        protected Void doInBackground(ArrayList<FileItem>... params) {
            if(isInstallTask)
                cleanCache(Constants.CACHEPATH);
            ArrayList<FileItem> selectedFiles = params[0];
            message = "target file/folder is :" +selectedFiles.get(0).getName()+" ...";
            publishProgress(0);
            String localDirectory  = isInstallTask ? Constants.CACHEPATH : Constants.COPYPATH;
            if(!(localDirectory.endsWith("/") || localDirectory.endsWith("\\"))){
                Toast.makeText(BrowserActivity.this,
                        "path format is wrong!", Toast.LENGTH_SHORT).show();
                return null;
            }
            for(int i =0 ; i<selectedFiles.size();i++)
            {
                if(isInstallTask && (selectedFiles.get(i).isFile() &&
                        ( !selectedFiles.get(i).getPath().endsWith(".apk")
                                && !selectedFiles.get(i).getPath().endsWith(".APK"))))
                {
                    continue;
                }
                try {
                    SmbFile smbFile = new SmbFile(selectedFiles.get(i).getPath());
                    if(smbFile.isDirectory()){
                        SmbFile[] files = smbFile.listFiles();
                        for(SmbFile file : files){
                            Log.d(TAG,"file:"+file.getName());

                            //if(file.getName().endsWith(".apk") || file.getName().endsWith(".APK"))
                            {
                                message = "copy file:" +file.getName()+"...";
                                publishProgress(0);
                                copyRemoteFile(file, localDirectory);

                            }
                        }
                    }else if(smbFile.isFile()){

                        message = "copy file:" +smbFile.getName()+"...";
                        publishProgress(0);
                        copyRemoteFile(smbFile, localDirectory);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(!isInstallTask)
            {
                message = "copy task finish!";
                publishProgress(0);
                return null;
            }
            message = "installing...";
            publishProgress(0);
            File folder = new File(Constants.CACHEPATH);
            if(!folder.exists()) return null;
            File[] files = folder.listFiles();
            int counter = 0;
            for(File f : files)
            {
                if(f.getName().endsWith(".apk") || f.getName().endsWith(".APK"))
                {
                    counter++;
                    message = "installing file:" +f.getName()+"...";
                    publishProgress(0);
                    try {
                        Runtime.getRuntime() .exec("adb install "+f.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            message = "total "+counter+" apps be installed!";
            publishProgress(0);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            message = "updating...";
            publishProgress(0);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate();
            mCopyPD.setMessage(message);
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            mCopyPD.dismiss();
            if(isInstallTask)
                BrowserActivity.this.finish();

        }


    }

    class SearchTask extends AsyncTask<String, Void, Void>
    {
        ArrayList<FileItem> item = new ArrayList<FileItem>();

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mLoadingPD.show();
        }

        @Override
        protected Void doInBackground(String... params)
        {

            try
            {
                SmbFile smbFile = new SmbFile(params[0]);

                Log.d(TAG, "create smb file uname:"
                        +root.getUserName()+",pw:"+root.getPassword()+",info:"+params[0]);
                ArrayList<SmbFile> fileList = new ArrayList<SmbFile>();

                SmbFile[] fs = smbFile.listFiles();

                for (SmbFile f : fs)
                {
                    fileList.add(f);
                }

                for (SmbFile f : fileList)
                {

                    String filePath = f.getPath();
                    String fileName = f.getName();
                    boolean isFile = f.isFile();
                    Log.d(TAG, "fileName: " + fileName + " " + filePath
                            + " isFile: " + isFile);
                    item.add(new FileItem(root.getServerIp(),
                            root.getUserName(),root.getPassword(),fileName, filePath, isFile));
                }

            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (SmbException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            if (!item.isEmpty())
            {
                AppAdapter adapter =  (AppAdapter)mList.getAdapter();
                adapter.set(item);
            }
            else
            {
                Toast.makeText(BrowserActivity.this, "no file found! ",
                        Toast.LENGTH_SHORT).show();
            }

            mLoadingPD.dismiss();
        }

    }


    class AppAdapter extends BaseAdapter
    {
        ArrayList<FileItem> files = new ArrayList<FileItem>();
        boolean isEditMode = false;
        HashMap<Integer,Boolean> statusList = new HashMap<Integer,Boolean>();

        public void add(FileItem fi)
        {
            files.add(fi);
            notifyDataSetChanged();
        }

        public void setEditMode(boolean toEdit)
        {
            if(isEditMode && toEdit || !toEdit && !isEditMode)
                return;
            isEditMode = toEdit;
            if(!isEditMode)
            {
                for(int i=0;i<files.size();i++)
                {
                    statusList.put(i, false);
                }
            }
            notifyDataSetChanged();
            invalidateOptionsMenu();
        }
        public boolean isEditMode()
        {
            return isEditMode;
        }
        public void selectItem(int position, boolean status)
        {
            statusList.put(position, status);
            notifyDataSetChanged();
        }
        public boolean isSelected(int position)
        {
            boolean res =  statusList.get(position) == null? false : statusList.get(position);
            return res;
        }
        public void set(ArrayList<FileItem> list)
        {
            files = list;
            for(int i=0;i<files.size();i++)
            {
                statusList.put(i, false);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return files.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return files.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        public ArrayList<FileItem> getSelectedFiles()
        {
            ArrayList<FileItem> list = new ArrayList<FileItem>();
            for(int i=0;i<statusList.size();i++)
            {
                if(statusList.get(i))
                    list.add(files.get(i));
            }
            return list;
        }
        private int getFileIcon(String path)
        {

            if(path.endsWith(".APK")||path.endsWith(".apk"))
            {
                return R.drawable.ic_launcher;
            }

            //check video format
            for(int i=0;i<Constants.VIDEO.length;i++) {
                if(path.indexOf(Constants.VIDEO[i]) > 0) {
                    return R.drawable.video;
                }
            }

            for(int i=0;i<Constants.AUDIO.length;i++) {
                if(path.indexOf(Constants.AUDIO[i]) > 0) {
                    return R.drawable.music;
                }
            }

            for(int i=0;i<Constants.IMAGE.length;i++) {
                if(path.indexOf(Constants.IMAGE[i]) > 0) {
                    return R.drawable.pic;
                }
            }


            return R.drawable.file;
        }
        @Override
        public View getView(int arg0, View convertView, ViewGroup arg2) {
            // TODO Auto-generated method stub
            ViewHolder holder = null;
            if(convertView == null)
            {
                LayoutInflater inflater = LayoutInflater.from(BrowserActivity.this);
                convertView = inflater.inflate(R.layout.fileinfo, null);
                android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(android.widget.AbsListView.LayoutParams.MATCH_PARENT, 80);
                convertView.setLayoutParams(params);
                holder = new ViewHolder();
                holder.image = (ImageView)convertView.findViewById(R.id.icon);
                holder.text  = (TextView)convertView.findViewById(R.id.name);
                holder.box = (CheckBox)convertView.findViewById(R.id.flag);
                convertView.setTag(holder);
            }
            else
            {
                holder  = (ViewHolder)convertView.getTag();
            }
            if(!files.get(arg0).isFile())
                holder.image.setImageResource(R.drawable.folder);
            else
                holder.image.setImageResource(getFileIcon(files.get(arg0).getName()));

            holder.text.setText(files.get(arg0).getName());
            if(isEditMode)
            {
                holder.box.setVisibility(View.VISIBLE);
                holder.box.setChecked(isSelected(arg0));
            }
            else
                holder.box.setVisibility(View.INVISIBLE);
            return convertView;
        }
        class ViewHolder
        {
            ImageView image;
            TextView  text;
            CheckBox   box;
        }

    }


    private  boolean copyRemoteFile(SmbFile smbFile, String localDirectory) {


        Log.d(TAG,"copyRemoteFile  smbFile:"+smbFile.getName()+",start");

        SmbFileInputStream in = null;
        FileOutputStream out = null;
        try {
            File[] localFiles = new File(localDirectory).listFiles();
            if(null == localFiles){
                new File(localDirectory).mkdirs();
            }else if(localFiles.length > 0){

            }
            in = new SmbFileInputStream(smbFile);
            out = new FileOutputStream(localDirectory + smbFile.getName());
            byte[] buffer = new byte[1024];
            int len = -1;
            while((len=in.read(buffer)) != -1){
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            Log.d(TAG,"copyRemoteFile  smbFile:"+smbFile.getName()+",failed");
            e.printStackTrace();
            return false;
        } finally {
            if(null != out){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(null != in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.d(TAG,"copyRemoteFile  smbFile:"+smbFile.getName()+",end");
        return true;
    }


    public  boolean getRemoteFile(copyTask task,
            String remoteUsername, String remotePassword,
            String remoteFilepath,String localDirectory) {
        boolean isSuccess = false;
        if(remoteFilepath.startsWith("/") || remoteFilepath.startsWith("\\")){
            Log.d(TAG, "path format is wrong!");
            return isSuccess;
        }
        if(!(localDirectory.endsWith("/") || localDirectory.endsWith("\\"))){
            Log.d(TAG, "path format is wrong!");
            return isSuccess;
        }

        Log.d(TAG,"getRemoteFile  remoteUsername:"
                +remoteUsername+",remotePassword:"+remotePassword
                +",remoteFilepath:"+remoteFilepath);
        try {
            SmbFile smbFile = new SmbFile(remoteFilepath);
            if(smbFile.isDirectory()){
                SmbFile[] files = smbFile.listFiles();
                for(SmbFile file : files){
                    Log.d(TAG,"file:"+file.getName());

                    if(file.getName().endsWith(".apk") || file.getName().endsWith(".APK"))
                    {
                        isSuccess = copyRemoteFile(file, localDirectory);
                    }
                }
            }else if(smbFile.isFile()){
                if(smbFile.getName().endsWith(".apk") || smbFile.getName().endsWith(".APK"))
                    isSuccess = copyRemoteFile(smbFile, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }


}
