package com.android.launcher.overview.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.android.launcher3.R;

public class WallpaperLayout extends LinearLayout implements InitPage{

    static final String TAG = "WallpaperLayout";
    private PagerAdapter mAdapter;
    private ViewPager mPager;
    private UnderlinePageIndicator mIndicator = null;
    private List<View> mListViews = new ArrayList<View>();
    private static final int PORT_PAGE_ITEM = 3;
    private static final int COLUMN_NUM = 3;
    private Context mContext;
    private ArrayList<WallpaperTileInfo> mWallpapers ;
    private List<BuiltInWallpapersAdapter> mAdapterList = new ArrayList<BuiltInWallpapersAdapter>();
    private LayoutAnimationController mAnimController;

    public WallpaperLayout(Context context) {
        super(context);
        init(context);
    }

    public WallpaperLayout(Context context,LayoutAnimationController animController) {
        super(context);
        mAnimController = animController;
        init(context);
    }

    public WallpaperLayout(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public WallpaperLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public static abstract class WallpaperTileInfo {
        protected View mView;
        protected Drawable mThumb;
        public void setView(View v) {
            mView = v;
        }
        public void onClick(Context a) {}
        public void onSave(Context a) {}
        public void onDelete(Context a) {}
        public boolean isSelectable() { return false; }
        public boolean isNamelessWallpaper() { return false; }
        public void onIndexUpdated(CharSequence label) {
            if (isNamelessWallpaper()) {
                mView.setContentDescription(label);
            }
        }
    }

    public static class PickImageInfo extends WallpaperTileInfo {
        @Override
        public void onClick(Context context) {
            Intent chooseIntent = new Intent(Intent.ACTION_SET_WALLPAPER);  
            context.startActivity(Intent.createChooser(chooseIntent, context.getResources().getString(R.string.wallpaper_select)));
        }
    }

    public void init(Context context) {
        mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.snail_overview_underlines, this, true);
        initData();
        if(mWallpapers != null){
            initListViews();
            initView(view);
        }
    }

    @SuppressLint("ServiceCast")
	public class ResourceWallpaperInfo extends WallpaperTileInfo {
        private Resources mResources;
        private int mResId;

        public ResourceWallpaperInfo(Resources res, int resId, Drawable thumb) {
            mResources = res;
            mResId = resId;
            mThumb = thumb;
        }
        @Override
        public void onClick(Context a) {
            WallpaperManager wpm = (WallpaperManager) mContext.getSystemService(Context.WALLPAPER_SERVICE);
            try {
                wpm.setResource(mResId);
            } catch (IOException e) {
                Log.e("lmq", "setResource-----IOException");
                e.printStackTrace();
            }
        }
        @Override
        public void onSave(Context a) {
        }
        @Override
        public boolean isSelectable() {
            return true;
        }
        @Override
        public boolean isNamelessWallpaper() {
            return true;
        }
    }
    
    private void initData() {
        if(mWallpapers != null){
            mWallpapers.clear();
        }
        // Populate the built-in wallpapers
        mWallpapers = findBundledWallpapers();
        mWallpapers.add(0, new PickImageInfo());
    }

    private void initView(View view) {
        mAdapter = new MyPageAdater(mListViews);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mIndicator = (UnderlinePageIndicator) view.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setFades(mWallpapers.size() <= PORT_PAGE_ITEM ? true : false);
    }

