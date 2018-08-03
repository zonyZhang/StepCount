package com.youyou.stepcount.listener;

/**
 * 计步监听
 *
 * @author zony
 * @time 18-6-20
 */
public interface OnStepCountListener {

    /**
     * 用于显示步数
     *
     * @param step 步数
     */
    void onStepCounterChange(int step);
}
