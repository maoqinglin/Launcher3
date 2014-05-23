package com.android.launcher3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class CellLayoutCreator extends CellLayout {

    private Rect mRect = new Rect();

    public CellLayoutCreator(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public CellLayoutCreator(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public CellLayoutCreator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        Drawable addNewScreenDrawable = getResources().getDrawable(R.drawable.much_preview_addscreen);
        int drawableHeight = addNewScreenDrawable.getIntrinsicHeight();
        int drawableWidght = addNewScreenDrawable.getIntrinsicWidth();
        int left = (getMeasuredWidth()-drawableWidght)/2;
        int top = (getMeasuredHeight()-drawableHeight)/2;
        mRect.set(left, top, left+drawableWidght, top+drawableHeight);
        addNewScreenDrawable.setBounds(mRect);
        addNewScreenDrawable.draw(canvas);
    }
}
