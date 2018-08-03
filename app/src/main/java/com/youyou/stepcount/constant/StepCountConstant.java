package com.youyou.stepcount.constant;

/**
 * 项目相关常量
 *
 * @author zony
 */

public interface StepCountConstant {

    /**
     * 计歩相关
     */
    interface StepCount {
        /**
         * 上一次计步器的步数
         */
        String STEP_OFFSET = "step_offset";

        /**
         * 当天，用来判断是否跨天
         */
        String STEP_TODAY = "step_today";

        /**
         * 当前步数
         */
        String CURR_STEP = "curr_step";

        /**
         * 系统运行时间
         */
        String ELAPSED_REALTIMEl = "elapsed_realtime";
    }
}
