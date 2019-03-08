package com.codeape.apkextract.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * 沉浸样式
 *
 * @author LiuPeiming
 *         Created at 2017/6/30 5:47
 */

public class ImmersionStyles {

    static final immersionStyle IMST;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            IMST = new MarshmallowImst();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            IMST = new LollipopImst();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            IMST = new KitkatImst();
        } else {
            IMST = new immersionStyle() {
                @Override
                public void setImmersionStyle(Window window, int statusBarColor, int navigationBarColor, boolean lightStatusBar) {
                }

                @Override
                public void setImmersionStyle(Window window, int navigationBarColor) {

                }
            };
        }
    }

    /**
     * 设置系统状态栏颜色
     *
     * @param activity           Context
     * @param statusBarColor     状态栏颜色
     * @param navigationBarColor 导航栏颜色
     */
    public static void setImmersionStyle(Activity activity, int statusBarColor, int navigationBarColor) {
        boolean isLightColor = nearWhite(Color.red(statusBarColor)) && nearWhite(Color.green(statusBarColor)) && nearWhite(Color.blue(statusBarColor));
        setImmersionStyle(activity, statusBarColor, navigationBarColor, isLightColor);
    }

    private static boolean nearWhite(int singleColor) {
        return singleColor > 200;
    }

    /**
     * 设置系统状态栏颜色
     *
     * @param activity           Context
     * @param statusBarColor     状态栏颜色
     * @param navigationBarColor 导航栏颜色
     * @param lightStatusBar     在设置了浅色状态栏下是否修改状态栏字体图标颜色，仅对一些设备有效（比如MIUI）
     */
    public static void setImmersionStyle(Activity activity, int statusBarColor, int navigationBarColor, boolean lightStatusBar) {
        setImmersionStyle(activity.getWindow(), statusBarColor, navigationBarColor, lightStatusBar);
    }

    /**
     * 设置系统状态栏-导航栏颜色
     *
     * @param activity           Context
     * @param navigationBarColor 导航栏颜色
     */
    public static void setImmersionStyle(Activity activity, int navigationBarColor) {
        setImmersionStyle(activity.getWindow(), navigationBarColor);
    }

    /**
     * 设置系统状态栏颜色
     *
     * @param window             Window
     * @param statusBarColor     状态栏颜色
     * @param navigationBarColor 导航栏颜色
     * @param lightStatusBar     在设置了浅色状态栏下是否修改状态栏字体图标颜色，仅对一些设备有效（比如MIUI）
     */
    public static void setImmersionStyle(Window window, int statusBarColor, int navigationBarColor, boolean lightStatusBar) {
        if ((window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) > 0) {
            return;
        }
        IMST.setImmersionStyle(window, statusBarColor, navigationBarColor, lightStatusBar);
    }

    /**
     * 设置系统状态栏颜色
     *
     * @param window             Window
     * @param navigationBarColor 导航栏颜色
     */
    public static void setImmersionStyle(Window window, int navigationBarColor) {
        if ((window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) > 0) {
            return;
        }
        IMST.setImmersionStyle(window, navigationBarColor);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void setFitsSystemWindows(Window window, boolean fitSystemWindows) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                mChildView.setFitsSystemWindows(fitSystemWindows);//注意不是设置ContentView的FitsSystemWindows，而是设置ContentView的第一个子View，预留出系统View的空间
            }
        }
    }
}

/**
 * 沉浸样式接口
 */
interface immersionStyle {

    /**
     * 设置系统状态栏颜色
     *
     * @param window             Window
     * @param statusBarColor     状态栏颜色
     * @param navigationBarColor 导航栏颜色
     * @param lightStatusBar     在设置了浅色状态栏下是否修改状态栏字体图标颜色，仅对一些设备有效（比如MIUI）
     */
    void setImmersionStyle(Window window, int statusBarColor, int navigationBarColor, boolean lightStatusBar);

    /**
     * 设置系统状态栏-导航栏颜色
     *
     * @param window             Window
     * @param navigationBarColor 导航栏颜色
     * @inform 隐藏了状态栏情况下（如使用主题@android:style/Theme.NoTitleBar.Fullscreen）设置导航栏颜色
     */
    void setImmersionStyle(Window window, int navigationBarColor);

}

/**
 * 兼容M、N及O版本
 */
class MarshmallowImst implements immersionStyle {

    @SuppressLint({"InlinedApi", "NewApi"})
    public void setImmersionStyle(Window window, int statusBarColor, int navigationBarColor, boolean lightStatusBar) {

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//取消设置透明状态栏,使ContentView内容不再覆盖状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);//需要设置这个flag才能调用setStatusBarColor和setNavigationBarColor
        window.setStatusBarColor(statusBarColor);//设置状态栏颜色
        window.setNavigationBarColor(navigationBarColor);//设置导航栏颜色

        //设置浅色状态栏时的界面显示
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (lightStatusBar) {
            ui |= 8192;
            CustomSystemUtils.setStatusBarTextColor(window);//兼容MIUI
        } else {
            ui &= ~8192;
        }
        decor.setSystemUiVisibility(ui);

        //去掉系统状态栏下的windowContentOverlay
        View v = window.findViewById(android.R.id.content);
        if (v != null) {
            //v.setForeground(null);
            //throw new RuntimeException("Stub!");
        }
    }

    @SuppressLint({"InlinedApi", "NewApi"})
    public void setImmersionStyle(Window window, int navigationBarColor) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);//需要设置这个flag才能调用setStatusBarColor和setNavigationBarColor
        window.setNavigationBarColor(navigationBarColor);//设置导航栏颜色
    }
}


/**
 * 兼容Lollipop版本
 */
class LollipopImst implements immersionStyle {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setImmersionStyle(Window window, int statusBarColor, int navigationBarColor, boolean lightStatusBar) {

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//取消设置透明状态栏,使ContentView内容不再覆盖状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);//需要设置这个flag才能调用setStatusBarColor和setNavigationBarColor
        window.setStatusBarColor(statusBarColor);//设置状态栏颜色
        window.setNavigationBarColor(navigationBarColor);//设置导航栏颜色

        if (lightStatusBar) {
            CustomSystemUtils.setStatusBarTextColor(window);//兼容MIUI
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setImmersionStyle(Window window, int navigationBarColor) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);//需要设置这个flag才能调用setStatusBarColor和setNavigationBarColor
        window.setNavigationBarColor(navigationBarColor);//设置导航栏颜色
    }
}


/**
 * 兼容Kitkat版本
 *
 * @inform Kitkat版本无法设置导航栏颜色，只能设置透明
 */
class KitkatImst implements immersionStyle {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setImmersionStyle(Window window, int statusBarColor, int navigationBarColor, boolean lightStatusBar) {

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//设置透明导航栏
        ViewGroup decorViewGroup = (ViewGroup) window.getDecorView();
        View statusBarView = new View(window.getContext());
        int statusBarHeight = getStatusBarHeight(window.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, statusBarHeight);
        params.gravity = Gravity.TOP;
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(statusBarColor);
        decorViewGroup.addView(statusBarView);
        ImmersionStyles.setFitsSystemWindows(window, true);

        if (lightStatusBar) {
            CustomSystemUtils.setStatusBarTextColor(window);//兼容MIUI
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void setImmersionStyle(Window window, int navigationBarColor) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//设置透明导航栏
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     */
    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}

