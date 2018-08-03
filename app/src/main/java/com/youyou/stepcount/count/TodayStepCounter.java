package com.youyou.stepcount.count;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

import com.youyou.stepcount.listener.OnStepCountListener;
import com.youyou.stepcount.util.LogUtil;
import com.youyou.stepcount.util.StepCountPreference;
import com.youyou.stepcount.util.StepCountUtil;

import static android.content.Context.SENSOR_SERVICE;
import static com.youyou.stepcount.constant.StepCountConstant.StepCount;

/**
 * 计步传感器计算当天步数，不需要后台Service
 *
 * @author zony
 * @time 18-6-20
 */
public class TodayStepCounter implements SensorEventListener {

    private static final String TAG = "TodayStepCounter";

    private Context mContext;

    /**
     * 传感器
     */
    private SensorManager sensorManager;

    /**
     * 步伐总数传感器
     */
    private Sensor stepCounter;

    /**
     * 单次步伐传感器
     */
    private Sensor stepDetector;

    /**
     * 计步监听
     */
    private OnStepCountListener mOnStepCountListener;

    /**
     * 当天日期
     */
    private String mTodayDate;

    /**
     * 偏移步数
     */
    private int offsetStep = 0;

    /**
     * 当前步数
     */
    private int currStep = 0;


    /**
     * 是否关机过
     */
    private boolean isShutdown = false;

    /**
     * 是否需要清除步数
     */
    private boolean isCleanStep = false;

    /**
     * 检测是否日期变化
     */
    private boolean isDateChange = false;

    private StepCountPreference mPreference;

    public TodayStepCounter(Context context, OnStepCountListener onStepCountListener) {
        this.mContext = context;
        this.mOnStepCountListener = onStepCountListener;
        init(context);
    }

    /**
     * 初始化
     *
     * @author zony
     * @time 18-6-20
     */
    private void init(Context context) {
        if (context == null) {
            LogUtil.w(TAG, "init context is null, return!");
            return;
        }
        //获取传感器系统服务
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        //获取计步总数传感器
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        //获取单次计步传感器
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        mPreference = StepCountPreference.getInstance(context);
        initData();
        initBroadcastReceiver(context);
    }

    /**
     * 初始化数据
     *
     * @author zony
     * @time 18-6-21
     */
    private void initData() {
        mTodayDate = (String) mPreference.getData(StepCount.STEP_TODAY, "");
        currStep = (int) mPreference.getData(StepCount.CURR_STEP, 0);
        offsetStep = (int) mPreference.getData(StepCount.STEP_OFFSET, 0);
        isShutdown = isShutdown();
        LogUtil.i(TAG, "initData mTodayDate: " + mTodayDate + ", currStep: " + currStep + ", offsetStep: " + offsetStep + ", isShutdown: " + isShutdown);
    }

    /**
     * 是否关机过
     *
     * @author zony
     * @time 18-6-21
     */
    private boolean isShutdown() {
        if ((long) mPreference.getData(StepCount.ELAPSED_REALTIMEl, 0L)
                > SystemClock.elapsedRealtime()) {
            //上次运行的时间大于当前运行时间判断为重启，只是增加精度，极端情况下连续重启，会判断不出来
            LogUtil.i(TAG, "上次运行的时间大于当前运行时间判断为重启");
            return true;
        }
        return false;
    }

