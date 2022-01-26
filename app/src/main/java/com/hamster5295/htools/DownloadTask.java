package com.hamster5295.htools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class DownloadTask {
    public static int currentID = 0;

    public static final int
            READY = 0,
            RUNNING = 1,
            PAUSED = 2,
            CANCELLED = 3,
            FINISHED = 4,
            ERROR = -1;

    public int getTaskID() {
        return taskID;
    }

    private int taskID;
    private InputStream in;
    private OutputStream out;
    private String fileName;

    private int state;

    private long total = 0, current = 0;

    private Exception exception;

    private final Thread downloadThread = new Thread(() -> {
        byte[] temp = new byte[512];
        int len = 0;

        try {
            while ((len = in.read(temp)) != -1) {
                while (state == PAUSED) {
                    Thread.sleep(1000);
                }

                if (state == ERROR || state == CANCELLED) {
                    in.close();
                    out.close();
                    return;
                } else {
                    current += len;
                    out.write(temp);
                }
            }
            state = FINISHED;
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
            state = ERROR;
        }
    });

    public DownloadTask(InputStream in, OutputStream out, long contentLength, String fileName) throws IOException {
        this.in = in;
        this.out = out;
        this.fileName = fileName;
        total = contentLength;
        state = READY;

        taskID = currentID;
        currentID++;
    }

    public void start() {
        if (state != READY) return;
        state = RUNNING;

        downloadThread.start();
    }

    public void pause() {
        state = PAUSED;
    }

    public void restart() {
        if (state != PAUSED) return;
        state = RUNNING;
    }

    public void cancel() {
        state = CANCELLED;
    }

    private void error(Exception e) {
        exception = e;
        e.printStackTrace();
        state = ERROR;
    }

    public int getState() {
        return state;
    }

    public int getProgress() {
        switch (state) {
            case FINISHED:
            case CANCELLED:
            case ERROR:
                return 100;

            case RUNNING:
            case PAUSED:
                if (total == 0) return 0;
                return Math.round(100 * current / total);

            default:
                return 0;
        }
    }

    public String getFileName() {
        return fileName;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadTask that = (DownloadTask) o;
        return taskID == that.taskID && state == that.state && total == that.total && current == that.current && Objects.equals(in, that.in) && Objects.equals(out, that.out) && Objects.equals(fileName, that.fileName) && Objects.equals(exception, that.exception) && Objects.equals(downloadThread, that.downloadThread);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskID, in, out, fileName, state, total, current, exception, downloadThread);
    }
}
