
package com.android.launcher3.much;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.launcher3.R;

public class LoadThemeUtil {
    public static final boolean THEME_APP_NEED_DECORATE = true;
    private static final String THEME = "much_theme.xml";
    private static final String SEPARATOR = "/";
    private static final String THEME_ID = "appfilter_snail";
    private static final String THEME_ICON_PATTERN = THEME_ID + SEPARATOR + "much_icon_bg.png";
    private static final String THEME_ICON_MASK = THEME_ID + SEPARATOR + "much_icon_mask.png";

    public static class ThemeApp {
        String mPackageName;
        String mClassName;
        String mIconId;
        String mThemeId;

        public ThemeApp(String packageName, String className) {
            mPackageName = packageName;
            mClassName = className;
        }

        public ThemeApp(String packageName, String className, String iconId,
                String themeId) {
            mPackageName = packageName;
            mClassName = className;
            mIconId = iconId;
            mThemeId = themeId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ThemeApp)) {
                return false;
            }

            ThemeApp otherApp = (ThemeApp) other;

            return mPackageName.equals(otherApp.mPackageName)
                    && mClassName.equals(otherApp.mClassName);
        }

        @Override
        public int hashCode() {
            int result = 3 * mPackageName.hashCode();
            result = 4 * mClassName.hashCode() + 28;
            return result;
        }

        @Override
        public String toString() {
            return "App [packageName=" + mPackageName + ", className="
                    + mClassName + ", iconid=" + mIconId + ", themeid="
                    + mThemeId + "]";
        }
    }

    public static ArrayList<ThemeApp> loadThemeInfos(Context context) {
        return loadThemeInfos(context, THEME_ID);
    }

    public static ArrayList<ThemeApp> loadThemeInfos(Context context,
            String themeId) {
        SAXParserFactory saxParserFatory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        SimpleSaxHandler ssh = new SimpleSaxHandler(themeId);

        try {
            saxParser = saxParserFatory.newSAXParser();
            AssetManager am = context.getAssets();
            InputStream inputStream = am.open(themeId + SEPARATOR + THEME);
            saxParser.parse(inputStream, ssh);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ssh.getApps();
    }

    public static Bitmap getThemeIcon(Context context, ThemeApp app) {
        return getThemeIcon(context, app.mThemeId + SEPARATOR + app.mIconId);
    }

    public static Bitmap getThemeIcon(Context context, String path) {
        AssetManager am = context.getAssets();
        try {
            return BitmapFactory.decodeStream(am.open(path));
        } catch (IOException e) {
            return null;
        }
    }

    public static Bitmap getIconPattern(Context context) {
        String path = THEME_ICON_PATTERN;
//        if(MuchConfig.getInstance().isLauncherShortcutNeedBg()){
//            path = THEME_ICON_PATTERN;
//        }else{
//            path = THEME_ICON_PATTERN_TRANSLUCENT;
//        }
        Bitmap bitmap = getThemeIcon(context, path);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.much_icon_bg);
        }
        return bitmap;
    }

    public static Bitmap getIconMask(Context context) {
        Bitmap bitmap = getThemeIcon(context, THEME_ICON_MASK);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.much_icon_mask);
        }
        return bitmap;
    }

    static class SimpleSaxHandler extends DefaultHandler {
        private static final String APPLICATION = "application";
        private static final String PACKAGE_NAME = "packageName";
        private static final String CLASS_NAME = "className";
        private static final String ICON_NAME = "iconName";
        private String mThemeId;

        ArrayList<ThemeApp> apps = new ArrayList<ThemeApp>();

        public SimpleSaxHandler(String themeId) {
            mThemeId = themeId;
        }

        public ArrayList<ThemeApp> getApps() {
            return apps;
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            if (APPLICATION.equals(localName)) {
                String packageName = attributes.getValue(PACKAGE_NAME);
                String className = attributes.getValue(CLASS_NAME);
                String iconName = attributes.getValue(ICON_NAME);
                apps.add(new ThemeApp(packageName, className, iconName,
                        mThemeId));
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {

        }
    }
}
