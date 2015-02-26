package com.android.launcher3.much;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.launcher3.CellLayout;
import com.android.launcher3.R;

public class CustomPage extends CellLayout implements OnClickListener {

	private static final String ACTION_FREE_STORE = "com.ireadygo.app.freestore.STORE_DETAIL";
	private static final String EXTRA_OUTSIDE_TAG = "EXTRA_OUTSIDE_TAG";
	private Context mContext;

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
		recommendBtn.setOnClickListener(this);
		cotegoryBtn.setOnClickListener(this);
		collectionBtn.setOnClickListener(this);
		searchBtn.setOnClickListener(this);
		managerBtn.setOnClickListener(this);
	}

	private void jumpToFreeStoreMenu(String extra) {
		Intent intent = new Intent(ACTION_FREE_STORE);
		intent.putExtra(EXTRA_OUTSIDE_TAG, extra);
		mContext.startActivity(intent);
	}

	private void clickFreeStoreBanner(int position,int count) {
		
	}

	public enum Destination {
		GAME_DETAIL, STORE_RECOMMEND, STORE_CATEGORY, STORE_COLLECTION, STORE_SEARCH, STORE_GAME_MANAGE, //
		STORE_SETTINGS, COLLECTION_DETAIL, CATEGORY_DETAIL,ACCOUNT_WEALTH, ACCOUNT_PERSONAL, ACCOUNT_RECHARGE, ACCOUNT_FREECARD
	}
}
