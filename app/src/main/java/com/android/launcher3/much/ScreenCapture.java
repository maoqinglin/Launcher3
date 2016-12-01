package com.android.launcher3.much;

import java.io.File;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class ScreenCapture {

    private static final String SCREEN_CAP_URI = "content://com.ireadygo.provider.screen_capture";
    private static final String SCREEN_CAP_PKG_NAME = "com.ireadygo.app.screencapture";

    private Context mContext;

    public ScreenCapture(Context context) {
        mContext = context;
    }

    /**
     * 4.3以上调用
     * 
     * @return
     */
    public Bitmap captureScreen() {
        WindowManager manager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        if (Surface.ROTATION_0 == display.getRotation()) {
            try {
                Method method = Class.forName("android.view.SurfaceControl").getDeclaredMethod("screenshot",
                        int.class, int.class);
                Bitmap bm = (Bitmap) method
                        .invoke(null, displayMetrics.widthPixels,
                                displayMetrics.heightPixels);
                return bm;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        float[] dims = { displayMetrics.widthPixels,
                displayMetrics.heightPixels };
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.preRotate(display.getRotation() * 90);
        matrix.mapPoints(dims);
        dims[0] = Math.abs(dims[0]);
        dims[1] = Math.abs(dims[1]);

        Bitmap capBitmap = null;
        try {
            Method method = Class.forName("android.view.SurfaceControl").getDeclaredMethod("screenshot",
                    int.class, int.class);
            capBitmap = (Bitmap) method.invoke(null, (int) dims[0],
                    (int) dims[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (capBitmap == null) {
            return capBitmap;
        }

        Bitmap destBitmap = Bitmap.createBitmap(displayMetrics.widthPixels,
                displayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(destBitmap);
        canvas.translate(destBitmap.getWidth() / 2, destBitmap.getHeight() / 2);
        canvas.rotate(-display.getRotation() * 90);
        canvas.translate(-dims[0] / 2, -dims[1] / 2);
        canvas.drawBitmap(capBitmap, 0, 0, null);
        canvas.save();

        return destBitmap;
    }

    public String queryContentToShare(Context context, File capFile) {
        if (isAppInstalled(context, SCREEN_CAP_PKG_NAME)) {
            try {
                return context.getContentResolver().getType(Uri.parse(SCREEN_CAP_URI + capFile.getAbsolutePath()));
            } catch (Exception e) {
            } // Ignore
        }

        return "";
    }

    private boolean isAppInstalled(Context context, String pkgName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(pkgName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if (pi != null) {
                return true;
            }
        } catch (NameNotFoundException e) {
        } // Ignore

        return false;
    }

    public static class CaptureException extends Exception {

        private static final long serialVersionUID = 817619670694055224L;

        public CaptureException() {
            super();
        }

        public CaptureException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public CaptureException(String detailMessage) {
            super(detailMessage);
        }

        public CaptureException(Throwable throwable) {
            super(throwable);
        }
    }
}
