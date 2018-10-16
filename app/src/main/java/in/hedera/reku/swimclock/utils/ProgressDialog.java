package in.hedera.reku.swimclock.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by rakeshkalyankar on 01/10/18.
 * Contact k.rakeshlal@gmail.com for licence
 */
public class ProgressDialog {
    Dialog dialog;
    private ProgressBar progressBar;
    private TextView textView;
    Context context;

    public ProgressDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        // CardView
        CardView cardView = new CardView(context);
        RelativeLayout.LayoutParams cardViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 275);
        cardViewParams.addRule(RelativeLayout.CENTER_VERTICAL);
        cardView.setCardElevation(15f);
        cardView.setRadius(20f);
        cardViewParams.setMargins(dip(16f), dip(0f), dip(16f), dip(0f));

        // Inner layout
        LinearLayout innerLayout = new LinearLayout(context);
        RelativeLayout.LayoutParams innerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        innerLayout.setPadding(dip(8f), dip(8f), dip(8f), dip(8f));
        innerLayout.setOrientation(LinearLayout.HORIZONTAL);
        cardView.addView(innerLayout, innerLayoutParams);

        // ProgressBar
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams progressBarParams = new RelativeLayout.LayoutParams(125, RelativeLayout.LayoutParams.MATCH_PARENT);
        progressBarParams.addRule(RelativeLayout.CENTER_VERTICAL);
        progressBarParams.setMargins(dip(0f), dip(0f), dip(16f), dip(0f));
        innerLayout.addView(progressBar, progressBarParams);

        // TextView
        textView = new TextView(context);
        RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        textViewParams.addRule(RelativeLayout.CENTER_VERTICAL);
        textView.setPadding(dip(16f), dip(0f), dip(0f), dip(0f));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        innerLayout.addView(textView, textViewParams);

        // layout
        RelativeLayout relativeLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.setLayoutParams(layoutParams);
        relativeLayout.addView(cardView, cardViewParams);

        dialog.getWindow().setContentView(relativeLayout, layoutParams);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    }

    /**
     * @param dp size in dp
     * @return size in px
     **/
    // Convert dp to px
    private int dip(Float dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }

    public void show() {

        if (!dialog.isShowing() && dialog != null) {
            dialog.show();

        }

    }

    public void dismiss() {

        if (dialog.isShowing() && dialog != null) {
            dialog.dismiss();
        }
    }

    public void setMessage(String message) {
        textView.setText(message);
    }

    public void setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
    }


    public void setCanceledOnTouchOutside(boolean flag) {
        dialog.setCanceledOnTouchOutside(flag);
    }

    public void setColor(int colour) {
        progressBar.getIndeterminateDrawable().setColorFilter(colour, android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    public boolean isShowing() {

        return dialog.isShowing();
    }

}
