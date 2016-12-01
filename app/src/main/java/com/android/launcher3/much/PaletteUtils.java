package com.android.launcher3.much;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;

/**
 * 调色工具类
 * @author skydsai
 *
 */
public class PaletteUtils {

	/**
	 * 按样本类型来对色相进行升序排序（耗时操作）
	 * @param appIconMaps
	 * @param swatchType
	 * @return
	 */
	public static List<String> sortToHue(HashMap<String, Bitmap> appIconMaps, SwatchType swatchType) {
		List<String> appIcons = new ArrayList<String>();
		appIcons.addAll(sortToHslByDefinite(appIconMaps, swatchType, HSLType.H, 0, true, true));
		appIcons.addAll(sortToHslByDefinite(appIconMaps, swatchType, HSLType.H, 360, true, true));
		appIcons.addAll(sortToHslBySection(appIconMaps, swatchType, HSLType.H, 300, 360, true, true));
		appIcons.addAll(sortToHslBySection(appIconMaps, swatchType, HSLType.H, 0, 300, true, true));
		appIcons.addAll(sortToHslByNotAnalysis(appIconMaps, swatchType));
		return appIcons;
	}
	
	/**
	 * 通过HSL方式进行区间刷选（耗时操作）
	 * @param appIconMaps 需要刷选图标集合
	 * @param swatchType 刷选样本
	 * @param hslType 刷选条件
	 * @param start 区间初始值，根据hslType而定。H范围[0, 360], S范围[0, 1], L范围[0, 1]
	 * @param end 区间终点值，根据hslType而定。H范围[0, 360], S范围[0, 1], L范围[0, 1]
	 * @param isSort 是否需要排序
	 * @param isAsc 升序还是降序
	 * @return 
	 */
	public static List<String> sortToHslBySection(
			HashMap<String, Bitmap> appIconMaps, SwatchType swatchType, HSLType hslType, float start,
			float end, boolean isSort, boolean isAsc) {
		List<String> sectionList = new ArrayList<String>();
		HashMap<String, float[]> sortMaps = new HashMap<String, float[]>();
		if (start >= end) {
			throw new IllegalArgumentException("start must be smaller than end!");
		}
		
		switch (hslType) {
		case S:
		case L:
			if(start < 0 || end > 1) {
				throw new IllegalArgumentException("start and end must be [0, 1]");
			}
			break;

		default:
			if(start < 0 || end > 360) {
				throw new IllegalArgumentException("start and end must be [0, 360]");
			}
			break;
		}
		
		for (Entry<String, Bitmap> entry : appIconMaps.entrySet()) {
			Bitmap bm = entry.getValue();
			if (entry.getValue() != null) {
				Swatch swatch = getBitmapSwatch(bm, swatchType);
				float hsl;
				if (swatch == null) {
					continue;
				}

				switch (hslType) {
					case S:
						hsl = swatch.getHsl()[1];
						break;
					case L:
						hsl = swatch.getHsl()[2];
						break;
	
					default:
						hsl = swatch.getHsl()[0];
						break;
				}

				if (hsl > start && hsl < end) {
					sortMaps.put(entry.getKey(), swatch.getHsl());
					sectionList.add(entry.getKey());
				}
			}
		}

		if (isSort) {
			return sortAppIcon(sortMaps, HSLType.H, isAsc);
		}

		return sectionList;
	}

	/**
	 * 通过HSL方式进行特值刷选（耗时操作）
	 * @param appIconMaps 需要刷选图标集合
	 * @param swatchType 刷选样本
	 * @param hslType 刷选条件
	 * @param definite 特值。根据hslType而定。H范围[0, 360], S范围[0, 1], L范围[0, 1]
	 * @param isSort 是否需要排序
	 * @param isAsc 升序还是降序
	 * @return
	 */
	public static List<String> sortToHslByDefinite(
			HashMap<String, Bitmap> appIconMaps, SwatchType swatchType, HSLType hslType, float definite, boolean isSort, boolean isAsc) {
		List<String> definiteList = new ArrayList<String>();
		HashMap<String, float[]> sortMaps = new HashMap<String, float[]>();
		
		switch (hslType) {
		case S:
		case L:
			if(definite < 0 || definite > 1) {
				throw new IllegalArgumentException("definite must be [0, 1]");
			}
			break;

		default:
			if(definite < 0 || definite > 360) {
				throw new IllegalArgumentException("definite must be [0, 360]");
			}
			break;
		}
		
		for (Entry<String, Bitmap> entry : appIconMaps.entrySet()) {
			Bitmap bm = entry.getValue();
			if (entry.getValue() != null) {
				Swatch swatch = getBitmapSwatch(bm, swatchType);
				float hsl;
				if (swatch == null) {
					continue;
				}

				switch (hslType) {
				case S:
					hsl = swatch.getHsl()[1];
					break;
				case L:
					hsl = swatch.getHsl()[2];
					break;

				default:
					hsl = swatch.getHsl()[0];
					break;
				}

				if (hsl == definite) {
					sortMaps.put(entry.getKey(), swatch.getHsl());
					definiteList.add(entry.getKey());
				}
			}
		}

		if (isSort) {
			return sortAppIcon(sortMaps, hslType, isAsc);
		}
		return definiteList;
	}

