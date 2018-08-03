package com.youyou.stepcount;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.TextView;

import com.youyou.stepcount.count.TodayStepCounter;
import com.youyou.stepcount.listener.OnStepCountListener;
import com.youyou.stepcount.util.LogUtil;
import com.youyou.stepcount.util.StepCountUtil;

public class MainActivity extends AppCompatActivity implements OnStepCountListener {
    private static final String TAG = "MainActivity";

    private TextView stepCounterText;
    private TextView isSupportStepDetector;
    private TextView isSupportStepCounter;
    private TodayStepCounter mTodayStepCounter;
    private NotificationManager notificationManager;
    private Notification notification;
    private NotificationCompat.Builder notificationBuilder;
    private static final int BROADCAST_REQUEST_CODE = 1000;

    /**
     * 卡路里
     */
    private String calorie;

    /**
     * 距离,单位KM
     */
    private String distanceKm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        mTodayStepCounter = new TodayStepCounter(this, this);
    }

    protected void initView() {
        setContentView(R.layout.activity_main);
        stepCounterText = findViewById(R.id.step_counter);
        isSupportStepDetector = findViewById(R.id.support_step_detector);
        isSupportStepCounter = findViewById(R.id.support_step_counter);

    }

    protected void initData() {
        isSupportStepCounter.setText("is support StepCounter:" + StepCountUtil.isSupportStepCounter(this));
        isSupportStepDetector.setText("is support StepDetector:" + StepCountUtil.isSupportStepDetector(this));
    }

    @Override
    public void onPause() {
        super.onPause();
        mTodayStepCounter.unregisterSensor();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTodayStepCounter.registerSensor();
    }

    @Override
    public void onStepCounterChange(int step) {
        distanceKm = StepCountUtil.getDistanceByStep(step);
        calorie = StepCountUtil.getCalorieByStep(step);

        if (notification == null) {
            initNotification(step);
        } else {
            updateNotification(step);
        }
        stepCounterText.setText("total step: " + step);
    }

    /**
     * 初始化通知栏通知
     *
     * @author zony
     * @time 18-6-22
     */
    private void initNotification(int currentStep) {
        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setPriority(Notification.PRIORITY_MIN);

        String receiverName = "com.youyou.stepcount.count.StepCountBroadcast";
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, BROADCAST_REQUEST_CODE, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        if (!TextUtils.isEmpty(receiverName)) {
            try {
                contentIntent = PendingIntent.getBroadcast(this, BROADCAST_REQUEST_CODE,
                        new Intent(this, Class.forName(receiverName)), PendingIntent.FLAG_UPDATE_CURRENT);
            } catch (Exception e) {
                e.printStackTrace();
                contentIntent = PendingIntent.getBroadcast(this, BROADCAST_REQUEST_CODE,
                        new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
        notificationBuilder.setContentIntent(contentIntent);
        int smallIcon = getResources().getIdentifier("icon_step_small", "mipmap", getPackageName());
        if (0 != smallIcon) {
            LogUtil.i(TAG, "initNotification smallIcon");
            notificationBuilder.setSmallIcon(smallIcon);
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round);// 设置通知小ICON
        }
        int largeIcon = getResources().getIdentifier("icon_step_large", "mipmap", getPackageName());
        if (0 != largeIcon) {
            LogUtil.i(TAG, "largeIcon");
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), largeIcon));
        } else {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));

        }
        notificationBuilder.setTicker(getString(R.string.app_name));
        notificationBuilder.setContentTitle(getString(R.string.notification_current_step, String.valueOf(currentStep)));
        notificationBuilder.setContentText(calorie + " 千卡  " + distanceKm + " 公里");

        //设置不可清除
        notificationBuilder.setOngoing(true);
        notification = notificationBuilder.build();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(R.string.app_name, notification);
    }

    /**
     * 更新通知
     *
     * @author zony
     * @time 18-6-22
     */
    private void updateNotification(int stepCount) {
        if (null == notificationBuilder || null == notificationManager) {
            return;
        }
        notificationBuilder.setContentTitle(getString(R.string.notification_current_step, String.valueOf(stepCount)));
        notificationBuilder.setContentText(calorie + " 千卡  " + distanceKm + " 公里");
        notification = notificationBuilder.build();
        notificationManager.notify(R.string.app_name, notification);
    }
}