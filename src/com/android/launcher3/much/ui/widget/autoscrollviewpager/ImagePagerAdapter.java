/*
 * Copyright 2014 trinea.cn All right reserved. This software is the confidential and proprietary information of
 * trinea.cn ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with trinea.cn.
 */
package com.android.launcher3.much.ui.widget.autoscrollviewpager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.launcher3.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * ImagePagerAdapter
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2014-2-23
 */
public class ImagePagerAdapter extends RecyclingPagerAdapter {

	private Context mContext;
	private List<BitmapDrawable> mBannerImageList = new ArrayList<BitmapDrawable>();

	private int mSize;
	private boolean isInfiniteLoop;
	private OnChildClickListener mClickListener;
	
	private Drawable mDefaultDrawable;

	public ImagePagerAdapter(Context context, List<BitmapDrawable> bannerImages) {
		mContext = context;
		mBannerImageList = bannerImages;
		mSize = getSize(mBannerImageList);
		isInfiniteLoop = false;
		mDefaultDrawable = context.getResources().getDrawable(R.drawable.much_app_icon_bg);
	}

	public interface OnChildClickListener {
	    public void onItemChildViewClick(View view, int index);
	}

	@Override
	public int getCount() {
		// Infinite loop
		return isInfiniteLoop ? Integer.MAX_VALUE : getSize(mBannerImageList);
	}

	/**
	 * get really position
	 * 
	 * @param position
	 * @return
	 */
	private int getPosition(int position) {
	    if(mSize == 0){
	        return 0;
	    }
		return isInfiniteLoop ? position % mSize : position;
	}

	public void setOnClickListener(OnChildClickListener listener) {
	    mClickListener = listener;
	}

	@Override
	public View getView(final int position, View view, ViewGroup container) {
		ViewHolder holder;
		if (view == null) {
			holder = new ViewHolder();
			view = holder.imageView = new ImageView(mContext);
			holder.imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			holder.imageView.setScaleType(ScaleType.FIT_XY);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		BitmapDrawable bitmapDrawable = mBannerImageList.get(getPosition(position));
		if (bitmapDrawable == null) {
			holder.imageView.setImageDrawable(mDefaultDrawable);
		} else {
			holder.imageView.setImageDrawable(mBannerImageList.get(getPosition(position)));
		}
		holder.imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemChildViewClick(v, getPosition(position));
            }
        });
		return view;
	}

	private static class ViewHolder {

		ImageView imageView;
	}

	/**
	 * @return the isInfiniteLoop
	 */
	public boolean isInfiniteLoop() {
		return isInfiniteLoop;
	}

	/**
	 * @param isInfiniteLoop
	 *            the isInfiniteLoop to set
	 */
	public ImagePagerAdapter setInfiniteLoop(boolean isInfiniteLoop) {
		this.isInfiniteLoop = isInfiniteLoop;
		return this;
	}

	private <V> int getSize(List<V> sourceList) {
		return sourceList == null ? 0 : sourceList.size();
	}

    @Override
    public void notifyDataSetChanged() {
        mSize = getSize(mBannerImageList);
        super.notifyDataSetChanged();
    }
	
}
