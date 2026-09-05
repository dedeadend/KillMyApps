package dedeadend.killmyapps.ui.excluded;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import dedeadend.killmyapps.databinding.FragmentExcludedBinding;
import dedeadend.killmyapps.model.AppInfo;
import dedeadend.killmyapps.util.CapsuleToast;

public class ExcludedFragment extends Fragment implements ExcludedRecyclerViewAdapter.onIconClickListener {

    private FragmentExcludedBinding binding;
    private ExcludedViewModel excludedViewModel;
    private CardView searchLayout;
    private EditText searchEditText;

    private ExcludedRecyclerViewAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        excludedViewModel = new ViewModelProvider(this).get(ExcludedViewModel.class);
        binding = FragmentExcludedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.excludedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.excludedRecyclerView.setItemAnimator(null);
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
        excludedViewModel.refreshList();
        searchEditText.setText("");
    }

    @Override
    public void onStop() {
        super.onStop();
        excludedViewModel.clearList();
        searchLayout.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        excludedViewModel.getExcludedList().removeObservers(getViewLifecycleOwner());
        binding = null;
    }

    private void setObservers() {
        excludedViewModel.getExcludedList().observe(getViewLifecycleOwner(), new Observer<List<AppInfo>>() {
            @Override
            public void onChanged(List<AppInfo> appInfos) {
                setAdapter();
                if (excludedViewModel.getAppsList().getValue().isEmpty() && appInfos.isEmpty())
                    searchLayout.setVisibility(View.GONE);
                else {
                    searchLayout.setAlpha(0f);
                    searchLayout.animate().alpha(1f).setDuration(400L).start();
                    searchLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setListeners() {
        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                excludedViewModel.refreshList();
                binding.refreshLayout.setRefreshing(false);
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
        adapter = new ExcludedRecyclerViewAdapter(excludedViewModel.getAppsList().getValue(),
                excludedViewModel.getExcludedList().getValue(), this);
        binding.excludedRecyclerView.swapAdapter(adapter, true);
    }

    @Override
    public void onAddIconClick(AppInfo appInfo) {
        excludedViewModel.addExcluded(appInfo);
        CapsuleToast.showInfo(getActivity(), "\"" + appInfo.getName() + "\" added to selection list");
    }

    @Override
    public void onRemoveIconClick(AppInfo appInfo) {
        excludedViewModel.removeExcluded(appInfo);
        CapsuleToast.showInfo(getActivity(), "\"" + appInfo.getName() + "\" removed from selection list");
    }

    @Override
    public void onAppInfoLongClicked(View v, String pkgName) {
        ClipboardManager clipboardManager = (ClipboardManager) App.context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("pkgName", pkgName);
        clipboardManager.setPrimaryClip(clipData);
        CapsuleToast.showInfo(getActivity(), "package name copied to clipboard");
    }
}