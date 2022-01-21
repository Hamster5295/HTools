package com.hamster5295.htools;

import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class OutputUtil {
    public static String save(byte[] content, String path, String fileName) {
        try {
            File f = new File(path);
            f.mkdirs();
            FileOutputStream out = new FileOutputStream(f.getAbsolutePath() + "/" + fileName);
            out.write(content);
            out.flush();
            out.close();
            return "保存成功! 路径: " + f.getAbsolutePath() + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "错误: " + e.getMessage();
        }
    }
}
