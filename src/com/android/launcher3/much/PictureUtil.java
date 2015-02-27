package com.android.launcher3.much;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.android.launcher3.R;

public class PictureUtil {

	/**
	 * 圆角处理
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		// 创建一个指定宽度和高度的空位图对象
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
				android.graphics.Bitmap.Config.ARGB_8888);
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
		final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
		// 最终在画布上呈现的就是该圆角矩形图片，然后我们返回该Bitmap对象
		canvas.drawBitmap(bitmap, src, rect, paint);
		
		return output;
	}

	/**
	 * 　为指定图片增加阴影
	 * 
	 * @param map
	 *            　图片
	 * @param radius
	 *            　阴影的半径
	 * @return
	 */
	public static Bitmap drawShadow(Bitmap map, int radius) {
		if (map == null)
			return null;

		BlurMaskFilter blurFilter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER);
		Paint shadowPaint = new Paint();
		shadowPaint.setMaskFilter(blurFilter);
		shadowPaint.setShadowLayer(1, 5.0f, 5.0f, Color.BLACK);

		int[] offsetXY = new int[2];
		Bitmap shadowImage = map.extractAlpha(shadowPaint, offsetXY);
		shadowImage = shadowImage.copy(Config.ARGB_8888, true);
		Canvas c = new Canvas(shadowImage);
		c.drawBitmap(map, -offsetXY[0], -offsetXY[1], null);
		return shadowImage;
	}

	/**
	 * Drawable--->Bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	/*
	 * 叠图函数
	 */
	public static Bitmap overlayBitmaps(int width, int height, Bitmap... bitmaps) {
		int maxWidth = 0;
		for (Bitmap bitmap : bitmaps) {
			if (bitmap.getWidth() >= maxWidth) {
				maxWidth = bitmap.getWidth();
			}
		}
		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		for (Bitmap bitmap : bitmaps) {
			float left = Math.abs(maxWidth - bitmap.getWidth()) / 2;
			float top = Math.abs(maxWidth - bitmap.getHeight()) / 2;
			canvas.drawBitmap(bitmap, left, top, null);
		}
		return result;
	}

	private static Bitmap maskFigure(int width, int height, Bitmap icon, Bitmap mask) {
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(mask, 0, 0, null);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		float left = Math.abs(icon.getWidth() - mask.getWidth()) / 2;
		float top = Math.abs(icon.getHeight() - mask.getHeight()) / 2;
		canvas.drawBitmap(icon, left, top, paint);

		return result;
	}

	public static Bitmap decorateIcon(Context context, Bitmap mask, Bitmap icon, Bitmap bottom) {
		int width = mask.getWidth();
		int height = mask.getHeight();
		if (null == icon) {
//			icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.much_default);
		}
		Bitmap bitmapIcon = icon;
		if (icon.getWidth() != width || icon.getHeight() != height) {
			bitmapIcon = Bitmap.createScaledBitmap(icon, width, height, true);
		}
		Bitmap maskedFigure = maskFigure(mask.getWidth(), mask.getHeight(), bitmapIcon, mask);
		return overlayBitmaps(bottom.getWidth(), bottom.getHeight(), bottom, maskedFigure);
	}

	public static boolean saveBitmap(Context context, Bitmap bt, String fileName) {
		if (null == bt) {
			return false;
		}
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(fileName);
			bt.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
			return true;
		} catch (FileNotFoundException e) {
			// UI should not know this exception
			return false;
		} finally {
			if (null != fileOutputStream) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static int getBitmapId(String pic) {
//		if (pic == null || pic.trim().equals("")) {
//			return R.drawable.much_default;
//		}
//		Class draw = R.drawable.class;
//		try {
//			Field field = draw.getDeclaredField(pic);
//			return field.getInt(pic);
//		} catch (SecurityException e) {
//			return R.drawable.much_default;
//		} catch (NoSuchFieldException e) {
//			return R.drawable.much_default;
//		} catch (IllegalArgumentException e) {
//			return R.drawable.much_default;
//		} catch (IllegalAccessException e) {
//			return R.drawable.much_default;
//		}
		return 0;
	}

	public static final String FIGURE_PATH = Environment.getExternalStorageDirectory().getPath() + "/test/";

	public static boolean saveBitmapExt(Bitmap bt, Context context, String name) {
		if (null == bt) {
			return false;
		}
		try {
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				String path = FIGURE_PATH;
				File file = new File(path);
				if (!file.exists()) {
					file.mkdirs();
				}
				FileOutputStream fileOutputStream = new FileOutputStream(new File(FIGURE_PATH + name + ".png"));
				bt.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
				return true;
			}
			return false;
		} catch (FileNotFoundException e) {
			// UI should not know this exception
			return false;
		}
	}

	public static Bitmap readBitmap(Context context, final String path) {
		try {
			FileInputStream stream = new FileInputStream(path);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			opts.inPurgeable = true;
			opts.inInputShareable = true;
			Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opts);
			stream.close();
			return bitmap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将多个Bitmap合并成一个图片。
	 * 
	 * @param int 将多个图合成多少列
	 * @param Bitmap
	 *            ... 要合成的图片
	 * @return
	 */
	public static Bitmap combineBitmaps(int columns, Bitmap... bitmaps) {
		if (columns <= 0 || bitmaps == null || bitmaps.length == 0) {
			throw new IllegalArgumentException("Wrong parameters: columns must > 0 and bitmaps.length must > 0.");
		}
		int maxWidthPerImage = 0;
		int maxHeightPerImage = 0;
		for (Bitmap b : bitmaps) {
			maxWidthPerImage = maxWidthPerImage > b.getWidth() ? maxWidthPerImage : b.getWidth();
			maxHeightPerImage = maxHeightPerImage > b.getHeight() ? maxHeightPerImage : b.getHeight();
		}
		int rows = 0;
		if (columns >= bitmaps.length) {
			rows = 1;
			columns = bitmaps.length;
		} else {
			rows = bitmaps.length % columns == 0 ? bitmaps.length / columns : bitmaps.length / columns + 1;
		}
		Bitmap newBitmap = Bitmap.createBitmap(columns * maxWidthPerImage, rows * maxHeightPerImage, Config.RGB_565);

		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int index = x * columns + y;
				if (index >= bitmaps.length)
					break;
				newBitmap = mixtureBitmap(newBitmap, bitmaps[index], new PointF(y * maxWidthPerImage, x
						* maxHeightPerImage));
			}
		}
		return newBitmap;
	}

	/**
	 * Mix two Bitmap as one.
	 * 
	 * @param bitmapOne
	 * @param bitmapTwo
	 * @param point
	 *            where the second bitmap is painted.
	 * @return
	 */
	public static Bitmap mixtureBitmap(Bitmap first, Bitmap second, PointF fromPoint) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}
		Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(), first.getHeight(), Config.ARGB_4444);
		Canvas cv = new Canvas(newBitmap);
		cv.drawBitmap(first, 0, 0, null);
		cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
		cv.save(Canvas.ALL_SAVE_FLAG);
		cv.restore();
		return newBitmap;
	}

	// 图片剪切
	public static Bitmap cutBitmap(Bitmap mBitmap, Rect r, Bitmap.Config config) {
		int width = r.width();
		int height = r.height();

		Bitmap croppedImage = Bitmap.createBitmap(width, height, config);

		Canvas cvs = new Canvas(croppedImage);
		Rect dr = new Rect(0, 0, width, height);
		cvs.drawBitmap(mBitmap, r, dr, null);
		return croppedImage;
	}

	/*
	 * 在给定图标的右上角画一个小图--带数字
	 */
	public static Bitmap markCountIcon(Resources resources, Bitmap icon, int count) {
		if (count < 1) {
			return icon;
		}
		// 初始化画布
		int appIconSize = icon.getWidth();
		Bitmap contactIcon = Bitmap.createBitmap(appIconSize, appIconSize, Config.ARGB_8888);

		Canvas canvas = new Canvas(contactIcon);
		// 拷贝图片
		Paint iconPaint = new Paint();
		iconPaint.setDither(true);// 防抖动
		iconPaint.setFilterBitmap(true);// 用来对Bitmap进行滤波处理，这样，当你选择Drawable时，会有抗锯齿的效果
		Rect src = new Rect(0, 0, appIconSize, appIconSize);
		Rect dst = new Rect(0, 0, appIconSize, appIconSize);
		canvas.drawBitmap(icon, src, dst, iconPaint);
		Bitmap indicate = BitmapFactory.decodeResource(resources, R.drawable.much_new_number_bg);
		canvas.drawBitmap(indicate, appIconSize - indicate.getWidth(), 0, iconPaint);
		// 启用抗锯齿和使用设备的文本字距
		Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
		countPaint.setColor(Color.WHITE);
		countPaint.setTextSize(20f);
		countPaint.setTypeface(Typeface.DEFAULT_BOLD);
		if (count < 10) {
			canvas.drawText(String.valueOf(count), appIconSize - 2 * indicate.getWidth() / 3, 25, countPaint);
		} else {
			if (count > 99) {
				count = 99;
			}
			canvas.drawText(String.valueOf(count), appIconSize - 6 * indicate.getWidth() / 7, 25, countPaint);
		}
		return contactIcon;
	}

}
