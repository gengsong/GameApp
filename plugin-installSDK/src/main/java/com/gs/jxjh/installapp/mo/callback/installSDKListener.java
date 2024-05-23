package com.gs.jxjh.installapp.mo.callback;

public interface installSDKListener {

    /**
     * 接口参数错误
     */
    int PARAM_ERROR = -1;
    /**
     * 初始化SDK成功回调
     */
    int INIT_SUCCESS = 1;
    /**
     * 初始化SDK失败回调
     */
    int INIT_FAIL = 2;

    void onCallBack(int code,String msg);
}
