package com.youyou.stepcount.util;

import android.content.Context;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 计歩相关工具类
 *
 * @author zony
 * @time 18-6-20
 */
public class StepCountUtil {

    private static final String TAG = "StepCountUtil";

    /**
     * 是否支持StepCounter
     *
     * @param context 上下文环境
     * @author zony
     * @time 18-6-20
     */
    public static boolean isSupportStepCounter(Context context) {
        if (context == null) {
            LogUtil.w(TAG, "isSupportStepCounter context is null, return!");
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            LogUtil.w(TAG, "isSupportStepCounter packageManager is null, return!");
            return false;
        }
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER);
    }

    /**
     * 是否支持StepDetector
     *
     * @param context 上下文环境
     * @author zony
     * @time 18-6-20
     */
    public static boolean isSupportStepDetector(Context context) {
        if (context == null) {
            LogUtil.w(TAG, "isSupportStepDetector context is null, return!");
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            LogUtil.w(TAG, "isSupportStepDetector packageManager is null, return!");
            return false;
        }
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    /**
     * 获得当前日期
     *
     * @author zony
     * @time 18-6-21
     */
    public static String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }


    /**
     * 公里计算公式
     *
     * @param steps 步数
     * @author zony
     * @time 18-6-22
     */
    public static String getDistanceByStep(long steps) {
        return String.format("%.2f", steps * 0.6f / 1000);
    }


    /**
     * 千卡路里计算公式
     *
     * @param steps 步数
     * @author zony
     * @time 18-6-22
     */
    public static String getCalorieByStep(long steps) {
        return String.format("%.1f", steps * 0.6f * 60 * 1.036f / 1000);
    }
}
