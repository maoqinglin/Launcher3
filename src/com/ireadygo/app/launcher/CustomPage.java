package com.ireadygo.app.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.CellLayout;

public class CustomPage extends CellLayout {

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
}
