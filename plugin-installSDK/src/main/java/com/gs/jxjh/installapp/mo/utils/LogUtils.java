package com.gs.jxjh.installapp.mo.utils;

import android.util.Log;

import com.gs.jxjh.installapp.mo.constant.constants;


public class LogUtils {

    public static void d(String tag, String message) {
        if (constants.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (constants.DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (constants.DEBUG) {
            Log.i(tag, message);
        }
    }

    // 为其他日志级别（e、i、w等）提供类似的方法
}