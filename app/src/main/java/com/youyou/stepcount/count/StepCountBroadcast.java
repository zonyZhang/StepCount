package com.youyou.stepcount.count;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.youyou.stepcount.MainActivity;

public class StepCountBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainIntent);
    }
}