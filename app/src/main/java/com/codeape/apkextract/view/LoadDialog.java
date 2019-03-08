package com.codeape.apkextract.view;

import android.app.Dialog;
import android.content.Context;

import com.codeape.apkextract.R;


/**
 * 加载框
 *
 * @author LiuPeiming
 *         Created at 2017/5/14 12:58
 */
public class LoadDialog extends Dialog {

    private Context mContext;

    private int backgroundType = 0;

    /**
     * 创建Dialog
     *
     * @param context
     */
    public LoadDialog(Context context) {
        super(context, R.style.LoadDialog);
        initDialog();
        mContext = context;
    }

    public LoadDialog LoadDialog(Context context) {
        return new LoadDialog(mContext);
    }

    /**
     * 初始化Dialog
     */
    private void initDialog() {
        setContentView(R.layout.view_loaddialog);
        // 点击对话框外不消失
        setCanceledOnTouchOutside(false);
    }


    /**
     * 显示
     */
    public void showDialog() {

        show();
    }

}