	private static Swatch getBitmapSwatch(Bitmap bm, SwatchType swatchType) {
		Palette palette = Palette.from(bm).generate();
		switch (swatchType) {
		case Vibrant:
			return palette.getVibrantSwatch();
			
		case VibrantLight:
			return palette.getLightVibrantSwatch();

		case VibrantDark:
			return palette.getDarkVibrantSwatch();
			
		case Muted:
			return palette.getMutedSwatch();

		case MutedLight:
			return palette.getLightMutedSwatch();
			
		case MutedDark:
			return palette.getDarkMutedSwatch();

		default:
			TreeMap<Integer, Swatch> populationMaps = new TreeMap<Integer, Palette.Swatch>();
			List<Swatch> swatchs = palette.getSwatches();
			for (Swatch swatch : swatchs) {
				populationMaps.put(swatch.getPopulation(), swatch);
			}
			if(SwatchType.PopulationMax == swatchType) {
				return populationMaps.get(populationMaps.lastKey());
			}
			return populationMaps.get(populationMaps.firstKey());
		}
	}
	
	/**
	 * 获取所选样本无法解析的图标集合（耗时操作）
	 * @param appIconMaps 需要刷选图标集合
	 * @param swatchType 刷选样本
	 * @return
	 */
	public static List<String> sortToHslByNotAnalysis(
			HashMap<String, Bitmap> appIconMaps, SwatchType swatchType) {
		List<String> notAnalysisList = new ArrayList<String>();
		for (Entry<String, Bitmap> entry : appIconMaps.entrySet()) {
			Bitmap bm = entry.getValue();
			if (entry.getValue() != null) {
				Swatch swatch = getBitmapSwatch(bm, swatchType);
				if (swatch == null) {
					notAnalysisList.add(entry.getKey());
				}
			}
		}
		return notAnalysisList;
	}

	
	private static List<String> sortAppIcon(HashMap<String, float[]> hslMaps,
			final HSLType hslType, final boolean isAsc) {

		List<Entry<String, float[]>> entryList = new ArrayList<Entry<String, float[]>>(hslMaps.entrySet());
		Collections.sort(entryList,
				new Comparator<Entry<String, float[]>>() {
					public int compare(Entry<String, float[]> entry1,
							Entry<String, float[]> entry2) {
						float[] value1 = entry1.getValue();
						float[] value2 = entry2.getValue();
						int result;

						switch (hslType) {
						case S:
							result = (int) (value2[1] - value1[1]);
							break;

						case L:
							result = (int) (value2[2] - value1[2]);
							break;

						default:
							result = (int) (value2[0] - value1[0]);
							break;
						}
						return isAsc ? -result : result;
					}
				});

		List<String> sortAppList = new ArrayList<String>();
		for (Entry<String, float[]> entry : entryList) {
			sortAppList.add(entry.getKey());
		}

		return sortAppList;
	}

	public enum HueType {
		Red, RedToYellow, Yellow, YellowToGreen, Green, GreenToCyan, Cyan, CyanToBlue, Blue, BlueToMagenta, Magenta, MagentaToRed, Other
	}

	/**
	 * Vibrant：活力型
	 * VibrantDark： 活力暗色
	 * VibrantLight：活力亮色
	 * Muted：柔和型
	 * MutedDark：柔和暗色
	 * MutedLight：柔和亮色
	 * PopulationMax：占用像素最多的样本
	 * PopulationMin：占用像素最少的样本
	 * @author skydsai
	 *
	 */
	public enum SwatchType {
		Vibrant, VibrantDark, VibrantLight, Muted, MutedDark, MutedLight, PopulationMax, PopulationMin
	}

	/**
	 * H：色相
	 * S：饱和度
	 * L：明度
	 * @author skydsai
	 *
	 */
	public enum HSLType {
		H, S, L
	}
}
