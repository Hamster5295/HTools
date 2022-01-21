package com.hamster5295.htools.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hamster5295.htools.OutputUtil;
import com.hamster5295.htools.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class VideoGetActivity extends AppCompatActivity {

    private EditText input_url, input_p;
    private Button btn_resolve;
    private TextView text_log;
    private ProgressBar bar_download;

    private Thread thread_download;

    private long tempFileLength;
    private InputStream tempInput;

    private boolean isDownloading = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_get);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        input_url = findViewById(R.id.et_video_url);
        input_p = findViewById(R.id.et_video_p);
        btn_resolve = findViewById(R.id.btn_video_resolve);
        text_log = findViewById(R.id.text_video_log);
        bar_download = findViewById(R.id.pgbar_video_download);


        btn_resolve.setOnClickListener((v) -> {
            String in = input_url.getText().toString();

            if (!(in.contains("bv") || in.contains("BV"))) {
                log("输入错误: 请直接将BV号复制粘贴");
                return;
            }

            if (!isDownloading) {
                thread_download = new Thread(() -> {
                    isDownloading = true;

                    log("开始解析视频...");
                    OkHttpClient client = new OkHttpClient.Builder()
                            .build();

                    String requestUrl = "https://api.injahow.cn/bparse/?bv=" + input_url.getText().toString().substring(2) +
                            "&p=" + (input_p.getText().toString() == "" ? "1" : input_p.getText().toString()) +
                            "&format=mp4" +
                            "&otype=json";
                    Request request = new Request.Builder()
                            .url(requestUrl)
                            .get()
                            .build();

                    Call call = client.newCall(request);
                    try {
                        Response re = call.execute();
                        if (re.isSuccessful()) {
                            JSONObject videoInfo = JSONObject.parseObject(re.body().string());

                            if (videoInfo.getString("url") == null) {
                                log("错误: 找不到该视频");
                                return;
                            }
                            log("视频解析完毕, 正在下载...");

                            client = client.newBuilder()
                                    .build();

                            request = new Request.Builder()
                                    .url(videoInfo.getString("url"))
                                    .build();

                            call = client.newCall(request);

                            re = call.execute();
                            if (re.isSuccessful()) {
                                ResponseBody response = re.body();
                                tempInput = response.byteStream();
                                tempFileLength = response.contentLength();

                                if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_default_path", false)) {
                                    showProgressBar();
                                    new Thread(() -> {
                                        log(OutputUtil.save(tempInput, this.getExternalFilesDir(null) + "/Video", input_url.getText().toString() + ".mp4",
                                                (current) -> {
                                                    setProgressBarValue(Math.round(100 * current / tempFileLength));
                                                }));
                                        hideProgressBar();
                                    }).start();
                                } else {
                                    Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                    it.addCategory(Intent.CATEGORY_OPENABLE);
                                    it.setType("video/mp4");
                                    it.putExtra(Intent.EXTRA_TITLE, input_url.getText().toString() + ".mp4");
                                    startActivityForResult(it, 1);
                                }

                            } else {
                                log("获取失败: 第二次API请求出错, 无法下载视频");
                            }

                        } else {
                            log("获取失败: 第一次API请求出错, 无法获取视频Url");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        log("错误: " + e.getMessage());
                    } finally {
                        isDownloading = false;
                    }

                });
                thread_download.start();
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

            if (tempInput == null) {
                log(getString(R.string.err_file_null));
                return;
            }

            Uri u = data.getData();
            showProgressBar();
            new Thread(() -> {
                try {
                    log(OutputUtil.save(tempInput, getContentResolver().openOutputStream(u), u,
                            (current) -> setProgressBarValue(Math.round(current * 100 / tempFileLength))));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    log("错误: " + e.getMessage());
                } finally {
                    hideProgressBar();
                }
            }).start();

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread_download.interrupt();
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
                    .setMessage(R.string.help_video)
                    .setPositiveButton(getString(R.string.ok), (d, which) -> {
                        //啥都不干...
                    }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void log(String s) {
        runOnUiThread(() -> text_log.setText(s));
    }

    private void showProgressBar() {
        runOnUiThread(() -> bar_download.setVisibility(View.VISIBLE));
    }

    private void hideProgressBar() {
        runOnUiThread(() -> bar_download.setVisibility(View.GONE));
    }

    private void setProgressBarValue(int value) {

        runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                bar_download.setProgress(value, true);
            else
                bar_download.setProgress(value);
        });

    }
}