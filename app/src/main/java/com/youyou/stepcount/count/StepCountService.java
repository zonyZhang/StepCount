package com.youyou.stepcount.count;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StepCountService extends Service {
    public StepCountService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
