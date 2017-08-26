package com.example.ubuntu.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by shivam on 8/26/17.
 */

class OverlayEye extends ViewGroup {
    private Context context;
    private AttributeSet attrs;
    private TextView textView;
    private int textColor;

    public OverlayEye(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
    }

    private int depthOffset;
    private int viewWidth;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;
        final int height = bottom - top;

        final float verticalTextPos = 0.52f;

        float topMargin = height * verticalTextPos;
        viewWidth = width;
        textView.layout(0, (int) topMargin, width, bottom);
    }

    public void setColor(int color) {
        this.textColor = color;
    }

    public void addContent(String text) {
        textView = new TextView(context, attrs);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(textColor);
        textView.setText(text);
        textView.setX(depthOffset);
        addView(textView);
    }
    public void setDepthFactor(float factor) {
        this.depthOffset = (int)(factor * viewWidth);
    }
}

public class OverlayView extends LinearLayout{
    private final OverlayEye leftEye;
    private final OverlayEye rightEye;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(0, 0, 0, 0);

        leftEye = new OverlayEye(context, attrs);
        leftEye.setLayoutParams(params);
        addView(leftEye);

        rightEye = new OverlayEye(context, attrs);
        rightEye.setLayoutParams(params);
        addView(rightEye);

        setDepthFactor(0.01f);
        setColor(Color.rgb(150, 255, 180));
        addContent("Hello Virtual World!");
        setVisibility(View.VISIBLE);
    }

    public void setDepthFactor(float factor) {
        leftEye.setDepthFactor(factor);
        rightEye.setDepthFactor(-factor);
    }

    public void setColor(int color) {
        leftEye.setColor(color);
        rightEye.setColor(color);
    }

    public void addContent(String text) {
        leftEye.addContent(text);
        rightEye.addContent(text);
    }

}