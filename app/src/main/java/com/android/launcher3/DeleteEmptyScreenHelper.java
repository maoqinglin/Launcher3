package com.android.launcher3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.Toast;

public class DeleteEmptyScreenHelper {
    private static final String TAG = "DeleteEmptyScreen";
    private static final int CLICK_DELAY = 200;
    private Context mContext;
    private Rect rect = new Rect(); //删除图标的矩形区域
    private Rect offRect = new Rect();  //扩大删除图标触摸区域
    private boolean isDelete;
    private boolean mIsTouchAlwaysInRect = false;
    private CheckLongPressHelper mLongPressHelper;
    private View view;
    private Launcher mLauncher;
    private int left;
    private int top;
    private boolean isClickDelete;
    private static final int TOUCH_OFFSET = 50;
    private static final int PADDING_OFFSET = 10;

    public DeleteEmptyScreenHelper(View view) {
        this.view = view;
        mContext = view.getContext();
        mLongPressHelper = new CheckLongPressHelper(view);
        if (mContext instanceof Launcher) {
            mLauncher = ((Launcher) mContext);
        }
    }

    // draw delete image
    public void drawDeleteIcon(Canvas canvas, int scrollX, int scrollY) {
        if (!isDelete) {//抖动时才绘制
            return;
        }

        Drawable deleteIcon = mContext.getResources().getDrawable(R.drawable.much_delete_empty_screen);
        int drawableWidth = deleteIcon.getIntrinsicWidth();
        int drawableHeight = deleteIcon.getIntrinsicHeight();
        if (view instanceof CellLayout) {
            left = view.getMeasuredWidth()-drawableWidth;
            top = PADDING_OFFSET;
        } 
        
        rect.set(left-PADDING_OFFSET, top, view.getMeasuredWidth()-PADDING_OFFSET, top + drawableHeight);
        offRect.set(left-TOUCH_OFFSET, top-TOUCH_OFFSET, view.getMeasuredWidth()+TOUCH_OFFSET, top + drawableHeight+TOUCH_OFFSET);
        deleteIcon.setBounds(rect);
        canvas.save();
        canvas.translate(scrollX, scrollY);
        deleteIcon.draw(canvas);
        canvas.translate(-scrollX, -scrollY);
        canvas.restore();
    }

    // check touch rect
    public boolean onTouchEventDelete(boolean result, MotionEvent event) {
        if (!isDelete) {
            return result;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mIsTouchAlwaysInRect = isTouchInRect(event);
            if (!mIsTouchAlwaysInRect) {
                mLongPressHelper.postCheckForLongPress(CLICK_DELAY);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (mIsTouchAlwaysInRect) {
                mIsTouchAlwaysInRect = isTouchInRect(event);
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mIsTouchAlwaysInRect) {
                mIsTouchAlwaysInRect = isTouchInRect(event);
                if (mIsTouchAlwaysInRect) {
                    deleteViewInScreen();
                    return true;
                }
            }
            mLongPressHelper.cancelLongPress();
            break;
        default:
            mLongPressHelper.cancelLongPress();
            break;
        }
        return result;
    }

    private boolean isTouchInRect(MotionEvent event) {
        int x;
        int y;
        x = (int) event.getX();
        y = (int) event.getY();
        return  offRect.contains(x, y);
    }

    private void deleteViewInScreen() {
        if (null == mLauncher) {
            return;
        }
        if(null == view){
            return;
        }
        ViewParent parent = view.getParent();
        if(parent != null){
            mLauncher.getWorkspace().deleteNewEmptyScreen(view);
            isClickDelete = true;
        }
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    public boolean isClickDelete() {
        return isClickDelete;
    }
}
