package com.hamster5295.htools.adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hamster5295.htools.DownloadTask;
import com.hamster5295.htools.R;
import com.hamster5295.htools.services.DownloadService;

import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadEntry> {

    private DownloadService.Binder binder;

    private List<DownloadTask> tasks;

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
        return binder.getTasks().size();
    }

    public void refresh() {
        tasks.clear();
        tasks.addAll(binder.getTasks());
        notifyDataSetChanged();
    }

    class DownloadEntry extends RecyclerView.ViewHolder {
        private DownloadTask task;
        private int index;

        private final TextView text_fileName;
        private final TextView text_progress;
        private final ProgressBar bar;

        private final View view;

        public DownloadEntry(@NonNull View v) {
            super(v);
            view = v;
            bar = v.findViewById(R.id.pgbar_download);
            text_fileName = v.findViewById(R.id.text_download_file_name);
            text_progress = v.findViewById(R.id.text_download_per_cent);
        }

        public void init(DownloadTask t, int index) {
            this.task = t;
            this.index = index;

            text_fileName.setText(task.getFileName());

            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(500);

                        //追踪下载进度
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            bar.setProgress(t.getProgress(), true);
                        } else {
                            bar.setProgress(t.getProgress());
                        }
                        text_progress.setText(t.getProgress() + "%");

                        if (task.getState() == DownloadTask.FINISHED) {
                            text_progress.setText("已完成");
                            return;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
