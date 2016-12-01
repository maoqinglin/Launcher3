
package com.android.launcher3.much;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;

public class InOutAnimation extends AnimationSet {
    public static final int DURATION_IN = 800;
    public static final int DURATION_OUNT = 300;
    public Direction mDirection;

    public enum Direction {
        IN, OUT;
    }

    public InOutAnimation(Direction direction, long l, View[] views) {
        super(true);
        mDirection = direction;

        switch (mDirection) {
            case IN:
                addInAnimation(views);
                break;
            case OUT:
                addOutAnimation(views);
                break;
        }
        setDuration(l);
    }

    public static void startAnimations(ViewGroup viewgroup,
            Direction direction) {
        switch (direction) {
            case IN:
                startAnimationsIn(viewgroup);
                break;
            case OUT:
                startAnimationsOut(viewgroup);
                break;
            default:
                throw new IllegalArgumentException(
                        "InOutAnimation direction is unknow");
        }
    }

    private static void startAnimationsIn(ViewGroup viewgroup) {
        for (int i = 0; i < viewgroup.getChildCount(); i++) {
            View view = viewgroup.getChildAt(i);
            InOutAnimation animation = new InOutAnimation(
                    Direction.IN, DURATION_IN, new View[] {
                        view
                    });
            animation.setInterpolator(new OvershootInterpolator(2F));
            view.startAnimation(animation);
        }
    }

    private static void startAnimationsOut(ViewGroup viewgroup) {
        for (int i = 0; i < viewgroup.getChildCount(); i++) {
            View view = viewgroup.getChildAt(i);
            InOutAnimation animation = new InOutAnimation(
                    Direction.OUT, DURATION_OUNT, new View[] {
                        view
                    });
            animation.setInterpolator(new AnticipateInterpolator(2F));
            view.startAnimation(animation);
        }
    }

    protected void addInAnimation(View[] aview) {
        addAnimation(new TranslateAnimation(113, 0F,
                113, 0F));
        addAnimation(new AlphaAnimation(0.2F, 1F));
    }

    protected void addOutAnimation(View[] aview) {
        addAnimation(new TranslateAnimation(0F, 113, 0F,
                113));
    }

}
