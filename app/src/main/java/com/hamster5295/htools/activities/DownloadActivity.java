package com.hamster5295.htools.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hamster5295.htools.R;
import com.hamster5295.htools.adapters.DownloadAdapter;
import com.hamster5295.htools.services.DownloadService;

public class DownloadActivity extends AppCompatActivity {

    private RecyclerView list;

    private DownloadService.Binder binder;

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (DownloadService.Binder) iBinder;
            list.setAdapter(new DownloadAdapter(binder));
            list.setLayoutManager(new LinearLayoutManager(DownloadActivity.this, RecyclerView.VERTICAL, false));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        list = findViewById(R.id.recycler_download);

        //绑定下载Service
        bindService(new Intent(this, DownloadService.class), conn, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}