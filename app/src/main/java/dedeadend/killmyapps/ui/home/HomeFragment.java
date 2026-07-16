package dedeadend.killmyapps.ui.home;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.InfoDialog;
import dedeadend.killmyapps.R;
import dedeadend.killmyapps.databinding.FragmentHomeBinding;
import dedeadend.killmyapps.model.AppInfo;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private HomeRecyclerViewAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.homeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_scale_in);
        binding.homeRecyclerView.setLayoutAnimation(animation);
        setObservers();
        setListeners();
        if (App.isFirstRun) {
            InfoDialog infoDialog = new InfoDialog(getContext());
            infoDialog.show();
            App.isFirstRun = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        homeViewModel.refreshList();
        binding.search.setQuery("", false);
    }

    @Override
    public void onStop() {
        super.onStop();
        homeViewModel.clearList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        homeViewModel.getAppsList().removeObservers(getViewLifecycleOwner());
        binding = null;
    }

    private void setObservers() {
        homeViewModel.getAppsList().observe(getViewLifecycleOwner(), new Observer<List<AppInfo>>() {
            @Override
            public void onChanged(List<AppInfo> appInfos) {
                setAdapter();
                if (appInfos.isEmpty()) {
                    ObjectAnimator.ofPropertyValuesHolder(binding.allDead,
                                    PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f))
                            .setDuration(3000L).start();
                    binding.killAllBtn.setVisibility(View.INVISIBLE);
                    binding.search.setVisibility(View.INVISIBLE);
                    binding.allDead.setVisibility(View.VISIBLE);
                } else {
                    if (App.settings.getBoolean(App.SHOW_SCROLL_ANIMATION, true)) {
                        ObjectAnimator.ofPropertyValuesHolder(binding.killAllBtn,
                                        PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f))
                                .setDuration(400L).start();
                        ObjectAnimator.ofPropertyValuesHolder(binding.search,
                                        PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f))
                                .setDuration(400L).start();
                    }
                    binding.allDead.setVisibility(View.INVISIBLE);
                    binding.killAllBtn.setVisibility(View.VISIBLE);
                    binding.search.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setListeners() {
        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                homeViewModel.refreshList();
                binding.refreshLayout.setRefreshing(false);
            }
        });
        binding.killAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator.ofPropertyValuesHolder(v,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.9f, 1),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.9f, 1)
                ).setDuration(400L).start();

                homeViewModel.onKillAllAppsClicked(new HomeViewModel.OnResultListener() {
                    @Override
                    public void onKillSuccessfully(int count) {
                        if (count == 1)
                            App.toast(getActivity(), "DONE", "1 app is dead");
                        else
                            App.toast(getActivity(), "DONE", count + " apps are dead");
                    }

                    @Override
                    public void onKillFailed() {
                        InfoDialog infoDialog = new InfoDialog(getContext());
                        infoDialog.show();
                    }
                });
            }
        });

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null)
                    adapter.filterList(newText);
                return true;
            }
        });
    }


    private void setAdapter() {
        adapter = new HomeRecyclerViewAdapter(homeViewModel.getAppsList().getValue(), new HomeRecyclerViewAdapter.onItemClickListener() {
            @Override
            public void onKillButtonClicked(String pkgName, String name) {
                homeViewModel.onKillSingleAppClicked(pkgName, new HomeViewModel.OnResultListener() {
                    @Override
                    public void onKillSuccessfully(int count) {
                        adapter.itemKilled(pkgName);
                        App.toast(getActivity(), "DONE", "\"" + name + "\" is dead");
                        homeViewModel.checkForRefresh();
                    }

                    @Override
                    public void onKillFailed() {
                        InfoDialog infoDialog = new InfoDialog(getContext());
                        infoDialog.show();
                    }
                });
            }

            @Override
            public void onAppInfoLongClicked(View v, String pkgName) {
                ClipboardManager clipboardManager = (ClipboardManager) App.context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("pkgName", pkgName);
                clipboardManager.setPrimaryClip(clipData);
                App.toast(getActivity(), "DONE", "package name copied to clipboard");
            }
        });
        binding.homeRecyclerView.swapAdapter(adapter, true);
    }
}