package com.ne0fhyklabs.androhud.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.ne0fhyklabs.androhud.R;

import static com.ne0fhyklabs.androhud.utils.Constants.DEFAULT_STROKE_WIDTH;

/**
 * Hud Scroller widget
 */
public class SimpleScroller extends View {

    /**
     * Ways the scroller could be oriented.
     */
    private enum Handedness {
        LEFT,
        RIGHT
    }

    /*
    Widget width and height accounting for padding.
     */
    private float mHeight;
    private float mWidth;

    /**
     * Canvas horizontal and vertical paddings
     */
    private float mCanvasXPadding;
    private float mCanvasYPadding;

    /**
     * Height for the scroller arrow.
     */
    private float mArrowHeight;

    /**
     * Width for the scroller's tics.
     */
    private float mTicWidth;

    /*
    Scroller's text horizontal and vertical margins.
     */
    private float mTextHorizontalMargin;
    private float mTextVerticalMargin;

    /**
     * Paint used to render the scroller widget.
     */
    private Paint mStrokeColor;

    /**
     * Paint used to render the arrow contour.
     */
    private Paint mArrowStrokeColor;

    /**
     * Paint used to render the scroller's arrow background.
     */
    private Paint mArrowBgColor;

    /**
     * Specify which way the scroller is oriented.
     */
    private Handedness mHandedness;

    /**
     * Used for drawing operations requiring a rectf object.
     */
    private final RectF mCacheRectF = new RectF();

    /**
     * Used for drawing operations requiring a path object.
     */
    private final Path mCachePath = new Path();

    /**
     * Value the scroller arrow should indicate.
     */
    private float mScrollTo;

    /**
     * Range for the scrollTo value.
     */
    private float mScrollToRange;

    public SimpleScroller(Context context) {
        this(context, null);
    }

