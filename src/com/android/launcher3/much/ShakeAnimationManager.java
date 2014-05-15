package com.android.launcher3.much;

import java.util.HashMap;

import com.android.launcher3.LauncherAnimUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Paint;
import android.view.View;

public class ShakeAnimationManager {

	private static ShakeAnimationManager manager;
	private HashMap<View, ShakeAnimation> mAnimators = new HashMap<View, ShakeAnimation>();
	private static final float AMPLITUDE = 2.5f;

	private ShakeAnimationManager() {

	}

	public static ShakeAnimationManager getInstance() {
		if (manager == null) {
			synchronized (ShakeAnimationManager.class) {
				if (manager == null) {
					manager = new ShakeAnimationManager();
				}
			}
		}
		return manager;
	}

	public void startShakeAnim(View view) {
		ShakeAnimation shakeAnimation = new ShakeAnimation();
		shakeAnimation.animateShake(view);
	}

	public void startHeartbeatAnim(View view) {
		ShakeAnimation shakeAnimation = new ShakeAnimation();
		shakeAnimation.animateHeartbeatAnim(view);
	}

	public void stopAnim() {
		for (ShakeAnimation a : mAnimators.values()) {
			a.completeAnimationImmediately();
		}
		mAnimators.clear();
	}

	public void stopAnim(View view) {
		if (mAnimators.containsKey(view)) {
			ShakeAnimation oldAnimation = mAnimators.get(view);
			oldAnimation.cancel();
			mAnimators.remove(view);
			oldAnimation.completeAnimationImmediately();
		}
	}

	class ShakeAnimation {

		private View child;
		private float finalScale;
		private float initScale;
		private float finalRotation;
		private float initRotation;
		private Animator a;
		private Paint sPaint = new Paint();

		public ShakeAnimation() {
			sPaint.setAntiAlias(true);
		}

		void animateShake(final View view) {
			if (view == null) {
				return;
			}
			this.child = view;

			view.setLayerType(View.LAYER_TYPE_HARDWARE, sPaint);
			checkHaveAnimation(child);

			int dir = (System.currentTimeMillis() % 2 == 0) ? -1 : 1;
			// finalRotation = 1.8f*dir;
			finalRotation = AMPLITUDE * dir;
			initRotation = child.getRotation();

			ValueAnimator va = LauncherAnimUtils.ofFloat(null,-1.0f, 1.0f); //modify by linmaoqing 2014-5-14
			a = va;
			va.setRepeatMode(ValueAnimator.REVERSE);
			va.setRepeatCount(ValueAnimator.INFINITE);
			va.setDuration(110);
			va.setStartDelay((int) (Math.random() * 250));
			va.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float r = (Float) animation.getAnimatedValue();

					if (finalRotation != initRotation) {
						float v = r * finalRotation;
						child.setRotation(v);
					}
				}
			});
			va.addListener(new AnimatorListenerAdapter() {
				public void onAnimationRepeat(Animator animation) {
					initScale = 1.0f;
					initRotation = 0.0f;
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					view.setLayerType(View.LAYER_TYPE_NONE, null);
					child.setRotation(initRotation);
				}
			});
			va.start();
			mAnimators.put(child, this);
		}

		void animateHeartbeatAnim(final View view) {

			this.child = view;

			if (child == null) {
				return;
			}
			view.setLayerType(View.LAYER_TYPE_HARDWARE, sPaint);
			checkHaveAnimation(child);

			initScale = child.getScaleX();
			finalScale = initScale * 1.03f;

			ValueAnimator va = LauncherAnimUtils.ofFloat(null,0.0f, 1.0f); //modify by linmaoqing 2014-5-14
			a = va;
			va.setRepeatMode(ValueAnimator.REVERSE);
			va.setRepeatCount(ValueAnimator.INFINITE);
			va.setDuration(500);
			va.setStartDelay((int) (Math.random() * 30));
			va.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float r = (Float) animation.getAnimatedValue();
					if (finalScale != initScale) {
						float s = r * finalScale + (1 - r) * initScale;
						child.setScaleX(s);
						child.setScaleY(s);
					}
				}
			});
			va.addListener(new AnimatorListenerAdapter() {
				public void onAnimationRepeat(Animator animation) {
					initScale = 1.0f;
					initRotation = 0.0f;
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					view.setLayerType(View.LAYER_TYPE_NONE, null);
					child.setScaleX(initScale);
					child.setScaleY(initScale);
				}
			});
			va.start();
			mAnimators.put(child, this);
		}

		void checkHaveAnimation(View child) {
			if (mAnimators.containsKey(child)) {
				ShakeAnimation oldAnimation = mAnimators.get(child);
				oldAnimation.cancel();
				mAnimators.remove(child);
				completeAnimationImmediately();
			}
		}

		private void cancel() {
			if (a != null) {
				a.cancel();
			}
		}

		private void completeAnimationImmediately() {
			if (a != null) {
				a.cancel();
			}

			AnimatorSet s = LauncherAnimUtils.createAnimatorSet();
			a = s;
			s.playTogether(LauncherAnimUtils.ofFloat(child, "scaleX", 1),
					LauncherAnimUtils.ofFloat(child, "scaleY", 1),
					LauncherAnimUtils.ofFloat(child, "translationX", 0f),
					LauncherAnimUtils.ofFloat(child, "translationY", 0f),
					LauncherAnimUtils.ofFloat(child, "rotation", 0f));
			s.setDuration(200);
			s.setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f));
			s.start();
		}
	}
}
