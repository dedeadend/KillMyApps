package dedeadend.killmyapps;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import dedeadend.killmyapps.databinding.ActivityMainBinding;
import dedeadend.killmyapps.util.CapsuleToast;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        App.setAppThemeMode();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navController);

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    insets.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return windowInsets;
        });

        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(binding.logo, "rotation", 0f, 360f);
        rotateAnimator.setDuration(6000);
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.start();

        binding.searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.searchEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        binding.donateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator.ofPropertyValuesHolder(v,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 0.9f, 1),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0.9f, 1)
                ).setDuration(500L).start();
                try {
                    String url = "https://nowpayments.io/donation/dedeadend/";
                    Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(urlIntent);
                    CapsuleToast.showInfo(navHostFragment.getActivity(), "Thank You");
                } catch (ActivityNotFoundException e) {
                    CapsuleToast.showInfo(navHostFragment.getActivity(), "No browser app found to open link");
                }
            }
        });
    }

    public CardView getSearchLayout() {
        return binding.searchLayout;
    }

    public EditText getSearchEditText() {
        return binding.searchEditText;
    }

    public CardView getDonateLayout() {
        return binding.donateLayout;
    }
}