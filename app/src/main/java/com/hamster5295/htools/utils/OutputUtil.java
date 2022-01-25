package com.hamster5295.htools.utils;

import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OutputUtil {
    public interface CallBack {
        void onSaveProgress(long current);
    }

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

    public static String save(InputStream in, OutputStream out, Uri u, CallBack call) {
        int len;
        byte[] tempFile = new byte[512];

        long current = 0;

        try {
            while ((len = in.read(tempFile)) != -1) {
                out.write(tempFile, 0, len);
                current += len;
                call.onSaveProgress(current);
            }
            return "保存成功! 路径: " + u.getPath();
        } catch (IOException e) {
            e.printStackTrace();
            return "错误: " + e.getMessage();
        }
    }

    public static String save(InputStream in, String path, String fileName, CallBack call) {
        int len;
        long current = 0;
        byte[] temp = new byte[512];

        try {
            File f = new File(path);
            f.mkdirs();
            FileOutputStream out = new FileOutputStream(f.getAbsolutePath() + "/" + fileName);

            while ((len = in.read(temp)) != -1) {
                out.write(len);
                current += len;
                call.onSaveProgress(current);
            }
            return "保存成功! 路径: " + f.getAbsolutePath() + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "错误: " + e.getMessage();
        }
    }
}
