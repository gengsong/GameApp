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
    /**
     * 安装成功回调
     */
    int INSTALL_SUCCESS = 3;
    /**
     * 安装失败回调
     */
    int INSTALL_FAIL = 4;

    /**
     * 下载成功回调
     */
    int DOWNLOAD_SUCCESS = 5;
    /**
     * 下载失败回调
     */
    int DOWNLOAD_FAIL = 6;



    void onCallBack(int code,String msg);
}
