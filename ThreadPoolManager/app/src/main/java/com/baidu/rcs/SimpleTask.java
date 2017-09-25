package com.baidu.rcs;


import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SimpleTask implements Runnable {
    @Override
    public void run() {
        try {
            String filePath = Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator + "encrypt.txt";
            System.out.println(filePath);
            File file = new File(filePath);
            System.out.println(file.exists());
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "";
            StringBuffer sb = new StringBuffer();
            while (reader.readLine() != null) {
                sb.append(line);
                System.out.println(line);
            }
            reader.close();
            System.out.println("文件读取完毕");
            System.out.println(sb.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
