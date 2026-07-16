package dedeadend.killmyapps;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

public class InfoDialog extends Dialog {
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
