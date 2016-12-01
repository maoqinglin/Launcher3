package com.android.launcher3;

import com.android.launcher3.CellLayout.LayoutParams;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MuchFolderCellLayout extends CellLayout {

	private static final String TAG = "MuchFolderCellLayout";
	private int mIndex;

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int mIndex) {
		this.mIndex = mIndex;
	}

	public MuchFolderCellLayout(Context context) {
		super(context);
	}

	public MuchFolderCellLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MuchFolderCellLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	int getCountX() {
		return super.getCountX();
	}

	@Override
	int getCountY() {
		return super.getCountY();
	}

	@Override
	public View getChildAt(int x, int y) {
		return super.getChildAt(x, y);

	}

	@Override
	public boolean addViewToCellLayout(View child, int index, int childId, LayoutParams params, boolean markCells) {
		return super.addViewToCellLayout(child, index, childId, params, markCells);
	}

	@Override
	public boolean animateChildToPosition(final View child, int cellX, int cellY, int duration, int delay,
			boolean permanent, boolean adjustOccupied) {
		return super.animateChildToPosition(child, cellX, cellY, duration, delay, permanent, adjustOccupied);
	}

	@Override
	int[] findNearestArea(int pixelX, int pixelY, int spanX, int spanY, int[] result) {
		return super.findNearestArea(pixelX, pixelY, spanX, spanY, result);
	}

	@Override
	public void setGridSize(int x, int y) {
		super.setGridSize(x, y);
	}

	@Override
	public int getDesiredWidth() {
		return super.getDesiredWidth();
	}

	@Override
	public int getDesiredHeight() {
		return super.getDesiredHeight();
	}

	@Override
	public void removeAllViews() {
		super.removeAllViews();
	}

	@Override
	public boolean getVacantCell(int[] vacant, int spanX, int spanY) {
		return super.getVacantCell(vacant, spanX, spanY);
	}

	public ShortcutAndWidgetContainer getShortcutsAndWidgets() {
		return super.getShortcutsAndWidgets();
	}

	@Override
	public void removeView(View view) {
		super.removeView(view);
	}

	@Override
	public void removeAllViewsInLayout() {
		super.removeAllViewsInLayout();
	}

	@Override
	boolean findCellForSpan(int[] cellXY, int spanX, int spanY) {
		return super.findCellForSpan(cellXY, spanX, spanY);
	}

	/*
	 * @Override public void measure(int widthMeasureSpec, int
	 * heightMeasureSpec) { super.measure(widthMeasureSpec, heightMeasureSpec);
	 * }
	 */
	private int mLastX = 0;
	private boolean mSmoothState;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int x = 0, y=0;
//		switch (ev.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			mLastX = (int)getX();
//			y = (int)getY();
//			break;
//		case MotionEvent.ACTION_MOVE:
//			x = Math.abs((int)getX()-mLastX);
//			if(x>getWidth()/3){
//				mLastX = 0;
//				mSmoothState = true;
//				return true;
//			}
//			break;
//		}
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		return false;
	}

	@Override
	public void setOnInterceptTouchListener(OnTouchListener listener) {
		// TODO Auto-generated method stub
		super.setOnInterceptTouchListener(listener);
	}

}

interface ICellLayout {
	int getCountX();

	int getCountY();

	public View getChildAt(int x, int y);

	public boolean addViewToCellLayout(View child, int index, int childId, LayoutParams params, boolean markCells);

	public boolean animateChildToPosition(final View child, int cellX, int cellY, int duration, int delay,
										  boolean permanent, boolean adjustOccupied);

	int[] findNearestArea(int pixelX, int pixelY, int spanX, int spanY, int[] result);

	public void setGridSize(int x, int y);

	public int getDesiredWidth();

	public int getDesiredHeight();

	public void removeAllViews();

	public void removeIconView(View view);

	public void removeAllViewsInLayout();

	public boolean getVacantCell(int[] vacant, int spanX, int spanY);

	public ShortcutAndWidgetContainer getShortcutsAndWidgets();

	boolean findCellForSpan(int[] cellXY, int spanX, int spanY);

	void measure(int widthMeasureSpec, int heightMeasureSpec);
	
	public void setFixedSize(int width, int height);
	
	public void setCellDimensions(int width, int height);
	
	public void setInvertIfRtl(boolean invert);
	
}
