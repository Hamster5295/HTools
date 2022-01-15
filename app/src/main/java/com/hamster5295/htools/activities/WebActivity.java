package com.hamster5295.htools.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hamster5295.htools.R;

public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        String url = getIntent().getStringExtra("ExtraStr");
        if (url == null) {
            Toast.makeText(this, "未传递Url!", Toast.LENGTH_LONG).show();
            return;
        }

        WebView w = findViewById(R.id.web_basic);
        ProgressBar pb = findViewById(R.id.pgbar_web);
        w.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        w.setWebChromeClient(new WebChromeClient() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                pb.setProgress(newProgress, true);
                if (newProgress == 100) {
                    Animation ani = AnimationUtils.loadAnimation(WebActivity.this, R.anim.web_bar_anim);
                    pb.startAnimation(ani);

                    final Handler h = new Handler(){
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            super.handleMessage(msg);
                            pb.setVisibility(View.GONE);
                        }
                    };

                    new Thread(()->{
                        try {
                            Thread.sleep(ani.getDuration());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        h.sendEmptyMessage(0x00);

                    }).start();
                }else{
                    pb.setVisibility(View.VISIBLE);
                }
            }
        });

        w.loadUrl(url);

        WebSettings settings = w.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

    }
}