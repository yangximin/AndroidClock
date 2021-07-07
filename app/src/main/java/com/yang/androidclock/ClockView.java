package com.yang.androidclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;


/**
 * Created by yxm on 2021/7/7.
 */
public class ClockView extends View {

    private float mHourDegree;
    private float mMinuteDegree;
    private float mSecondDegree;

    private int mLightColor;
    private Paint mSecondPaint;
    private Paint mHourPaint;

    private int mRadius;
    private Path mSecondPath = new Path();
    private Path mHourPath = new Path();
    /* 加一个默认的padding值，为了防止用camera旋转时钟时造成四周超出view大小 */
    private float mDefaultPadding;

    private Path mDialPath = new Path();
    private Paint mDialPaint;

    Paint textPaint = new Paint();

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        //秒针
        mSecondPaint = new Paint((Paint.ANTI_ALIAS_FLAG));
        mSecondPaint.setStyle(Paint.Style.FILL);
        mSecondPaint.setColor(getResources().getColor(R.color.white));
        //分针
        mMinutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mMinutePaint.setStyle(Paint.Style.STROKE);
        mMinutePaint.setColor(getResources().getColor(R.color.white));
        //时针
        mHourPaint = new Paint((Paint.ANTI_ALIAS_FLAG));
        mHourPaint.setStyle(Paint.Style.FILL);
        mHourPaint.setColor(getResources().getColor(R.color.red));
        //刻度
        mDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDialPaint.setColor(getResources().getColor(R.color.white));
        mDialPaint.setStyle(Paint.Style.STROKE);
        mDialPaint.setStrokeWidth(2);
        //文字
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(2);
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(true);
        textPaint.setColor(getResources().getColor(R.color.white));
        textPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = w / 2;
        mDefaultPadding = 0.2f * mRadius;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureDimension(widthMeasureSpec), measureDimension(heightMeasureSpec));
    }

    private int measureDimension(int measureSpec) {
        int defaultSize = 800;
        int model = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (model) {
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.AT_MOST:
                return Math.min(size, defaultSize);
            case MeasureSpec.UNSPECIFIED:
                return defaultSize;
        }
        return defaultSize;
    }

    private void getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        float milliSecond = calendar.get(Calendar.MILLISECOND);
        float second = calendar.get(Calendar.SECOND) + milliSecond / 1000;
        float minute = calendar.get(Calendar.MINUTE) + second / 60;
        float hour = calendar.get(Calendar.HOUR) * 5 + minute / 60;
        mSecondDegree = second / 60 * 360;
        mMinuteDegree = minute / 60 * 360;
        mHourDegree = hour / 60 * 360;
    }

    Canvas mCanvas;

    private Paint mMinutePaint;
    private Path mMinutePath = new Path();
    private RectF mCircleRectF = new RectF();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        drawCircleSurface();
        getCurrentTime();
        drawSecondNeedle();
        drawMinuteNeedle();
        drawHourNeedle();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        }, 10);
    }

    boolean isDrawSurface = false;

    private void drawCircleSurface() {
        if (isDrawSurface) return;
        float center = getWidth() >> 1;
        mDialPaint.setStyle(Paint.Style.STROKE);
        mCanvas.drawCircle(center, center, mRadius - mDefaultPadding, mDialPaint);
        mDialPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawCircle(center, center, 4, mDialPaint);
        for (int i = 0; i < 60; i++) {
            mCanvas.save();
            mCanvas.rotate(360 + i * 6, center, center);
            if (i % 5 == 0) {
                mDialPaint.setStrokeWidth(8);
                mCanvas.drawLine(center, mDefaultPadding, center, mDefaultPadding + 30, mDialPaint);
            } else {
                mDialPaint.setStrokeWidth(4);
                mCanvas.drawLine(center, mDefaultPadding, center, mDefaultPadding + 15, mDialPaint);
            }
            if (i % 15 == 0) {
                String text = "" + (i == 0 ? "12" : ((i / 15) * 3));
                //计算baseline
                Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
                float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
                float baseline = mDefaultPadding + 60 + distance;
                mCanvas.drawText(text, center, baseline, textPaint);
            }
            mCanvas.restore();
//            mDialPath.reset();
        }
    }


    /**
     * 秒针
     */
    private void drawSecondNeedle() {
        mCanvas.save();
        float center = getWidth() >> 1;
        mCanvas.rotate(mSecondDegree, center, center);
        mSecondPath.reset();
//        canvas.save();
        float offset = mDefaultPadding + 35;
        mSecondPath.moveTo(center, offset);
        mSecondPath.lineTo(center - 0.05f * mRadius, offset + 0.08f * mRadius);
        mSecondPath.lineTo(center + 0.05f * mRadius, offset + 0.08f * mRadius);
        mSecondPath.close();
        mCanvas.drawPath(mSecondPath, mSecondPaint);
        mCanvas.restore();
    }

    private int lineWith = 3;
    float circleRadius = 16;

    /**
     * 分针
     */
    private void drawMinuteNeedle() {
        float center = getWidth() >> 1;
        mCanvas.save();
        mCanvas.rotate(mMinuteDegree, center, center);
        mMinutePath.reset();
        float offset = (float) (mRadius * 0.3) + mDefaultPadding;
        mMinutePath.moveTo(center - lineWith, center - circleRadius);
        mMinutePath.lineTo(center - lineWith, offset);
        mMinutePath.quadTo(center, offset - 6, center + lineWith, offset);//画圆角
        mMinutePath.lineTo(center + lineWith, center - circleRadius);
        mMinutePath.close();
        mMinutePaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(mMinutePath, mMinutePaint);
//        mCircleRectF.set(center - circleRadius, center - circleRadius, center + circleRadius, center + circleRadius);
        mMinutePaint.setStyle(Paint.Style.STROKE);
        mMinutePaint.setStrokeWidth(6);
        mCanvas.drawCircle(center, center, circleRadius, mMinutePaint);
        mCanvas.restore();
    }

    private void drawHourNeedle() {
        float center = getWidth() >> 1;
        float offset = (float) (mRadius * 0.4) + mDefaultPadding;
        mCanvas.save();
        mCanvas.rotate(mHourDegree, center, center);
        mMinutePath.reset();
        mHourPath.moveTo(center - 4, center - circleRadius - 3);
        mHourPath.lineTo(center, offset);
        mHourPath.lineTo(center + 4, center - circleRadius - 3);
        mCanvas.drawPath(mHourPath, mHourPaint);
        mCanvas.restore();

    }

}
