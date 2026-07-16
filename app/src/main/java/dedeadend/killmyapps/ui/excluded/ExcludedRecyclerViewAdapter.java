package dedeadend.killmyapps.ui.excluded;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.R;
import dedeadend.killmyapps.model.AppInfo;

public class ExcludedRecyclerViewAdapter extends RecyclerView.Adapter<ExcludedRecyclerViewAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private List<AppInfo> appList, backupAppList;
    private List<AppInfo> excludedList, backupExcludedList;
    private List<AppInfo> allList;
    private Map<String, AppInfo> allListMap;
    private Set<String> excludedListHash;
    private onIconClickListener listener;
    private boolean isAppInfoEnable, isLongClickEnable, isShowPackageNameEnable, isScrollAnimationEnable;

    private Drawable addIcon, removeIcon;

    public ExcludedRecyclerViewAdapter(List<AppInfo> appList, List<AppInfo> excludedList, onIconClickListener listener) {
        this.appList = appList;
        this.excludedList = excludedList;
        backupAppList = new ArrayList<>(appList);
        backupExcludedList = new ArrayList<>(excludedList);
        allList = new ArrayList<>();
        allList.addAll(excludedList);
        allList.addAll(appList);
        allListMap = new HashMap<>();
        for (AppInfo appInfo : allList)
            allListMap.put(appInfo.getPkgName(), appInfo);
        excludedListHash = new HashSet<>();
        for (AppInfo appInfo : excludedList)
            excludedListHash.add(appInfo.getPkgName());
        this.listener = listener;
        isAppInfoEnable = App.settings.getBoolean(App.CLICK_TO_APP_INFO, true);
        isLongClickEnable = App.settings.getBoolean(App.LONG_CLICK_TO_MENU, true);
        isShowPackageNameEnable = App.settings.getBoolean(App.SHOW_PKGNAME, true);
        isScrollAnimationEnable = App.settings.getBoolean(App.SHOW_SCROLL_ANIMATION, true);
        addIcon = App.context.getDrawable(R.drawable.ic_add);
        removeIcon = App.context.getDrawable(R.drawable.ic_remove);
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
        if (excludedListHash.contains(allList.get(position).getPkgName())) {
            holder.add.setImageDrawable(removeIcon);
            holder.add.setRotation(0);
        } else {
            holder.add.setImageDrawable(addIcon);
            holder.add.setRotation(45);
        }
        holder.add.setTag(allList.get(position).getPkgName());
        holder.parent.setTag(allList.get(position).getPkgName());
        holder.add.setOnClickListener(this);
        if (isAppInfoEnable)
            holder.parent.setOnClickListener(this);
        if (isLongClickEnable)
            holder.parent.setOnLongClickListener(this);
        if (isShowPackageNameEnable)
            holder.pkgName.setVisibility(View.VISIBLE);
        else
            holder.pkgName.setVisibility(View.GONE);
        if (isScrollAnimationEnable)
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(App.context, R.anim.scale_in));
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
            AppInfo clickedApp = allListMap.get(pkgName);
            int index = allList.indexOf(clickedApp);
            if (excludedListHash.contains(pkgName)) {
                listener.onRemoveIconClick(clickedApp);
                appList.add(clickedApp);
                backupAppList.add(clickedApp);
                backupExcludedList.remove(clickedApp);
                excludedList.remove(clickedApp);
                excludedListHash.remove(pkgName);
                allList.remove(clickedApp);
                notifyItemRemoved(index);
                allList.add(clickedApp);
                notifyItemInserted(allList.size() - 1);
            } else {
                listener.onAddIconClick(clickedApp);
                backupAppList.remove(clickedApp);
                appList.remove(clickedApp);
                excludedList.add(clickedApp);
                backupExcludedList.add(clickedApp);
                excludedListHash.add(pkgName);
                allList.remove(clickedApp);
                notifyItemRemoved(index);
                allList.add(excludedList.size() - 1, clickedApp);
                notifyItemInserted(excludedList.size() - 1);
            }
        } else {
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
        listener.onAppInfoLongClicked(v.findViewById(R.id.package_name), pkgName);
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

        void onAppInfoLongClicked(View v, String pkgName);
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
