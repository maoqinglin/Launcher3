package com.android.launcher3;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.android.launcher3.much.MuchConfig;

/**
 * delete icon when long click
 * 
 * @author linmaoqing
 * 
 */
public class DeleteRect {
	private static final String TAG = "DeleteRect";
	private static final int CLICK_DELAY = 200;
	private Context mContext;
	private Rect rect = new Rect(); //删除图标的矩形区域
	private Rect offRect = new Rect();  //扩大删除图标触摸区域
	private Rect iconRect = new Rect();//图标区域不包含文字
	private boolean isDelete;
	private boolean mIsTouchAlwaysInRect = false;
	private CheckLongPressHelper mLongPressHelper;
	private View view;
	private Launcher mLauncher;
	private int left;
	private int top;
	private boolean isShowUninstallDialog;

	public DeleteRect(View view) {
		this.view = view;
		mContext = view.getContext();
		mLongPressHelper = new CheckLongPressHelper(view);
		if (mContext instanceof Launcher) {
			mLauncher = ((Launcher) mContext);
		}
	}

	// draw delete image
	public void drawDelete(Canvas canvas, int scrollX, int scrollY) {
		if (!isDelete) {//抖动时才绘制
			return;
		}

		// 系统应用不绘制图标
		if (checkSystemIcon()) {
			return;
		}

		//modify by lilu 隐藏分享图标
		//Drawable shadowIcon = mContext.getResources().getDrawable(R.drawable.much_share_shadow);
		Drawable deleteIcon = mContext.getResources().getDrawable(R.drawable.much_delete_circle_x);
		int drawableWidth = deleteIcon.getIntrinsicWidth();
		int drawableHeight = deleteIcon.getIntrinsicHeight();
		if (view instanceof BubbleTextView) {
			Drawable[] drawables = ((BubbleTextView) view).getCompoundDrawables();
			Drawable drawableTop = drawables[1];
			if (drawableTop != null) {//FastBitmapDrawable
				int w = drawableTop.getBounds().width();
				int h = drawableTop.getBounds().height();
				int centerX = Math.abs(view.getMeasuredWidth() - w) / 2;
				int centerY = view.getPaddingTop();
				left = centerX - drawableWidth/2+5 ;
				left = left < 0 ? 0 : left;
				top = centerY - drawableHeight/2+5 ;
				//叠加阴影图片
				iconRect.set(centerX, centerY, centerX+w, centerY+h);
			}
		} else if (view instanceof FolderIcon) {
			// FolderIcon icon = (FolderIcon)view;
			// left = icon.getPreviewBackgroundLeft()-drawableWidth/2;
			// left = left<0?0:left;
			// top = icon.getPreviewBackgroundTop()-drawableHeight/2;
			// top = top<0?0:top;
		} else {
			left = 0;
			top = 0;
		}
		// modify by linmaoqing
		rect.set(left, top, left + drawableWidth, top + drawableHeight);
		offRect.set(rect.left-drawableWidth/2, rect.top-drawableHeight/2, rect.right+drawableWidth/2, rect.bottom+drawableHeight/2);
		// end
		//shadowIcon.setBounds(iconRect);
		deleteIcon.setBounds(rect);
		canvas.save();
		canvas.translate(scrollX, scrollY);
		//shadowIcon.draw(canvas);
		deleteIcon.draw(canvas);
		canvas.translate(-scrollX, -scrollY);
		canvas.restore();
	}

	private boolean checkSystemIcon() {
		boolean isSystemApp = false;
		if(null == view){
			isSystemApp = !isSystemApp;
		}
		ItemInfo item = (ItemInfo) view.getTag();
		if (item instanceof ShortcutInfo) {
			ShortcutInfo info = (ShortcutInfo) item;
			isSystemApp = isSysApp(info.getIntent().getComponent().getPackageName()); //modify by linmaoqing 2014-5-14
		}else if(item instanceof LauncherAppWidgetInfo){
			LauncherAppWidgetInfo info = (LauncherAppWidgetInfo)item;
			if(MuchConfig.APP_WIDGET_SYSTEM_MESSAGE.equals(info.providerName.getClassName())){
				isSystemApp = true;
			}
		}
		return isSystemApp;
	}

	private boolean isSysApp(final String pkgName){
		if (!TextUtils.isEmpty(pkgName)) {
			try {
				MuchItemInfoManager manager = LauncherAppState.getInstance().getMuchItemInfoManager();
				return manager.isSystemApp(pkgName);
			} catch (NameNotFoundException e) {
				Toast.makeText(mContext, "not find pkgName = " + pkgName, Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}

	// check touch rect
	public boolean onTouchEventDelete(boolean result, MotionEvent event) {
		isShowUninstallDialog = false;
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
					isShowUninstallDialog = true;
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
		ItemInfo info = (ItemInfo)view.getTag();
		mLauncher.showUninstallSharePrompt(info, null);
		MuchUninstallSharePrompt uninstall = mLauncher.getUninstallSharePrompt();
		if(null!=uninstall){
			uninstall.deleteItemInfo();
		}
	}

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	public boolean isShowUninstallDialog() {
		return isShowUninstallDialog;
	}
}