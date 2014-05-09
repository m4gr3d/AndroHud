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
 * HUD Pitch widget.
 * Created by fhuya on 3/3/14.
 */
public class SimplePitchRoll extends View {

    private final static float DEG_2_RAD = (float)Math.PI / 180f;
    /**
     * Normalized upper bound in degrees for the pitch value.
     */
    private final static float NORMALIZED_PITCH_UPPER_BOUND = 5;

    /**
     * Normalized lower bound in degrees for the pitch value.
     */
    private final static float NORMALIZED_PITCH_LOWER_BOUND = -NORMALIZED_PITCH_UPPER_BOUND;

    /**
     * Sweep angle in degrees for the roll arc.
     */
    private final static float ROLL_ARC_SWEEP_ANGLE = 90f;

    /**
     * Normalized upper and lower bound for the roll value.
     */
    private final static float NORMALIZED_ROLL_UPPER_BOUND = ROLL_ARC_SWEEP_ANGLE/2;
    private final static float NORMALIZED_ROLL_LOWER_BOUND = -NORMALIZED_ROLL_UPPER_BOUND;

    /**
     * This is the view's height accounting for padding.
     */
    private float mHeight;

    /**
     * This is the view's width, accounting for padding.
     */
    private float mWidth;

    /**
     * Canvas horizontal and vertical paddings
     */
    private float mCanvasXPadding;
    private float mCanvasYPadding;

    /**
     * Paint used to render the pitch's hud.
     */
    private Paint mPitchPaint;

    /**
     * Pitch value used to update the widget.
     */
    private float mPitch;

    /**
     * Lower bound for the pitch value.
     */
    private float mPitchMin;

    /**
     * Upper bound for the pitch value.
     */
    private float mPitchMax;

    /**
     * Width for the pitch's scales.
     */
    private float mPitchScaleWidth;

    /**
     * Hprizontal margin for the pitch's scales.
     */
    private float mPitchScaleMargin;

    /**
     * Distance between each pitch degree based on the view's height.
     */
    private float mPitchDegreesPerPixel;

    /**
     * Paint used to render the roll's hud.
     */
    private Paint mRollPaint;

    /**
     * Roll value used to update the widget.
     */
    private float mRoll;

    /**
     * Lower bound for the roll value.
     */
    private float mRollMin;

    /**
     * Upper bound for the roll value.
     */
    private float mRollMax;

    /**
     * Paint used to render the hud's reticle.
     */
    private Paint mReticlePaint;

    /**
     * Reticle's radius.
     */
    private float mReticleRadius;

    /*
    Allocated at startup, and reused to avoid unnecessary memory allocation at runtime.
     */
    private final RectF mRectFCache = new RectF();
    private final Path mPathCache = new Path();

    public SimplePitchRoll(Context context) {
        this(context, null);
    }

    public SimplePitchRoll(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplePitchRoll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.SimplePitchRoll, defStyleAttr, 0);

