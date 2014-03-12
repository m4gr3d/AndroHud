package com.ne0fhyklabs.androhud.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.ne0fhyklabs.androhud.R;

import static com.ne0fhyklabs.androhud.utils.Constants.DEFAULT_STROKE_WIDTH;

/**
 * HUD Yaw bar widget.
 * Created by fhuya on 3/2/14.
 */
public class SimpleYaw extends View {

    private enum TickPosition {
        BOTTOM,
        TOP
    }

    private static final int DEFAULT_TICK_POSITION = TickPosition.BOTTOM.ordinal();

    private static final int YAW_DEGREES_TO_SHOW = 120;

    private static final String COMPASS[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

    /**
     * This is the view's height, accounting for padding.
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
     * Where to place the tick markers at the top or bottom.
     */
    private TickPosition mTickPos;

    /**
     * Paint used to draw the markers' ticks, and text.
     */
    private Paint mTicksPaint;

    /**
     * Yaw value that's being reflected by the view.
     */
    private float mYaw;

    /**
     * Distance between each yaw degrees based on the view's width.
     */
    private float mYawDegreesPerPixel;

    /**
     * Paint used to draw the yaw needle.
     */
    private Paint mYawNeedlePaint;

    public SimpleYaw(Context context) {
        this(context, null);
    }

    public SimpleYaw(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleYaw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SimpleYaw,
                defStyleAttr,  0);

        try{
            mTickPos = TickPosition.values()[attributes.getInt(R.styleable.SimpleYaw_ticksPosition,
                    DEFAULT_TICK_POSITION)];

            mTicksPaint = new Paint();
            mTicksPaint.setStyle(Paint.Style.STROKE);
            mTicksPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
            mTicksPaint.setAntiAlias(true);
            mTicksPaint.setTextAlign(Paint.Align.CENTER);
            mTicksPaint.setTextSize(attributes.getDimension(R.styleable.SimpleYaw_android_textSize,
                    25f));
            mTicksPaint.setColor(attributes.getColor(R.styleable.SimpleYaw_ticksColor, Color.WHITE));

            mYaw = attributes.getFloat(R.styleable.SimpleYaw_yaw, 0f);

            mYawNeedlePaint = new Paint();
            mYawNeedlePaint.setAntiAlias(true);
            mYawNeedlePaint.setStrokeWidth(attributes.getDimension(R.styleable
                    .SimpleYaw_yawNeedleThickness, DEFAULT_STROKE_WIDTH));
            mYawNeedlePaint.setColor(attributes.getColor(R.styleable.SimpleYaw_yawNeedleColor,
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

        final float halfWidth = mWidth / 2;
        final float halfHeight = mHeight / 2;
        final float ticksHeight = mHeight / 5;

        final float ticksStartHeight = mTickPos == TickPosition.BOTTOM
                ? mHeight - ticksHeight
                : 0;
        final float ticksEndHeight = mTickPos == TickPosition.BOTTOM
                ? mHeight
                : ticksHeight;

        final float textYPos = halfHeight + mTicksPaint.getTextSize() / 3;

        final float centerDegrees = mYaw;
        final float mod = mYaw % 5;
        final float halfYawDegreesToShow = YAW_DEGREES_TO_SHOW / 2f;
        for(float angle = (centerDegrees - mod) - halfYawDegreesToShow;
                angle <= (centerDegrees - mod) + halfYawDegreesToShow;
                angle += 5){

            //Protect from wraparound
            float workAngle = (angle + 360f);
            while(workAngle >= 360)
                workAngle -= 360;

            //Need to draw "angle". How many pixels from center should it be.
            int distanceToCenter = (int) (((angle - centerDegrees) * mYawDegreesPerPixel) +
                    halfWidth);

            canvas.drawLine(distanceToCenter, ticksStartHeight, distanceToCenter,
                    ticksEndHeight, mTicksPaint);

            String yawText = "";
            if(workAngle % 45 == 0){
                int index = (int) workAngle / 45;
                yawText += COMPASS[index];
            }
            else if(workAngle % 15 == 0){
                yawText += (int)workAngle;
            }
            canvas.drawText(yawText, distanceToCenter, textYPos, mTicksPaint);
        }

        //Draw the center line
        canvas.drawLine(halfWidth, 0, halfWidth, mHeight, mYawNeedlePaint);
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

        mYawDegreesPerPixel = mWidth / YAW_DEGREES_TO_SHOW;
    }

    public float getYaw(){
        return mYaw;
    }

    public void setYaw(float yaw){
        mYaw = yaw;
        invalidate();
    }

    public int getTicksColor(){
        return mTicksPaint.getColor();
    }

    public void setTicksColor(int color){
        mTicksPaint.setColor(color);
        invalidate();
    }

    public int getYawNeedleColor(){
        return mYawNeedlePaint.getColor();
    }

    public void setYawNeedleColor(int color){
        mYawNeedlePaint.setColor(color);
        invalidate();
    }

    public int getTicksPosition(){
        return mTickPos.ordinal();
    }

    public void setTicksPosition(int positionIndex){
        mTickPos = TickPosition.values()[positionIndex];
        invalidate();
    }

    public float getTextSize(){
        return mTicksPaint.getTextSize();
    }

    public void setTextSize(float textSize){
        mTicksPaint.setTextSize(textSize);
        invalidate();
    }

    public float getYawNeedleThickness(){
        return mYawNeedlePaint.getStrokeWidth();
    }

    public void setYawNeedleThickness(float thickness){
        mYawNeedlePaint.setStrokeWidth(thickness);
        invalidate();
    }
}
