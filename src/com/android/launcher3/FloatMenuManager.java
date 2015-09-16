package com.android.launcher3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.floatmenu.FloatingActionMenu;
import com.android.launcher3.floatmenu.FloatingActionMenu.MenuItemClickListener;
import com.android.launcher3.floatmenu.SubActionButton;
import com.android.launcher3.much.MuchNavBroadcast;
import com.umeng.analytics.MobclickAgent;

public class FloatMenuManager implements FloatingActionMenu.MenuStateChangeListener, MenuItemClickListener {

    private FloatingActionMenu mCurrentMenu;
    private Launcher mLauncher;
    private Handler mHandler;
    private boolean mIsFolderCell;
    private int[] mFolderPoint = new int[2];

    enum MenuDirection {
        RIGHT_UP, LEFT_UP, RIGHT_DOWN, LEFT_DOWN, LEFT_VERTICAL
    }

    private static final int DELETE_MENU_ID = 10000;
    private static final int SHARE_MENU_ID = 10001;
    private static final int DIY_MENU_ID = 10002;
    private static final int POWER_MENU_ID = 10003;
    
    private static final String APP_POWER_ACTION = "com.ireadygo.app.recentapps.ACTION_START_POWERMANAGE_ACTIVITY";
    private static final String EXTRA_PKG_NAME = "PKG_NAME";

    public FloatMenuManager() {

    }

    public void setFolderPoint(int[] folderPoint) {
        mIsFolderCell = true;
        this.mFolderPoint = folderPoint;
    }

    public boolean isIsFolderCell() {
        return mIsFolderCell;
    }

    @Override
    public void onMenuOpened(FloatingActionMenu menu) {
        // update our current menu reference
        mCurrentMenu = menu;
    }

    @Override
    public void onMenuClosed(FloatingActionMenu menu) {
        mCurrentMenu = null;
    }

    public void init(Launcher launcher, Handler handler) {
        mLauncher = launcher;
        mHandler = handler;
        registerLocalBroadcast();
    }

    public void createFloatMenu(final View cell, final int cellX, final int cellY) {
        if (cell == null || !(cell instanceof BubbleTextView)) {
            return;
        }
        if (mCurrentMenu != null && mCurrentMenu.isOpen()) {
            return;
        }
        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(mLauncher)
                .setBackgroundDrawable(getResources().getDrawable(R.drawable.float_menu_item_press_selector));

        LinearLayout deleteLayout = createMenuItem(R.drawable.icon_delete, R.string.much_menu_delete);

        LinearLayout shareLayout = createMenuItem(R.drawable.icon_share, R.string.much_menu_share);

//        LinearLayout diyLayout = createMenuItem(R.drawable.icon_diy, R.string.much_menu_keydiy);

        LinearLayout powerLayout = createMenuItem(R.drawable.icon_power, R.string.much_menu_power);

        int startAngle = 270;
        int endAngle = 360;
        MenuDirection direction = MenuDirection.RIGHT_UP;

        boolean isPortrait = !LauncherAppState.isScreenLandscape(mLauncher);
        int container = 0;
        Object tag = cell.getTag();
        if (tag != null && tag instanceof ItemInfo) {
            container = (int) ((ItemInfo) tag).container;
        }
        if (isPortrait) {
            if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                if (cellX > 3) {
                    direction = MenuDirection.LEFT_UP;
                }
            } else if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                if (cellX > 2) {
                    direction = MenuDirection.LEFT_UP;
                    if (cellY < 1) {
                        direction = MenuDirection.LEFT_DOWN;
                    }
                } else if (cellY < 1) {
                    direction = MenuDirection.LEFT_DOWN;
                    if (cellX < 1) {
                        direction = MenuDirection.RIGHT_DOWN;
                    }
                }
            }

        } else if (!isPortrait) {
            if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                // direction = MenuDirection.LEFT_VERTICAL;
                direction = MenuDirection.LEFT_DOWN;
                if (cellY > 3) {
                    direction = MenuDirection.LEFT_UP;
                }
            } else if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                if (cellY < 1) {
                    direction = MenuDirection.RIGHT_DOWN;
                }
            }
        }

        switch (direction) {
        case LEFT_UP:
            startAngle = 180;
            endAngle = 270;
            break;
        case RIGHT_UP:
            startAngle = 270;
            endAngle = 360;
            break;
        case LEFT_DOWN:
            startAngle = 90;
            endAngle = 180;
            break;
        case RIGHT_DOWN:
            startAngle = 0;
            endAngle = 90;
            break;
        case LEFT_VERTICAL:
            startAngle = 225;
            endAngle = 135;
            break;
        default:
            break;
        }

        final FloatingActionMenu floatMenu = new FloatingActionMenu.Builder(mLauncher)
                .addSubActionView(rLSubBuilder.setContentView(deleteLayout).setId(DELETE_MENU_ID).setTag(tag).build())
                .addSubActionView(rLSubBuilder.setContentView(shareLayout).setId(SHARE_MENU_ID).setTag(tag).build())
