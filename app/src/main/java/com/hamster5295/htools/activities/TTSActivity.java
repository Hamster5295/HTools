package com.hamster5295.htools.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hamster5295.htools.GlobalData;
import com.hamster5295.htools.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TTSActivity extends AppCompatActivity {

    TextView log;
    Button b;
    EditText t, fn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsactivity);

        log = findViewById(R.id.text_tts_log);
        b = findViewById(R.id.btn_tts_go);
        t = findViewById(R.id.et_tts);
        fn = findViewById(R.id.et_tts_fileName);

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + GlobalData.APP_KEY + "&client_secret=" + GlobalData.SECRET_KEY)//访问连接
                        .get()
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.isSuccessful()) {
                    JSONObject j = JSON.parseObject(response.body().string());
                    GlobalData.accessToken = j.get("access_token").toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            b.setOnClickListener((v) -> new Thread(() -> {
                try {
                    getTTS();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.setText("错误: " + e.getMessage());
                }
            }).start());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                b.setOnClickListener((v) -> new Thread(() -> {
                    try {
                        getTTS();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start());
            } else {
                log.setText(R.string.no_permission);
            }
        } else {
            log.setText(R.string.no_permission);
        }
    }

    protected void getTTS() throws Exception {
        if (fn.getText().toString().equals("")) {
            log.setText("请输入文件名");
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(5000, TimeUnit.MILLISECONDS)
                .build();

        FormBody body = new FormBody.Builder()
                .add("tex", URLEncoder.encode(t.getText().toString(), "utf-8"))
                .add("tok", GlobalData.accessToken)
                .add("cuid", "qwq")
                .add("ctp", "1")
                .add("lan", "zh")
                .add("spd", "5")
                .add("per", "0")
                .add("aue", "3")
                .build();

        Request request = new Request.Builder()
                .url("http://tsn.baidu.com/text2audio")//访问连接
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            File f = new File(Environment.getExternalStorageDirectory(), "HTools/TTS/");
            f.mkdirs();
            FileOutputStream os = new FileOutputStream(f.getPath() + "/" + fn.getText().toString() + ".mp3");
            os.write(response.body().bytes());
            os.flush();
            os.close();
            log.setText("保存成功!\n目录: " + f.getPath() + "/" + fn.getText().toString() + ".mp3");
        } else {
            switch (response.code()) {
                case 501:
                    log.setText("超时");
                    break;

                case 502:
                    log.setText("参数错误");
                    break;

                case 503:
                    log.setText("Token无效");
                    break;

                case 504:
                    log.setText("文本编码有误");
                    break;

                default:
                    assert response.body() != null;
                    log.setText(response.body().string());
                    break;
            }
        }
    }
}