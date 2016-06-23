package com.braceletdemo.view;

import java.util.Random;

import com.braceletdemo.util.DensityUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author gaopengfei
 */
public class EffectSleepView extends View {

    /** 画文字、画睡眠质量、画Touch时竖线的画笔 */
    private Paint mTxtPaint, mSleepQualityPaint, mTouchEffectPaint;
    /** 当前view的宽高尺寸(像素) */
    private int mViewWidth, mViewHeight;
    /** 每分钟所占宽度 */
    private float mUnitWidth;
    /** 每小时所占宽度(左右两边预留10像素间距) */
    private float mUnitTimeWidth;
    /** 下方时间轴的预设高度 */
    private int mTimelineHeight = 30;
    /** 总的分钟数(默认12个小时) */
    private int mUnitCount = 60 * 12;
    /** 起始时间,小时为单位,外后推延12个小时 */
    private int mStartTime = 21;
    /** 每分钟的睡眠质量数据 */
    private int mData[];

    /** onTouch down时的坐标 */
    float mStartX = 0;

    public EffectSleepView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 初始化文字画笔,宽度2,打开抗锯齿,画白色
        mTxtPaint = new Paint();
        mTxtPaint.setStrokeWidth(2);
        mTxtPaint.setAntiAlias(true);
        mTxtPaint.setColor(Color.WHITE);

        // 初始化睡眠质量画笔,开启抗锯齿,画#80E1C2
        mSleepQualityPaint = new Paint();
        mSleepQualityPaint.setAntiAlias(true);
        mSleepQualityPaint.setColor(Color.parseColor("#80E1C2"));

        // 初始化onTouch时竖线的画笔
        mTouchEffectPaint = new Paint();
        mTouchEffectPaint.setAntiAlias(true);
        mTouchEffectPaint.setColor(Color.parseColor("#FF9000"));
        mTouchEffectPaint.setStrokeWidth(2.0f);
    }

    {
        mData = new int[mUnitCount];
        updata();
    }

    private void updata() {
        Random r = new Random();
        for (int i = 0; i < mData.length; i++) {
            mData[i] = r.nextInt(200);
        }
        invalidate();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            this.mViewWidth = getWidth();
            this.mViewHeight = getHeight();
            this.mUnitTimeWidth = mViewWidth / 13.0f;
            this.mUnitWidth = this.mViewWidth / (float) mData.length;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTimeline(canvas);
        drawSleepQuality(canvas);
    }

    /** 字体大小22,时间12个小时 */
    private void drawTimeline(Canvas canvas) {
        canvas.drawLine(0, mViewHeight - mTimelineHeight, mViewWidth, mViewHeight - mTimelineHeight, mTxtPaint);
        mTxtPaint.setTextSize(22);
        for (int i = 0; i < 13; i++) {
            String time;
            if (mStartTime % 24 < 10) {
                time = "0" + mStartTime++ % 24;
            } else {
                time = " " + mStartTime++ % 24;
            }
            canvas.drawText(time, i * mUnitTimeWidth, mViewHeight - 5, mTxtPaint);
        }
        mStartTime--;// 上面的time加多了一个减回去
    }

    /** 绘制睡眠质量 */
    private void drawSleepQuality(Canvas canvas) {
        /**
         * 加盐,防止数据绘制时溢出,具体盐分要看具体数据取值范围与屏幕大小
         */
        float salt = DensityUtil.dip2px(getContext(), 75.0f / 200.0f);
        for (int i = 0; i < mData.length; i++) {
            canvas.drawRect(i * mUnitWidth, mViewHeight - mTimelineHeight - mData[i] * salt, i * mUnitWidth + mUnitWidth, mViewHeight - mTimelineHeight,
                    mSleepQualityPaint);
        }
    }

    /** 这个onTouchEvent是手势切换数据的效果 */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (event.getX() > mStartX + 100) {// 向右滑动
                    updata();
                } else if (event.getX() < mStartX - 100) {// 向左滑动
                    updata();
                }
                break;
        }
        return true;
    }
}
