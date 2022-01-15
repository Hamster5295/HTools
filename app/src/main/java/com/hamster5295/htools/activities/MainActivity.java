package com.hamster5295.htools.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;

import com.hamster5295.htools.MenuAdapter;
import com.hamster5295.htools.MenuItem;
import com.hamster5295.htools.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<MenuItem> data = new ArrayList<MenuItem>() {
        };

        data.add(new MenuItem(getString(R.string.func_tts), ContextCompat.getDrawable(this, R.drawable.ic_func_tts), TTSActivity.class));
        data.add(new MenuItem(getString(R.string.func_draw), ContextCompat.getDrawable(this, R.drawable.ic_func_draw),WebActivity.class, "https://www.desmos.com/calculator?lang=zh-CN"));
        data.add(new MenuItem(getString(R.string.func_heading_poem), ContextCompat.getDrawable(this, R.drawable.ic_func_heading_poem),WebActivity.class, "https://cts.mofans.net/"));

        RecyclerView v = findViewById(R.id.menu);

        v.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        v.setAdapter(new MenuAdapter(data));
    }
}