        try{
            final float textSize = attributes.getDimension(R.styleable.SimplePitchRoll_android_textSize, 20f);

            /*Pitch properties */
            mPitchMin = attributes.getFloat(R.styleable.SimplePitchRoll_pitchMin,
                    NORMALIZED_PITCH_LOWER_BOUND);
            mPitchMax = attributes.getFloat(R.styleable.SimplePitchRoll_pitchMax,
                    NORMALIZED_PITCH_UPPER_BOUND);
            mPitch = attributes.getFloat(R.styleable.SimplePitchRoll_pitch,
                    (mPitchMax + mPitchMin) / 2);
            checkPitchIsWithinRange(mPitch);

            mPitchScaleWidth = attributes.getDimension(R.styleable.SimplePitchRoll_pitchScaleWidth,
                    30f);
            mPitchScaleMargin = attributes.getDimension(R.styleable
                    .SimplePitchRoll_pitchScaleMargin, 8f);
            mPitchPaint = new Paint();
            mPitchPaint.setStyle(Paint.Style.STROKE);
            mPitchPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
            mPitchPaint.setTextAlign(Paint.Align.CENTER);
            mPitchPaint.setAntiAlias(true);
            mPitchPaint.setColor(attributes.getColor(R.styleable.SimplePitchRoll_pitchColor,
                    Color.WHITE));
            mPitchPaint.setTextSize(textSize);

            mRollMin = attributes.getFloat(R.styleable.SimplePitchRoll_rollMin, NORMALIZED_ROLL_LOWER_BOUND);
            mRollMax = attributes.getFloat(R.styleable.SimplePitchRoll_rollMax,
                    NORMALIZED_ROLL_UPPER_BOUND);
            mRoll = attributes.getFloat(R.styleable.SimplePitchRoll_roll,
                    (mRollMax + mRollMin)/ 2);
            checkRollIsWithinRange(mRoll);

            mRollPaint = new Paint();
            mRollPaint.setStyle(Paint.Style.STROKE);
            mRollPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
            mRollPaint.setAntiAlias(true);
            mRollPaint.setColor(attributes.getColor(R.styleable.SimplePitchRoll_rollColor,
                    Color.WHITE));
            mRollPaint.setTextSize(textSize);

            mReticleRadius = attributes.getDimension(R.styleable.SimplePitchRoll_reticleRadius, 10f);
            mReticlePaint = new Paint();
            mReticlePaint.setStyle(Paint.Style.STROKE);
            mReticlePaint.setAntiAlias(true);
            mReticlePaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
            mReticlePaint.setColor(attributes.getColor(R.styleable.SimplePitchRoll_reticleColor,
                    Color.RED));
        }
        finally{
            attributes.recycle();
        }
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        //Translate the canvas to account for the view padding
        canvas.translate(mCanvasXPadding, mCanvasYPadding);

