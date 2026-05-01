package dedeadend.killmyapps.ui.home;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;
import java.util.concurrent.Executors;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.R;
import dedeadend.killmyapps.SuUtils;
import dedeadend.killmyapps.databinding.FragmentHomeBinding;
import dedeadend.killmyapps.model.AppInfo;

public class HomeFragment extends Fragment implements HomeRecyclerViewAdapter.onItemClickListener {

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
        homeViewModel.refreshList();
        if (App.isFirstRun) {
            InfoDialog infoDialog = new InfoDialog(getContext());
            infoDialog.show();
            App.isFirstRun = false;
        }
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
                if (appInfos.size() == 0) {
                    binding.killAllBtn.setVisibility(View.GONE);
                    binding.search.setVisibility(View.GONE);
                    binding.allDead.setVisibility(View.VISIBLE);
                } else {
                    binding.killAllBtn.setVisibility(View.VISIBLE);
                    binding.search.setVisibility(View.VISIBLE);
                    binding.allDead.setVisibility(View.GONE);
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
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        List<AppInfo> appList = homeViewModel.getAppsList().getValue();
                        if (appList != null) {
                            int result = SuUtils.killListOfApps(homeViewModel.getAppsList().getValue());
                            if (result == -1) {
                                App.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onSuError();
                                    }
                                });
                            } else {
                                App.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (result == 1)
                                            SuUtils.killMyApps();
                                        App.toast(getActivity(), "DONE",
                                                homeViewModel.clearList() + " apps killed successfully");
                                    }
                                });
                            }
                        }
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
        adapter = new HomeRecyclerViewAdapter(homeViewModel.getAppsList().getValue(), this);
        binding.homeRecyclerView.swapAdapter(adapter, true);
    }

    @Override
    public void onPaused(int position) {
        App.toast(getActivity(), "DONE",
                "\"" + homeViewModel.getAppsList().getValue().get(position).getName() +
                        "\" killed successfully");
        homeViewModel.checkForRefresh();
    }

    @Override
    public void onSuError() {
        InfoDialog infoDialog = new InfoDialog(getContext());
        infoDialog.show();
    }

    @Override
    public void onAppInfo(String pkgName) {
        ClipboardManager clipboardManager = (ClipboardManager) App.context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("pkgName", pkgName);
        clipboardManager.setPrimaryClip(clipData);
        App.toast(getActivity(), "DONE", "package name copied to clipboard");
    }

    private class InfoDialog extends Dialog {
        Context context;
        Button close;

        public InfoDialog(@NonNull Context context) {
            super(context);
            this.context = context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_info);
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            setCancelable(true);
            close = findViewById(R.id.close_dialog_btn);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(findViewById(R.id.dialog_icon),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.8f, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.8f, 1)
            );
            objectAnimator.setDuration(2000L);
            objectAnimator.setRepeatCount(30);
            objectAnimator.start();
        }
    }
}