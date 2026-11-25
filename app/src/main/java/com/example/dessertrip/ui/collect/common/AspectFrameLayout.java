package com.example.dessertrip.ui.collect.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.example.dessertrip.R;

/** 너비에 비례해 높이를 정하는 FrameLayout. 높이 = 너비 * aspect (기본 1 = 정사각형) */
public class AspectFrameLayout extends FrameLayout {
    private float aspect = 1f;

    public AspectFrameLayout(Context context) { this(context, null); }

    public AspectFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectFrameLayout);
            aspect = a.getFloat(R.styleable.AspectFrameLayout_aspect, 1f);
            a.recycle();
        }
    }

    public void setAspect(float aspect) { this.aspect = aspect; requestLayout(); }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.round(width * aspect);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
}
