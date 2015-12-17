package com.android.launcher3;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.much.MuchConfig;

public class MuchFolderPageView extends ViewPager implements ICellLayout {
	private static final String TAG = "MuchFolderPageView";
	private static final int CELL_COUNT_X = 4;
	private static final int CELL_COUNT_Y = 3;
	private ArrayList<MuchFolderCellLayout> mCellLayoutList = new ArrayList<MuchFolderCellLayout>();

	private MuchFolderCellLayout mCurrentCellLayout;
	private LayoutInflater mInflater;
	
	private float mDownMotionX;
    private float mDownMotionY;
    private int mDesiredWidth = 568;
    private int mDesiredHeight = 462;
    
    
    protected float mLastMotionX;
    protected float mLastMotionXRemainder;
    protected float mLastMotionY;
    protected float mTotalMotionX;
    
    
    protected static final float INTERUPTE_DISTANCE = 25;
    
    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;

    protected int mTouchState = TOUCH_STATE_REST;
    protected static final int INVALID_POINTER = -1;
    protected boolean mAllowLongPress = true;
    protected int mActivePointerId = INVALID_POINTER;

    private ArrayList<FolderViewPageListener> mFolderViewPageListeners = new ArrayList<MuchFolderPageView.FolderViewPageListener>();

	public MuchFolderPageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
	}

	public MuchFolderPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MuchFolderPageView(Context context) {
		super(context);
	}

	public void setLayoutInflater(LayoutInflater inflater) {
		mInflater = inflater;
	}
	@Override
	public int getCountX() {
		return CELL_COUNT_X;
	}

	@Override
	public int getCountY() {
		return CELL_COUNT_Y * mCellLayoutList.size();
	}

	/*
	 * 根据相对坐标获取当前Page的View对象，返回的view携带相对坐标
	 */
	@Override
	public View getChildAt(int x, int y) {
		return mCurrentCellLayout.getChildAt(x,y);
	}

	/*
	 * 根据绝对坐标获取View对象，返回view携带相对坐标
	 */
	public View getChildAtByAbsoluteCoord(int x, int y) {
		int childIndex = getPageIndexByY(y);
		int childY = getYIndexInPage(y);
		if (childIndex >= mCellLayoutList.size()) {
			return null;
		}
		return mCellLayoutList.get(childIndex).getChildAt(x, childY);
	}

	@Override
	public ShortcutAndWidgetContainer getShortcutsAndWidgets() {
		return mCurrentCellLayout.getShortcutsAndWidgets();
	}

	public List<ShortcutAndWidgetContainer> getAllShortcutsAndWidgets() {
		int size = mCellLayoutList.size();
		List<ShortcutAndWidgetContainer> containerList = new ArrayList<ShortcutAndWidgetContainer>(size);
		for (int i = 0; i < size; i++) {
			ShortcutAndWidgetContainer container = mCellLayoutList.get(i).getShortcutsAndWidgets();
			if (null != container) {
				containerList.add(container);
			}
		}
		return containerList;
	}

	public int getAllShortcutContainerChildCount() {
		int count = 0;
		int size = mCellLayoutList.size();
		for (int i = 0; i < size; i++) {
			ShortcutAndWidgetContainer container = mCellLayoutList.get(i).getShortcutsAndWidgets();
			if (null != container) {
				count += container.getChildCount();
			}
		}
		return count;
	}

	@Override
	public boolean addViewToCellLayout(View child, int index, int childId,
			com.android.launcher3.CellLayout.LayoutParams params, boolean markCells) {
		com.android.launcher3.CellLayout.LayoutParams lp = params;
		int pageIndex = getPageIndexByY(lp.cellY);
		lp.cellY = getYIndexInPage(lp.cellY);
		return mCellLayoutList.get(pageIndex).addViewToCellLayout(child, index, childId, lp, markCells);
	}

	public boolean addViewToCurrentCellLayout(View child, int index, int childId,
			com.android.launcher3.CellLayout.LayoutParams params, boolean markCells) {
		com.android.launcher3.CellLayout.LayoutParams lp = params;
		int pageIndex = getPageIndexByY(lp.cellY);
		lp.cellY = getYIndexInPage(lp.cellY);
		return mCurrentCellLayout.addViewToCellLayout(child, index, childId, lp, markCells);
	}

	/*
	 * 在当前页进行图标移动，此方法传入坐标是相对坐标，
	 */
	@Override
	public boolean animateChildToPosition(View child, int cellX, int cellY, int duration, int delay, boolean permanent,
			boolean adjustOccupied) {
		if(getPageIndexByY(cellY)>=mCellLayoutList.size()){
			return false;
		}
		return mCurrentCellLayout.animateChildToPosition(child, cellX, getYIndexInPage(cellY), duration, delay, permanent,
				adjustOccupied);
	}

	@Override
	public void setGridSize(int x, int y) {
		mCurrentCellLayout.setGridSize(x, y);
	}

	@Override
	public int getDesiredWidth() {
		if(mCellLayoutList.size() > 0){
			mDesiredWidth = mCellLayoutList.get(0).getDesiredWidth();
		} else if(mCurrentCellLayout != null){
			mDesiredWidth = mCurrentCellLayout.getDesiredWidth();
		}
		return mDesiredWidth;
		
	}

	@Override
	public int getDesiredHeight() {
		if(mCellLayoutList.size() > 0){
			mDesiredHeight = mCellLayoutList.get(0).getDesiredHeight();
		} else if(mCurrentCellLayout != null){
			mDesiredHeight= mCurrentCellLayout.getDesiredHeight();
		}
		return mDesiredHeight;
	}

	@Override
	public boolean getVacantCell(int[] vacant, int spanX, int spanY) {
		int size = mCellLayoutList.size();
		for (int i = 0; i < size; i++) {
			if (mCellLayoutList.get(i).getVacantCell(vacant, spanX, spanY)) {
				return true;
			}
		}
		MuchFolderCellLayout newCellLayout = createNewCellLayout();
		return newCellLayout.getVacantCell(vacant, spanX, spanY);
	}

	public boolean getVacantCellAbCoord(int[] vacant, int spanX, int spanY) {
		int size = mCellLayoutList.size();
		for (int i = 0; i < size; i++) {
			if (mCellLayoutList.get(i).getVacantCell(vacant, spanX, spanY)) {
				vacant[1] = vacant[1] + i * CELL_COUNT_Y;
				return true;
			}
		}
		MuchFolderCellLayout newCellLayout = createNewCellLayout();
		boolean result = newCellLayout.getVacantCell(vacant, spanX, spanY);
		vacant[1] = vacant[1] + size * CELL_COUNT_Y;
		return result;
	}
	public MuchFolderCellLayout createNewCellLayout() {
		MuchFolderCellLayout newCellLayout = (MuchFolderCellLayout)mInflater.inflate(R.layout.much_folder_screen, null);
		newCellLayout.setIndex(mCellLayoutList.size());
		addCellLayout(newCellLayout);  
		notifyDataChanged();
		return newCellLayout;
	}

	private void addCellLayout(MuchFolderCellLayout newCellLayout) {
		newCellLayout.setGridSize(CELL_COUNT_X, CELL_COUNT_Y);
		mCellLayoutList.add(newCellLayout);
		synchronized (mFolderViewPageListeners) {
			for (FolderViewPageListener listener : mFolderViewPageListeners) {
				listener.onPageCountChange(mCellLayoutList.size());
			}
		}
	}

	private void notifyDataChanged() {
		MyPagerAdapter adapter = (MyPagerAdapter) getAdapter();
		if (null != adapter) {
			adapter.setHasChanged(true);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean findCellForSpan(int[] cellXY, int spanX, int spanY) {
		if(mCellLayoutList.size() <=0){
			return false;
		}
		MuchFolderCellLayout lastCellLayout = mCellLayoutList.get(mCellLayoutList.size() - 1);
		if (!lastCellLayout.findCellForSpan(cellXY, spanX, spanY)) {
			lastCellLayout = createNewCellLayout();
		}
		boolean result = lastCellLayout.findCellForSpan(cellXY, spanX, spanY);
		cellXY[1] = cellXY[1] + lastCellLayout.getIndex() * CELL_COUNT_Y;
		return result;
	}

	@Override
	public int[] findNearestArea(int pixelX, int pixelY, int spanX, int spanY, int[] result) {
		return mCurrentCellLayout.findNearestArea(pixelX, pixelY, spanX, spanY, result);
	}

	@Override
	public void removeAllViews() {
		removeAllViewsInPageView();
	}
	
	public void removeAllCellLayout(){
		super.removeAllViews();
	}

	@Override
	public void removeAllViewsInLayout() {
		removeAllViewsInPageView();
	}

	void removeAllViewsInPageView() {
		int size = mCellLayoutList.size();
		for (int i = 0; i < size; i++) {
			mCellLayoutList.get(i).removeAllViewsInLayout();
		}
	}

	public class MyPagerAdapter extends PagerAdapter {

		private boolean hasChanged ;  
		public boolean isHasChanged() {
			return hasChanged;
		}

		public void setHasChanged(boolean hasChanged) {
			this.hasChanged = hasChanged;
		}

		public MyPagerAdapter(MuchFolderCellLayout layout) {
			addCellLayout(layout);
			mCurrentCellLayout = layout;
		}

		public MyPagerAdapter() {
			
		}

		@Override
		public int getCount() {
			return mCellLayoutList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
				return POSITION_NONE;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((MuchFolderPageView) arg0).removeView((MuchFolderCellLayout)arg2);
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			if (mCellLayoutList.get(arg1).getParent() == null) {
				((MuchFolderPageView) arg0).addView(mCellLayoutList.get(arg1));
				if(isHasChanged()){
					requestLayout();
				}
			}
			return mCellLayoutList.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void finishUpdate(View arg0) {
		}
	}

	public class MuchPagedViewChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			mCurrentCellLayout = mCellLayoutList.get(arg0);
			synchronized (mFolderViewPageListeners) {
				for (FolderViewPageListener listener : mFolderViewPageListeners) {
					listener.onPageSelected(arg0);
				}
			}
		}

	}

	private boolean needInterupteInDistance(MotionEvent ev) {
        if(MuchConfig.SUPPORT_MUCH_STYLE) {
            float distanceX =  (ev.getX() - mDownMotionX);
            float distanceY =  (ev.getY() - mDownMotionY);
            final double xyDist = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
            return xyDist > INTERUPTE_DISTANCE;
        }

        return false;
    }
	
	private VelocityTracker mVelocityTracker;
	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		super.onInterceptTouchEvent(ev);
		 acquireVelocityTrackerAndAddMovement(ev);

	        // Skip touch handling if there are no pages to swipe
	        if (getChildCount() <= 0) return super.onInterceptTouchEvent(ev);

	        /*
	         * Shortcut the most recurring case: the user is in the dragging
	         * state and he is moving his finger.  We want to intercept this
	         * motion.
	         */
	        final int action = ev.getAction();
	        if ((action == MotionEvent.ACTION_MOVE) &&
	                (mTouchState == TOUCH_STATE_SCROLLING)) {
	            return true;
	        }

	        switch (action & MotionEvent.ACTION_MASK) {
	            case MotionEvent.ACTION_MOVE: {
	                /*
	                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
	                 * whether the user has moved far enough from his original down touch.
	                 */

	                if (mActivePointerId != INVALID_POINTER) {
//	                    determineScrollingStart(ev);
	                    if(needInterupteInDistance(ev)) {
	                        return true;
	                       }
	                    break;
	                }
	                if(needInterupteInDistance(ev)) {
	                    return true;
	                }
	                // if mActivePointerId is INVALID_POINTER, then we must have missed an ACTION_DOWN
	                // event. in that case, treat the first occurence of a move event as a ACTION_DOWN
	                // i.e. fall through to the next case (don't break)
	                // (We sometimes miss ACTION_DOWN events in Workspace because it ignores all events
	                // while it's small- this was causing a crash before we checked for INVALID_POINTER)
	            }

	            case MotionEvent.ACTION_DOWN: {
	                final float x = ev.getX();
	                final float y = ev.getY();
	                // Remember location of down touch
	                mDownMotionX = x;
	                mDownMotionY = y;
	                mLastMotionX = x;
	                mLastMotionY = y;
	                mLastMotionXRemainder = 0;
	                mTotalMotionX = 0;
	                mActivePointerId = ev.getPointerId(0);
	                mAllowLongPress = true;

	                /*
	                 * If being flinged and user touches the screen, initiate drag;
	                 * otherwise don't.  mScroller.isFinished should be false when
	                 * being flinged.
	                 */
//	                final int xDist = Math.abs(getFinalX() - mScroller.getCurrX());
//	                final boolean finishedScrolling = (mScroller.isFinished() || xDist < mTouchSlop);
//	                if (finishedScrolling) {
//	                    mTouchState = TOUCH_STATE_REST;
//	                    mScroller.abortAnimation();
//	                } else {
//	                    mTouchState = TOUCH_STATE_SCROLLING;
//	                }

	                // check if this can be the beginning of a tap on the side of the pages
	                // to scroll the current page
//	                if (mTouchState != TOUCH_STATE_PREV_PAGE && mTouchState != TOUCH_STATE_NEXT_PAGE) {
//	                    if (getChildCount() > 0) {
//	                        if (hitsPreviousPage(x, y)) {
//	                            mTouchState = TOUCH_STATE_PREV_PAGE;
//	                        } else if (hitsNextPage(x, y)) {
//	                            mTouchState = TOUCH_STATE_NEXT_PAGE;
//	                        }
//	                    }
//	                }
	                break;
	            }

	            case MotionEvent.ACTION_UP:
	            case MotionEvent.ACTION_CANCEL:
	                mTouchState = TOUCH_STATE_REST;
	                mAllowLongPress = false;
	                mActivePointerId = INVALID_POINTER;
	                releaseVelocityTracker();
	                break;

	            case MotionEvent.ACTION_POINTER_UP:
//	                onSecondaryPointerUp(ev);
	                releaseVelocityTracker();
	                break;
	        }

	        /*
	         * The only time we want to intercept motion events is if we are in the
	         * drag mode.
	         */
	        
	        return mTouchState != TOUCH_STATE_REST;
	}
	
	public int getPageIndexByY(int cellY) {
		return cellY / CELL_COUNT_Y;
	}

	public int getYIndexInPage(int cellY) {
		return cellY % CELL_COUNT_Y;
	}

	public int getYIndexInAllPage(int cellY) {
		return cellY + mCurrentCellLayout.getIndex() * CELL_COUNT_Y;
	}

	@Override
	public void removeIconView(View view) {
		//绝对坐标改相对坐标
	    int size = mCellLayoutList.size();
	    for (int i = 0; i < size; i++) {
            mCellLayoutList.get(i).removeView(view);
        }
	}
	
	/**
	 * 移除加号
	 * @param view
	 */
	public void removeLastView(View view) {
	    int size = mCellLayoutList.size();
	    MuchFolderCellLayout lastCellLayout = mCellLayoutList.get(size-1);
	    lastCellLayout.removeView(view);
    }

	public boolean checkAndRemoveEmptyPage() {
		//只在文件夹关闭后进行处理，文件夹关闭后，图标会重排，因此空页出现在最后一页
	    if(mCellLayoutList.isEmpty()){
	        return false;
	    }
		MuchFolderCellLayout lastCellLayout = mCellLayoutList.get(mCellLayoutList.size() - 1);
		if (0 == lastCellLayout.getShortcutsAndWidgets().getChildCount()) {
			removeView(lastCellLayout);
			mCellLayoutList.remove(lastCellLayout);
			lastCellLayout = null;
			
			notifyDataChanged();
			
			synchronized (mFolderViewPageListeners) {
				for (FolderViewPageListener listener : mFolderViewPageListeners) {
					listener.onPageCountChange(mCellLayoutList.size());
				}
			}
			return true;
		}
		return false;
	}

	public void addFolderViewPageListener(FolderViewPageListener listener) {
		synchronized (mFolderViewPageListeners) {
			mFolderViewPageListeners.add(listener);
		}
        if (listener != null) {
        	listener.onPageCountChange(mCellLayoutList.size());
        	listener.onPageSelected(this.getCurrentItem());
        }
	}

	public void removeFolderViewPageListener(FolderViewPageListener listener) {
		synchronized (mFolderViewPageListeners) {
			mFolderViewPageListeners.remove(listener);
		}
	}

	public interface FolderViewPageListener {
		void onPageSelected(int position);
		void onPageStateChange(int state);
		void onPageCountChange(int pageNum);
	}

	@Override
	public void setFixedSize(int width, int height) {
		mCurrentCellLayout.setFixedSize(width, height);
	}

	@Override
	public void setCellDimensions(int width, int height) {
		mCurrentCellLayout.setCellDimensions(width, height);
	}

	@Override
	public void setInvertIfRtl(boolean invert) {
		mCurrentCellLayout.setInvertIfRtl(invert);
	}

}
