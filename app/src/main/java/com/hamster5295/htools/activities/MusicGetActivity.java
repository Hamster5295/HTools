package com.hamster5295.htools.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hamster5295.htools.DownloadTask;
import com.hamster5295.htools.R;
import com.hamster5295.htools.services.DownloadService;

import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MusicGetActivity extends AppCompatActivity {

    private EditText input_url;
    private Button btn_start;
    private TextView text_log;
    private ProgressBar bar_download;

    private ResponseBody response;
    private String fileName;

    private DownloadService.Binder binder;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (DownloadService.Binder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_get);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        input_url = findViewById(R.id.et_music_url);
        btn_start = findViewById(R.id.btn_music_start);
        text_log = findViewById(R.id.text_music_log);
        bar_download = findViewById(R.id.pgbar_music_download);

        bindService(new Intent(this, DownloadService.class), conn, BIND_AUTO_CREATE);

        btn_start.setOnClickListener((v) -> {
            String in = input_url.getText().toString();
            if (!(in.contains("song?id=") || in.contains("/song/"))) {
                log("输入错误: 是不是复制错了");
                return;
            } else {
                String songId;
                if (in.contains("song?id="))
                    songId = in.split("song\\?id=")[1].split("&")[0];
                else if (in.contains("/song/"))
                    songId = in.split("/song/")[1].split("/")[0];
                else {
                    log("输入错误: 是不是复制错了");
                    return;
                }

                bar_download.setVisibility(View.VISIBLE);

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
                                response = re.body();
                                fileName = song.getString("artist") + " - " + song.getString("name") + ".mp3";

                                if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_default_path", false)) {
                                    binder.startDownloadTask(new DownloadTask(response.byteStream(), new FileOutputStream(this.getExternalFilesDir(null) + "/Music/" + fileName),
                                            response.contentLength(), fileName));
                                    log(getString(R.string.add_to_download_queue));
                                } else {
                                    Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                    it.addCategory(Intent.CATEGORY_OPENABLE);
                                    it.setType("audio/mpeg");
                                    it.putExtra(Intent.EXTRA_TITLE, fileName);
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
                        log("错误: " + e.getMessage());
                    } finally {
                        hideProgressBar();
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

            if (response == null) {
                log(getString(R.string.err_file_null));
                return;
            }

            Uri u = data.getData();

            try {
                binder.startDownloadTask(new DownloadTask(response.byteStream(), getContentResolver().openOutputStream(u), response.contentLength(), fileName));
            } catch (IOException e) {
                e.printStackTrace();
                log("错误: " + e.getMessage());
            }

            log(getString(R.string.add_to_download_queue));

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    private void log(String s) {
        runOnUiThread(() -> text_log.setText(s));
    }

    private void hideProgressBar() {
        runOnUiThread(() -> bar_download.setVisibility(View.GONE));
    }
}