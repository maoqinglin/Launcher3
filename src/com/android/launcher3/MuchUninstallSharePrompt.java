
package com.android.launcher3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.launcher3.LauncherSettings.Favorites;
import com.android.launcher3.much.InOutAnimation;
import com.android.launcher3.much.InOutAnimation.Direction;
import com.umeng.analytics.MobclickAgent;

public class MuchUninstallSharePrompt {
	private static final String TAG = "MuchUninstallSharePrompt";
    private Launcher mLauncher;
    private WindowManager mWindowManager;
    private MuchItemInfoManager mMuchItemInfoManager;
    public String mPackageName;
    public String mLabel;
    private ViewGroup mView;
    public static final int[] mCenterLocation = new int[2];
    private boolean mShowUninstall = true;
    private boolean mShowShare = true;
    private ItemInfo mDragInfo;
    private Folder mFolder;
    private AppInfo mAppInfo;

	public MuchUninstallSharePrompt(Launcher launcher, ItemInfo item, Folder folder) {
        mLauncher = launcher;
        mMuchItemInfoManager = LauncherAppState.getInstance().getMuchItemInfoManager();
        mDragInfo = item;
        mFolder = folder;
        View v = null;
        if (Favorites.CONTAINER_DESKTOP == item.container || Favorites.CONTAINER_HOTSEAT == item.container) {
            CellLayout celllayout = mLauncher.getCellLayout(item.container, item.screenId);
            if(Favorites.CONTAINER_HOTSEAT == item.container) {
                int x = mLauncher.getHotseat().getCellXFromOrder((int)item.screenId);
                int y = mLauncher.getHotseat().getCellYFromOrder((int)item.screenId);
                v = celllayout.getChildAt(x, y);
            } else {
                v = celllayout.getChildAt(item.cellX, item.cellY);
            }
        } else if(folder != null) {
//            CellLayout celllayout = folder.mContent;
//            v = celllayout.getChildAt(item.cellX, item.cellY); modify by linmaoqing
        }
        if(v != null) {
            v.getLocationOnScreen(mCenterLocation);
            mCenterLocation[0] = mCenterLocation[0] + v.getWidth() / 2;
            mCenterLocation[1] = mCenterLocation[1] + v.getHeight() / 2 - 50;
        }
        checkUninstallShareNeedShow();
    }

    public void showView() {
        setParams();
        initUninstallShareViews();
        startAnimations(Direction.IN);
    }

    private void setParams() {
        mView = (ViewGroup) View.inflate(mLauncher, R.layout.much_uninstall_share_btn, null);
        mWindowManager = (WindowManager) mLauncher
                .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams windLp = new WindowManager.LayoutParams();
        windLp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        windLp.format = 1;
        windLp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        windLp.alpha = 1.0f;
        windLp.height = LayoutParams.MATCH_PARENT;
        windLp.width = LayoutParams.MATCH_PARENT;
        mWindowManager.addView(mView, windLp);
    }

    private void initUninstallShareViews() {
        FrameLayout layout = (FrameLayout) mView
                .findViewById(R.id.parent);
        RelativeLayout center = (RelativeLayout) mView.findViewById(R.id.center);
        FrameLayout.LayoutParams centerLp = (FrameLayout.LayoutParams) center.getLayoutParams();
        centerLp.leftMargin = mCenterLocation[0]- centerLp.width / 2;
        centerLp.topMargin = mCenterLocation[1] - centerLp.height / 2;
        center.setLayoutParams(centerLp);
//        layout.setOnClickListener(this);

        ImageView uninstall = (ImageView) mView.findViewById(R.id.btn_uninstall);
//        uninstall.setOnClickListener(this);
        if(centerLp.topMargin < 0) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) uninstall.getLayoutParams();
            lp.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            uninstall.setLayoutParams(lp);
        }

        ImageView share = (ImageView) mView.findViewById(R.id.btn_share);
