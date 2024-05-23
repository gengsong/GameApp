package com.gengsong.jxjh.installapp.gameapp;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gs.jxjh.installapp.mo.InstallSDK;
import com.gs.jxjh.installapp.mo.callback.installSDKListener;

import android.view.View;
import java.util.HashMap;
import java.util.Map;

/**
 * 此Demo用演示集成安装SDK，具体步骤如下：
 * 1、MainActivity 中初始化完成后调用installAPKs方法
 * 2、res-xml 实现filepaths.xml
 * 3、AndroidManifest.xml中定义 provider,并设置权限uses-permission
 * 4、assets文件夹放置要安装的apk
 */
public class MainActivity extends Activity implements installSDKListener {
    private InstallSDK installSDK = InstallSDK.getInstance();
    private FloatingActionButton actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setListener();
        initViews();
        initInstallSDK();
    }

    private void initViews() {
        actionButton = this.findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                installSDK.installAPKs();
//                installSDK.downloadAPKs();
            }
        });
    }

    private void initInstallSDK() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("appKey","df8979e126c04e6eabfb9fe65a928d67");
//        map.put("appName","WS_2606_3.apk"); //assets 中安装包的名字
//        map.put("packageName","com.yowhats.stab");//apk 包名
//        map.put("mainActivity","com.yowhats.stab.Main");//主activity
        //下面是远程下载apk的包体信息
        map.put("appName","Bricks_new.apk"); //assets 中安装包的名字
        map.put("packageName","com.Brick.Blast2024");//apk 包名
        map.put("mainActivity","com.Brick.Blast2024.MainActivity");//主activity
        map.put("downloadUrl","");//远程地址
        installSDK.init(this,map);

    }


    private void setListener() {
        installSDK.setListener(this);
    }


    @Override
    public void onCallBack(int code,String msg) {
        switch (code) {
            case INIT_SUCCESS:// 初始化成功
//                installSDK.installAPKs();
//                installSDK.downloadAPKs();
                break;
            case INIT_FAIL:// 初始化失败
                showMessage(msg);
                break;
            default:
                break;
        }
    }


    public void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new android.app.AlertDialog.Builder(MainActivity.this).setTitle("回调参数").setMessage(message).setCancelable(true)
                        .setPositiveButton("确定",null).show();
            }
        });

    }
}