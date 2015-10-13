package com.android.launcher.overview.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher.overview.ui.TabPageIndicator.OnTabReselectedListener;
import com.android.launcher3.R;

public class TabsFragment extends Fragment implements OnTabReselectedListener {
    private static final String[] CONTENT = new String[] { "壁纸", "小部件", "桌面特效" };
    FragmentPagerAdapter mPageAdapter = null;
    private List<Fragment> mFragments = new ArrayList<Fragment>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.snail_overview_tabs, null);
        initFragmentList();
        initView(view);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initView(View view) {
        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(mPageAdapter);

        TabPageIndicator indicator = (TabPageIndicator) view.findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setOnTabReselectedListener(this);
    }

    private void initFragmentList() {
        mFragments.clear();
//        mFragments.add(new UnderlinesNoFadeFragment());
        // mFragments.add(new UnderlinesNoFadeFragment());
        // mFragments.add(new UnderlinesNoFadeFragment());
        mPageAdapter = new OverViewAdapter(getFragmentManager(), mFragments);
    }

    class OverViewAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public OverViewAdapter(FragmentManager fm) {
            super(fm);
        }

        public OverViewAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("lmq", "getItem position = " + position);
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

    @Override
    public void onTabReselected(int position) {
        Log.d("lmq", "onTabReselected position = " + position);
        Fragment fragment = mFragments.get(position);
//        if (fragment != null && fragment instanceof UnderlinesNoFadeFragment) {
////            ((UnderlinesNoFadeFragment) fragment).setInitPage();
//        }
    }
}
