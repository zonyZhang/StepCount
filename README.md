#### 实现功能:

    1.根据系统计步器获取当天步数

    2.关机之后保存当天步数

    3.改变时区或日期后清零步数



#### 使用说明:

    1.初始化
        可以在onCreate中初始化
        mTodayStepCounter = new TodayStepCounter(this, this);

    2.注册传感器监听
            在onResume中注册传感器监听
            @Override
            public void onResume() {
                super.onResume();
                mTodayStepCounter.registerSensor();
            }

    3.解注册传感器事件监听器
            在onPause中解注册传感器监听
            @Override
            public void onPause() {
                super.onPause();
                mTodayStepCounter.unregisterSensor();
            }