    public SimpleScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.SimpleScroller, defStyleAttr, 0);

        try {
            mHandedness = Handedness.values()[attributes.getInt(R.styleable
                    .SimpleScroller_handedness, 0)];

            mArrowHeight = attributes.getDimension(R.styleable.SimpleScroller_arrowHeight, 25f);
            mTicWidth = attributes.getDimension(R.styleable.SimpleScroller_ticWidth, 16f);
            mTextHorizontalMargin = attributes.getDimension(R.styleable
                    .SimpleScroller_textHorizontalMargin, 23f);
            mTextVerticalMargin = attributes.getDimension(R.styleable
                    .SimpleScroller_textVerticalMargin, 10f);

            mStrokeColor = new Paint();
            mStrokeColor.setAntiAlias(true);
            mStrokeColor.setStyle(Paint.Style.STROKE);
            mStrokeColor.setTextAlign(mHandedness == Handedness.LEFT ? Paint.Align.RIGHT : Paint
                    .Align.LEFT);
            mStrokeColor.setStrokeWidth(DEFAULT_STROKE_WIDTH);
            mStrokeColor.setColor(attributes.getColor(R.styleable.SimpleScroller_strokeColor,
                    Color.WHITE));
            mStrokeColor.setTextSize(attributes.getDimension(R.styleable
                    .SimpleScroller_android_textSize, 25f));

            mArrowStrokeColor = new Paint();
            mArrowStrokeColor.setAntiAlias(true);
            mArrowStrokeColor.setStyle(Paint.Style.STROKE);
            mArrowStrokeColor.setStrokeWidth(DEFAULT_STROKE_WIDTH);
            mArrowStrokeColor.setColor(attributes.getColor(R.styleable
                    .SimpleScroller_arrowStrokeColor, Color.WHITE));

            mArrowBgColor = new Paint();
            mArrowBgColor.setAntiAlias(true);
            mArrowBgColor.setStyle(Paint.Style.FILL);
            mArrowBgColor.setColor(attributes.getColor(R.styleable.SimpleScroller_arrowBgColor,
                    Color.BLACK));

            mScrollTo = attributes.getFloat(R.styleable.SimpleScroller_scrollTo, 0);
            mScrollToRange = attributes.getFloat(R.styleable.SimpleScroller_scrollToRange, 26f);
        } finally {
            attributes.recycle();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Translate the canvas to account for the view padding
        canvas.translate(mCanvasXPadding, mCanvasYPadding);

        //Outside box
        mCacheRectF.set(0, 0, mWidth, mHeight);
        canvas.drawRect(mCacheRectF, mStrokeColor);

        final float centerY = mHeight / 2;
        final float ticMargin = mHeight / mScrollToRange;
        final int start = (int) (mScrollTo - mScrollToRange / 2);
        final int end = (int) (mScrollTo + mScrollToRange / 2);
        final float ticStart = mHandedness == Handedness.LEFT ? mWidth : 0;
        final float ticEnd = mHandedness == Handedness.LEFT ? ticStart - mTicWidth: ticStart +
                mTicWidth;
        final float textStart = mHandedness == Handedness.LEFT ? mWidth - mTextHorizontalMargin:
                mTextHorizontalMargin;
        final float textOffset = mStrokeColor.getTextSize() / 2 + mTextVerticalMargin;

        for (int a = start; a <= end; a++) {
            if (a % 5 == 0) {
                float lineHeight = centerY - ticMargin * (a - mScrollTo);
                canvas.drawLine(ticStart, lineHeight, ticEnd, lineHeight, mStrokeColor);
                canvas.drawText(String.valueOf(a), textStart, lineHeight + textOffset, mStrokeColor);
            }
        }

        //Arrow with current speed
        final int borderWidth = Math.round(mArrowStrokeColor.getStrokeWidth());
        final float arrowStickX, arrowBaseX, arrowPointX;
        if(mHandedness == Handedness.LEFT){
            arrowStickX = -borderWidth;
            arrowBaseX = mWidth - mArrowHeight / 4 - borderWidth;
            arrowPointX = mWidth - borderWidth;
        }
        else{
            arrowStickX = mWidth + borderWidth;
            arrowBaseX = mArrowHeight / 4 + borderWidth;
            arrowPointX = borderWidth;
        }

        final float arrowStartY = centerY - mArrowHeight / 2;
        final float arrowEndY = centerY + mArrowHeight / 2;

        mCachePath.reset();
        Path arrow = mCachePath;
        arrow.moveTo(arrowStickX, arrowStartY);
        arrow.lineTo(arrowBaseX, arrowStartY);
        arrow.lineTo(arrowPointX, centerY);
        arrow.lineTo(arrowBaseX, arrowEndY);
        arrow.lineTo(arrowStickX, arrowEndY);
        canvas.drawPath(arrow, mArrowBgColor);
        canvas.drawPath(arrow, mArrowStrokeColor);
        canvas.drawText(String.valueOf((int)mScrollTo), textStart, centerY + textOffset,
                mStrokeColor);

    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mCanvasXPadding = getPaddingLeft();
        mCanvasYPadding = getPaddingTop();

        //Account for padding
        float xPad = mCanvasXPadding + getPaddingRight();
        float yPad = mCanvasYPadding + getPaddingBottom();

        mWidth = width - xPad;
        mHeight = height - yPad;
    }

    public float getArrowHeight() {
        return mArrowHeight;
    }

    public void setArrowHeight(float mArrowHeight) {
        this.mArrowHeight = mArrowHeight;
        invalidate();
    }

    public float getTicWidth() {
        return mTicWidth;
    }

    public void setTicWidth(float mTicWidth) {
        this.mTicWidth = mTicWidth;
        invalidate();
    }

    public float getTextHorizontalMargin() {
        return mTextHorizontalMargin;
    }

    public void setTextHorizontalMargin(float mTextHorizontalMargin) {
        this.mTextHorizontalMargin = mTextHorizontalMargin;
        invalidate();
    }

    public float getTextVerticalMargin() {
        return mTextVerticalMargin;
    }

    public void setTextVerticalMargin(float mTextVerticalMargin) {
        this.mTextVerticalMargin = mTextVerticalMargin;
        invalidate();
    }

    public int getStrokeColor() {
        return mStrokeColor.getColor();
    }

    public void setStrokeColor(int mStrokeColor) {
        this.mStrokeColor.setColor(mStrokeColor);
        invalidate();
    }

    public int getArrowStrokeColor() {
        return mArrowStrokeColor.getColor();
    }

    public void setArrowStrokeColor(int mArrowStrokeColor) {
        this.mArrowStrokeColor.setColor(mArrowStrokeColor);
        invalidate();
    }

    public int getArrowBgColor() {
        return mArrowBgColor.getColor();
    }

    public void setArrowBgColor(int mArrowBgColor) {
        this.mArrowBgColor.setColor(mArrowBgColor);
        invalidate();
    }

    public float getTextSize() {
        return mStrokeColor.getTextSize();
    }

    public void setTextSize(float textSize) {
        mStrokeColor.setTextSize(textSize);
        invalidate();
    }

    public int getHandedness() {
        return mHandedness.ordinal();
    }

    public void setHandedness(int handednessIndex) {
        mHandedness = Handedness.values()[handednessIndex];
        mStrokeColor.setTextAlign(mHandedness == Handedness.LEFT ? Paint.Align.RIGHT : Paint
                .Align.LEFT);
        invalidate();
    }

    public float getScrollToRange() {
        return mScrollToRange;
    }

    public void setScrollToRange(float mScrollToRange) {
        this.mScrollToRange = mScrollToRange;
        invalidate();
    }

    public float getScrollTo() {
        return mScrollTo;
    }

    public void setScrollTo(float mScrollTo) {
        this.mScrollTo = mScrollTo;
        invalidate();
    }
}
