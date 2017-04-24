package com.iflytek.eduvoiceassistant.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.iflytek.eduvoiceassistant.R;

import java.util.ArrayList;


/**
 * Created by fyu on 11/3/14.
 */

public class RippleBackground extends RelativeLayout {
	private  int MIN_RADIUS;
	private static final int DEFAULT_RIPPLE_COUNT = 6;
	private static final int DEFAULT_DURATION_TIME = 3000;
	private static final float DEFAULT_SCALE = 6.0f;
	private static final int DEFAULT_FILL_TYPE = 0;

	private int rippleColor;
	private float rippleStrokeWidth;
	private float rippleRadius;
	private int rippleDurationTime;
	private int rippleAmount;
	private int rippleDelay;
	private float rippleScale;
	private int rippleType;
	private Paint paint;
	private boolean animationRunning = false;
	private AnimatorSet animatorSet;
	private ArrayList<Animator> animatorList;
	private LayoutParams rippleParams;
	private ArrayList<RippleView> rippleViewList = new ArrayList<RippleView>();



	public RippleBackground(Context context) {
		super(context);
	}

	public RippleBackground(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public RippleBackground(Context context, AttributeSet attrs,
							int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(final Context context, final AttributeSet attrs) {
		if (isInEditMode())
			return;

		if (null == attrs) {
			throw new IllegalArgumentException(
					"Attributes should be provided to this view,");
		}
		MIN_RADIUS = (int) getContext().getResources().getDimension(R.dimen.MIN_RADIUS);
		
		final TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.RippleBackground);
		rippleColor = typedArray.getColor(
				R.styleable.RippleBackground_rb_color,
				getResources().getColor(R.color.rippelwhiteColor));
		rippleStrokeWidth = typedArray.getDimension(
				R.styleable.RippleBackground_rb_strokeWidth, getResources()
						.getDimension(R.dimen.rippleStrokeWidth));// 6.0
		rippleRadius = typedArray.getDimension(
				R.styleable.RippleBackground_rb_radius, getResources()
						.getDimension(R.dimen.rippleRadius));// 96
		rippleDurationTime = typedArray
				.getInt(R.styleable.RippleBackground_rb_duration,
						DEFAULT_DURATION_TIME);// 3000
		rippleAmount = typedArray.getInt(
				R.styleable.RippleBackground_rb_rippleAmount,
				DEFAULT_RIPPLE_COUNT);// 波纹个数6
		rippleScale = typedArray.getFloat(
				R.styleable.RippleBackground_rb_scale, DEFAULT_SCALE);//6 
		rippleType = typedArray.getInt(R.styleable.RippleBackground_rb_type,
				DEFAULT_FILL_TYPE);// 1
		typedArray.recycle();

		rippleDelay = 400;//rippleDurationTime / rippleAmount / 2;//延时产生下一个波纹的时间

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(rippleColor);

		// 布局充满父容器
		rippleParams = new LayoutParams(
							  LayoutParams.MATCH_PARENT
							, LayoutParams.MATCH_PARENT);
		
		rippleParams.addRule(CENTER_IN_PARENT, TRUE);
		
		// 创建动画
		animatorSet = new AnimatorSet();
		animatorSet.setDuration(rippleDurationTime);
		animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
		createStrokeAnimator();
		
		animatorSet.playTogether(animatorList);

	}
	
	public void createStrokeAnimator() {
		if (animatorList == null) {
			animatorList = new ArrayList<Animator>();
		}
		else {
			animatorList.clear();
		}

		for (int i = 0; i < rippleAmount; i++) {
			RippleView rippleView = new RippleView(getContext());
			addView(rippleView, rippleParams);
			rippleViewList.add(rippleView);
			final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(
					rippleView, "radius", MIN_RADIUS, MIN_RADIUS * rippleScale);
			scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE);
			scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
			scaleYAnimator.setStartDelay(i * rippleDelay);
			scaleYAnimator.setDuration(rippleDurationTime);
			animatorList.add(scaleYAnimator);//波纹扩展动画

			final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(
					rippleView, "Alpha", 1.0f, 0f);
			alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE);
			alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
			alphaAnimator.setStartDelay(i * rippleDelay);
			alphaAnimator.setDuration(rippleDurationTime);
			animatorList.add(alphaAnimator);// 渐变动画
		}
	}

	/***
	 * 单个波纹view
	 * */
	private class RippleView extends View {
		
		private float scaleRad = MIN_RADIUS;
		private int x;
		private int y;
		
		public RippleView(Context context) {
			super(context);
			this.setVisibility(View.INVISIBLE);
		}
		
		public float getRadius() {
			return scaleRad;
		}
		
		public void setRadius(float r) {
			scaleRad = r;
			invalidate();
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			x = MeasureSpec.getSize(widthMeasureSpec) >> 1;
			y = MeasureSpec.getSize(heightMeasureSpec) >> 1;
			
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
		
		
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawCircle(x, y, scaleRad, paint);
		}
	}

	public void startRippleAnimation() {
		if (!isRippleAnimationRunning()) {
			for (RippleView rippleView : rippleViewList) {
				rippleView.setVisibility(VISIBLE);
			}
			animatorSet.start();
			animationRunning = true;
		}
	}

	public void stopRippleAnimation() {
		if (isRippleAnimationRunning()) {
			animatorSet.end();
			animationRunning = false;
		}
	}

	public boolean isRippleAnimationRunning() {
		return animationRunning;
	}
}
