package dedeadend.killmyapps.ui.home;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import dedeadend.killmyapps.App;
import dedeadend.killmyapps.MainActivity;
import dedeadend.killmyapps.R;
import dedeadend.killmyapps.databinding.FragmentHomeBinding;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.util.CapsuleToast;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private HomeRecyclerViewAdapter adapter;
    private CardView searchLayout;
    private EditText searchEditText;

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
        if (App.isFirstRun) {
            InfoDialog infoDialog = new InfoDialog(getContext());
            infoDialog.show();
            App.isFirstRun = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        searchLayout = ((MainActivity) requireActivity()).getSearchLayout();
        searchEditText = ((MainActivity) requireActivity()).getSearchEditText();
        setObservers();
        setListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        homeViewModel.refreshList();
        searchEditText.setText("");
    }

    @Override
    public void onStop() {
        super.onStop();
        homeViewModel.clearList();
        searchLayout.setVisibility(View.GONE);
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
                    ObjectAnimator.ofPropertyValuesHolder(
                            binding.allDead,
                            PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
                    ).setDuration(5000L).start();
                    binding.killAllBtn.setVisibility(View.INVISIBLE);
                    searchLayout.setVisibility(View.INVISIBLE);
                    binding.allDead.setVisibility(View.VISIBLE);
                } else {
                    ObjectAnimator.ofPropertyValuesHolder(
                            binding.killAllBtn,
                            PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
                    ).setDuration(500L).start();
                    ObjectAnimator.ofPropertyValuesHolder(
                            searchLayout,
                            PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
                    ).setDuration(500L).start();
                    binding.allDead.setVisibility(View.INVISIBLE);
                    binding.killAllBtn.setVisibility(View.VISIBLE);
                    searchLayout.setVisibility(View.VISIBLE);
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
                ).setDuration(500L).start();
                CapsuleToast capsuleToast = CapsuleToast.showLoading(getActivity(), "Killing...");
                homeViewModel.onKillAllAppsClicked(new HomeViewModel.OnResultListener() {
                    @Override
                    public void onKillSuccessfully(int count) {
                        if (count == 1)
                            capsuleToast.updateSuccess("1 app is dead");
                        else
                            capsuleToast.updateSuccess(count + " apps are dead");
                    }

                    @Override
                    public void onKillFailed() {
                        capsuleToast.updateError("Failed");
                        InfoDialog infoDialog = new InfoDialog(getContext());
                        infoDialog.show();
                    }
                });
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null)
                    adapter.filterList(s.toString());
            }
        });
    }


    private void setAdapter() {
        adapter = new HomeRecyclerViewAdapter(homeViewModel.getAppsList().getValue(), new HomeRecyclerViewAdapter.onItemClickListener() {
            @Override
            public void onKillButtonClicked(String pkgName, String name) {
                CapsuleToast capsuleToast = CapsuleToast.showLoading(getActivity(), "Killing...");
                homeViewModel.onKillSingleAppClicked(pkgName, new HomeViewModel.OnResultListener() {
                    @Override
                    public void onKillSuccessfully(int count) {
                        adapter.itemKilled(pkgName);
                        capsuleToast.updateSuccess("\"" + name + "\" is dead");
                        homeViewModel.checkForRefresh();
                    }

                    @Override
                    public void onKillFailed() {
                        capsuleToast.updateError("Failed");
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
                CapsuleToast.showInfo(getActivity(), "package name copied to clipboard");
            }
        });
        binding.homeRecyclerView.swapAdapter(adapter, true);
    }
}