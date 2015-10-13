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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.much.MuchConfig;

public class UnderlinesNoFadeLayout extends LinearLayout {

    
    private PagerAdapter mAdapter;
    private ViewPager mPager;
    private UnderlinePageIndicator mIndicator = null;
    private List<View> mListViews = new ArrayList<View>();
    private List<EffectItem> mEffectList = new ArrayList<EffectItem>();
    private List<EffectAdapter> mEffectAdapterList = new ArrayList<UnderlinesNoFadeLayout.EffectAdapter>();
    private static final int PAGE_ITEM = 6;
    private Context mContext;

    public UnderlinesNoFadeLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public UnderlinesNoFadeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public UnderlinesNoFadeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public void init() {
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
        int startIndex = pageIndex * PAGE_ITEM;
        int endIndex = Math.min((pageIndex + 1) * PAGE_ITEM - 1, mEffectList.size() - 1);
        GridView grid = (GridView) LayoutInflater.from(mContext).inflate(R.layout.snail_overview_grid, null);
        // 根据索引返回数据
        List<EffectItem> items = mEffectList.subList(startIndex, endIndex + 1);
        EffectAdapter adapter = new EffectAdapter(items);
        grid.setAdapter(adapter);
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

    private int getPageCount() {
        if (mEffectList.size() != 0) {
            return mEffectList.size() % PAGE_ITEM == 0 ? mEffectList.size() / PAGE_ITEM : mEffectList.size()
                    / PAGE_ITEM + 1;
        }
        return 0;
    }

    class EffectAdapter extends BaseAdapter {
        List<EffectItem> effects = null;

        public EffectAdapter(List<EffectItem> effects) {
            Log.d("lmq", "effects.size = "+effects.size());
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

    public void setInitPage() {
        if (mIndicator != null && mPager != null) {
            mIndicator.setViewPager(mPager, 0);
        }
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

    private int getEffectIndex(){
        SharedPreferences preferences = mContext.getSharedPreferences(MuchConfig.LAUNCHER_PREFS, Context.MODE_PRIVATE);
        return preferences.getInt(MuchConfig.SCREEN_EFFECT_PREFS, 0);
    }

    private void saveEffectIndex(int index){
        Editor editor = mContext.getSharedPreferences(MuchConfig.LAUNCHER_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(MuchConfig.SCREEN_EFFECT_PREFS,index).commit();
        
    }
}