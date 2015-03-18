package com.android.launcher3.much;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.MuchFolderPageView.FolderViewPageListener;
import com.android.launcher3.R;

public class MuchFolderPagedViewIndicator extends PagedViewIndicator implements FolderViewPageListener {

	public MuchFolderPagedViewIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mWhilteDot = BitmapFactory.decodeResource(getResources(), R.drawable.page_indicator_selected);
		mBlackDot = BitmapFactory.decodeResource(getResources(), R.drawable.page_indicator_normal);
		maxWidth = Math.max(mWhilteDot.getWidth(), mBlackDot.getWidth());
		maxHeigth = Math.max(mWhilteDot.getHeight(), mBlackDot.getHeight());
		mPadding = getResources().getDimensionPixelSize(R.dimen.much_workspace_indicator_item_padding);
		mPageCount = 1;
	}

	public int getMaxHeigh() {
		return maxHeigth + 18;
	}

	public MuchFolderPagedViewIndicator(Context context) {
		super(context);
	}

	public MuchFolderPagedViewIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		mWhilteDot = BitmapFactory.decodeResource(getResources(), R.drawable.page_indicator_selected);
		mBlackDot = BitmapFactory.decodeResource(getResources(), R.drawable.page_indicator_normal);
		maxWidth = Math.max(mWhilteDot.getWidth(), mBlackDot.getWidth());
		maxHeigth = Math.max(mWhilteDot.getHeight(), mBlackDot.getHeight());
		mPadding = getResources().getDimensionPixelSize(R.dimen.much_workspace_indicator_item_padding);
		mPageCount = 1;
		mPadding = 0;

	}

	@Override
	public void onPageSwitch(View newPage, int newPageIndex) {
		//do nothing
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@Override
	public void onPageSelected(int position) {
		mCurrentPage = position;
		invalidate();
	}

	@Override
	public void onPageStateChange(int state) {
	}

	@Override
	public void onPageCountChange(int pageNum) {
		mPageCount = pageNum;
		invalidate();
	}

}
