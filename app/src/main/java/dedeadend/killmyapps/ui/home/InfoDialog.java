package dedeadend.killmyapps.ui.home;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

import androidx.annotation.NonNull;

import dedeadend.killmyapps.R;

public class InfoDialog extends Dialog {

    private ObjectAnimator objectAnimator;

    public InfoDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_info);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        setCancelable(true);
        Button close = findViewById(R.id.close_dialog_btn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        objectAnimator = ObjectAnimator.ofPropertyValuesHolder(findViewById(R.id.dialog_icon),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f, 1.0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f, 1.0f)
        );
        objectAnimator.setDuration(2000L);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    protected void onStart() {
        super.onStart();
        objectAnimator.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        objectAnimator.cancel();
    }
}
