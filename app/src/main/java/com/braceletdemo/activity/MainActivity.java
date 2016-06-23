package com.braceletdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.braceletdemo.R;
import com.braceletdemo.view.GesturePulseView;
import com.braceletdemo.view.GesturePulseView.OnPulseFrequencyTouchListener;
import com.braceletdemo.view.GestureSleepView;
import com.braceletdemo.view.GestureSleepView.OnSleepQualityTouchListener;

public class MainActivity extends Activity {

    /** 睡眠图表 */
    private GestureSleepView mSleepView;
    /** 睡眠质量数值 */
    private TextView mSleepQualityTV;

    /** 脉搏图表 */
    private GesturePulseView mPulseView;
    /** 脉搏数值 */
    private TextView mPulseTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /** 实现睡眠质量的回调 */
        mSleepView = (GestureSleepView) findViewById(R.id.gesturesleepview);
        mSleepQualityTV = (TextView) findViewById(R.id.gesture_sleepquality);
        mSleepView.SetOnSleepQualityTouchListener(new OnSleepQualityTouchListener() {
            @Override
            public void OnSleepQualityTouch(int data) {
                mSleepQualityTV.setText("质量:\t" + data);
            }
        });

        /** 实现脉搏的回调 */
        mPulseView = (GesturePulseView) findViewById(R.id.gesturepulseview);
        mPulseTV = (TextView) findViewById(R.id.gesture_normal_value);
        mPulseView.SetOnPulseFrequencyTouchListener(new OnPulseFrequencyTouchListener() {
            @Override
            public void OnSleepQualityTouch(int data) {
                mPulseTV.setText("数值:\t" + data);
            }
        });
    }
}