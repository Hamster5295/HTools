package com.hamster5295.htools.activities;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.DocumentsContract;
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
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MusicGetActivity extends AppCompatActivity {

    private final int CODE_SONG = 1, CODE_PLAYLIST = 2;

    private EditText input_url;
    private Button btn_start;
    private TextView text_log;
    private ProgressBar bar_download;

    private final HashMap<String, ResponseBody> songs = new HashMap<>();

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
            String songId;
            songs.clear();

            if (!(in.contains("song?id=") || in.contains("/song/") || in.contains("playlist?id="))) {
                log("输入错误: 是不是复制错了");
                return;
            } else if (in.contains("playlist?id=")) {
                String playListID = in.split("playlist\\?id=")[1].split("&")[0];
                bar_download.setVisibility(View.VISIBLE);

                log("正在解析歌单信息...");

                new Thread(() -> {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .build();

                    Request request = new Request.Builder()
                            .url("https://api.injahow.cn/meting/?type=playlist&id=" + playListID)
                            .get()
                            .build();

                    Call call = client.newCall(request);
                    try {
                        Response re = call.execute();
                        if (re.isSuccessful()) {
                            JSONArray songList = JSONArray.parseArray(re.body().string());

                            for (int i = 0; i < songList.size(); i++) {
                                try {
                                    log("正在解析第" + (i + 1) + "首歌 ");
                                    JSONObject song = songList.getJSONObject(i);

                                    request = new Request.Builder()
                                            .url(song.getString("url"))
                                            .addHeader("Connection", "close")    //防止出现莫名其妙的Timeout
                                            .build();

                                    call = client.newCall(request);

                                    re = call.execute();
                                    if (re.isSuccessful()) {
                                        songs.put(song.getString("artist") + " - " + song.getString("name") + ".mp3", re.body());
                                    } else {
                                        log("获取失败: 第" + (i + 1) + "首歌曲的请求出错");
                                    }
                                } catch (Exception e) {
                                    log("错误: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_default_path", false)) {
                                for (Map.Entry<String, ResponseBody> item :
                                        songs.entrySet()) {
                                    binder.startDownloadTask(new DownloadTask(item.getValue().byteStream(), new FileOutputStream(this.getExternalFilesDir(null) + "/Music/" + item.getKey()),
                                            item.getValue().contentLength(), item.getKey()));
                                }
                                log(getString(R.string.add_to_download_queue));
                            } else {
                                Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                startActivityForResult(it, CODE_PLAYLIST);
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
            } else {
                //单曲
                songs.clear();

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
                                String fileName = song.getString("artist") + " - " + song.getString("name") + ".mp3";
                                songs.put(fileName, re.body());

                                if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_default_path", false)) {
                                    for (Map.Entry<String, ResponseBody> item :
                                            songs.entrySet()) {
                                        binder.startDownloadTask(new DownloadTask(item.getValue().byteStream(), new FileOutputStream(this.getExternalFilesDir(null) + "/Music/" + item.getKey()),
                                                item.getValue().contentLength(), item.getKey()));
                                    }
                                    log(getString(R.string.add_to_download_queue));
                                    log(getString(R.string.add_to_download_queue));
                                } else {
                                    Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                    it.addCategory(Intent.CATEGORY_OPENABLE);
                                    it.setType("audio/mpeg");
                                    it.putExtra(Intent.EXTRA_TITLE, fileName);
                                    startActivityForResult(it, CODE_SONG);
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

        Uri u = null;
        if (requestCode == CODE_SONG)
            u = data.getData();
        else if (requestCode == CODE_PLAYLIST)
            u = DocumentsContract.buildDocumentUriUsingTree(data.getData(), DocumentsContract.getTreeDocumentId(data.getData()));
        ContentResolver resolver = getContentResolver();

        boolean isAlright = true;
        for (Map.Entry<String, ResponseBody> item :
                songs.entrySet()) {
            try {
                if (requestCode == CODE_SONG)
                    binder.startDownloadTask(new DownloadTask(item.getValue().byteStream(), resolver.openOutputStream(u),
                            item.getValue().contentLength(), item.getKey()));
                else if (requestCode == CODE_PLAYLIST) {
                    binder.startDownloadTask(new DownloadTask(item.getValue().byteStream(), resolver.openOutputStream(DocumentsContract.createDocument(resolver, u, "audio/mpeg", item.getKey())),
                            item.getValue().contentLength(), item.getKey()));
                }
            } catch (IOException e) {
                isAlright = false;
                e.printStackTrace();
                log("错误: " + e.getMessage());
            }
        }

        if (isAlright) log(getString(R.string.add_to_download_queue));

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