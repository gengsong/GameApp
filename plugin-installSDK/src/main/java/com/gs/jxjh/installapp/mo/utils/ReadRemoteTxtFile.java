package com.gs.jxjh.installapp.mo.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ReadRemoteTxtFile {

    public static void main(String[] args) {
        try {
            URL url = new URL("https://lsq-1304172184.cos.ap-beijing.myqcloud.com/config/game1.txt"); // 远程文件的URL
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}