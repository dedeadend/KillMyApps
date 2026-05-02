package dedeadend.killmyapps.ui.excluded;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.R;
import dedeadend.killmyapps.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class ExcludedRecyclerViewAdapter extends RecyclerView.Adapter<ExcludedRecyclerViewAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    List<AppInfo> appList, backupAppList;
    List<AppInfo> excludedList, backupExcludedList;
    List<AppInfo> allList;
    onIconClickListener listener;

    public ExcludedRecyclerViewAdapter(List<AppInfo> appList, List<AppInfo> excludedList, onIconClickListener listener) {
        this.appList = appList;
        this.excludedList = excludedList;
        backupAppList = new ArrayList<>(appList);
        backupExcludedList = new ArrayList<>(excludedList);
        allList = new ArrayList<>();
        allList.addAll(excludedList);
        allList.addAll(appList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExcludedRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(dedeadend.killmyapps.R.layout.recycler_app, parent, false);
        return new ExcludedRecyclerViewAdapter.ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ExcludedRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.name.setText(allList.get(position).getName());
        holder.pkgName.setText(allList.get(position).getPkgName());
        holder.icon.setImageDrawable(allList.get(position).getIcon());
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(App.context, R.anim.scale_in));
        if (excludedList.contains(allList.get(position))) {
            holder.add.setImageDrawable(App.context.getDrawable(R.drawable.ic_remove));
            holder.add.setRotation(0);
        } else {
            holder.add.setImageDrawable(App.context.getDrawable(R.drawable.ic_add));
            holder.add.setRotation(45);
        }
        holder.add.setTag(allList.get(position).getPkgName());
        holder.parent.setTag(allList.get(position).getPkgName());
        holder.add.setOnClickListener(this);
        if (App.settings.getBoolean(App.CLICK_TO_APP_INFO, true))
            holder.parent.setOnClickListener(this);
        if (App.settings.getBoolean(App.LONG_CLICK_TO_COPY, true))
            holder.parent.setOnLongClickListener(this);
        if (App.settings.getBoolean(App.SHOW_PKGNAME, true))
            holder.pkgName.setVisibility(View.VISIBLE);
        else
            holder.pkgName.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return allList.size();
    }

    @Override
    public void onClick(View v) {
        ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.9f, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.9f, 1)
        ).setDuration(400L).start();
        String pkgName = (String) v.getTag();
        if (v == v.findViewById(R.id.kill_icon)) {
            for (int i = 0; i < allList.size(); i++) {
                if (allList.get(i).getPkgName().equals(pkgName)) {
                    if (excludedList.contains(allList.get(i))) {
                        listener.onRemoveIconClick(allList.get(i));
                        appList.add(excludedList.get(i));
                        backupAppList.add(excludedList.get(i));
                        backupExcludedList.remove(excludedList.get(i));
                        excludedList.remove(i);
                        allList.remove(i);
                        notifyItemRemoved(i);
                        allList.add(appList.get(appList.size() - 1));
                        notifyItemInserted(allList.size() - 1);
                    } else {
                        listener.onAddIconClick(allList.get(i));
                        backupAppList.remove(appList.get(i - excludedList.size()));
                        appList.remove(i - excludedList.size());
                        excludedList.add(allList.get(i));
                        backupExcludedList.add(allList.get(i));
                        allList.remove(i);
                        notifyItemRemoved(i);
                        allList.add(excludedList.size() - 1, excludedList.get(excludedList.size() - 1));
                        notifyItemInserted(excludedList.size() - 1);
                    }
                    break;
                }
            }
        }
        else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + pkgName.trim()));
            App.context.startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        ObjectAnimator.ofPropertyValuesHolder(v,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.9f, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.9f, 1)
        ).setDuration(400L).start();
        String pkgName = (String) v.getTag();
        listener.onAppInfo(pkgName);
        return true;
    }

    public void filterList(String filter) {
        excludedList.clear();
        appList.clear();
        allList.clear();
        excludedList.addAll(backupExcludedList);
        appList.addAll(backupAppList);
        allList.addAll(excludedList);
        allList.addAll(appList);
        for (int i = 0; i < allList.size(); i++) {
            if (!allList.get(i).getName().toLowerCase().contains(filter.toLowerCase())) {
                excludedList.remove(allList.get(i));
                appList.remove(allList.get(i));
                allList.remove(i);
                i--;
            }
        }
        notifyDataSetChanged();
    }

    public interface onIconClickListener {
        void onAddIconClick(AppInfo appInfo);

        void onRemoveIconClick(AppInfo appInfo);

        void onAppInfo(String pkgName);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon, add;
        TextView name, pkgName;
        View parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView;
            icon = itemView.findViewById(R.id.app_icon);
            add = itemView.findViewById(R.id.kill_icon);
            name = itemView.findViewById(R.id.app_name);
            pkgName = itemView.findViewById(R.id.package_name);
        }
    }
}
