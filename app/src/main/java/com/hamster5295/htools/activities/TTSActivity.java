package com.hamster5295.htools.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hamster5295.htools.GlobalData;
import com.hamster5295.htools.OutputUtil;
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

    private TextView text_log;
    private Button btn_save;
    private EditText input_content, input_fileName;
    private Spinner spinner_soundFont;

    private byte[] tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsactivity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        text_log = findViewById(R.id.text_tts_log);
        btn_save = findViewById(R.id.btn_tts_go);
        input_content = findViewById(R.id.et_tts);
        input_fileName = findViewById(R.id.et_tts_fileName);
        spinner_soundFont = findViewById(R.id.spinner_tts);

        //获取百度API的Token
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

        //检查权限是否获取
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //添加Listener
            btn_save.setOnClickListener((v) ->
                    new Thread(() -> {
                        try {
                            getTTS();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start()
            );
        } else {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btn_save.setOnClickListener((v) ->
                        new Thread(() -> {
                            try {
                                getTTS();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start()
                );
            } else {
                text_log.setText(R.string.no_permission);
            }
        } else {
            text_log.setText(R.string.no_permission);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!(resultCode == RESULT_OK) || data == null) {
            log(getString(R.string.err_file_select_blocked));
            return;
        }


        if (requestCode == 1) {

            if (tempFile == null) {
                log(getString(R.string.err_file_null));
                return;
            }

            Uri u = data.getData();

            try {
                getContentResolver().openOutputStream(u).write(tempFile);
                log("保存成功! 目录: " + u.getPath());
            } catch (IOException e) {
                e.printStackTrace();
                log("错误: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void getTTS() throws Exception {
        if (input_fileName.getText().toString().equals("")) {
            log("请输入文件名");
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(5000, TimeUnit.MILLISECONDS)
                .build();

        String soundFont;

        switch (spinner_soundFont.getSelectedItemPosition()) {
            case 0:
                soundFont = "1";
                break;
            case 1:
                soundFont = "3";
                break;
            case 2:
                soundFont = "0";
                break;
            case 3:
                soundFont = "4";
                break;

            default:
                soundFont = "0";
                break;
        }

        FormBody body = new FormBody.Builder()
                .add("tex", URLEncoder.encode(input_content.getText().toString(), "utf-8"))
                .add("tok", GlobalData.accessToken)
                .add("cuid", "qwq")
                .add("ctp", "1")
                .add("lan", "zh")
                .add("spd", "5")
                .add("per", soundFont)
                .add("aue", "3")
                .build();

        Request request = new Request.Builder()
                .url("http://tsn.baidu.com/text2audio")//访问连接
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            tempFile = response.body().bytes();

            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_default_path", false)) {
                log(OutputUtil.save(tempFile, getExternalFilesDir(null) + "/TTS", input_fileName.getText().toString()));
            } else {
                Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                it.addCategory(Intent.CATEGORY_OPENABLE);
                it.setType("audio/mpeg");
                it.putExtra(Intent.EXTRA_TITLE, input_fileName.getText().toString() + ".mp3");
                startActivityForResult(it, 1);
            }
        } else {
            switch (response.code()) {
                case 501:
                    log("网络超时");
                    break;

                case 502:
                    log("参数错误");
                    break;

                case 503:
                    log("Token无效");
                    break;

                case 504:
                    log("文本编码有误");
                    break;

                default:
                    assert response.body() != null;
                    log(response.body().string());
                    break;
            }
        }
    }

    private void log(String str) {
        runOnUiThread(() -> text_log.setText(str));
    }
}