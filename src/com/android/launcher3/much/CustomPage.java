package com.android.launcher3.much;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.android.launcher3.CellLayout;
import com.android.launcher3.R;
import com.android.launcher3.much.ui.widget.autoscrollviewpager.AutoScrollViewPager;
import com.android.launcher3.much.ui.widget.autoscrollviewpager.ImagePagerAdapter;
import com.android.launcher3.much.ui.widget.autoscrollviewpager.ImagePagerAdapter.OnChildClickListener;

public class CustomPage extends CellLayout implements OnClickListener {

	private static final String ACTION_FREE_STORE = "com.ireadygo.app.freestore.STORE_DETAIL";
	private static final String EXTRA_OUTSIDE_TAG = "EXTRA_OUTSIDE_TAG";
	private Context mContext;
	private AutoScrollViewPager mAutoScrollViewPager;
	private ImageView mAdImageRT;
	private ImageView mAdImageRB;
	private ViewGroup mDotsLayout;
	private List<BitmapDrawable> mAdLeftBannerList = new ArrayList<BitmapDrawable>();
	private List<BitmapDrawable> mAdRightBannerList = new ArrayList<BitmapDrawable>();
	private int mCurrentSelectedIndex = 0;
	private ArrayList<ImageView> mDotViewList = new ArrayList<ImageView>();
	private ImagePagerAdapter mImagePagerAdapter;


	public CustomPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public CustomPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public CustomPage(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected int[] createArea(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX, int spanY, View dragView,
			int[] result, int[] resultSpan, int mode) {
		return new int[] { -1, -1 };
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.recommend_btn:
			jumpToFreeStoreMenu(Destination.STORE_RECOMMEND.toString());
			break;
		case R.id.category_btn:
			jumpToFreeStoreMenu(Destination.STORE_CATEGORY.toString());
			break;
		case R.id.collection_btn:
			jumpToFreeStoreMenu(Destination.STORE_COLLECTION.toString());
			break;
		case R.id.search_btn:
			jumpToFreeStoreMenu(Destination.STORE_SEARCH.toString());
			break;
		case R.id.manager_btn:
			jumpToFreeStoreMenu(Destination.STORE_GAME_MANAGE.toString());
			break;
		case R.id.settings_btn:
			jumpToFreeStoreMenu(Destination.STORE_SETTINGS.toString());
			break;

		default:
			break;
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		View recommendBtn = (View)findViewById(R.id.recommend_btn);
		View cotegoryBtn = (View)findViewById(R.id.category_btn);
		View collectionBtn = (View)findViewById(R.id.collection_btn);
		View searchBtn = (View)findViewById(R.id.search_btn);
		View managerBtn = (View)findViewById(R.id.manager_btn);
		View settingsBtn = (View)findViewById(R.id.settings_btn);
		recommendBtn.setOnClickListener(this);
		cotegoryBtn.setOnClickListener(this);
		collectionBtn.setOnClickListener(this);
		searchBtn.setOnClickListener(this);
		managerBtn.setOnClickListener(this);
		settingsBtn.setOnClickListener(this);

		initViewPager();
		addTestData();
		updateLeftAdBanner();
		updateRightAdBanner();
		updateDotLayout();
		initListener();
	}

	private void initViewPager() {
		mAutoScrollViewPager = (AutoScrollViewPager)findViewById(R.id.storeRecommendViewPager);
		mDotsLayout = (ViewGroup)findViewById(R.id.storeRecommendDots);
		mAdImageRT = (ImageView)findViewById(R.id.store_ad_right_1);
		mAdImageRB = (ImageView)findViewById(R.id.store_ad_right_2);
		mAdImageRT.setOnClickListener(this);
		mAdImageRB.setOnClickListener(this);
	}

	private void updateLeftAdBanner() {
		if (!mAdLeftBannerList.isEmpty()) {
			mImagePagerAdapter = new ImagePagerAdapter(mContext, mAdLeftBannerList);
			mImagePagerAdapter.setOnClickListener(mLeftBannerOnClickListener);
			mImagePagerAdapter.setInfiniteLoop(true);
			mAutoScrollViewPager.setAdapter(mImagePagerAdapter);
			mAutoScrollViewPager.setCurrentItem(mAdLeftBannerList.size() * 10000);
			mAutoScrollViewPager.setBorderAnimation(true);
			mAutoScrollViewPager.setScrollDurationFactor(4.0);
			mAutoScrollViewPager.startAutoScroll();
		}
	}

	private void addTestData() {
		mAdLeftBannerList.add((BitmapDrawable)mContext.getResources().getDrawable(R.drawable.store_ad_large));
		mAdLeftBannerList.add((BitmapDrawable)mContext.getResources().getDrawable(R.drawable.store_ad_large));
		mAdLeftBannerList.add((BitmapDrawable)mContext.getResources().getDrawable(R.drawable.store_ad_large));
		mAdRightBannerList.add((BitmapDrawable)mContext.getResources().getDrawable(R.drawable.store_ad_small1));
		mAdRightBannerList.add((BitmapDrawable)mContext.getResources().getDrawable(R.drawable.store_ad_small2));
	}

	private void updateRightAdBanner() {
		if (mAdRightBannerList.size() == 2) {
			mAdImageRT.setImageDrawable(mAdRightBannerList.get(0));
			mAdImageRB.setImageDrawable(mAdRightBannerList.get(1));
		}
	}

	private void updateDotLayout() {
		if (!mAdLeftBannerList.isEmpty()) {
			mDotViewList.clear();
			mDotsLayout.removeAllViews();
			int count = mAdLeftBannerList.size();
			for (int i = 0; i < count; i++) {
				ImageView dot = new ImageView(mContext);
				dot.setImageResource(R.drawable.page_indicator_normal);
				dot.setPadding(8, 8, 8, 8);
				mDotViewList.add(dot);
				mDotsLayout.addView(dot);
			}
			mDotViewList.get(0).setImageResource(R.drawable.page_indicator_selected);
		}
	}

	private OnChildClickListener mLeftBannerOnClickListener = new OnChildClickListener() {
		
		@Override
		public void onItemChildViewClick(View view, int index) {
		}
	};

	private void jumpToFreeStoreMenu(String extra) {
		Intent intent = new Intent(ACTION_FREE_STORE);
		intent.putExtra(EXTRA_OUTSIDE_TAG, extra);
		mContext.startActivity(intent);
	}

	private void clickFreeStoreBanner(int position,int count) {
		
	}



	private void initListener(){
		mAutoScrollViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int index) {
				if (mAdLeftBannerList.size() == 0) {
					return;
				}
				if (mDotViewList == null || mDotViewList.size() == 0) {
					return;
				}
				int actualIndex = index % mAdLeftBannerList.size();
				mDotViewList.get(mCurrentSelectedIndex).setImageResource(R.drawable.page_indicator_normal);
				mDotViewList.get(actualIndex).setImageResource(R.drawable.page_indicator_selected);
				mCurrentSelectedIndex = actualIndex;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	public enum Destination {
		GAME_DETAIL, STORE_RECOMMEND, STORE_CATEGORY, STORE_COLLECTION, STORE_SEARCH, STORE_GAME_MANAGE, //
		STORE_SETTINGS, COLLECTION_DETAIL, CATEGORY_DETAIL,ACCOUNT_WEALTH, ACCOUNT_PERSONAL, ACCOUNT_RECHARGE, ACCOUNT_FREECARD
	}
}
