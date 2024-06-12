package com.gs.jxjh.installapp.mo;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import com.gs.jxjh.installapp.mo.base.Pre;
import com.gs.jxjh.installapp.mo.callback.installSDKListener;
import com.gs.jxjh.installapp.mo.constant.constants;
import com.gs.jxjh.installapp.mo.utils.ApkInstaller;
import com.gs.jxjh.installapp.mo.utils.LogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InstallSDK extends Pre {
    public static final String TAG = InstallSDK.class.getSimpleName();
    private ArrayList<String> packagNameList;
    private MyReceiver receiver;
    private ApkInstaller mApkInstaller;
    public boolean flag=false;
    private String assetPath = "";
    private String appName = "";
    private String sonApkpackageName = "";
    private String getSonMainActivity = "";
    private String isOpen = "0";
    private static InstallSDK mInstance;
    private String mainAPKPackageName = "";
    private String apkFileName = "Bricks_new.apk";
    private long downloadId;

    private InstallSDK() {
    }

    public synchronized static InstallSDK getInstance() {
        if (null == mInstance){
            mInstance = new InstallSDK();
        }
        return mInstance;
    }

    @Override
    public void init() {
        registerReceiver();
        initpackagNameList();
        mainAPKPackageName = activity.getPackageName();
        appName = data.get("appName") +"";
        if (!TextUtils.isEmpty(appName)) {
            apkFileName = appName;
        }
        sonApkpackageName = data.get("packageName") +"";
        getSonMainActivity = data.get("mainActivity") + "";
        constants.isOpenUrl = data.get("isOpen") + "";
        constants.apkUrl = data.get("downloadUrl") + "";
        LogUtils.d(TAG, "myPackageName==" +mainAPKPackageName + " appName=="+appName + " sonApkpackageName=="+sonApkpackageName +" getSonMainActivity=="+getSonMainActivity);
        //判断远程开关

        onCallBack(installSDKListener.INIT_SUCCESS,"SUCCESS");
        new MyAsyncTask().execute();
    }


    public void installAPKs() {
        boolean installed = detectApk(sonApkpackageName);
        if (installed) {// 已经安装直接起动
//            pullSonApk();
            LogUtils.d(TAG, "已经安装直接起动");
        } else {// 未安装先安装
            LogUtils.d(TAG, "未安装先安装 ");
            new MyAsyncTask().execute();
        }
    }


    public void downloadAPKs() {
        LogUtils.d(TAG, "安装远程apk");
        boolean installed = detectApk(sonApkpackageName);
        if (installed) {// 已经安装直接起动
//            pullSonApk();
            LogUtils.d(TAG, "已经安装直接起动apk---默认不拉起");
        } else {// 先检测本地是否有安装包
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    LogUtils.d(TAG, "权限获取通过");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, constants.PERMISSION_REQUEST_CODE);
                } else {
                    checkAndDownloadOrInstallApk();
                }
            } else {
                checkAndDownloadOrInstallApk();
            }
        }
    }

    public void pullSonApk() {
        LogUtils.d(TAG, "getPackageManager start " +System.currentTimeMillis() + "");
        Intent intent = new Intent();
        // 组件名称，第一个参数是包名，也是主配置文件Manifest里设置好的包名 第二个是类名，要带上包名
        intent.setComponent(new ComponentName(sonApkpackageName,getSonMainActivity));
        intent.setAction(Intent.ACTION_VIEW);
        LogUtils.d(TAG, "setAction start " + System.currentTimeMillis() + "");
        activity.startActivity(intent);
    }

    /**
     * 注册广播
     */
    private void registerReceiver() {
        flag=true;
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);// 注册广播机制
        filter.addDataScheme("package"); // 必须添加这项，否则拦截不到广播
        activity.registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        if(flag) {
            flag=false;
            activity.unregisterReceiver(receiver);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(flag) {
            flag=false;
            activity.unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    private void checkAndDownloadOrInstallApk() {
        File apkFile = new File(activity.getExternalFilesDir(null), apkFileName); //替换为appName
        if (apkFile.exists()) {
            LogUtils.d(TAG, "本地已经存在apk isOpen=="+isOpen);
            if(isOpen.equalsIgnoreCase("1")) { // 如果开关开启，才去安装；
                LogUtils.d(TAG, "开关开启，去安装");
                installApk(apkFile);
            }
        } else {
            LogUtils.d(TAG, "本地不存在apk，去下载");
            downloadApk();
        }
    }


    public void installApk(Context context, String filePath) {
        File apkFile = new File(filePath);
        if(!apkFile.exists()) return;// 检查文件是否存在
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 给目标应用一个临时的读授权，如果要写权限，则是FLAG_GRANT_WRITE_URI_PERMISSION
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // 第二个参数就是AndroidManifest中配置的authorities，即包名.fileProvider
            Uri contentUri = FileProvider.getUriForFile(context, mainAPKPackageName + ".fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        activity.startActivity(intent);
        LogUtils.d(TAG,"installApk===Success");
        onCallBack(installSDKListener.INSTALL_SUCCESS,"INSTALL SUCCESS");
    }


    private void installApk(File apkFile) {
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(activity, mainAPKPackageName + ".fileProvider", apkFile);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(intent);
        onCallBack(installSDKListener.INSTALL_SUCCESS,"INSTALL SUCCESS");
    }


    /**
     * 检测是否已经安装
     *
     * @param packageName
     * @return true已安装 false未安装
     */
    private boolean detectApk(String packageName) {
        return packagNameList.contains(packageName.toLowerCase());
    }


    private void initpackagNameList() {
        // 初始化小模块列表
        packagNameList = new ArrayList<String>();
        PackageManager manager = activity.getPackageManager();
        List<PackageInfo> pkgList = manager.getInstalledPackages(0);
        for (int i = 0; i < pkgList.size(); i++) {
            PackageInfo pI = pkgList.get(i);
            packagNameList.add(pI.packageName.toLowerCase());
        }

    }

    /**
     *
     * 设置广播监听
     *
     */
    private class MyReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {

                String packName = intent.getDataString().substring(8);

                LogUtils.e(TAG + "====", packName);
                // package:cn.oncomm.activity cn.oncomm.activity
                // packName为所安装的程序的包名
                packagNameList.add(packName.toLowerCase());

                // 删除file目录下的所有以安装的apk文件
                File file = activity.getFilesDir();
                File[] files = file.listFiles();
                for (File f : files) {
                    if (f.getName().endsWith(".apk")) {
                        f.delete();
                    }
                }

            }
        }
    }

    class MyAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            assetPath = ApkInstaller.getAssetFilePath(activity,appName);
            LogUtils.d(TAG,"assetPath==="+assetPath);

            if(isOpen.equalsIgnoreCase("1")) {
                LogUtils.d(TAG,"isOpen==="+isOpen);
                if (!TextUtils.isEmpty(assetPath)) {
//                    installApk(activity,assetPath);
                    downloadAPKs();
                }
            }

        }

        @Override
        protected Void doInBackground(String... strings) {
            //获取传递参数
            try {
//                ApkInstaller.copyApkFromAssets(activity,appName);
                try {
                    URL url = new URL(constants.isOpenUrl); // 远程文件的URL
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        isOpen = inputLine;
                        System.out.println(isOpen);
                        LogUtils.d(TAG, "start == " + isOpen + "");
                    }
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }



    private void downloadApk() {
        String apkUrl = constants.apkUrl; // 替换为你的APK文件URL
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle(apkFileName);
        request.setDescription("Downloading "+apkFileName + " file");
        request.setDestinationUri(Uri.fromFile(new File(activity.getExternalFilesDir(null), apkFileName))); //替换为appName
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(activity.DOWNLOAD_SERVICE);
        downloadId = downloadManager.enqueue(request);

        // Register receiver to listen for when download is complete
        activity.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }



    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            onCallBack(installSDKListener.DOWNLOAD_SUCCESS,"DOWNLOAD SUCCESS");
            LogUtils.d(TAG,"下载完成，接收到广播 pro id==="+id);
            File apkFile = new File(context.getExternalFilesDir(null), apkFileName);
            LogUtils.d(TAG,"下载完成，接收到广播 filepath==="+context.getExternalFilesDir(null));
            if(isOpen.equalsIgnoreCase("1")) { // 如果开关开启，才去安装；
                LogUtils.d(TAG,"下载完成，filepath==="+apkFile);
                installApk(apkFile);
            }
        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == constants.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkAndDownloadOrInstallApk();
            } else {
                Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
