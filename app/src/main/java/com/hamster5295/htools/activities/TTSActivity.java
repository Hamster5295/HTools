package com.hamster5295.htools.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hamster5295.htools.GlobalData;
import com.hamster5295.htools.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
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
    Handler logHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsactivity);

        log = findViewById(R.id.text_tts_log);
        b = findViewById(R.id.btn_tts_go);
        t = findViewById(R.id.et_tts);
        fn = findViewById(R.id.et_tts_fileName);

        logHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                log.setText(msg.getData().getString("Log"));
            }
        };

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            b.setOnClickListener((v) ->
                    new Thread(() -> {
                        try {
                            getTTS(logHandler);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start()
            );
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                b.setOnClickListener((v) ->
                        new Thread(() -> {
                            try {
                                getTTS(logHandler);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start()
                );
            } else {
                log.setText(R.string.no_permission);
            }
        } else {
            log.setText(R.string.no_permission);
        }
    }

    protected void getTTS(Handler logger) throws Exception {
        Message msg = new Message();
        Bundle b = new Bundle();

        if (fn.getText().toString().equals("")) {
            b.putString("Log", "请输入文件名");
            msg.setData(b);
            logger.sendMessage(msg);
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
            File f = new File(TTSActivity.this.getExternalFilesDir(null), "/TTS");
            f.mkdirs();
            FileOutputStream os = new FileOutputStream(f.getAbsolutePath() + "/" + fn.getText().toString() + ".mp3");
            os.write(response.body().bytes());
            os.flush();
            os.close();
            log.setText("保存成功!\n目录: " + f.getPath() + "/" + fn.getText().toString() + ".mp3");
        } else {
            switch (response.code()) {
                case 501:
                    b.putString("Log", "网络超时");
                    msg.setData(b);
                    logger.sendMessage(msg);
                    break;

                case 502:
                    b.putString("Log", "参数错误");
                    msg.setData(b);
                    logger.sendMessage(msg);
                    break;

                case 503:
                    b.putString("Log", "Token无效");
                    msg.setData(b);
                    logger.sendMessage(msg);
                    break;

                case 504:
                    b.putString("Log", "文本编码有误");
                    msg.setData(b);
                    logger.sendMessage(msg);
                    break;

                default:
                    assert response.body() != null;
                    log.setText(response.body().string());
                    break;
            }
        }
    }
}