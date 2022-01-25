package com.hamster5295.htools.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.hamster5295.htools.DownloadTask;
import com.hamster5295.htools.adapters.DownloadAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DownloadService extends Service {
    private Binder mBinder;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0x01) {
                mBinder.getAdapter().refresh();
            }
        }
    };

    private boolean flag = true;

    private ArrayList<DownloadTask> tasks = new ArrayList<>(), finishedTasks = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new Binder();

        //检查下载完成
        new Thread(() -> {
            Looper.prepare();
            while (flag) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //如果完成，就干掉他
                Iterator<DownloadTask> iterator = tasks.iterator();
                while (iterator.hasNext()) {
                    DownloadTask t = iterator.next();
                    if (t.getState() == DownloadTask.FINISHED) {
                        finishedTasks.add(t);
                        iterator.remove();
                        handler.sendEmptyMessage(0x01);
                    }
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class Binder extends android.os.Binder {
        private DownloadAdapter adapter;

        public DownloadAdapter getAdapter() {
            return adapter;
        }

        public void setAdapter(DownloadAdapter adapter) {
            this.adapter = adapter;
        }

        public List<DownloadTask> getTasks() {
            ArrayList<DownloadTask> l = new ArrayList<>();
            l.addAll(tasks);
            l.addAll(finishedTasks);
            return l;
        }

        public void startDownloadTask(DownloadTask task) {
            tasks.add(task);
            task.start();
        }

        public void pauseDownloadTask(int index) {
            tasks.get(index).pause();
        }

        public void continueDownloadTask(int index) {
            tasks.get(index).restart();
        }
    }
}