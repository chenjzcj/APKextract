package com.codeape.apkextract.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.codeape.apkextract.R;


/**
 * Created by LiuPeiming on 2017/5/20.
 */

public class CustomToolbar extends Toolbar {

    private Context mContext;

    private TextView bar_tv_left, bar_tv_right;

    public CustomToolbar(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        View titlebar_layout = View.inflate(mContext, R.layout.view_toolbar, null);
        bar_tv_left = titlebar_layout.findViewById(R.id.toolbar_left);
        bar_tv_right = titlebar_layout.findViewById(R.id.right_bt);
        addView(titlebar_layout);
    }

    /**
     * 设置左边文字
     *
     * @param text
     */
    public void setLeftText(String text) {
        bar_tv_left.setText(text);
    }


    /**
     * 设置左边文字大小
     *
     * @param size
     */
    public void setLeftTextSize(float size) {
        bar_tv_left.setTextSize(size);
    }

    /**
     * 设置左边文字样式
     *
     * @param style
     */
    public void setLeftTextStyle(int style) {
        bar_tv_left.setTypeface(Typeface.defaultFromStyle(style));
    }


    /**
     * 设置右边文字
     *
     * @param text
     */
    public void setRightText(String text) {
        bar_tv_right.setText(text);
    }

    /**
     * 设置左按钮点击事件
     *
     * @param listener
     */
    public void setRightOnClickListener(OnClickListener listener) {
        bar_tv_right.setOnClickListener(listener);
    }

}
