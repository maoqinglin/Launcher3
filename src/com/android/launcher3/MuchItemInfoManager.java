
package com.android.launcher3;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.android.launcher3.LauncherSettings.Favorites;
import com.android.launcher3.much.MuchConfig;

public class MuchItemInfoManager {
    private final String TAG = MuchItemInfoManager.class.getName();
    public static final String SIM_PKG = "com.android.stk";
    private static final String SIM1_ACTIVITY = "com.android.stk.StkLauncherActivity";
    private static final String SIM2_ACTIVITY = "com.android.stk.StkLauncherActivityII";
    public static final String PINYIN_PKG = "com.google.android.inputmethod.pinyin";
    private static final String PINYIN_ACTIVITY = "com.google.android.apps.inputmethod.libs.framework.core.LauncherActivity";
    private static final String GOOGLE_PKG = "com.google.android.apps.maps";
    public static final String PKG_ADD = "PKG_ADD";
    public static final String PKG_DELETE = "PKG_DELETE";
    public static final String OLD_DB_AUTHORITY = "com.ireadygo.app.much.settings";
    public static final Uri MUCH_OLD_LAUNCHER_DB_URI = Uri.parse("content://"
            + OLD_DB_AUTHORITY + "/" + LauncherProvider.TABLE_FAVORITES + "?"
            + LauncherProvider.PARAMETER_NOTIFY + "=true");
    private static final Uri COPY_OLD_DATA_FLAG_URI = Uri.parse("content://" +
            OLD_DB_AUTHORITY + "/" + "olddata");

    private static int sCellCountX = 4;
    private static int sCellCountY = 4;
    private final Context mContext;
    private PackageManager mPackageManager;

    public MuchItemInfoManager(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
//        sCellCountX = context.getResources().getInteger(R.integer.cell_count_x);
//        sCellCountY = context.getResources().getInteger(R.integer.cell_count_y);
    }

    public void updateScreenForShortcutInfo(int position, int newPosition) {
        arrangeItemInfosInModel(position, newPosition);
        arrangeItemInfoInDB(position, newPosition);
    }

    private void arrangeItemInfoInDB(int position, int newPosition) {
        LauncherProvider provider = LauncherAppState.getLauncherProvider();
        final String updateWhere = LauncherSettings.Favorites.SCREEN
                + "=? and container = ?";
        final String[] whereArgs = new String[] {
                position + "",
                LauncherSettings.Favorites.CONTAINER_DESKTOP + ""
        };
        ContentValues values = new ContentValues();
        values.put(LauncherSettings.Favorites.SCREEN, newPosition);
        provider.update(LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values,
                updateWhere, whereArgs);
    }

    private void arrangeItemInfosInModel(int position, int newPosition) {
        synchronized (LauncherModel.sBgLock) {
            for (final ItemInfo item : LauncherModel.sBgWorkspaceItems) {
                if (LauncherSettings.Favorites.CONTAINER_DESKTOP == item.container
                        && item.screenId == position) {
                    item.screenId = newPosition;
                }
            }
            for (final ItemInfo item : LauncherModel.sBgAppWidgets) {
                if (LauncherSettings.Favorites.CONTAINER_DESKTOP == item.container
                        && item.screenId == position) {
                    item.screenId = newPosition;
                }
            }
        }
    }

    public void loadAllShortcutInfosIfNecessary() {
        initDatabase();
    }

    public void shareItemInfo(String packageName, String label) {
        if (packageName != null && packageName.length() > 0 && label != null) {
            String Url = queryUrl(packageName);
            if (Url != null) {
                Resources res = mContext.getResources();
                String msg = String.format(res.getString(R.string.much_share_message_with_url), "<"
                        + label + ">", Url);
                startAcitivityForSharing(msg);
            } else {
                Resources res = mContext.getResources();
                String msg = String.format(res.getString(R.string.much_share_message_without_url),
                        "<" + label + ">");
                startAcitivityForSharing(msg);
            }
        }
    }

