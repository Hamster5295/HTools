package com.hamster5295.htools.utils;

import androidx.recyclerview.widget.DiffUtil;

import com.hamster5295.htools.DownloadTask;

import java.util.List;

public class DownloadAdapterCallBack extends DiffUtil.Callback {
    private List<DownloadTask> old, newList;

    public DownloadAdapterCallBack(List<DownloadTask> oldT, List<DownloadTask> newT) {
        this.old = oldT;
        this.newList = newT;
    }

    @Override
    public int getOldListSize() {
        return old.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return old.get(oldItemPosition).getTaskID() == newList.get(newItemPosition).getTaskID();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return old.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
