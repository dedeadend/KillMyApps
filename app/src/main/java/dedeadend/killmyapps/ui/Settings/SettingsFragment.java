package dedeadend.killmyapps.ui.Settings;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import dedeadend.killmyapps.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setObservers();
        setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setObservers() {
        settingsViewModel.getThemeMode().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == 0)
                    binding.themeSettings.check(binding.autoTheme.getId());
                else if (integer == 1)
                    binding.themeSettings.check(binding.lightTheme.getId());
                else if (integer == 2)
                    binding.themeSettings.check(binding.darkTheme.getId());
            }
        });
        settingsViewModel.getKillerMode().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == 1)
                    binding.killerModeSettings.check(binding.rootMode.getId());
                else if (integer == 2)
                    binding.killerModeSettings.check(binding.shizukuMode.getId());
            }
        });
        settingsViewModel.getListMode().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == 0)
                    binding.listModeSettings.check(binding.userMode.getId());
                else if (integer == 1)
                    binding.listModeSettings.check(binding.launcherMode.getId());
                else if (integer == 2)
                    binding.listModeSettings.check(binding.systemMode.getId());
            }
        });
        settingsViewModel.getSelectionMode().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == 0)
                    binding.selectionModeSettings.check(binding.excludeMode.getId());
                else if (integer == 1)
                    binding.selectionModeSettings.check(binding.includeMode.getId());
            }
        });
        settingsViewModel.getHideKillMyApps().observe(getViewLifecycleOwner(), binding.hideKillMyApps::setChecked);
        settingsViewModel.getHideDefaultLauncher().observe(getViewLifecycleOwner(), binding.hideDefaultLauncher::setChecked);
        settingsViewModel.getHideDefaultAlarm().observe(getViewLifecycleOwner(), binding.hideDefaultAlarm::setChecked);
        settingsViewModel.getHideDefaultKeyboard().observe(getViewLifecycleOwner(), binding.hideDefaultKeyboard::setChecked);
        settingsViewModel.getHideDefaultDialer().observe(getViewLifecycleOwner(), binding.hideDefaultDialer::setChecked);
        settingsViewModel.getHideDefaultSMS().observe(getViewLifecycleOwner(), binding.hideDefaultSms::setChecked);
        settingsViewModel.getHideCriticalSystemApps().observe(getViewLifecycleOwner(), binding.hideCriticalSystemApps::setChecked);
        settingsViewModel.getShowAppsPkgName().observe(getViewLifecycleOwner(), binding.showPkgname::setChecked);
        settingsViewModel.getClickToAppInfo().observe(getViewLifecycleOwner(), binding.clickToAppInfo::setChecked);
        settingsViewModel.getLongClickToMenu().observe(getViewLifecycleOwner(), binding.longClickToMenu::setChecked);
        settingsViewModel.getShowScrollAnimation().observe(getViewLifecycleOwner(), binding.showScrollAnimation::setChecked);
    }

    private void setListeners() {
        binding.themeSettings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if (checkedId == binding.autoTheme.getId())
                    settingsViewModel.setThemeMode(0);
                else if (checkedId == binding.lightTheme.getId())
                    settingsViewModel.setThemeMode(1);
                else if (checkedId == binding.darkTheme.getId())
                    settingsViewModel.setThemeMode(2);
            }
        });
        binding.killerModeSettings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if (checkedId == binding.rootMode.getId())
                    settingsViewModel.setKillerMode(1);
                else if (checkedId == binding.shizukuMode.getId())
                    settingsViewModel.setKillerMode(2);
            }
        });
        binding.listModeSettings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if (checkedId == binding.userMode.getId())
                    settingsViewModel.setListMode(0);
                else if (checkedId == binding.launcherMode.getId())
                    settingsViewModel.setListMode(1);
                else if (checkedId == binding.systemMode.getId())
                    settingsViewModel.setListMode(2);
            }
        });
        binding.selectionModeSettings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if (checkedId == binding.excludeMode.getId())
                    settingsViewModel.setSelectionMode(0);
                else if (checkedId == binding.includeMode.getId())
                    settingsViewModel.setSelectionMode(1);
            }
        });
        binding.hideKillMyApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setHideKillMyApps(binding.hideKillMyApps.isChecked());
            }
        });
        binding.hideDefaultLauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setHideDefaultLauncher(binding.hideDefaultLauncher.isChecked());
            }
        });
        binding.hideDefaultAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setHideDefaultAlarm(binding.hideDefaultAlarm.isChecked());
            }
        });
        binding.hideDefaultKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setHideDefaultKeyboard(binding.hideDefaultKeyboard.isChecked());
            }
        });
        binding.hideDefaultDialer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setHideDefaultDialer(binding.hideDefaultDialer.isChecked());
            }
        });
        binding.hideDefaultSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setHideDefaultSMS(binding.hideDefaultSms.isChecked());
            }
        });
        binding.hideCriticalSystemApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setHideCriticalSystemApps(binding.hideCriticalSystemApps.isChecked());
            }
        });
        binding.showPkgname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setShowAppsPkgName(binding.showPkgname.isChecked());
            }
        });
        binding.clickToAppInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setClickToAppInfo(binding.clickToAppInfo.isChecked());
            }
        });
        binding.longClickToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setLongClickToMenu(binding.longClickToMenu.isChecked());
            }
        });
        binding.showScrollAnimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setShowScrollAnimation(binding.showScrollAnimation.isChecked());
            }
        });
        binding.github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator.ofPropertyValuesHolder(v,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.9f, 1),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.9f, 1)
                ).setDuration(400L).start();
                String url = "https://github.com/dedeadend/KillMyApps/";
                Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(urlIntent);
            }
        });
    }
}