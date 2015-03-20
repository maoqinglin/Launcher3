package com.android.launcher3.much;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.CellLayout;
import com.android.launcher3.R;
import com.android.launcher3.much.ui.widget.autoscrollviewpager.AutoScrollViewPager;
import com.android.launcher3.much.ui.widget.autoscrollviewpager.ImagePagerAdapter;
import com.android.launcher3.much.ui.widget.autoscrollviewpager.ImagePagerAdapter.OnChildClickListener;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class CustomPage extends CellLayout implements OnClickListener {

	private static final String ACTION_FREE_STORE = "com.ireadygo.app.freestore.STORE_DETAIL";
	private static final String EXTRA_OUTSIDE_TAG = "EXTRA_OUTSIDE_TAG";
	private static final String ACTION_BANNER_UPDATE = "com.ireadygo.app.gamelauncher.ACTION_INFO_BANNER_CHANGE";
	private static final String ACTION_BANNER_CLICK = "com.ireadygo.app.gamelauncher.ACTION_BANNER_CLICK";
	private static final String EXTRA_POSITION = "EXTRA_POSITION";
	private static final String EXTRA_INDEX = "EXTRA_INDEX";
	private static final String ACTION_GET_BANNER = "com.ireadygo.app.gamelauncher.ACTION_GET_BANNER";
	private static final int BANNER_LEFT = 1;
	private static final int BANNER_RIGHT = 2;
	private static final String URL_DIVIDER = ",";
	private static final String EXTRA_AUTO_URL = "EXTRA_AUTO_URL";
	private static final String EXTRA_STATIC_URL = "EXTRA_STATIC_URL";
	private static final long BANNER_SCROLL_DELAY = 8 * 1000;
	private AutoScrollViewPager mAutoScrollViewPager;
	private ImageView mAdImageRT;
	private ImageView mAdImageRB;
	private ViewGroup mDotsLayout;
	private List<String> mAdLeftBannerUrlList = new ArrayList<String>();
	private List<String> mAdRightBannerUrlList = new ArrayList<String>();
	private int mCurrentSelectedIndex = 0;
	private ArrayList<ImageView> mDotViewList = new ArrayList<ImageView>();
	private ImagePagerAdapter mImagePagerAdapter;
	private ImageLoader mImageLoader;
	private Drawable mDefaultDrawable;
	private Drawable mEmptyDrawable;

	public CustomPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomPage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomPage(Context context) {
		super(context);
	}

	@Override
	protected int[] createArea(int pixelX, int pixelY, int minSpanX, int minSpanY, int spanX, int spanY, View dragView,
			int[] result, int[] resultSpan, int mode) {
		return new int[] { -1, -1 };
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.store_recommend:
			jumpToFreeStoreMenu(Destination.STORE_RECOMMEND.toString());
			break;
		case R.id.store_category:
			jumpToFreeStoreMenu(Destination.STORE_CATEGORY.toString());
			break;
		case R.id.store_collection:
			jumpToFreeStoreMenu(Destination.STORE_COLLECTION.toString());
			break;
		case R.id.store_search:
			jumpToFreeStoreMenu(Destination.STORE_SEARCH.toString());
			break;
		case R.id.store_manager:
			jumpToFreeStoreMenu(Destination.STORE_GAME_MANAGE.toString());
			break;
		case R.id.store_user:
			jumpToFreeStoreMenu(Destination.STORE_SETTINGS.toString());
			break;
		case R.id.store_ad_right_1:
			clickFreeStoreBanner(BANNER_RIGHT, 0);
			break;
		case R.id.store_ad_right_2:
			clickFreeStoreBanner(BANNER_RIGHT, 1);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mImageLoader = ImageLoader.getInstance();
		configImageLoader();
		mDefaultDrawable = getContext().getResources().getDrawable(R.drawable.store_banner_default);
		mEmptyDrawable = getContext().getResources().getDrawable(R.drawable.store_ad_large);
		initView();
	}

	private void initView() {
		View recommendBtn = (View) findViewById(R.id.store_recommend);
		initItem(recommendBtn, R.string.much_store_recommend_title, R.drawable.store_recommend_selector);

		View cotegoryBtn = (View) findViewById(R.id.store_category);
		initItem(cotegoryBtn, R.string.much_store_category_title, R.drawable.store_category_selector);

		View collectionBtn = (View) findViewById(R.id.store_collection);
		initItem(collectionBtn, R.string.much_store_collection_title, R.drawable.store_collection_selector);

		View searchBtn = (View) findViewById(R.id.store_search);
		initItem(searchBtn, R.string.much_store_search_title, R.drawable.store_search_selector);

		View managerBtn = (View) findViewById(R.id.store_manager);
		initItem(managerBtn, R.string.much_store_manage_title, R.drawable.store_manage_selector);

		View userBtn = (View) findViewById(R.id.store_user);
		initItem(userBtn, R.string.much_store_user_title, R.drawable.store_user_selector);

		mDotsLayout = (ViewGroup) findViewById(R.id.storeRecommendDots);
		mAdImageRT = (ImageView) findViewById(R.id.store_ad_right_1);
		mAdImageRB = (ImageView) findViewById(R.id.store_ad_right_2);
		mAdImageRT.setOnClickListener(this);
		mAdImageRB.setOnClickListener(this);
	}

	private void initItem(View item, int titleId, int iconId) {
		if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
			android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams)item.getLayoutParams();
			params.rightMargin = 64;
			item.setLayoutParams(params);
		}
		item.setOnClickListener(this);
		TextView title = (TextView) item.findViewById(R.id.store_title);
		title.setText(titleId);
		ImageView icon = (ImageView) item.findViewById(R.id.store_icon);
		icon.setImageResource(iconId);
	}

	private void updateAutoViewPaper() {
		if (!mAdLeftBannerUrlList.isEmpty()) {
			mAutoScrollViewPager = (AutoScrollViewPager) findViewById(R.id.storeRecommendViewPager);
			mImagePagerAdapter = new ImagePagerAdapter(getContext(), mAdLeftBannerUrlList, mImageLoader);
			mImagePagerAdapter.setOnClickListener(mLeftBannerOnClickListener);
			mImagePagerAdapter.setInfiniteLoop(true);
			mAutoScrollViewPager.setOnPageChangeListener(mAutoScrollListener);
			mAutoScrollViewPager.setAdapter(mImagePagerAdapter);
			mAutoScrollViewPager.setCurrentItem(mAdLeftBannerUrlList.size() * 10000);
			mAutoScrollViewPager.setBorderAnimation(true);
			mAutoScrollViewPager.setScrollDurationFactor(10.0);
			mAutoScrollViewPager.setInterval(BANNER_SCROLL_DELAY);
			mAutoScrollViewPager.startAutoScroll();
		}
	}

	private void initBroadcast() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_BANNER_UPDATE);
		getContext().registerReceiver(mReceiver, intentFilter);
	}

	private void updateLeftAdBanner() {
		updateAutoViewPaper();
		if (mImagePagerAdapter != null) {
			mImagePagerAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		initBroadcast();
		fetchBannerImage();
	};

	@Override
	protected void onDetachedFromWindow() {
		getContext().unregisterReceiver(mReceiver);
		mImageLoader.clearMemoryCache();
		if (mAutoScrollViewPager != null) {
			mAutoScrollViewPager.stopAutoScroll();
		}
		super.onDetachedFromWindow();
	}

	private void updateRightAdBanner() {
		if (mAdRightBannerUrlList.size() >= 2) {
			mImageLoader.displayImage(mAdRightBannerUrlList.get(0), mAdImageRT, getDisplayImageOptions());
			mImageLoader.displayImage(mAdRightBannerUrlList.get(1), mAdImageRB, getDisplayImageOptions());
		}
	}

	private void updateDotLayout() {
		if (!mAdLeftBannerUrlList.isEmpty()) {
			mDotViewList.clear();
			mDotsLayout.removeAllViews();
			int count = mAdLeftBannerUrlList.size();
			for (int i = 0; i < count; i++) {
				ImageView dot = new ImageView(getContext());
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
			clickFreeStoreBanner(BANNER_LEFT, index);
		}
	};

	private void jumpToFreeStoreMenu(String extra) {
		Intent intent = new Intent(ACTION_FREE_STORE);
		intent.putExtra(EXTRA_OUTSIDE_TAG, extra);
		try {
			getContext().startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// do nothing
		}
	}

	private void clickFreeStoreBanner(int position, int index) {
		Intent intent = new Intent(ACTION_BANNER_CLICK);
		intent.putExtra(EXTRA_POSITION, position);
		intent.putExtra(EXTRA_INDEX, index);
		getContext().sendBroadcast(intent);
	}

	private OnPageChangeListener mAutoScrollListener = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int index) {
			if (mAdLeftBannerUrlList.size() == 0) {
				return;
			}
			if (mDotViewList == null || mDotViewList.size() == 0) {
				return;
			}
			int actualIndex = index % mAdLeftBannerUrlList.size();
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
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_BANNER_UPDATE.equals(action)) {
				String autoUrl = intent.getStringExtra(EXTRA_AUTO_URL);
				String staticUrl = intent.getStringExtra(EXTRA_STATIC_URL);
				decodeAutoBannerUrl(autoUrl);
				decodeStaticBannerUrl(staticUrl);
				updateData();
			}
		}
	};

	private void updateData() {
		updateLeftAdBanner();
		updateDotLayout();
		updateRightAdBanner();
		if(mAdLeftBannerUrlList == null || mAdLeftBannerUrlList.isEmpty()){
			mAutoScrollViewPager.setVisibility(View.INVISIBLE);
			mDotsLayout.setVisibility(View.INVISIBLE);
		}else{
			mAutoScrollViewPager.setVisibility(View.VISIBLE);
			mDotsLayout.setVisibility(View.VISIBLE);
		}
	}

	private void decodeAutoBannerUrl(String url) {
		mAdLeftBannerUrlList.clear();
		if (TextUtils.isEmpty(url)) {
			mAdLeftBannerUrlList.add("");
			return;
		}
		String[] urls = url.split(URL_DIVIDER);
		for (String bannerUrl : urls) {
			mAdLeftBannerUrlList.add(bannerUrl);
		}
	}

	private void decodeStaticBannerUrl(String url) {
		mAdRightBannerUrlList.clear();
		if (TextUtils.isEmpty(url)) {
			mAdRightBannerUrlList.add("");
			return;
		}
		String[] urls = url.split(URL_DIVIDER);
		for (String bannerUrl : urls) {
			mAdRightBannerUrlList.add(bannerUrl);
		}
	}

	private void fetchBannerImage() {
		Intent intent = new Intent(ACTION_GET_BANNER);
		getContext().sendBroadcast(intent);
	}

	private void configImageLoader() {
		ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
		DisplayImageOptions options = getDisplayImageOptions();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
				.memoryCacheSize(am.getMemoryClass() * 1024 * 1024 / 8).threadPoolSize(5)
				.denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO).threadPriority(Thread.MIN_PRIORITY)
				.defaultDisplayImageOptions(options).build();
		mImageLoader.init(config);
	}

	public DisplayImageOptions getDisplayImageOptions() {
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565).showImageOnFail(mDefaultDrawable)
				.showImageOnLoading(mDefaultDrawable).showImageForEmptyUri(mEmptyDrawable).build();
		return options;
	}

	public enum Destination {
		GAME_DETAIL, STORE_RECOMMEND, STORE_CATEGORY, STORE_COLLECTION, STORE_SEARCH, STORE_GAME_MANAGE, //
		STORE_SETTINGS, COLLECTION_DETAIL, CATEGORY_DETAIL, ACCOUNT_WEALTH, ACCOUNT_PERSONAL, ACCOUNT_RECHARGE, ACCOUNT_FREECARD
	}
}
