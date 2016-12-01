package com.android.launcher.overview.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.much.MuchConfig;

public class UnderlinesNoFadeLayout extends LinearLayout implements InitPage{

    
    private PagerAdapter mAdapter;
    private ViewPager mPager;
    private UnderlinePageIndicator mIndicator = null;
    private List<View> mListViews = new ArrayList<View>();
    private List<EffectItem> mEffectList = new ArrayList<EffectItem>();
    private List<EffectAdapter> mEffectAdapterList = new ArrayList<EffectAdapter>();
    private static final int PORT_PAGE_ITEM = 6;
    private static final int LANDSCAPE_PAGE_ITEM = 5;
    private Context mContext;
    private LayoutAnimationController mAnimController;
    private boolean mIsFirstDone = true;

    public UnderlinesNoFadeLayout(Context context, LayoutAnimationController animController) {
        super(context);
        mAnimController = animController;
        init(context);
    }

    public UnderlinesNoFadeLayout(Context context) {
        super(context);
        init(context);
    }

    public UnderlinesNoFadeLayout(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public UnderlinesNoFadeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.snail_overview_underlines, this, true);
        initData();
        initListViews();
        initView(view);
    }

    private void initData() {
        mEffectList.clear();
        TypedArray names = getResources().obtainTypedArray(R.array.effect_names);
        TypedArray images = getResources().obtainTypedArray(R.array.effect_icons);
        int selectedIndex = getEffectIndex();
        int len = images.length();
        for (int i = 0; i < len; i++) {
            String name = names.getString(i);
            Drawable iconRes = images.getDrawable(i);
            boolean selected = false;
            if(i == selectedIndex){
                selected = true;
            }
            EffectItem item = new EffectItem(name, iconRes, selected, i);
            mEffectList.add(item);
        }
        names.recycle();
        images.recycle();
    }

    private void initView(View view) {
        mAdapter = new MyPageAdater(mListViews);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mIndicator = (UnderlinePageIndicator) view.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setFades(false);
    }

    private void initListViews() {
        mEffectAdapterList.clear();
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
        int endIndex = Math.min((pageIndex + 1) * getPageItem() - 1, mEffectList.size() - 1);
        GridView grid = (GridView) LayoutInflater.from(mContext).inflate(R.layout.snail_overview_grid, null);
        // 根据索引返回数据
        List<EffectItem> items = mEffectList.subList(startIndex, endIndex + 1);
        EffectAdapter adapter = new EffectAdapter(items);
        grid.setAdapter(adapter);
        grid.setTag(pageIndex);
        mEffectAdapterList.add(adapter);
        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (EffectItem item : mEffectList) {
                    item.isSelected = false;
                }
                Holder holder = (Holder) view.getTag();
                holder.item.isSelected = true;
                saveEffectIndex(holder.item.index);
                mAdapter.notifyDataSetChanged();
                for (EffectAdapter effectAdapter : mEffectAdapterList) {
                    effectAdapter.notifyDataSetChanged();
                }
            }
        });
        return grid;
    }

    private int getPageItem() {
        return LauncherAppState.isScreenLandscape(mContext) ? LANDSCAPE_PAGE_ITEM : PORT_PAGE_ITEM;
    }
    
    private int getPageCount() {
        if (mEffectList.size() != 0) {
            return mEffectList.size() % getPageItem() == 0 ? mEffectList.size() / getPageItem() : mEffectList.size()
                    / getPageItem() + 1;
        }
        return 0;
    }

    class EffectAdapter extends BaseAdapter {
        List<EffectItem> effects = null;

        public EffectAdapter(List<EffectItem> effects) {
            this.effects = effects;
        }

        @Override
        public int getCount() {
            if (effects != null) {
                return effects.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return effects.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.snail_overview_effect_item, null);
                holder = new Holder();
                holder.effectName = (TextView) convertView.findViewById(R.id.name);
                holder.effectIcon = (ImageView) convertView.findViewById(R.id.icon);
                holder.effectSelected = (ImageView) convertView.findViewById(R.id.selected);
                convertView.setTag(holder);
                if(mAnimController != null && parent != null && (Integer)parent.getTag() == 0 && mIsFirstDone){
                    parent.setLayoutAnimation(mAnimController);
                    mIsFirstDone = false;
                }
            }
            holder = (Holder) convertView.getTag();
            EffectItem item = effects.get(position);
            holder.item = item;
            holder.effectName.setText(item.name);
            holder.effectIcon.setBackground(item.iconRes);
            holder.effectSelected.setVisibility(item.isSelected ? View.VISIBLE : View.INVISIBLE);
            return convertView;
        }
    }

    static class Holder {
        public TextView effectName;
        public ImageView effectIcon;
        public ImageView effectSelected;
        public EffectItem item;
    }

    static class EffectItem {
        String name;
        Drawable iconRes;
        boolean isSelected;
        int index;

        public EffectItem(String name, Drawable iconRes, boolean isSelected, int index) {
            this.name = name;
            this.iconRes = iconRes;
            this.isSelected = isSelected;
            this.index = index;
        }
    }

    static class MyPageAdater extends PagerAdapter {

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
            container.addView(mListViews.get(position),0);
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

    private int getEffectIndex(){
        SharedPreferences preferences = mContext.getSharedPreferences(MuchConfig.LAUNCHER_PREFS, Context.MODE_PRIVATE);
        return preferences.getInt(MuchConfig.SCREEN_EFFECT_PREFS, 0);
    }

    private void saveEffectIndex(int index){
        Editor editor = mContext.getSharedPreferences(MuchConfig.LAUNCHER_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(MuchConfig.SCREEN_EFFECT_PREFS,index).commit();
        
    }

    @Override
    public void initPage(int pageIndex) {
        if(pageIndex == 0 && !mListViews.isEmpty()){
            final ViewGroup parent = (ViewGroup)mPager.getChildAt(0);
            mPager.setCurrentItem(pageIndex);
            if(parent != null){
                if(mAnimController != null && parent != null && (Integer)parent.getTag() == 0){
                    parent.setLayoutAnimation(mAnimController);
                }
            }
        }
    }
}