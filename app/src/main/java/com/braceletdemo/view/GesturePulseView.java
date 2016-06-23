package com.braceletdemo.view;

import java.util.Random;

import com.braceletdemo.R;
import com.braceletdemo.util.DensityUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author gaopengfei
 */
public class GesturePulseView extends View {

    /**
     * 画文字与画脉搏指数的画笔
     * 这里健康指数为了显示成带折线下有阴影效果的样式,使用倒画的方式
     * 默认为显示全的阴影,通过健康指数的不同在阴影从上往下画透明梯形来做成折线下的阴影效果
     */
    private Paint mTxtPaint, mPulsePaint, mTouchEffectPaint, mDashLinePaint;
    /** 当前view的宽高尺寸(像素) */
    private int mViewWidth, mViewHeight;
    /** 每小时所占宽度 */
    private float mUnitWidth;
    /** 每2小时所占宽度(左右两边预留10像素间距) */
    private float mUnitTimeWidth;
    /** 下方时间轴的预设高度 */
    private int mTimelineHeight = 30;
    /** 每个小时的脉搏指数,24条线是25个点 */
    private int[] mData = new int[25];
    /** 盐分,让数据*盐分来实现数据正常显示在屏幕上,具体盐分大小需要具体适配调整 */
    private float mSalt;

    /** onTouch时的回调 */
    private OnPulseFrequencyTouchListener mPulseFrequencyTouchListener;
    /** onTouch down时的坐标 */
    private float mStartX = -5;

    /** 缓冲 */
    private Canvas mCacheCanvas;
    private Bitmap mCacheBitmap;

    public GesturePulseView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 初始化文字画笔,宽度2,打开抗锯齿,画白色
        mTxtPaint = new Paint();
        mTxtPaint.setStrokeWidth(2);
        mTxtPaint.setAntiAlias(true);
        mTxtPaint.setColor(Color.WHITE);

        // 初始化背景色画笔,打开抗锯齿,画背景色#5F8CCF
        // 这个#5F8CCF值是Android手机跑起来之后从显示出来的颜色抓取的
        // 如果改了背景色与每个组件的框框的话,这个值需要重新从运行效果中抓取
        mPulsePaint = new Paint();
        mPulsePaint.setAntiAlias(true);
        mPulsePaint.setColor(Color.parseColor("#5F8CCF"));

        // 初始化onTouch时竖线的画笔
        mTouchEffectPaint = new Paint();
        mTouchEffectPaint.setAntiAlias(true);
        mTouchEffectPaint.setColor(Color.parseColor("#FF9000"));
        mTouchEffectPaint.setStrokeWidth(2.0f);

        // 初始化虚线,不过因为是两种颜色的虚线,所以具体的时候再赋值颜色
        mDashLinePaint = new Paint();
        mDashLinePaint.setAntiAlias(true);
        mDashLinePaint.setStrokeWidth(2);

        // 初始化盐分
        mSalt = DensityUtil.dip2px(getContext(), 75.0f / 200.0f);

