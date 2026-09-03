package dedeadend.killmyapps.util;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import dedeadend.killmyapps.R;

public class CapsuleToast {

    private final View capsuleView;
    private final ProgressBar progressBar;
    private final ImageView iconView;
    private final TextView textView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private CapsuleToast(Activity activity) {
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        capsuleView = LayoutInflater.from(activity).inflate(R.layout.capsule_toast, rootView, false);
        View toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            int statusBarBottom = toolbar.getBottom();
            int marginInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    activity.getResources().getDisplayMetrics()
            );
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) capsuleView.getLayoutParams();
            params.topMargin = statusBarBottom + marginInPx;
            capsuleView.setLayoutParams(params);
        }
        progressBar = capsuleView.findViewById(R.id.capsule_progress);
        iconView = capsuleView.findViewById(R.id.capsule_icon);
        textView = capsuleView.findViewById(R.id.capsule_text);
        rootView.addView(capsuleView);
    }

    public static void showInfo(Activity activity, String message) {
        CapsuleToast toast = new CapsuleToast(activity);
        toast.showStatic(message);
    }

    public static CapsuleToast showLoading(Activity activity, String message) {
        CapsuleToast toast = new CapsuleToast(activity);
        toast.showProgress(message);
        return toast;
    }

    private void showProgress(String message) {
        iconView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        textView.setText(message);
        animateIn();
    }

    public void updateSuccess(String message) {
        progressBar.setVisibility(View.GONE);
        iconView.setImageResource(R.drawable.ic_success);
        iconView.setVisibility(View.VISIBLE);
        textView.setText(message);
        dismissWithDelay();
    }

    public void updateError(String message) {
        progressBar.setVisibility(View.GONE);
        iconView.setImageResource(R.drawable.ic_fail);
        iconView.setVisibility(View.VISIBLE);
        textView.setText(message);
        dismissWithDelay();
    }

    private void showStatic(String message) {
        progressBar.setVisibility(View.GONE);
        iconView.setImageResource(R.drawable.ic_info);
        iconView.setVisibility(View.VISIBLE);
        textView.setText(message);
        animateIn();
        dismissWithDelay();
    }

    private void animateIn() {
        textView.setVisibility(View.GONE);
        capsuleView.setAlpha(0f);
        capsuleView.setScaleX(0f);
        capsuleView.setScaleY(0f);
        capsuleView.setVisibility(View.VISIBLE);
        capsuleView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400L)
                .start();
        handler.postDelayed(this::animateSecond, 600L);
    }

    private void animateSecond() {
        textView.setVisibility(View.VISIBLE);
    }

    public void dismiss() {
        textView.setVisibility(View.GONE);
        capsuleView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(400)
                .setStartDelay(500L)
                .withEndAction(() -> {
                    ViewGroup parent = (ViewGroup) capsuleView.getParent();
                    if (parent != null)
                        parent.removeView(capsuleView);
                })
                .start();
    }

    private void dismissWithDelay() {
        handler.postDelayed(this::dismiss, 4000L);
    }
}