    private void startAcitivityForSharing(String msg) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.much_share_subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
        Intent selectedIntent = Intent.createChooser(shareIntent,
                mContext.getResources().getString(R.string.much_share_send_to_title));
        selectedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(selectedIntent);
    }

    private String queryUrl(String packageName) {
        ContentResolver cr = mContext.getContentResolver();
        final String selection = "app_pkg_name" + " = " + "'" + packageName + "'";
        Uri CONTENT_URI_APP_SHARE =
                Uri.parse("content://" + "com.ireadygo.provider.app_store" + "/" + "app_share");
        Cursor c = cr.query(CONTENT_URI_APP_SHARE, null, selection, null, null);
        String url = null;
        if (c == null) {
            return url;
        }
        try {
            final int appDownloadAddress = c.getColumnIndexOrThrow("app_download_address");

            while (c.moveToNext()) {
                try {
                    url = c.getString(appDownloadAddress);
                } catch (Exception e) {
                    Log.w(TAG, "Not Found Corresponding Url", e);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return url;
    }

    public void deleteItemInfoWithPackage(String packageName) {
        deletePackage(mPackageManager, packageName);
    }

    private void deletePackage(PackageManager pm, String pkgName) {
        try {
            java.lang.reflect.Method method = pm.getClass().getDeclaredMethod(
                    "deletePackage", String.class, IPackageDeleteObserver.class, int.class);
            method.invoke(pm, pkgName, new IPackageDeleteObserver.Stub() {
                @Override
                public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                }
            }, 0);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addShortcutInfo(Launcher launcher,
            ArrayList<AppInfo> apps) {
        ArrayList<ShortcutInfo> items = bindAppsAdded(apps);
        for (ShortcutInfo item : items) {
            View shortcut = launcher.createShortcut(item);
            launcher.addWorkspaceCountByItemInfo((int)item.screenId);
            launcher.getWorkspace().addInScreen(shortcut, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                    item.screenId, item.cellX,
                    item.cellY, 1, 1, false);
        }
    }

    public ArrayList<ShortcutInfo> bindAppsAdded(ArrayList<AppInfo> apps) {
        ArrayList<ShortcutInfo> items = new ArrayList<ShortcutInfo>();

        Coordinate cor = findLastCoordinate();
        int init = cor.screen * (sCellCountX * sCellCountY) + cor.y * sCellCountY + cor.x + 1;
        for(AppInfo info : apps) {
            info.screenId = init / (sCellCountX * sCellCountY);
            int a = init % (sCellCountX * sCellCountY);
            info.cellX = a % sCellCountY;
            info.cellY = a / sCellCountY;
            init++;
            if (info.screenId >= Launcher.SCREEN_COUNT) {
                Launcher.SCREEN_COUNT = (int)info.screenId + 1;
                MuchConfig.getInstance().setPageCount(Launcher.SCREEN_COUNT);
            }

            ShortcutInfo item = info.makeShortcut();
            item.setIcon(info.iconBitmap);
            LauncherModel.addOrMoveItemInDatabase(mContext,
                    item, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                    item.screenId, item.cellX, item.cellY);
            items.add(item);
        }

        return items;
    }

    public void bindAppsRemoved(ArrayList<String> packageNames) {
        ArrayList<ItemInfoObject> dbItems = loadItemsFromDB();
        ArrayList<ItemInfoObject> deleteItems = new ArrayList<ItemInfoObject>();
        for (ItemInfoObject item : dbItems) {
            if (Favorites.ITEM_TYPE_APPWIDGET == item.itemType || Favorites.ITEM_TYPE_FOLDER == item.itemType) {
                continue;
            }

            if(packageNames.contains(item.packageName)) {
                deleteItems.add(item);
            }
        }

        deleteItemFromDB(deleteItems);
    }

    private void initDatabase() {
        ArrayList<ItemInfoObject> dbItems = loadItemsFromDB();
        ArrayList<ItemInfoObject> mainItems = queryAllApps();
        //edit begin by lilu 20140416--去除，不需要对旧数据库进行备份了
        // copy the old db data
//        if (copyOldDBData(findLastCoordinate(dbItems))) {
//            return;
//        }
        //edit end by lilu 20140416
        for (ItemInfoObject item : dbItems) {
            if (Favorites.ITEM_TYPE_APPWIDGET == item.itemType
                    || Favorites.ITEM_TYPE_FOLDER == item.itemType) {
                continue;
            }

            if (mainItems.contains(item)) {
                mainItems.remove(item);
            }
        }
        insertIntoDB(mainItems, findLastCoordinate());
    }

    public HashMap<String, ArrayList<String>> getAddAndDeleteItemsWhenMounted() {
        ArrayList<String> addItems = new ArrayList<String>();
        ArrayList<String> deleteItems = new ArrayList<String>();
        ArrayList<ItemInfoObject> dbItems = loadItemsFromDB();
        ArrayList<ItemInfoObject> mainItems = queryAllApps();

        for (ItemInfoObject item : dbItems) {
            if (Favorites.ITEM_TYPE_APPWIDGET == item.itemType || Favorites.ITEM_TYPE_FOLDER == item.itemType) {
                continue;
            }

            if(mainItems.contains(item)) {
                mainItems.remove(item);
            } else {
                if(GOOGLE_PKG.equals(item.packageName) || PINYIN_PKG.equals(item.packageName) || SIM_PKG.equals(item.packageName)) {
                    continue;
                }
                deleteItems.add(item.packageName);
            }
        }

        for (ItemInfoObject item : mainItems) {
           addItems.add(item.packageName);
        }
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        map.put(PKG_ADD, addItems);
        map.put(PKG_DELETE, deleteItems);

        return map;
    }

    private void deleteItemFromDB(ArrayList<ItemInfoObject> items) {
        final ContentResolver cr = mContext.getContentResolver();
        for (int i = 0; i < items.size(); i++) {
            ItemInfoObject info = items.get(i);
            final Uri uriToDelete = LauncherSettings.Favorites.getContentUri(info.id, false);
            cr.delete(uriToDelete, null, null);
        }
    }

    private void insertIntoDB(ArrayList<ItemInfoObject> items, Coordinate cor) {
        LauncherProvider provider = LauncherAppState.getLauncherProvider();
        int position = cor.screen * (sCellCountX * sCellCountY) + cor.y * sCellCountY + cor.x + 1;
        ContentValues[] values = new ContentValues[items.size()];
        for (int i = 0; i < items.size(); i++) {
            ItemInfoObject info = items.get(i);
            info.screen = position / (sCellCountX * sCellCountY);
            int a = position % (sCellCountX * sCellCountY);
            Coordinate co = new Coordinate(a % sCellCountY, a / sCellCountY);
            co.screen = info.screen;
            info.coordinate = co;
            position++;
            if(info.screen >= Launcher.SCREEN_COUNT) {
                Launcher.SCREEN_COUNT = info.screen + 1;
                MuchConfig.getInstance().setPageCount(Launcher.SCREEN_COUNT);
            }

            ContentValues value = buildInsertItem(mPackageManager, info, provider.generateNewItemId());
            if(value.size() != 0) {
                values[i] = value;
            }
        }
//        values[values.length - 1] = buildMuchGameShortcut(init, provider);
        provider.bulkInsert(Favorites.CONTENT_URI_NO_NOTIFICATION, values);
    }

    private ContentValues buildInsertItem(PackageManager packageManager,
            ItemInfoObject item, long newID) {
        ContentValues values = new ContentValues();
        values.clear();
        ActivityInfo info;
        try {
            ComponentName cn;
            try {
                cn = new ComponentName(item.packageName, item.className);
                info = packageManager.getActivityInfo(cn, 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                String[] packages = packageManager
                        .currentToCanonicalPackageNames(new String[] {
                                item.packageName
                        });
                cn = new ComponentName(packages[0], item.className);
                info = packageManager.getActivityInfo(cn, 0);
            }

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            values.put(Favorites._ID, newID);
            values.put(Favorites.INTENT, intent.toUri(0));
            values.put(Favorites.CONTAINER, Favorites.CONTAINER_DESKTOP);
            values.put(Favorites.TITLE, info.loadLabel(packageManager)
                    .toString());
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
            values.put(Favorites.SCREEN, item.screen);
            values.put(Favorites.CELLX, item.coordinate.x);
            values.put(Favorites.CELLY, item.coordinate.y);
            values.put(Favorites.SPANX, 1);
            values.put(Favorites.SPANY, 1);
            Bitmap bitmap = LauncherAppState.getInstance().getIconCache().getIcon(intent);
            ItemInfo.writeBitmap(values, bitmap);
            return values;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Unable to add item: " + item.packageName + "/"
                    + item.className, e);
        }
        return values;
    }
    private ArrayList<ItemInfoObject> loadItemsFromDB() {
        ArrayList<ItemInfoObject> items = new ArrayList<ItemInfoObject>();
        final AppWidgetManager widgets = AppWidgetManager.getInstance(mContext);
        final ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(Favorites.CONTENT_URI, new String[] {
                Favorites._ID, Favorites.INTENT, Favorites.ITEM_TYPE, Favorites.CONTAINER,
                Favorites.SCREEN, Favorites.CELLX, Favorites.CELLY,
                Favorites.SPANX, Favorites.SPANY, Favorites.APPWIDGET_ID
        }, null, null, null);

        final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
        final int intentIndex = c.getColumnIndexOrThrow(Favorites.INTENT);
        final int itemTypeIndex = c.getColumnIndexOrThrow(Favorites.ITEM_TYPE);
        final int containerIndex = c.getColumnIndexOrThrow(Favorites.CONTAINER);
        final int screenIndex = c.getColumnIndexOrThrow(Favorites.SCREEN);
        final int cellXIndex = c.getColumnIndexOrThrow(Favorites.CELLX);
        final int cellYIndex = c.getColumnIndexOrThrow(Favorites.CELLY);
        final int spanXIndex = c.getColumnIndexOrThrow(Favorites.SPANX);
        final int spanYIndex = c.getColumnIndexOrThrow(Favorites.SPANY);
        final int appWidgetIdIndex = c
                .getColumnIndexOrThrow(Favorites.APPWIDGET_ID);
        String intentDescription;
        Intent intent;
        try {
            while (c.moveToNext()) {
                ItemInfoObject item = new ItemInfoObject();
                item.id = c.getInt(idIndex);
                item.itemType = c.getInt(itemTypeIndex);
                item.container = c.getInt(containerIndex);
                switch (item.itemType) {
                    case Favorites.ITEM_TYPE_APPLICATION:
                    case Favorites.ITEM_TYPE_SHORTCUT:
                        intentDescription = c.getString(intentIndex);
                        try {
                            intent = Intent.parseUri(intentDescription, 0);
                        } catch (URISyntaxException e) {
                            continue;
                        }
                        item.packageName = getPackageName(intent);
                        item.className = getClassName(intent);
                        break;
                    case Favorites.ITEM_TYPE_APPWIDGET:
                        if (item.container != Favorites.CONTAINER_DESKTOP
                                && item.container != Favorites.CONTAINER_HOTSEAT) {
                            continue;
                        }
                        int appWidgetId = c.getInt(appWidgetIdIndex);
                        AppWidgetProviderInfo provider = widgets
                                .getAppWidgetInfo(appWidgetId);

                        if ((provider == null || provider.provider == null || provider.provider
                                .getPackageName() == null)) {
                            continue;
                        }

                        item.packageName = provider.provider.getPackageName();
                        item.className = provider.provider.getClassName();
                        break;
                    case Favorites.ITEM_TYPE_FOLDER:
                    default:
                        break;
                }
                Coordinate cor = new Coordinate();
                cor.x = c.getInt(cellXIndex);
                cor.y = c.getInt(cellYIndex);
                item.spanX = c.getInt(spanXIndex);
                item.spanY = c.getInt(spanYIndex);
                item.screen = c.getInt(screenIndex);
                cor.screen = item.screen;
                item.coordinate = cor;
                items.add(item);
            }
        } catch (Exception e) {
            items.clear();
        } finally {
            c.close();
        }
        return items;
    }

    public boolean copyOldDBData(Coordinate cor) {
        if (hasCopyOldDBData()) {
            return false;
        }
        setHasCopyOldDBData();
        LinkedHashMap<Integer, ItemInfoObject> allAppMap = loadItemsFrmOldDB(cor);
        if ((null == allAppMap) || (0 == allAppMap.size())) {
            return false;
        }
        insertOldDataToDB(allAppMap, cor);
        return true;
    }

    private LinkedHashMap<Integer, ItemInfoObject> loadItemsFrmOldDB(
            Coordinate startCor) {
        LinkedHashMap<Integer, ItemInfoObject> allItemMap = new LinkedHashMap<Integer, MuchItemInfoManager.ItemInfoObject>();
        ArrayList<ItemInfoObject> items = new ArrayList<ItemInfoObject>();
        final ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(MUCH_OLD_LAUNCHER_DB_URI, new String[] {
                Favorites._ID, Favorites.INTENT, Favorites.ITEM_TYPE,
                Favorites.CONTAINER, Favorites.SCREEN, Favorites.CELLX,
                Favorites.CELLY, Favorites.SPANX, Favorites.SPANY,
                Favorites.APPWIDGET_ID, Favorites.TITLE }, null, null, null);
        if (null == c) {
            return null;
        }
        final int idIndex = c
                .getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
        final int intentIndex = c.getColumnIndexOrThrow(Favorites.INTENT);
        final int itemTypeIndex = c.getColumnIndexOrThrow(Favorites.ITEM_TYPE);
        final int containerIndex = c.getColumnIndexOrThrow(Favorites.CONTAINER);
        final int spanXIndex = c.getColumnIndexOrThrow(Favorites.SPANX);
        final int spanYIndex = c.getColumnIndexOrThrow(Favorites.SPANY);
        final int titleIndex = c.getColumnIndexOrThrow(Favorites.TITLE);
        String intentDescription;
        Intent intent;
        boolean isOnLauncher = false;
        try {
            while (c.moveToNext()) {
                ItemInfoObject item = new ItemInfoObject();
                item.id = c.getInt(idIndex);
                item.itemType = c.getInt(itemTypeIndex);
                item.container = c.getInt(containerIndex);
                item.title = c.getString(titleIndex);
                item.spanX = c.getInt(spanXIndex);
                item.spanY = c.getInt(spanYIndex);
                if (item.itemType == 6) {// the pre-install app,ignore
                    continue;
                }
                if ((Favorites.CONTAINER_DESKTOP == item.container)
                        || (Favorites.CONTAINER_HOTSEAT == item.container)) {
                    isOnLauncher = true;
                } else {
                    isOnLauncher = false;
                }
                switch (item.itemType) {
                case Favorites.ITEM_TYPE_APPLICATION:
                case Favorites.ITEM_TYPE_SHORTCUT:
                    intentDescription = c.getString(intentIndex);
                    try {
                        intent = Intent.parseUri(intentDescription, 0);
                    } catch (URISyntaxException e) {
                        continue;
                    }
                    item.packageName = getPackageName(intent);
                    if (isSystemApp(item.packageName)) {
                        // do not handle the system app
                        continue;
                    }
                    item.className = getClassName(intent);
                    if (isOnLauncher) {
                        allItemMap.put(item.id, item);
                    } else {
                        FolderItemObject folderItem = (FolderItemObject) allItemMap
                                .get(item.container);
                        if (null == folderItem) {
                            folderItem = new FolderItemObject(item.container);
                        }
                        folderItem.insertItem(item);
                        allItemMap.put(item.container, folderItem);
                    }
                    break;
                case Favorites.ITEM_TYPE_APPWIDGET:
                    continue;
                case Favorites.ITEM_TYPE_FOLDER:
                    FolderItemObject folderItem = (FolderItemObject) allItemMap
                            .get(item.id);
                    if (null == folderItem) {
                        folderItem = new FolderItemObject(item.id);
                    }
                    folderItem.itemType = item.itemType;
                    folderItem.container = item.container;
                    folderItem.id = item.id;
                    folderItem.title = item.title;
                    allItemMap.put(item.id, folderItem);
                default:
                    break;
                }
            }
        } catch (Exception e) {
            items.clear();
        } finally {
            c.close();
        }
        return allItemMap;
    }

    private void insertOldDataToDB(
            LinkedHashMap<Integer, ItemInfoObject> oldDataMap,
            Coordinate startCor) {
        if ((null == oldDataMap) || (oldDataMap.size() == 0)) {
            return;
        }
        int position = startCor.screen * (sCellCountX * sCellCountY)
                + startCor.y * sCellCountY + startCor.x + 1;
        LauncherProvider provider = LauncherAppState.getLauncherProvider();
        PackageManager pm = mContext.getPackageManager();
        ArrayList<ContentValues> allItems = new ArrayList<ContentValues>();
        Iterator iterator = oldDataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            int id = (Integer) entry.getKey();
            if (oldDataMap.get(id) instanceof FolderItemObject) {
                // build folder item
                int insertNum = buildFolderItems(
                        (FolderItemObject) oldDataMap.get(id), allItems,
                        provider, pm, position);
                if (0 != insertNum) {// empty folder,do not add position
                    position++;
                }
            } else {
                // build app item
                allItems.add(buildInsertOldItem(pm,
                        (ItemInfoObject) oldDataMap.get(id),
                        (int) provider.generateNewItemId(), position++));
            }
        }
        ContentValues[] values = new ContentValues[allItems.size()];
        for (int i = 0; i < allItems.size(); i++) {
            values[i] = allItems.get(i);
        }
        provider.bulkInsert(Favorites.CONTENT_URI_NO_NOTIFICATION, values);
    }

    private int buildFolderItems(FolderItemObject folderItem,
            ArrayList<ContentValues> valuesList, LauncherProvider provider,
            PackageManager pm, int position) {
        if ((null == folderItem) || (folderItem.isEmptyFolder())) {
            return 0;
        }
        int insertNum = 0;
        // insert the folder first
        int folderIndex = (int) provider.generateNewItemId();
        valuesList
                .add(buildInsertOldItem(pm, folderItem, folderIndex, position));
        insertNum++;
        // insert the item in the folder
        ArrayList<ItemInfoObject> itemList = folderItem.getItems();
        int itemStartPos = 0;
        for (ItemInfoObject item : itemList) {
            item.container = folderIndex;
            valuesList.add(buildInsertOldItem(pm, item,
                    (int) provider.generateNewItemId(), itemStartPos++));
            insertNum++;
        }
        return insertNum;
    }


    private boolean hasCopyOldDBData() {
        boolean result = true;
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(COPY_OLD_DATA_FLAG_URI, null, null, null, null);
        if (null == cursor) {
            return true;
        }
        while (cursor.moveToNext()) {
            int data = cursor.getInt(cursor.getColumnIndex("copy_flag"));
            result = (data == 1);
        }
        return result;
    }

    private void setHasCopyOldDBData() {
        ContentResolver cr = mContext.getContentResolver();
        cr.update(COPY_OLD_DATA_FLAG_URI, new ContentValues(), new String(), null);
    }

    private ContentValues buildInsertOldItem(PackageManager packageManager,
            ItemInfoObject item, long newID, int position) {
        LauncherApplication app = (LauncherApplication) mContext;
        ContentValues values = new ContentValues();
        values.clear();
        ActivityInfo info;
        int screen = position / (sCellCountX * sCellCountY);
        int a = position % (sCellCountX * sCellCountY);
        int x = a % sCellCountY;
        int y = a / sCellCountY;
        item.id = (int) newID;
        try {
            if (Favorites.ITEM_TYPE_FOLDER == item.itemType) {
                values.put(Favorites._ID, newID);
                values.put(Favorites.CONTAINER, Favorites.CONTAINER_DESKTOP);
                values.put(Favorites.TITLE, item.title);
                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_FOLDER);
                values.put(Favorites.SCREEN, screen);
                values.put(Favorites.CELLX, x);
                values.put(Favorites.CELLY, y);
                values.put(Favorites.SPANX, 1);
                values.put(Favorites.SPANY, 1);
                return values;
            }
            ComponentName cn;
            try {
                cn = new ComponentName(item.packageName, item.className);
                info = packageManager.getActivityInfo(cn, 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                String[] packages = packageManager
                        .currentToCanonicalPackageNames(new String[] { item.packageName });
                cn = new ComponentName(packages[0], item.className);
                info = packageManager.getActivityInfo(cn, 0);
            }

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            values.put(Favorites._ID, newID);
            values.put(Favorites.INTENT, intent.toUri(0));
            values.put(Favorites.CONTAINER, item.container);
            values.put(Favorites.TITLE, info.loadLabel(packageManager)
                    .toString());
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
            values.put(Favorites.SCREEN, screen);
            values.put(Favorites.CELLX, x);
            values.put(Favorites.CELLY, y);
            values.put(Favorites.SPANX, 1);
            values.put(Favorites.SPANY, 1);
            Bitmap bitmap = LauncherAppState.getInstance().getIconCache().getIcon(intent);
            ItemInfo.writeBitmap(values, bitmap);
            return values;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Unable to add item: " + item.packageName + "/"
                    + item.className, e);
        }
        return values;
    }

    public ArrayList<ItemInfoObject> querySystemApps() {
        ArrayList<ItemInfoObject> systemItems = new ArrayList<ItemInfoObject>();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = mPackageManager.queryIntentActivities(
                mainIntent, 0);

        for (ResolveInfo info : infos) {
            if (isSystemApp(info.activityInfo.applicationInfo)) {
                ItemInfoObject item = new ItemInfoObject();
                item.packageName = info.activityInfo.applicationInfo.packageName;
                item.className = info.activityInfo.name;
                systemItems.add(item);
            }
        }

        return systemItems;
    }

    public ArrayList<ItemInfoObject> queryUserApps() {
        ArrayList<ItemInfoObject> userItems = new ArrayList<ItemInfoObject>();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = mPackageManager.queryIntentActivities(
                mainIntent, 0);

        for (ResolveInfo info : infos) {
            if (!isSystemApp(info.activityInfo.applicationInfo)) {
                ItemInfoObject item = new ItemInfoObject();
                item.packageName = info.activityInfo.applicationInfo.packageName;
                item.className = info.activityInfo.name;
                userItems.add(item);
            }
        }

        return userItems;
    }

    public ArrayList<ItemInfoObject> queryAllApps() {
        ArrayList<ItemInfoObject> items = new ArrayList<ItemInfoObject>();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = mPackageManager.queryIntentActivities(
                mainIntent, 0);

        for (ResolveInfo info : infos) {
            ItemInfoObject item = new ItemInfoObject();
            item.packageName = info.activityInfo.applicationInfo.packageName;
            item.className = info.activityInfo.name;
            items.add(item);
        }

        return items;
    }

    public boolean isSystemApp(android.content.pm.ApplicationInfo app) {
        return ((app.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0)
                || ((app.flags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public boolean isSystemApp(String packageName) throws NameNotFoundException {
        android.content.pm.ApplicationInfo app = mPackageManager.getPackageInfo(packageName, 0).applicationInfo;
        return isSystemApp(app);
    }

    public void enableSpecialComponent() {
        ComponentName sim1 = new ComponentName(SIM_PKG, SIM1_ACTIVITY);
        ComponentName sim2 = new ComponentName(SIM_PKG, SIM2_ACTIVITY);
        ComponentName pinyin = new ComponentName(PINYIN_PKG,
                PINYIN_ACTIVITY);
        //edit by lilu 20140319
        try {
            mPackageManager.setComponentEnabledSetting(sim1,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            mPackageManager.setComponentEnabledSetting(sim2,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            //edit by linmaoqing 2014-04-01
            /*mPackageManager.setComponentEnabledSetting(pinyin,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);*/
            //不显示Google拼音输入法图标  edit by linmaoqing 2014-04-01
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        //end edit by lilu 20140319
        }
    }

    private Coordinate findLastCoordinate() {
        Coordinate cor = new Coordinate(0, 0);
        cor.screen = 0;
        int position = cor.screen * (sCellCountX * sCellCountY) + cor.y * sCellCountY + cor.x;
        ArrayList<ItemInfo> items = LauncherModel
                .getItemsInLocalCoordinates(mContext);
        for (ItemInfo item : items) {
            if (LauncherSettings.Favorites.CONTAINER_DESKTOP != item.container) {
                continue;
            }
            int pos = (int)item.screenId * (sCellCountX * sCellCountY) + (item.cellY + item.spanY - 1) * sCellCountY + item.cellX + item.spanX - 1;
            if(pos > position) {
                cor.screen = (int)item.screenId;
                cor.x = item.cellX + item.spanX - 1;
                cor.y = item.cellY + item.spanY - 1;
                position = pos;
            }
        }
        return cor;
    }

    private Coordinate findLastCoordinate(ArrayList<ItemInfoObject> items) {
        Coordinate cor = new Coordinate(0, 0);
        cor.screen = 0;
        int position = cor.screen * (sCellCountX * sCellCountY) + cor.y * sCellCountY + cor.x;
        for (ItemInfoObject item : items) {
            if (item.container != Favorites.CONTAINER_DESKTOP) {
                continue;
            }
            int pos = item.screen * (sCellCountX * sCellCountY) + (item.coordinate.y + item.spanY - 1) * sCellCountY + item.coordinate.x + item.spanX - 1;
            if(pos > position) {
                cor.screen = item.screen;
                cor.x = item.coordinate.x + item.spanX - 1;
                cor.y = item.coordinate.y + item.spanY - 1;
                position = pos;
            }
        }
        return cor;
    }

    private String getPackageName(Intent intent) {
        if (intent != null) {
            String packageName = intent.getPackage();
            if (packageName == null && intent.getComponent() != null) {
                packageName = intent.getComponent().getPackageName();
            }
            if (packageName != null) {
                return packageName;
            }
        }
        return "";
    }

    private String getClassName(Intent intent) {
        String className = "";
        if (intent != null) {
            if (intent.getComponent() != null) {
                className = intent.getComponent().getClassName();
            }
        }
        return className;
    }

    private class Coordinate {
        public int x;
        public int y;
        public int screen;

        public Coordinate() {
        }

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Coordinate)) {
                return false;
            }
            Coordinate cor = (Coordinate) other;
            return this.x == cor.x && this.y == cor.y;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }
    }

    private class ItemInfoObject {
        String packageName;
        String className;
        String title;
        int screen;
        Coordinate coordinate;
        int spanX;
        int spanY;
        int container;
        int itemType;
        int id;

        public ItemInfoObject() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof ItemInfoObject)) {
                return false;
            }

            ItemInfoObject lhs = (ItemInfoObject) o;

            return packageName.equals(lhs.packageName)
                    && className.equals(lhs.className);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((className == null) ? 0 : className.hashCode());
            result = prime * result
                    + ((packageName == null) ? 0 : packageName.hashCode());
            return result;
        }
    }

    private class FolderItemObject extends ItemInfoObject {
        private ArrayList<ItemInfoObject> items = new ArrayList<MuchItemInfoManager.ItemInfoObject>();
        private boolean isEmpty = true;

        public FolderItemObject (int id) {
            this.id = id;
        }

        public void insertItem(ItemInfoObject item) {
            items.add(item);
            isEmpty = false;
        }
        public ArrayList<ItemInfoObject> getItems() {
            return items;
        }
        public boolean isEmptyFolder() {
            return isEmpty;
        }
    }
}
