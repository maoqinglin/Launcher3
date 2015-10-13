
package com.android.launcher3.much;

import com.android.launcher3.LauncherAppState;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;

public final class MuchConfig {
    public static final boolean SUPPORT_MUCH_STYLE = true;
    public static final int KEYCODE_BUTTON_L1 = 102;
    public static final int KEYCODE_BUTTON_R1 = 103;
    public static final String ACTION_WEIXIN_SHORTCUT = "com.tencent.mm.action.BIZSHORTCUT";
    public static final String KEY_WEIXIN_USER_NAME = "LauncherUI.Shortcut.Username";
    public static final String MUCH_GAME_PHONE_USER_NAME = "gh_e3cb827e11a9";
    public static final String APP_WIDGET_SYSTEM_MESSAGE ="com.ireadygo.app.dynamic.SystemMessageWidget";

    private static final String PAGE_COUNT = "PAGE_COUNT";
    private static final String DB_CREATED_BUT_REMAIN_APPS_NOT_LOADED = "DB_CREATED_BUT_REMAIN_APPS_NOT_LOADED";

    private static final String ALREADY_SET_DEFAULT_WALLPAPER = "ALREADY_SET_DEFAULT_WALLPAPER";
    public static final String MUCH_LAUNCH_ICON_KEY = "much_launcher_icon_sw_key";
    
    private static MuchConfig sInstatnce;
    private Context mContext;
    public static final String LAUNCHER_PREFS = "com.android.launcher3.prefs";
    public static final String SCREEN_EFFECT_PREFS = "screenEffect";
    
    public static void init(Context context) {
        if (sInstatnce == null) {
            synchronized (MuchConfig.class) {
                if (sInstatnce == null) {
                    sInstatnce = new MuchConfig(context.getApplicationContext());
                }
            }
        }
    }

    public static MuchConfig getInstance() {
        if(sInstatnce == null) {
            throw new IllegalAccessError("init first !!!");
        }

        return sInstatnce;
    }

    private MuchConfig(Context context) {
        mContext = context;
    }

    public int getPageCount() {
        String spfkey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = mContext.getSharedPreferences(spfkey,
                Context.MODE_PRIVATE);
        int pageCount = sp.getInt(PAGE_COUNT, 5);
        return pageCount;
    }

    public void setPageCount(int screenCount) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = mContext.getSharedPreferences(spKey,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putInt(PAGE_COUNT, screenCount < 1 ? 1 : screenCount);
        editor.commit();
    }

    public boolean needLoadAllItemInfos() {
        String spfkey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = mContext.getSharedPreferences(spfkey,
                Context.MODE_PRIVATE);
        boolean load = sp.getBoolean(DB_CREATED_BUT_REMAIN_APPS_NOT_LOADED,
                false);
        return load;
    }

    public void setLoadAllItemInfosNotNeed() {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = mContext.getSharedPreferences(spKey,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(DB_CREATED_BUT_REMAIN_APPS_NOT_LOADED, true);
        editor.commit();
    }

    public static boolean isI5Platform() {
        return Build.MODEL.startsWith("MUCH i5");
    }

    public boolean isLauncherShortcutNeedBg(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean(MUCH_LAUNCH_ICON_KEY, true);
    }
}
