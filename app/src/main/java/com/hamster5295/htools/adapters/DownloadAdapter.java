package com.hamster5295.htools.adapters;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.hamster5295.htools.DownloadTask;
import com.hamster5295.htools.R;
import com.hamster5295.htools.services.DownloadService;
import com.hamster5295.htools.utils.DownloadAdapterCallBack;

import java.util.ArrayList;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadEntry> {

    private final DownloadService.Binder binder;

    private final List<DownloadTask> tasks;

    public DownloadAdapter(DownloadService.Binder binder) {
        this.binder = binder;
        binder.setAdapter(this);
        tasks = binder.getTasks();
    }

    @NonNull
    @Override
    public DownloadEntry onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_download, parent, false);
        return new DownloadAdapter.DownloadEntry(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadEntry holder, int position) {
        holder.init(tasks.get(position), position);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }


    public void refresh() {
        List<DownloadTask> old = new ArrayList<>();
        old.addAll(tasks);

        tasks.clear();
        tasks.addAll(binder.getTasks());

        DiffUtil.calculateDiff(new DownloadAdapterCallBack(old, tasks)).dispatchUpdatesTo(this);
    }


    class DownloadEntry extends RecyclerView.ViewHolder {
        private boolean flag = false;

        private DownloadTask task;
        private int index;

        private final TextView text_fileName;
        private final TextView text_progress;
        private final ProgressBar bar;
        private final ImageButton btn_pause, btn_cancel;

        private final View vi;

        private final Handler h = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                btn_pause.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
            }
        };

        public DownloadEntry(@NonNull View v) {
            super(v);
            vi = v;
            bar = v.findViewById(R.id.pgbar_download);
            text_fileName = v.findViewById(R.id.text_download_file_name);
            text_progress = v.findViewById(R.id.text_download_per_cent);
            btn_pause = v.findViewById(R.id.btn_download_pause);
            btn_cancel = v.findViewById(R.id.btn_download_cancel);

        }

        public void init(DownloadTask t, int index) {
            this.task = t;
            this.index = index;

            text_fileName.setText(task.getFileName());

            btn_pause.setOnClickListener((view) -> {
                if (task.getState() == DownloadTask.RUNNING) {
                    task.pause();
                    btn_pause.setImageResource(R.drawable.ic_start);
                } else if (task.getState() == DownloadTask.PAUSED) {
                    task.restart();
                    btn_pause.setImageResource(R.drawable.ic_pause);
                }
            });

            btn_cancel.setOnClickListener((view) -> new AlertDialog.Builder(view.getContext())
                    .setTitle("操作确认")
                    .setMessage("此操作不可撤回, 确定取消此下载进程?")
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        task.cancel();
                        btn_pause.setVisibility(View.GONE);
                        btn_cancel.setVisibility(View.GONE);
                    }).setNegativeButton("返回", (d, i) -> {
                    }).show());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                bar.setProgress(t.getProgress(), true);
            } else {
                bar.setProgress(t.getProgress());
            }

            //初始按钮形态
            if (task.getState() == DownloadTask.RUNNING) {
                btn_pause.setImageResource(R.drawable.ic_pause);
            } else if (task.getState() == DownloadTask.PAUSED) {
                btn_pause.setImageResource(R.drawable.ic_start);
            }

            if (flag) return;
            new Thread(() -> {
                flag = true;
                while (flag) {
                    try {
                        if (task.getState() == DownloadTask.PAUSED) {
                            text_progress.setText("已暂停");
                            Thread.sleep(500);
                            continue;
                        }

                        //追踪下载进度
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            bar.setProgress(t.getProgress(), true);
                        } else {
                            bar.setProgress(t.getProgress());
                        }
                        text_progress.setText(t.getProgress() + "%");

                        if (task.getState() == DownloadTask.FINISHED) {
                            text_progress.setText("已完成");
                            h.sendEmptyMessage(0x00);
                            break;
                        }

                        if (task.getState() == DownloadTask.ERROR) {
                            text_progress.setText("错误: " + task.getException().getMessage());
                            bar.setProgress(100);
                            h.sendEmptyMessage(0x00);
                            break;
                        }

                        if (task.getState() == DownloadTask.CANCELLED) {
                            text_progress.setText("已取消");
                            h.sendEmptyMessage(0x00);
                            break;
                        }

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
