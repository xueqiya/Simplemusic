package com.example.simplemusic.view;

import android.content.Context;
import android.graphics.Canvas;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;

public class RotateAnimator extends AppCompatImageView {
    ImageView imageView;
    private float angle, angle2;
    private float viewHeight, viewWidth;
    private MusicAnim musicAnim;

    public RotateAnimator(Context context, ImageView imageView) {
        super(context);
        init(imageView);
    }

    public void init(ImageView imageView) {
        this.imageView = imageView;
        angle = angle2 = 0;
        viewHeight = viewWidth = 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewHeight = getMeasuredHeight();
        viewWidth = getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(angle2, viewWidth/2, viewHeight/2);
        super.onDraw(canvas);
    }

    public class MusicAnim extends RotateAnimation {
        public MusicAnim(float fromD, float toD, float pivotX, float pivotY) {
            super(fromD, toD, Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            angle = interpolatedTime * 360;
        }
    }

    public void set_Needle() {
        final RotateAnimation animation1 = new RotateAnimation(0f, -35f, Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.15f);
        animation1.setDuration(1);
        animation1.setRepeatCount(0);
        animation1.setFillAfter(true);
        animation1.setStartOffset(0);
    }

    final RotateAnimation animation = new RotateAnimation(-35f, 0f, Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 0.15f);
    private void settime() {
        //animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(400);
        animation.setRepeatCount(0);
        animation.setStartOffset(0);
        animation.setFillAfter(true);
    }

    public void playAnimator() {
        settime();
        musicAnim = new MusicAnim(0, 3600000, viewWidth/2, viewHeight/2);
        musicAnim.setDuration(360000000);
        musicAnim.setInterpolator(new LinearInterpolator());
        musicAnim.setRepeatCount(-1);
        imageView.startAnimation(musicAnim);
        invalidate();
    }

    public void pauseAnimator() {
        angle2 = (angle2 + angle) % 360;
        musicAnim.cancel();
        animation.cancel();
        invalidate();
    }
}
