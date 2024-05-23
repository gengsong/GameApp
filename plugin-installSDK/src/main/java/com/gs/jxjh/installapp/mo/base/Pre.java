package com.gs.jxjh.installapp.mo.base;

import android.util.Log;


import com.gs.jxjh.installapp.mo.callback.installSDKListener;

import java.util.Map;

public class Pre extends Ref {
    public static Map<String, Object> data;

    protected installSDKListener listener;
    public synchronized void init(android.app.Activity activity,Map<String, Object> map){
        super.init(activity);
        data = map;
        if (data == null || data.get("appKey") == null){
            onCallBack(installSDKListener.PARAM_ERROR,"参数不合法，请检查appKey是否设置！");
            return;
        }

        init();

    }

    public void onRestart() {
    }

    public void onStart() {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onStop() {
    }

    public void onDestroy() {
    }


    public void init() {
    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
    }

    public void onCallBack(final int code,final String msg) {
        if (listener != null)
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run(){
                    listener.onCallBack(code,msg);
                    Log.e("callback","code:" + code + ";msg:" + msg);
                }
            });
    }

    public void setListener(installSDKListener listener) {
        this.listener = listener;
    }

}
