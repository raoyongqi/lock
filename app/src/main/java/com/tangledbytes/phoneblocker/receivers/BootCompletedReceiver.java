package com.tangledbytes.phoneblocker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.tangledbytes.phoneblocker.activities.AppreciationActivity;
import com.tangledbytes.phoneblocker.services.BlockerService;
import com.tangledbytes.phoneblocker.utils.BlockerSession;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot Completed");
        BlockerSession blockerSession = new BlockerSession(context);
        blockerSession.invalidateSession();
    }
}
