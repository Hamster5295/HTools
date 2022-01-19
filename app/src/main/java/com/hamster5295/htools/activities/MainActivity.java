package com.hamster5295.htools.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

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
        data.add(new MenuItem(getString(R.string.func_draw), ContextCompat.getDrawable(this, R.drawable.ic_func_draw), WebActivity.class, "https://www.desmos.com/calculator?lang=zh-CN"));
        data.add(new MenuItem(getString(R.string.func_heading_poem), ContextCompat.getDrawable(this, R.drawable.ic_func_heading_poem), WebActivity.class, "https://cts.mofans.net/"));

        RecyclerView v = findViewById(R.id.menu);

        v.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        v.setAdapter(new MenuAdapter(data));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.opt_main_settings) {
            Intent it = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(it);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}