package com.hamster5295.htools;

import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

public class ProgressResponseBody extends ResponseBody {

    public interface ProgressListener {
        void onProgress(long current, long total, boolean done);
    }

    private ResponseBody response;
    private ProgressListener listener;
    private BufferedSource source;

    public ProgressResponseBody(ResponseBody response, ProgressListener listener) {
        this.response = response;
        this.listener = listener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return response.contentType();
    }

    @Override
    public long contentLength() {
        return response.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (source == null) {
            source = Okio.buffer(new ForwardingSource(response.source()) {
                long current;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long read = super.read(sink, byteCount);
                    current += read == -1 ? 0 : read;
                    listener.onProgress(current, response.contentLength(), read == -1);
                    return read;
                }
            });
        }

        return source;
    }
}
