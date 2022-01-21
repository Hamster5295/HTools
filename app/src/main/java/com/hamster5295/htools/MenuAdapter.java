package com.hamster5295.htools;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuEntry> {

    private List<HItem> datas;

    public MenuAdapter(List<HItem> d) {
        datas = d;
    }

    @NonNull
    @Override
    public MenuEntry onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_menu, parent, false);
        return new MenuEntry(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuEntry holder, int position) {
        holder.Init(datas.get(position));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class MenuEntry extends RecyclerView.ViewHolder {

        private View v;
        private Button btn;

        public MenuEntry(@NonNull View itemView) {
            super(itemView);

            v = itemView;
            btn = itemView.findViewById(R.id.btn_menuItem);
        }

        public void Init(HItem i) {
            i.icon.setBounds(0, 0, i.icon.getMinimumWidth(), i.icon.getMinimumHeight());
            btn.setCompoundDrawables(i.icon, null, null, null);
            btn.setText(i.text);
            btn.setOnClickListener((v) -> {
                Intent it = new Intent(v.getContext(), i.cls);
                if (i.isContainExtra())
                    it.putExtra("ExtraStr", i.extraString);
                v.getContext().startActivity(it);
            });
        }
    }
}