    /**
     * 调整时间或时区以及一分钟广播
     *
     * @param context 上下文环境
     * @author zony
     * @time 18-6-21
     */
    private void initBroadcastReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String intentAction = intent.getAction();
                if (Intent.ACTION_DATE_CHANGED.equals(intentAction)
                        || Intent.ACTION_TIMEZONE_CHANGED.equals(intentAction)) {
                    isDateChange = true;
                    LogUtil.i(TAG, "ACTION_TIME_TICK intentAction: " + intentAction);
                }
            }
        };
        context.registerReceiver(mBatInfoReceiver, filter);
    }

    /**
     * 日期改变后步数清零
     *
     * @param counterStep 计步器步数
     * @author zony
     * @time 18-6-21
     */
    private void dateChangeCleanStep(int counterStep) {
        //时间改变后清零，或者0点分隔回调
        if (!StepCountUtil.getTodayDate().equals(mTodayDate)) {
            cleanStep(counterStep);

            mTodayDate = StepCountUtil.getTodayDate();
            LogUtil.i(TAG, "dateChangeCleanStep mTodayDate: " + mTodayDate);
            mPreference.putData(StepCount.STEP_TODAY, mTodayDate);
        }
    }

    /**
     * 清空步数
     *
     * @author zony
     * @time 18-6-21
     */
    private void cleanStep(int counterStep) {
        LogUtil.i(TAG, "cleanStep");
        //清除步数，步数归零
        offsetStep = counterStep;
        mPreference.putData(StepCount.STEP_OFFSET, offsetStep);

        currStep = 0;
        mPreference.putData(StepCount.CURR_STEP, currStep);
    }

    /**
     * 关机重启后显示上次步数
     *
     * @param currStep 上次保存计步器步数
     * @author zony
     * @time 18-6-21
     */
    private void shutdownStep(int currStep) {
        //重新设置offset
        offsetStep = currStep - (int) mPreference.getData(StepCount.CURR_STEP, 0);
        mPreference.putData(StepCount.STEP_OFFSET, offsetStep);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (Sensor.TYPE_STEP_COUNTER == sensorEvent.sensor.getType()) {
            int counterStep = (int) sensorEvent.values[0];

            //检测是否检测过日期变化
            if (isDateChange) {
                dateChangeCleanStep(counterStep);
                isDateChange = false;
            }

            //处理关机启动
            if (isShutdown) {
                LogUtil.i(TAG, "onSensorChanged shutdownStep");
                shutdownStep(counterStep);
                isShutdown = false;
            }

            currStep = counterStep - offsetStep;
            if (currStep < 0) {
                //容错处理，无论任何原因步数不能小于0，如果小于0，直接清零
                cleanStep(counterStep);
            }

            mPreference.putData(StepCount.CURR_STEP, currStep);
            mPreference.putData(StepCount.ELAPSED_REALTIMEl, SystemClock.elapsedRealtime());
            LogUtil.i(TAG, "onSensorChanged counterStep : " + counterStep + " --- "
                    + "offsetStep : " + offsetStep + " --- " + "currStep : " + currStep);

            if (mOnStepCountListener != null) {
                mOnStepCountListener.onStepCounterChange(currStep);
            }
        } else if (Sensor.TYPE_STEP_DETECTOR == sensorEvent.sensor.getType()) {
            LogUtil.i(TAG, "onSensorChanged TYPE_STEP_DETECTOR current step: " + sensorEvent.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        LogUtil.i(TAG, "onAccuracyChanged senor name: " + sensor.getName() + ", accuracy: " + accuracy);
    }

    /**
     * 注册传感器事件监听器
     *
     * @author zony
     * @time 18-6-20
     */
    public void registerSensor() {
        if (mContext == null) {
            LogUtil.w(TAG, "registerSensor context is null, return!");
            return;
        }

        if (StepCountUtil.isSupportStepCounter(mContext)) {
            if (sensorManager == null) {
                LogUtil.w(TAG, "registerSensor sensorManager is null, return!");
                return;
            }
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_FASTEST);
        }

        if (StepCountUtil.isSupportStepDetector(mContext)) {
            if (sensorManager == null) {
                LogUtil.w(TAG, "registerSensor sensorManager is null, return!");
                return;
            }
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    /**
     * 解注册传感器事件监听器
     *
     * @author zony
     * @time 18-6-20
     */
    public void unregisterSensor() {
        if (mContext == null) {
            LogUtil.w(TAG, "unregisterSensor context is null, return!");
            return;
        }
        if (StepCountUtil.isSupportStepCounter(mContext)
                && StepCountUtil.isSupportStepDetector(mContext)) {
            if (sensorManager == null) {
                LogUtil.w(TAG, "registerSensor sensorManager is null, return!");
                return;
            }
            sensorManager.unregisterListener(this);
        }
    }
}