    private void initListViews() {
        mAdapterList.clear();
        mListViews.clear();
        
        int pageCount = getPageCount();
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            View layout = createView(pageIndex);
            if (layout != null) {
                mListViews.add(layout);
            }
        }
    }

    private View createView(final int pageIndex) {
        if (pageIndex > getPageCount() - 1) {
            return null;
        }
        int startIndex = pageIndex * getPageItem();
        int endIndex = Math.min((pageIndex + 1) * getPageItem() - 1, mWallpapers.size() - 1);
        GridView grid = (GridView) LayoutInflater.from(mContext).inflate(R.layout.snail_overview_grid, null);
        grid.setNumColumns(COLUMN_NUM);
        grid.setTag(pageIndex);
        // 根据索引返回数据
        List<WallpaperTileInfo> items = mWallpapers.subList(startIndex, endIndex + 1);
        BuiltInWallpapersAdapter ia = new BuiltInWallpapersAdapter(mContext, items);
        grid.setAdapter(ia);
        mAdapterList.add(ia);
        return grid;
    }

    private int getPageItem() {
        return PORT_PAGE_ITEM;
    }
    
    private int getPageCount() {
        if (mWallpapers != null && mWallpapers.size() != 0) {
            return mWallpapers.size() % getPageItem() == 0 ? mWallpapers.size() / getPageItem() : mWallpapers.size()
                    / getPageItem() + 1;
        }
        return 0;
    }

    class MyPageAdater extends PagerAdapter {

        private List<View> mListViews;

        public MyPageAdater(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    private ArrayList<WallpaperTileInfo> findBundledWallpapers() {
        ArrayList<WallpaperTileInfo> bundledWallpapers =
                new ArrayList<WallpaperTileInfo>(24);

        Pair<ApplicationInfo, Integer> r = getWallpaperArrayResourceId();
        if (r != null) {
            try {
                Resources wallpaperRes = mContext.getPackageManager().getResourcesForApplication(r.first);
                bundledWallpapers = addWallpapers(wallpaperRes, r.first.packageName, r.second);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        // Add an entry for the default wallpaper (stored in system resources)
        ResourceWallpaperInfo defaultWallpaperInfo = getDefaultWallpaperInfo();
        if (defaultWallpaperInfo != null) {
            bundledWallpapers.add(0, defaultWallpaperInfo);
        }
        return bundledWallpapers;
    }

    public Pair<ApplicationInfo, Integer> getWallpaperArrayResourceId() {
        // Context.getPackageName() may return the "original" package name,
        // com.android.launcher3; Resources needs the real package name,
        // com.android.launcher3. So we ask Resources for what it thinks the
        // package name should be.
        final String packageName = getResources().getResourcePackageName(R.array.wallpapers);
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(packageName, 0);
            return new Pair<ApplicationInfo, Integer>(info, R.array.wallpapers);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private ArrayList<WallpaperTileInfo> addWallpapers(
            Resources res, String packageName, int listResId) {
        ArrayList<WallpaperTileInfo> bundledWallpapers =
                new ArrayList<WallpaperTileInfo>(24);
        final String[] extras = res.getStringArray(listResId);
        for (String extra : extras) {
            int resId = res.getIdentifier(extra, "drawable", packageName);
            if (resId != 0) {
                final int thumbRes = res.getIdentifier(extra + "_small", "drawable", packageName);

                if (thumbRes != 0) {
                    ResourceWallpaperInfo wallpaperInfo =
                            new ResourceWallpaperInfo(res, resId, res.getDrawable(thumbRes));
                    bundledWallpapers.add(wallpaperInfo);
                    // Log.d(TAG, "add: [" + packageName + "]: " + extra + " (" + res + ")");
                }
            } else {
                Log.e(TAG, "Couldn't find wallpaper " + extra);
            }
        }
        return bundledWallpapers;
    }

    private ResourceWallpaperInfo getDefaultWallpaperInfo() {
        Resources sysRes = Resources.getSystem();
        int resId = sysRes.getIdentifier("default_wallpaper", "drawable", "android");

        File defaultThumbFile = new File(mContext.getFilesDir(), "default_thumb.jpg");
        Bitmap thumb = null;
        boolean defaultWallpaperExists = false;
        if (defaultThumbFile.exists()) {
            thumb = BitmapFactory.decodeFile(defaultThumbFile.getAbsolutePath());
            defaultWallpaperExists = true;
        }
        if (defaultWallpaperExists) {
            return new ResourceWallpaperInfo(sysRes, resId, new BitmapDrawable(thumb));
        }
        return null;
    }

    private class BuiltInWallpapersAdapter extends BaseAdapter implements ListAdapter {
        private LayoutInflater mLayoutInflater;
        private List<WallpaperTileInfo> mPageWallpapers;

        BuiltInWallpapersAdapter(Context context, List<WallpaperTileInfo> wallpapers) {
            mLayoutInflater = LayoutInflater.from(context);
            mPageWallpapers = wallpapers;
        }

        public int getCount() {
            if(mPageWallpapers == null){
                return 0;
            }
            return mPageWallpapers.size();
        }

        public WallpaperTileInfo getItem(int position) {
            if(mPageWallpapers == null){
                return null;
            }
            return mPageWallpapers.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Drawable thumb = mPageWallpapers.get(position).mThumb;
            if (thumb == null) {
                Log.e(TAG, "Error decoding thumbnail for wallpaper #" + position);
            }
            View  view = createImageTileView(mLayoutInflater, position, convertView, parent, thumb);
            if(mPageWallpapers != null){
                view.setTag(mPageWallpapers.get(position));
            }
            if(mAnimController != null && parent != null && (Integer)parent.getTag() == 0){
                parent.setLayoutAnimation(mAnimController);
            }
            return view;
        }
        
        public  View createImageTileView(LayoutInflater layoutInflater, int position,
                View convertView, ViewGroup parent, Drawable thumb) {
            View view;
            if (convertView == null) {
                WallpaperTileInfo info = mPageWallpapers.get(0);
                if (position == 0 && info instanceof PickImageInfo) {
                    view = layoutInflater.inflate(R.layout.snail_overview_wallpaper_picker_local, parent, false);
                } else {
                    view = layoutInflater.inflate(R.layout.wallpaper_picker_item, parent, false);
                }
            } else {
                view = convertView;
            }

            view.setOnClickListener(new OnClickListener() {
                
                @SuppressLint("ServiceCast")
                @Override
                public void onClick(View v) {
                    WallpaperTileInfo info = (WallpaperTileInfo)v.getTag();
                    if(info != null){
                        info.onClick(mContext);
                    }
                }
            });
            setWallpaperItemPaddingToZero((FrameLayout) view);
            
            if(position == 0){
                WallpaperTileInfo info = mPageWallpapers.get(0);
                if(info instanceof PickImageInfo){
                    return view;
                }
            }
            ImageView image = (ImageView) view.findViewById(R.id.wallpaper_image);
            if (thumb != null) {
                image.setImageDrawable(thumb);
                thumb.setDither(true);
            }
            return view;
        }
    }
    

    static void setWallpaperItemPaddingToZero(FrameLayout frameLayout) {
        frameLayout.setPadding(0, 0, 0, 0);
        frameLayout.setForeground(new ZeroPaddingDrawable(frameLayout.getForeground()));
    }
    
    static class ZeroPaddingDrawable extends LevelListDrawable {
        public ZeroPaddingDrawable(Drawable d) {
            super();
            addLevel(0, 0, d);
            setLevel(0);
        }

        @Override
        public boolean getPadding(Rect padding) {
            padding.set(0, 0, 0, 0);
            return true;
        }
    }

    @Override
    public void initPage(int pageIndex) {
        if(pageIndex == 0 && !mListViews.isEmpty()){
            mPager.setCurrentItem(pageIndex);
        }
    }

}