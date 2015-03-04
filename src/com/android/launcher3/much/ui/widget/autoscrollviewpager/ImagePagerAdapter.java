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
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.launcher3.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * ImagePagerAdapter
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2014-2-23
 */
public class ImagePagerAdapter extends RecyclingPagerAdapter {

	private Context mContext;
	private List<String> mBannerImageUrlList = new ArrayList<String>();

	private int mSize;
	private boolean isInfiniteLoop;
	private OnChildClickListener mClickListener;
	
	private Drawable mDefaultDrawable;
	private Drawable mEmptyDrawable;
	private ImageLoader mImageLoader;

	public ImagePagerAdapter(Context context, List<String> bannerImages, ImageLoader imageLoader) {
		mContext = context;
		mBannerImageUrlList = bannerImages;
		mSize = getSize(mBannerImageUrlList);
		isInfiniteLoop = false;
		mImageLoader = imageLoader;
		mDefaultDrawable = context.getResources().getDrawable(R.drawable.store_ad_large);
		mEmptyDrawable = context.getResources().getDrawable(R.drawable.store_banner_default);
	}

	public interface OnChildClickListener {
	    public void onItemChildViewClick(View view, int index);
	}

	@Override
	public int getCount() {
		// Infinite loop
		return isInfiniteLoop ? Integer.MAX_VALUE : getSize(mBannerImageUrlList);
	}

	/**
	 * get really position
	 * 
	 * @param position
	 * @return
	 */
	private int getPosition(int position) {
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
		if (mSize == 0) {
			holder.imageView.setImageDrawable(mDefaultDrawable);
		} else {
			mImageLoader.displayImage(mBannerImageUrlList.get(getPosition(position)), holder.imageView,getDisplayImageOptions());
		}
		holder.imageView.setClickable(true);
		holder.imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemChildViewClick(v, getPosition(position));
            }
        });
		return view;
	}

	public DisplayImageOptions getDisplayImageOptions() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.cacheInMemory(true).cacheOnDisc(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnFail(mEmptyDrawable)
		.showImageForEmptyUri(mEmptyDrawable)
		.build();
		return options;
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
        mSize = getSize(mBannerImageUrlList);
        super.notifyDataSetChanged();
    }
	
}
