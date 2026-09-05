package dedeadend.killmyapps.ui.home;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.R;
import dedeadend.killmyapps.model.AppInfo;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private final List<AppInfo> appList, backupList;
    private final Map<String, AppInfo> appListMap;
    private final onItemClickListener listener;

    private final boolean isAppInfoEnable, isLongClickEnable, isShowPackageNameEnable, isScrollAnimationEnable;

    private final Drawable iconPause;

    public HomeRecyclerViewAdapter(List<AppInfo> appList, onItemClickListener listener) {
        this.appList = appList;
        this.listener = listener;
        backupList = new ArrayList<>(appList);
        appListMap = new HashMap<>();
        for (AppInfo appInfo : appList)
            appListMap.put(appInfo.getPkgName(), appInfo);
        isAppInfoEnable = App.settings.getBoolean(App.CLICK_TO_APP_INFO, true);
        isLongClickEnable = App.settings.getBoolean(App.LONG_CLICK_TO_MENU, true);
        isShowPackageNameEnable = App.settings.getBoolean(App.SHOW_PKG_NAME, true);
        isScrollAnimationEnable = App.settings.getBoolean(App.SHOW_SCROLL_ANIMATION, true);
        iconPause = AppCompatResources.getDrawable(App.context, R.drawable.ic_pause);
    }

    @NonNull
    @Override
    public HomeRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(dedeadend.killmyapps.R.layout.recycler_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.name.setText(appList.get(position).getName());
        holder.pkgName.setText(appList.get(position).getPkgName());
        holder.icon.setImageDrawable(appList.get(position).getIcon());
        holder.pause.setImageDrawable(iconPause);
        holder.pause.setTag(appList.get(position).getPkgName());
        holder.parent.setTag(appList.get(position).getPkgName());
        holder.pause.setOnClickListener(this);
        if (isAppInfoEnable)
            holder.parent.setOnClickListener(this);
        if (isLongClickEnable)
            holder.parent.setOnLongClickListener(this);
        if (isShowPackageNameEnable)
            holder.pkgName.setVisibility(View.VISIBLE);
        else
            holder.pkgName.setVisibility(View.GONE);
        if (isScrollAnimationEnable) {
            holder.itemView.setScaleX(0.5f);
            holder.itemView.setScaleY(0.5f);
            holder.itemView.setAlpha(0f);
            holder.itemView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(400)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    @Override
    public void onClick(View v) {
        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200L).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(200L).start()
        ).start();
        String pkgName = (String) v.getTag();
        if (v == v.findViewById(R.id.kill_icon)) {
            listener.onKillButtonClicked(pkgName, appListMap.get(pkgName).getName());
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + pkgName.trim()));
            App.context.startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200L).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(200L).start()
        ).start();
        String pkgName = (String) v.getTag();
        listener.onAppInfoLongClicked(v.findViewById(R.id.package_name), pkgName);
        return true;
    }

    public void filterList(String filter) {
        appList.clear();
        appList.addAll(backupList);
        for (int i = 0; i < appList.size(); i++) {
            if (!appList.get(i).getName().toLowerCase().contains(filter.toLowerCase())) {
                appList.remove(i);
                i--;
            }
        }
        notifyDataSetChanged();
    }

    public void itemKilled(String pkgName) {
        int index = appList.indexOf(appListMap.get(pkgName));
        appListMap.remove(pkgName);
        backupList.remove(index);
        appList.remove(index);
        notifyItemRemoved(index);
    }

    public interface onItemClickListener {
        void onKillButtonClicked(String pkgName, String Name);

        void onAppInfoLongClicked(View v, String pkgName);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon, pause;
        TextView name, pkgName;
        View parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView;
            icon = itemView.findViewById(R.id.app_icon);
            pause = itemView.findViewById(R.id.kill_icon);
            name = itemView.findViewById(R.id.app_name);
            pkgName = itemView.findViewById(R.id.package_name);
        }
    }
}
