
package com.android.launcher3.much;

import com.android.launcher3.R;
import com.android.launcher3.PagedView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class PagedViewIndicator extends View implements
        PagedView.PageSwitchListener {

    protected int mCurrentPage;
    protected int mPageCount;
    protected Bitmap mWhilteDot;
    protected Bitmap mBlackDot;
    protected int maxWidth;
    protected int maxHeigth;
    protected int mPadding;

    public PagedViewIndicator(Context context) {
        this(context, null);
    }

    public PagedViewIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PagedViewIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mWhilteDot = BitmapFactory.decodeResource(getResources(),
                R.drawable.much_pagedview_indicator_selected);
        mBlackDot = BitmapFactory.decodeResource(getResources(),
                R.drawable.much_pagedview_indicator_unselected);
        maxWidth = Math.max(mWhilteDot.getWidth(), mBlackDot.getWidth());
        maxHeigth = Math.max(mWhilteDot.getHeight(), mBlackDot.getHeight());
        mPadding = getResources().getDimensionPixelSize(
                R.dimen.much_workspace_indicator_item_padding);
        mPageCount = MuchConfig.getInstance().getPageCount();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int count = mPageCount;
        float heightOffset = getPaddingTop();
        float widthOffset = getPaddingLeft()
                + ((getWidth() - getPaddingLeft() - getPaddingRight()) / 2.0f)
                - ((count * maxWidth) / 2.0f) - ((count - 1) * mPadding / 2.0f);

        for (int iLoop = 0; iLoop < count; iLoop++) {
            float dx = widthOffset + iLoop * (maxWidth + mPadding)
                    + (maxWidth - mBlackDot.getWidth()) / 2;
            float dy = heightOffset + (maxHeigth - mBlackDot.getHeight()) / 2;

            if (iLoop != mCurrentPage) {
                canvas.drawBitmap(mBlackDot, dx, dy, null);
            } else {
                dx = widthOffset + iLoop * (maxWidth + mPadding);
                canvas.drawBitmap(mWhilteDot, dx, heightOffset, null);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {

            final int count = mPageCount;
            result = (int) (getPaddingLeft() + getPaddingRight() + count
                    * maxWidth + (count - 1) * mPadding + 1);

            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (getPaddingTop() + getPaddingBottom() + maxHeigth + 1);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public void onPageSwitch(View newPage, int newPageIndex) {
        try {
            ViewGroup parent = (ViewGroup) newPage.getParent();
            mPageCount = parent.getChildCount();
        } catch (Exception e) {
            // null
        }
        mCurrentPage = newPageIndex;
        invalidate();
    }
}
