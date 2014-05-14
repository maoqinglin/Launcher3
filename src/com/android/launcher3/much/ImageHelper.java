package com.android.launcher3.much;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.widget.LinearLayout;

import com.android.launcher3.Folder;

public class ImageHelper {

    private static final String TAG = "ImageHelper";
    private static final float scaleFactor = 16; //背景图片缩放比例
    private static final float radius = 4; // 设置模糊度
    
    public static void blur(Context context, Folder folder, Bitmap bkg) {
        LinearLayout folderFrame = folder.getFolderFrameLayout();
        Bitmap overlay = Bitmap.createBitmap((int) (folderFrame.getWidth() / scaleFactor),
                (int) (folderFrame.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-folderFrame.getLeft() / scaleFactor, -folderFrame.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);

        canvas.drawBitmap(bkg, 0, 0, paint);
        overlay = FastBlur.doBlur(overlay, (int) radius, true); // 模糊处理
        overlay = Bitmap.createScaledBitmap(overlay, folderFrame.getWidth(), folderFrame.getHeight(), true);
        overlay = getRoundedCornerBitmap(overlay);
        
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), overlay);
        drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        folder.setFolderFrameBg(drawable);
    }
    
    /**
	 * 圆角处理
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		// 创建一个指定宽度和高度的空位图对象
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
		// 用该位图创建画布
		Canvas canvas = new Canvas(output);
		// 画笔对象
		final Paint paint = new Paint();
		// 画笔的颜色
		final int color = 0xff424242;
		// 矩形区域对象
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		// 未知
		final RectF rectF = new RectF(rect);
		// 拐角的半径
		final float roundPx = 20;
		// 消除锯齿
		paint.setAntiAlias(true);
		// 画布背景色
		canvas.drawARGB(0, 0, 0, 0);
		// 设置画笔颜色
		paint.setColor(color);
		// 绘制圆角矩形
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		// 未知
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		// 把该图片绘制在该圆角矩形区域中
		canvas.drawBitmap(bitmap, rect, rect, paint);
		// 最终在画布上呈现的就是该圆角矩形图片，然后我们返回该Bitmap对象
		return output;
	}
}
