package com.espressif.espblufi.views;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

public class BubbleSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {

    private PopupWindow popupWindow;
    private TextView textView;

    public BubbleSeekBar(Context context) {
        super(context);
        init(context);
    }

    public BubbleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BubbleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        textView = new TextView(context);
        textView.setBackgroundColor(Color.WHITE); // Adjust color and styling as needed
        textView.setTextColor(Color.BLACK);
        textView.setPadding(8, 4, 8, 4); // Padding around text

        popupWindow = new PopupWindow(textView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(false);

        this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));
                if (popupWindow.isShowing()) {
                    popupWindow.update(BubbleSeekBar.this, (int) (progress * (getWidth() / (float) getMax())) - popupWindow.getWidth() / 2, -getHeight(), -1, -1);
                } else {
                    popupWindow.showAsDropDown(BubbleSeekBar.this, (int) (progress * (getWidth() / (float) getMax())) - popupWindow.getWidth() / 2, -getHeight());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Optionally do something here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                popupWindow.dismiss();
            }
        });
    }
}