//                .addSubActionView(rLSubBuilder.setContentView(diyLayout).setId(DIY_MENU_ID).setTag(tag).build())
                .addSubActionView(rLSubBuilder.setContentView(powerLayout).setId(POWER_MENU_ID).setTag(tag).build())
                .attachTo(cell).setStartAngle(startAngle).setEndAngle(endAngle).setStateChangeListener(this)
                .setMenuItemClickListener(this).build();
        mCurrentMenu = floatMenu;

        if (isIsFolderCell()) {
            mCurrentMenu.setFolderCell(true);
            mCurrentMenu.setFolderPoint(mFolderPoint);
        }
        mIsFolderCell = false; // 置位

        checkHandler();
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                floatMenu.toggle(true);
            }
        });

    }

    private void checkHandler() {
        if (mHandler == null) {
            throw new RuntimeException("mHandler is null please setHandler()");
        }
    }

    private LinearLayout createMenuItem(int iconRes, int textRes) {
        LinearLayout menuLayout = new LinearLayout(mLauncher);
        menuLayout.setFocusableInTouchMode(true);

        TextView menu = new TextView(mLauncher);
        Drawable drawable = getResources().getDrawable(iconRes);
        menu.setText(getResources().getString(textRes));
        menu.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        menu.setGravity(Gravity.CENTER);
        menu.setCompoundDrawablePadding(getResources().getDimensionPixelOffset(R.dimen.much_menu_drawable_padding));
        menu.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        menuLayout.addView(menu);
        return menuLayout;
    }

    public void closeFloatMenu() {
        checkHandler();
        if (mCurrentMenu != null) {
            mCurrentMenu.close(true);
        }
    }

    public boolean isFloatMenuOpen() {
        if (mCurrentMenu != null && mCurrentMenu.isOpen()) {
            return true;
        }
        return false;
    }

    public Resources getResources() {
        return mLauncher.getResources();
    }

    @Override
    public void onMenuItemClick(View v) {
        Object tag = v.getTag();
        if (tag != null && tag instanceof ShortcutInfo) {
            ShortcutInfo info = (ShortcutInfo) tag;
            ComponentName cpn = info.getIntent().getComponent();
            switch (v.getId()) {
            case DELETE_MENU_ID:
                if (cpn != null) {
                    if (isSystemApp(mLauncher, cpn.getPackageName())) {
                        Toast.makeText(mLauncher, mLauncher.getString(R.string.much_uninstall_prompt), Toast.LENGTH_SHORT).show();
                    } else {
                        showUninstallDialog(cpn.getPackageName());
                    }
                }
                break;
            case SHARE_MENU_ID:
                showShareDialog(info);
                break;
            case DIY_MENU_ID:
                Toast.makeText(mLauncher, "diy app" + info.title, Toast.LENGTH_SHORT).show();
                break;
            case POWER_MENU_ID:
                if (cpn != null) {
                    skipPowerUI(cpn.getPackageName());
                }
                break;
            default:
                break;
            }
        }

    }

    private void showUninstallDialog(String pkgName) {
        if (mLauncher != null) {
            if (!TextUtils.isEmpty(pkgName)) {
                Uri packageUri = Uri.parse("package:" + pkgName);
                Intent deleteIntent = new Intent();
                deleteIntent.setAction(Intent.ACTION_DELETE);
                deleteIntent.setData(packageUri);
                deleteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mLauncher.startActivity(deleteIntent);
            }
        }

    }

    private void showShareDialog(ItemInfo info) {
        if (mLauncher != null) {
            mLauncher.showUninstallSharePrompt(info, null);
            MuchUninstallSharePrompt uninstall = mLauncher.getUninstallSharePrompt();
            if (null != uninstall) {
                uninstall.shareItemInfo();
            }
            uninstall = null;
        }
    }

    private void skipPowerUI(String pgkName) {
        Intent powerIntent = new Intent();
        powerIntent.setAction(APP_POWER_ACTION);
        powerIntent.putExtra(EXTRA_PKG_NAME, pgkName);
        powerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mLauncher.startActivity(powerIntent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void registerLocalBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MuchNavBroadcast.ACTION_SHOW_NAVIGATION_BAR);
        filter.addAction(MuchNavBroadcast.ACTION_HIDE_NAVIGATION_BAR);
        LocalBroadcastManager.getInstance(mLauncher).registerReceiver(mFloatMenuReceiver, filter);
    }

    private void unRegisterLocalBroadcast() {
        LocalBroadcastManager.getInstance(mLauncher).unregisterReceiver(mFloatMenuReceiver);
    }

    BroadcastReceiver mFloatMenuReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MuchNavBroadcast.ACTION_SHOW_NAVIGATION_BAR.equals(intent.getAction())
                    || MuchNavBroadcast.ACTION_HIDE_NAVIGATION_BAR.equals(intent.getAction())) {
                updateFloatMenuPos();
            }
        }
    };

    private void updateFloatMenuPos() {
        if (mCurrentMenu != null) {
            mCurrentMenu.close(true);
        }
    }

    public void onDestory() {
        closeFloatMenu();
        unRegisterLocalBroadcast();
    }

    private boolean isSystemApp(Context context,String pkgName) {
        try {
            // 过滤系统应用
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    || (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
