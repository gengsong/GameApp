package com.gs.jxjh.installapp.mo.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ApkInstaller {

    public static String getAssetFilePath(Context context, String assetFileName) {
        File file = new File(context.getExternalFilesDir(null), assetFileName);
        return file.getAbsolutePath();
    }


    //读取本地assets文件夹中的zip，名重命名为apk
    public static String getAssetFilePathAndRename(Context context, String assetFileName, String newAssetFileName) {
        File oldFile = new File(context.getExternalFilesDir(null), assetFileName);
        File newFile = new File(context.getExternalFilesDir(null), newAssetFileName);
        boolean renamed = oldFile.renameTo(newFile);
        if(renamed) {
            return newFile.getAbsolutePath();
        } else {
            return oldFile.getAbsolutePath();
        }
    }


    public static String readAssetFile(Context context, String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(filename);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                stringBuilder.append(new String(buffer, 0, bytesRead));
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void installApkFromAssets(Context context, String apkFileName) {
        try {
            // 将 APK 文件从 assets 复制到应用程序的本地存储
            copyApkFromAssets(context, apkFileName);

            // 构建 APK 文件的本地存储路径
            File apkFile = new File(context.getExternalFilesDir(null), apkFileName);

            // 检查文件是否存在
            if (!apkFile.exists()) {
                throw new IOException("APK file not found");
            }

            // 创建安装 Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 检查是否有安装权限
            if (context.getPackageManager().canRequestPackageInstalls()) {
                // 启动安装
                context.startActivity(intent);
            } else {
                // 如果没有安装权限，提示用户授予权限
                // 这里您可以显示一个对话框或者启动系统设置页面
                // 以允许用户授予安装权限
                Log.e("ApkInstaller","您没有安装权限");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyApkFromAssets(Context context, String apkFileName) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getAssets().open(apkFileName);
            File outFile = new File(context.getExternalFilesDir(null), apkFileName);
            outputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }




}