//        share.setOnClickListener(this);
        if(centerLp.leftMargin < 0) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) share.getLayoutParams();
            lp.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            share.setLayoutParams(lp);
        }
        checkUninstallShareNeedShow();
        uninstall.setVisibility(mShowUninstall ? View.VISIBLE : View.GONE);
        share.setVisibility(mShowShare ? View.VISIBLE : View.GONE);
        mView.setVisibility(mShowShare || mShowUninstall ? View.VISIBLE : View.GONE);
        if(!mShowShare && !mShowUninstall) {
            dismiss();
        }
    }

    private void startAnimations(Direction direction) {
        ViewGroup views = (ViewGroup) mView.findViewById(R.id.center);
        if (views.getChildCount() == 0) {
            return;
        }
        InOutAnimation.startAnimations(views, direction);
    }

    public void dismiss() {
        if(mWindowManager != null && mView != null) {
            try {
                mWindowManager.removeView(mView);
                mView.setVisibility(View.GONE);
            } catch (Exception e) {}
        }
    }

    private void checkUninstallShareNeedShow() {
        if (mDragInfo instanceof ShortcutInfo) {
            ShortcutInfo info = (ShortcutInfo) mDragInfo;
            ComponentName componentName = info.getIntent().getComponent();//modify by linmaoqing 2015-3-31 修复空指针问题
            if(componentName == null){
                mPackageName = "";
                return ;
            }
            mPackageName = componentName.getPackageName();
            mLabel = info.title.toString();
            try {
                mShowUninstall = !mMuchItemInfoManager.isSystemApp(mPackageName);
                if(!mShowUninstall && !(Intent.ACTION_MAIN.equals(info.intent.getAction()) && info.intent.getCategories().contains(Intent.CATEGORY_LAUNCHER))) {
                    mShowUninstall = true;
                }
            } catch (NameNotFoundException e) {
                //ignore
            }
        } else if (mDragInfo instanceof AppInfo) {
            AppInfo info = (AppInfo) mDragInfo;
            mPackageName = info.getIntent().getComponent().getPackageName();
            mLabel = info.title.toString();
            try {
                mShowUninstall = !mMuchItemInfoManager.isSystemApp(mPackageName);
                if(!mShowUninstall && !(Intent.ACTION_MAIN.equals(info.intent.getAction()) && info.intent.getCategories().contains(Intent.CATEGORY_LAUNCHER))) {
                    mShowUninstall = true;
                }
            } catch (NameNotFoundException e) {
                //ignore
            }
        } else if (mDragInfo instanceof FolderInfo) {
            FolderInfo folder = (FolderInfo) mDragInfo;
            mShowUninstall = folder.contents.isEmpty();
        }
        mShowShare = (mPackageName != null && mPackageName.length() > 0 && mLabel != null);
    }

    void deleteItemInfo() {
        if(mLauncher.isVisitorMode()) {
            Toast.makeText(mLauncher.getBaseContext(), mLauncher.getString(R.string.much_visit_mode), Toast.LENGTH_SHORT).show();
            return;
        }
        if(mFolder != null) {
            mLauncher.closeFolder();
        }
        if (mDragInfo instanceof FolderInfo) {
            // Remove the folder from the workspace and delete the contents from
            // launcher model
            FolderInfo folderInfo = (FolderInfo) mDragInfo;
            removeView(mDragInfo);
            mLauncher.removeFolder(folderInfo);
            LauncherModel.deleteFolderContentsFromDatabase(mLauncher, folderInfo);
            return;
        }
        if (mDragInfo instanceof LauncherAppWidgetInfo) {
            removeView(mDragInfo);
            // Remove the widget from the workspace
            mLauncher.removeAppWidget((LauncherAppWidgetInfo) mDragInfo);
            LauncherModel.deleteItemFromDatabase(mLauncher, mDragInfo);

            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) mDragInfo;
            final LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
            if (appWidgetHost != null) {
                // Deleting an app widget ID is a void call but writes to disk
                // before returning
                // to the caller...
                new Thread("deleteAppWidgetId") {
                    public void run() {
                        appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
                    }
                }.start();
            }
            return;
        }
        if (mPackageName.length() > 0) {
            Intent intent = null;
            if (mDragInfo instanceof ShortcutInfo) {
                intent = ((ShortcutInfo) mDragInfo).intent;
            } else {
                intent = ((AppInfo) mDragInfo).intent;
            }
            if ((Intent.ACTION_MAIN.equals(intent.getAction()) && intent.getCategories() !=null && intent.getCategories().contains(
                    Intent.CATEGORY_LAUNCHER))) {
                createAppInfo(intent);
                mLauncher.showDialog(Launcher.CREATE_UNINSTALL_DIALOG);
                return;
            }
        }

        if(mFolder != null) {
            mFolder.mInfo.remove((ShortcutInfo)mDragInfo);
        } else {
            removeView(mDragInfo);
        }
        LauncherModel.deleteItemFromDatabase(mLauncher, mDragInfo);
    }

	private void createAppInfo(Intent intent) {
		mAppInfo = new AppInfo();
		mAppInfo.intent = intent;
		mAppInfo.componentName = intent.getComponent();
		mAppInfo.title = mDragInfo.title;
		mAppInfo.cellX = mDragInfo.cellX;
		mAppInfo.cellY = mDragInfo.cellY;
		mAppInfo.container = mDragInfo.container;
		mAppInfo.screenId = mDragInfo.screenId;
		mAppInfo.spanX = mDragInfo.spanX;
		mAppInfo.spanY = mDragInfo.spanY;
	}

    private void removeView(ItemInfo item) {
        CellLayout celllayout = mLauncher.getCellLayout(item.container, item.screenId);
        View v = celllayout.getChildAt(item.cellX, item.cellY);
        celllayout.removeView(v);
    }

    public AppInfo getAppInfo() {
		return mAppInfo;
	}
    
    public void shareItemInfo() {
        MobclickAgent.onEvent(mLauncher, "shareApp");
        mMuchItemInfoManager.shareItemInfo(mPackageName, mLabel);
    }
}
