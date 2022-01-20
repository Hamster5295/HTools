package com.hamster5295.htools.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hamster5295.htools.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MusicGetActivity extends AppCompatActivity {

    private EditText input_url;
    private Button btn_start;
    private TextView text_log;

    private Handler handler_log;

    private byte[] tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_get);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        handler_log = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                text_log.setText(msg.getData().getString("Log"));
            }
        };

        input_url = findViewById(R.id.et_music_url);
        btn_start = findViewById(R.id.btn_music_start);
        text_log = findViewById(R.id.text_music_log);

        btn_start.setOnClickListener((v) -> {
            String in = input_url.getText().toString();
            if (!in.contains("song?id=")) {
                log("输入错误: 是不是复制错了");
                return;
            } else {

                String songId = in.split("song\\?id=")[1].split("&")[0];

                new Thread(() -> {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .build();

                    Request request = new Request.Builder()
                            .url("https://api.injahow.cn/meting/?type=song&id=" + songId)
                            .get()
                            .build();

                    Call call = client.newCall(request);
                    try {
                        Response re = call.execute();
                        if (re.isSuccessful()) {

                            JSONObject song = JSONArray.parseArray(re.body().string()).getJSONObject(0);

                            request = new Request.Builder()
                                    .url(song.getString("url"))
                                    .build();

                            call = client.newCall(request);

                            re = call.execute();
                            if (re.isSuccessful()) {

                                tempFile = re.body().bytes();

                                if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_default_path", false)) {
                                    File f = new File(this.getExternalFilesDir(null), "/Music");
                                    f.mkdirs();
                                    FileOutputStream os = new FileOutputStream(f.getAbsolutePath() + "/" +
                                            song.getString("artist") + " - " + song.getString("name") + ".mp3");
                                    os.write(tempFile);
                                    os.flush();
                                    os.close();
                                    log("保存成功!\n目录: " + f.getPath() + "/" +
                                            song.getString("artist") + " - " + song.getString("name") + ".mp3");
                                } else {
                                    Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                    it.addCategory(Intent.CATEGORY_OPENABLE);
                                    it.setType("audio/mpeg");
                                    it.putExtra(Intent.EXTRA_TITLE, song.getString("artist") + " - " + song.getString("name") + ".mp3");
                                    startActivityForResult(it, 1);
                                }

                            } else {
                                log("获取失败: 第二次API请求出错, 无法下载音乐");
                            }

                        } else {
                            log("获取失败: 第一次API请求出错, 无法获取音乐Url");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }).start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) {
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

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_music, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.opt_music_help) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.help))
                    .setMessage(R.string.help_music)
                    .setPositiveButton(getString(R.string.ok), (d, which) -> {
                        //啥都不干...
                    }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void log(String s) {
        Bundle b = new Bundle();
        Message msg = new Message();
        b.putString("Log", s);
        msg.setData(b);
        handler_log.sendMessage(msg);
    }
}