package com.hamster5295.htools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadTask {
    public static final int
            READY = 0,
            RUNNING = 1,
            PAUSED = 2,
            CANCELLED = 3,
            FINISHED = 4,
            ERROR = -1;

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
                if (state == ERROR) {
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
    }

    public void start() {
        if (state != READY) return;
        state = RUNNING;

        downloadThread.start();
    }

    public void pause() {
        state = PAUSED;

        try {
            downloadThread.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
            exception = e;
            state = ERROR;
        }
    }

    public void restart() {
        if (state != PAUSED) return;

        downloadThread.notify();
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
                return 100;

            case RUNNING:
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
}