        mCacheCanvas = new Canvas();
    }

    {
        updata();
    }

    private void updata() {
        Random r = new Random();
        for (int i = 0; i < mData.length; i++) {
            mData[i] = r.nextInt(200);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.mUnitTimeWidth = (mViewWidth - 20) / 12;
        this.mUnitWidth = mViewWidth / 24.0f;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            this.mViewWidth = getWidth();
            this.mViewHeight = getHeight();
            this.mUnitTimeWidth = (mViewWidth - 20) / 12;
            this.mUnitWidth = mViewWidth / 24.0f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCacheBitmap == null) {// 没缓冲过,增加一份缓冲,以后直接画缓冲中的bitmap再加上touch效果就行了
            mCacheBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Config.ARGB_8888);
            mCacheCanvas.setBitmap(mCacheBitmap);
            drawTimeline(mCacheCanvas);
            drawBackground(mCacheCanvas);
            drawLine(mCacheCanvas);
            drawDashLine(mCacheCanvas);
        }
        canvas.drawBitmap(mCacheBitmap, 0, 0, mTxtPaint);
        drawTouchEffect(canvas);
    }

    /** 画touch效果 */
    private void drawTouchEffect(Canvas canvas) {
        float x = mStartX;
        for (int i = 0; i < 25; i++) {
            if (mUnitWidth * i - 5 < mStartX && mUnitWidth * i + 5 > mStartX) {// 吸附效果
                x = mUnitWidth * i;
                canvas.drawCircle(x, mViewHeight - mTimelineHeight - mData[i] * mSalt, 5, mTouchEffectPaint);
                if (mPulseFrequencyTouchListener != null) {
                    mPulseFrequencyTouchListener.OnSleepQualityTouch(mData[i]);
                }
                break;
            }
        }
        canvas.drawLine(x, 0, x, mViewHeight - mTimelineHeight, mTouchEffectPaint);
    }

    /** 画折线,为了实现阴影效果其实画的是上方的透明梯形 */
    private void drawLine(Canvas canvas) {

        // 首先先画遮盖住阴影部分的梯形
        Path path = new Path();
        for (int i = 0; i < mData.length - 1; i++) {
            path.moveTo(i * mUnitWidth, 0);
            path.lineTo(i * mUnitWidth, mViewHeight - mTimelineHeight - mData[i] * mSalt);
            path.lineTo(i * mUnitWidth + mUnitWidth, mViewHeight - mTimelineHeight - mData[i + 1] * mSalt);
            path.lineTo(i * mUnitWidth + mUnitWidth, 0);
            canvas.drawPath(path, mPulsePaint);
        }

        // 因为24条折线是25个点,所以先手动画出第一个点,再for循环
        canvas.drawCircle(0, mViewHeight - mTimelineHeight - mData[0] * mSalt, 5, mTxtPaint);
        for (int i = 0; i < mData.length - 1; i++) {
            // 画折线
            canvas.drawLine(i * mUnitWidth, mViewHeight - mTimelineHeight - mData[i] * mSalt, i * mUnitWidth + mUnitWidth,
                    mViewHeight - mTimelineHeight - mData[i + 1] * mSalt, mTxtPaint);
            // 画点
            canvas.drawCircle(i * mUnitWidth + mUnitWidth, mViewHeight - mTimelineHeight - mData[i + 1] * mSalt, 5, mTxtPaint);
        }
    }

    /** 画时间轴 */
    private void drawTimeline(Canvas canvas) {
        canvas.drawLine(0, mViewHeight - mTimelineHeight, mViewWidth, mViewHeight - mTimelineHeight, mTxtPaint);
        mTxtPaint.setTextSize(22);
        for (int i = 0; i <= 12; i++) {
            String time;
            if (i < 5) {
                time = "0" + i * 2;
            } else if (i < 12) {
                time = " " + i * 2;
            } else {
                time = "00";
            }
            canvas.drawText(time, i * mUnitTimeWidth, mViewHeight - 5, mTxtPaint);
        }
    }

    /** 画阴影背景底色 */
    private void drawBackground(Canvas canvas) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        RectF rect = new RectF();
        rect.top = 0;
        rect.bottom = mViewHeight - mTimelineHeight;
        rect.left = 0;
        rect.right = mViewWidth;
        canvas.drawBitmap(bitmap, null, rect, mTxtPaint);
    }

    /** 画虚线标尺,因为高版本好像已经不持支PathEffect了,所以画虚线就是for循环出来的 */
    private void drawDashLine(Canvas canvas) {
        // 在值160与60的两个高度画两条虚线(需要注意的是,当前的取值范围为0~200)
        int highValue = 160;
        int deepValue = 60;
        mDashLinePaint.setColor(Color.parseColor("#A7DF8C"));
        for (int i = 0; i < mViewWidth; i += 10) {
            canvas.drawLine(i, mViewHeight - mTimelineHeight - highValue * mSalt, i + 5, mViewHeight - mTimelineHeight - highValue * mSalt, mDashLinePaint);
        }

        mDashLinePaint.setColor(Color.parseColor("#6AA9DA"));
        for (int i = 0; i < mViewWidth; i += 10) {
            canvas.drawLine(i, mViewHeight - mTimelineHeight - deepValue * mSalt, i + 5, mViewHeight - mTimelineHeight - deepValue * mSalt, mDashLinePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mStartX = event.getX();
        invalidate();
        return true;
    }

    public interface OnPulseFrequencyTouchListener {
        public void OnSleepQualityTouch(int data);
    }

    /** touch时回调方法 */
    public void SetOnPulseFrequencyTouchListener(OnPulseFrequencyTouchListener listener) {
        this.mPulseFrequencyTouchListener = listener;
    }
}