        //Draw each view component.
        drawRoll(canvas);
        drawPitch(canvas);
        drawReticle(canvas);
    }

    private float normalizeRoll(float roll){
        return normalizeValue(NORMALIZED_ROLL_UPPER_BOUND, NORMALIZED_ROLL_LOWER_BOUND, roll,
                mRollMax, mRollMin);
    }

    private float denormalizeRoll(float normalizedRoll){
        return denormalizeValue(NORMALIZED_ROLL_UPPER_BOUND, NORMALIZED_ROLL_LOWER_BOUND,
                normalizedRoll, mRollMax, mRollMin);
    }

    private float normalizePitch(float pitch){
        return normalizeValue(NORMALIZED_PITCH_UPPER_BOUND, NORMALIZED_PITCH_LOWER_BOUND, pitch,
                mPitchMax, mPitchMin);
    }

    private float denormalizePitch(float normalizedPitch){
        return denormalizeValue(NORMALIZED_PITCH_UPPER_BOUND, NORMALIZED_PITCH_LOWER_BOUND,
                normalizedPitch, mPitchMax, mPitchMin);
    }

    private float normalizeValue(float scaleMax, float scaleMin, float value, float valueMax,
                                 float valueMin){
        if(valueMax == valueMin || scaleMax == scaleMin){
            throw new IllegalArgumentException("Max and minimum value should not be equal.");
        }

        //Shortcut
        if(scaleMax == valueMax && scaleMin == valueMin)
            return value;

        return scaleMin + (value - valueMin) * (scaleMax - scaleMin) / (valueMax - valueMin);
    }

    private float denormalizeValue(float scaleMax, float scaleMin, float normalizedValue,
                                   float valueMax, float valueMin){
        if(scaleMax == scaleMin || valueMax == valueMin){
            throw new IllegalArgumentException("Max and minimum value should not be equal.");
        }

        //Shortcut
        if(scaleMax == valueMax && scaleMin == valueMin)
            return normalizedValue;

        return valueMin + ((normalizedValue - scaleMin) * (valueMax - valueMin) /
                (scaleMax - scaleMin));
    }

    private void drawRoll(Canvas canvas){
        final float halfWidth = mWidth / 2;
        final float halfHeight = mHeight / 2;
        final float arcHRadius = mWidth / 2 - mReticleRadius;
        final float arcVRadius = mHeight / 2 - mReticleRadius;
        final float halfRadius = mReticleRadius / 2;
        final float normalizedRoll = normalizeRoll(mRoll);

        mRectFCache.set(mReticleRadius, mReticleRadius, mWidth - mReticleRadius,
                mHeight - mReticleRadius);

        //Draw the arc
        canvas.drawArc(mRectFCache, 225, ROLL_ARC_SWEEP_ANGLE, false, mRollPaint);

        //Draw the center triangle
        mPathCache.reset();
        Path arrow = mPathCache;
        float tempOffset = 2 * mReticlePaint.getStrokeWidth();
        arrow.moveTo(halfWidth, mReticleRadius - tempOffset);
        arrow.lineTo(halfWidth - mReticleRadius, 0);
        arrow.lineTo(halfWidth + mReticleRadius, 0);
        arrow.close();
        canvas.drawPath(arrow, mReticlePaint);

        //Draw the ticks.
        for(int i = (int) NORMALIZED_ROLL_LOWER_BOUND; i <= 0; i+=
                15){
            if(i != 0){
                //Draw ticks
                float sinI = (float) Math.sin(i * DEG_2_RAD);
                float cosI = (float) Math.cos(i * DEG_2_RAD);

                float dx = sinI * arcHRadius;
                float dy = cosI * arcVRadius;
                float ex = sinI * (arcHRadius + halfRadius);
                float ey = cosI * (arcVRadius + halfRadius);

                canvas.drawLine(halfWidth + dx, halfHeight -dy, halfWidth + ex, halfHeight -ey,
                        mRollPaint);

                //Draw symmetric ticks
                canvas.drawLine(halfWidth - dx, halfHeight - dy, halfWidth - ex, halfHeight - ey,
                        mRollPaint);
            }
        }

        //Draw the roll triangle bottom
        canvas.save();

        final float rollTriangleBaseY = 2 * mReticleRadius + tempOffset;
        canvas.rotate(-normalizedRoll, halfWidth, halfHeight);

        arrow.reset();
        arrow.moveTo(halfWidth, mReticleRadius + tempOffset);
        arrow.lineTo(halfWidth - mReticleRadius, rollTriangleBaseY);
        arrow.lineTo(halfWidth + mReticleRadius, rollTriangleBaseY);
        arrow.close();
        canvas.drawPath(arrow, mReticlePaint);

        canvas.restore();
    }

    private void drawPitch(Canvas canvas){
        final float halfWidth = mWidth / 2;
        final float halfHeight = mHeight / 2;
        final float upperLimit = 2.5f * mReticleRadius;
        final float lowerLimit = mHeight - upperLimit;
        final float pitchScaleXOffset = mPitchScaleWidth + mPitchScaleMargin;
        final float textOffset = mPitchPaint.getTextSize() / 4;
        final float pitchYOffset = normalizePitch(mPitch) * mPitchDegreesPerPixel;

        canvas.save();

        //Rotate the canvas to reflect the current roll value.
        canvas.rotate(-normalizeRoll(mRoll), halfWidth, halfHeight);

        //Draw the pitch gauge
        final float halfPitchScaleWidth = mPitchScaleWidth / 2;
        for(int i = (int)NORMALIZED_PITCH_LOWER_BOUND ; i <= (int)
                NORMALIZED_PITCH_UPPER_BOUND; i++){

            float yPos = (-i* mPitchDegreesPerPixel + pitchYOffset) + halfHeight;
            if(yPos >= upperLimit && yPos <= lowerLimit){
                if(i % 2 == 0){
                    canvas.drawLine(halfWidth - pitchScaleXOffset, yPos,
                            halfWidth - mPitchScaleMargin, yPos, mPitchPaint);
                    canvas.drawText(String.valueOf((int)denormalizePitch(i)), halfWidth,
                            yPos + textOffset, mPitchPaint);
                    canvas.drawLine(halfWidth + mPitchScaleMargin, yPos,
                            halfWidth + pitchScaleXOffset, yPos, mPitchPaint);
                }
                else{
                    canvas.drawLine(halfWidth - halfPitchScaleWidth, yPos,
                            halfWidth + halfPitchScaleWidth, yPos, mPitchPaint);
                }
            }
        }

        canvas.restore();
    }

    private void drawReticle(Canvas canvas){
        final float halfWidth = mWidth / 2;
        final float halfHeight = mHeight / 2;

        canvas.save();

        //Rotate the canvas based on the roll value
        canvas.rotate(-normalizeRoll(mRoll), halfWidth, halfHeight);

        canvas.drawCircle(halfWidth, halfHeight, mReticleRadius, mReticlePaint);
        canvas.drawLine(halfWidth - mReticleRadius, halfHeight, halfWidth - mReticleRadius * 2,
                halfHeight, mReticlePaint);
        canvas.drawLine(halfWidth + mReticleRadius, halfHeight, halfWidth + mReticleRadius *2,
                halfHeight, mReticlePaint);
        canvas.drawLine(halfWidth, halfHeight - mReticleRadius, halfWidth,
                halfHeight - mReticleRadius * 2, mReticlePaint);

        canvas.restore();
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mCanvasXPadding = getPaddingLeft();
        mCanvasYPadding = getPaddingTop();

        //Account for padding
        float xPad = mCanvasXPadding + getPaddingRight();
        float yPad = mCanvasYPadding + getPaddingBottom();

        mWidth = width - xPad;
        mHeight = height - yPad;

        mPitchDegreesPerPixel = (mHeight - 5 * mReticleRadius) *2 / (NORMALIZED_PITCH_UPPER_BOUND -
                NORMALIZED_PITCH_LOWER_BOUND);
    }

    public float getPitchScaleWidth(){
        return mPitchScaleWidth;
    }

    public void setPitchScaleWidth(float pitchScaleWidth){
        mPitchScaleWidth = pitchScaleWidth;
        invalidate();
    }

    public float getPitchScaleMargin(){
        return mPitchScaleMargin;
    }

    public void setPitchScaleMargin(float margin){
        mPitchScaleMargin = margin;
        invalidate();
    }

    public int getPitchColor(){
        return mPitchPaint.getColor();
    }

    public void setPitchColor(int color){
        mPitchPaint.setColor(color);
        invalidate();
    }

    public int getRollColor(){
        return mRollPaint.getColor();
    }

    public void setRollColor(int color){
        mRollPaint.setColor(color);
        invalidate();
    }

    public float getTextSize(){
        return mPitchPaint.getTextSize();
    }

    public void setTextSize(float textSize){
        mPitchPaint.setTextSize(textSize);
        invalidate();
    }

    public float getPitch() {
        return mPitch;
    }

    public void setPitch(float pitch) {
        checkPitchIsWithinRange(pitch);

        this.mPitch = pitch;
        invalidate();
    }

    private void checkPitchIsWithinRange(float pitch){
        if(pitch > mPitchMax || pitch < mPitchMin){
            throw new IllegalArgumentException("Pitch vlaue should be within max pitch (" +
                    mPitchMax + ") and min pitch (" + mPitchMin + ").");
        }
    }

    public float getRoll() {
        return mRoll;
    }

    public void setRoll(float roll) {
        checkRollIsWithinRange(roll);

        this.mRoll = roll;
        invalidate();
    }

    private void checkRollIsWithinRange(float roll){
        if(roll > mRollMax || roll < mRollMin){
            throw new IllegalArgumentException("Roll value should be within max roll (" +
                    mRollMax + ") and min roll (" + mRollMin + ").");
        }
    }

    public void setPitchRoll(float pitch, float roll){
        checkPitchIsWithinRange(pitch);
        checkRollIsWithinRange(roll);

        mPitch = pitch;
        mRoll = roll;
        invalidate();
    }

    public float getReticleRadius(){
        return mReticleRadius;
    }

    public void setReticleRadius(float radius){
        mReticleRadius = radius;
        invalidate();
    }

    public int getReticleColor(){
        return mReticlePaint.getColor();
    }

    public void setReticleColor(int color){
        mReticlePaint.setColor(color);
        invalidate();
    }

    public float getPitchMin() {
        return mPitchMin;
    }

    public void setPitchMin(float mPitchMin) {
        this.mPitchMin = mPitchMin;
        invalidate();
    }

    public float getPitchMax() {
        return mPitchMax;
    }

    public void setPitchMax(float mPitchMax) {
        this.mPitchMax = mPitchMax;
        invalidate();
    }

    public float getRollMin() {
        return mRollMin;
    }

    public void setRollMin(float mRollMin) {
        this.mRollMin = mRollMin;
        invalidate();
    }

    public float getRollMax() {
        return mRollMax;
    }

    public void setRollMax(float mRollMax) {
        this.mRollMax = mRollMax;
        invalidate();
    }
}
