package com.android.launcher.overview.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher.overview.ui.TabPageIndicator.OnTabReselectedListener;
import com.android.launcher3.DragController;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.MuchAppsCustomizePagedView;
import com.android.launcher3.MuchAppsCustomizeTabHost;
import com.android.launcher3.R;

public class OverViewTabs implements OnTabReselectedListener {

    private List<View> mListViews = new ArrayList<View>();
    private HashMap<Integer, TextView> mTitleMap = new HashMap<Integer, TextView>();
    private Context mContext;

    private static final int EFFECT_INDEX = 0;
    private static final int WALLPAPER_INDEX = 1;
    private static final int WIDGET_INDEX = 2;
    private TextView mTitleEffect, mTitleWallPaper, mTitleWidget;
    private ImageView mNavIndicator;// 动画图片
    private FrameLayout mContainer;
    private UnderlinesNoFadeLayout mEffectContent;
    private WallpaperLayout mWallPaperContent;
    
    private MuchAppsCustomizeTabHost mAppsCustomizeTabHost;
    private MuchAppsCustomizePagedView mAppsCustomizeContent;
    private int mCurrIndex = 0;// 当前页卡编号
    private static final int TITLE_COUNT = 3;
    private DragController mDragController;
    
    public void init(Context context, View view, DragController dragController) {
        mContext = context;
        mDragController = dragController;
        InitTextView(view);
        InitContainerView(context, view);
    }

    private void InitContainerView(Context context, View view) {
        mContainer = (FrameLayout) view.findViewById(R.id.container);
        mEffectContent = new UnderlinesNoFadeLayout(mContext);
        mListViews.clear();
        mListViews.add(mEffectContent);
        mContainer.removeAllViewsInLayout();
        mContainer.addView(mEffectContent);
    }

    /**
     * 初始化头标
     */

    private void InitTextView(View view) {
        mTitleMap.clear();
        mTitleEffect = (TextView) view.findViewById(R.id.title_effect);
        mTitleWallPaper = (TextView) view.findViewById(R.id.title_wrapper);
        mTitleWidget = (TextView) view.findViewById(R.id.title_widget);
        mTitleMap.put(EFFECT_INDEX, mTitleEffect);
        mTitleMap.put(WALLPAPER_INDEX, mTitleWallPaper);
        mTitleMap.put(WIDGET_INDEX, mTitleWidget);

        mNavIndicator = (ImageView) view.findViewById(R.id.nav_indicator);
        LayoutParams params = mNavIndicator.getLayoutParams();
        int marginLeft = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.much_overmode_function_nav_margin_left);
        int marginRight = mContext.getResources().getDimensionPixelOffset(
                R.dimen.much_overmode_function_nav_margin_right);
        params.width = (getScreenWidth() - marginLeft - marginRight) / TITLE_COUNT;

        mTitleEffect.setOnClickListener(new MyOnClickListener(EFFECT_INDEX));
        mTitleWallPaper.setOnClickListener(new MyOnClickListener(WALLPAPER_INDEX));
        mTitleWidget.setOnClickListener(new MyOnClickListener(WIDGET_INDEX));
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;// 获取分辨率宽度
    }

    private class MyOnClickListener implements OnClickListener {
        private int index = 0;
        public MyOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            if(mCurrIndex == index){
               return; 
            }
            loadContentByIndex(index);
            int titleWidth = mTitleEffect.getWidth();
            Animation animation = new TranslateAnimation(titleWidth * mCurrIndex, titleWidth * index, 0, 0);
            mCurrIndex = index;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            mNavIndicator.startAnimation(animation);
        }

    }

    private void loadContentByIndex(int index) {
        mContainer.removeAllViewsInLayout();
        if(index == EFFECT_INDEX){
            if(mEffectContent == null){
                mEffectContent = new UnderlinesNoFadeLayout(mContext);
            }
            mContainer.addView(mEffectContent);
        }else if(index == WALLPAPER_INDEX){
            if(mWallPaperContent == null){
                mWallPaperContent = new WallpaperLayout(mContext);
            }
            mContainer.addView(mWallPaperContent);
        }else if(index == WIDGET_INDEX){
            if(mAppsCustomizeContent == null){
             // Setup AppsCustomize
                 mAppsCustomizeTabHost = (MuchAppsCustomizeTabHost)LayoutInflater.from(mContext).inflate(R.layout.snail_apps_customize_pane, null);
                mAppsCustomizeContent = (MuchAppsCustomizePagedView)
                        mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
                mAppsCustomizeContent.setup((Launcher)mContext, mDragController);
            }
            mAppsCustomizeTabHost.reset();
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.onPackagesUpdated(
                    LauncherModel.getSortedWidgetsAndShortcuts(mContext));
            }
            mAppsCustomizeTabHost.setContentTypeImmediate(MuchAppsCustomizePagedView.ContentType.Widgets);
            Launcher launcher = (Launcher)mContext;
            launcher.dispatchOnLauncherTransitionPrepare(mAppsCustomizeTabHost, true, false);
            launcher.dispatchOnLauncherTransitionStart(mAppsCustomizeTabHost, true, false);
            launcher.dispatchOnLauncherTransitionEnd(mAppsCustomizeTabHost, true, false);
            mContainer.addView(mAppsCustomizeTabHost);
        }
        setSeletorTitleAlpha(index);
    }

    private void setSeletorTitleAlpha(int selectedIndex) {
        for(Map.Entry<Integer, TextView> entry : mTitleMap.entrySet()){
            if(entry.getKey().equals(selectedIndex)){
                TextView selectedView = entry.getValue();
                selectedView.setAlpha(1f);
            }
            if(entry.getKey().equals(mCurrIndex)){
                TextView unSelectedView = entry.getValue();
                unSelectedView.setAlpha(0.6f);
            }
        }
        
    }

    @Override
    public void onTabReselected(int position) {
        View view = mListViews.get(position);
        if (view != null && view instanceof UnderlinesNoFadeLayout) {
            ((UnderlinesNoFadeLayout) view).setInitPage();
        }
    }
}
