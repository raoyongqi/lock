package com.tangledbytes.phoneblocker.services;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.tangledbytes.phoneblocker.R;
import com.tangledbytes.phoneblocker.activities.AppreciationActivity;
import com.tangledbytes.phoneblocker.overlays.BlockerOverlay;
import com.tangledbytes.phoneblocker.utils.BlockerSession;
import com.tangledbytes.phoneblocker.utils.Constants;
import com.tangledbytes.phoneblocker.utils.Utils;

public class BlockerService extends Service {
    private final String TAG = "BlockerService";
    private final int FOREGROUND_NOTIFICATION_ID = 23;
    private CountdownManager countdownManager;
    private NotificationCompat.Builder notificationBuilder;
    private boolean isBlockerThreadRunning = false;
    private boolean preventPowerOff;
    private boolean blockNotifications;
    private boolean blockCalls;
    private Handler handler;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BlockerSession blockerSession = new BlockerSession(this);
        handler = new Handler();
        mOverlay = new BlockerOverlay(this);
        preventPowerOff = blockerSession.preventFromPowerOff();
        blockNotifications = blockerSession.blockNotifications();
        blockCalls = blockerSession.blockCalls();
        long countdownDuration = blockerSession.getRemainingSessionDuration();
        showNotification();
        blockCallsAndNotifications();
        countdownManager = CountdownManager.startCountdown(countdownDuration);
        startBlockerThread();
        sendBroadcast(new Intent(Constants.BC_DEVICE_LOCKED));
        return START_REDELIVER_INTENT;
    }

    BlockerOverlay mOverlay;

    private void onCountdownCompletes() {
        Log.d(TAG, "Countdown completed. Device can be unlocked!!");
        notificationBuilder.setContentText("Timer has completed. You can now use your phone normally").setContentTitle("Timer Completed").setTicker("Timer Completed Ticker").setSmallIcon(R.mipmap.ic_launcher_round).setPriority(NotificationCompat.PRIORITY_MAX).build();
        mOverlay.removeOverlay();
        PendingIntent piAppreciationActivity = PendingIntent.getActivity(this, 0, new Intent(this, AppreciationActivity.class), -1);
        notificationBuilder.setContentIntent(piAppreciationActivity);
        NotificationManagerCompat.from(this).notify(FOREGROUND_NOTIFICATION_ID, notificationBuilder.build());
        sendBroadcast(new Intent(Constants.BC_DEVICE_UNLOCKED));
        new AppreciationActivityStarter().start();
    }

    private void blockCallsAndNotifications() {
        if (blockCalls && blockNotifications)
            Log.d(TAG, "You forgot to implement call blocker and notifications");
    }

    public void startBlockerThread() {
        if (isBlockerThreadRunning) return;
        Blocker blocker = new Blocker();
        blocker.start();
    }

    private void showNotification() {
        notificationBuilder = new NotificationCompat.Builder(this, Constants.DEFAULT_NOTIFICATION_CHANNEL).setSmallIcon(R.mipmap.ic_launcher_round).setContentTitle(getString(R.string.blocker_service_notification_content_title)).setOnlyAlertOnce(true).setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification notification = notificationBuilder.build();
        NotificationManagerCompat.from(this).notify(FOREGROUND_NOTIFICATION_ID, notification);
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    private void updateNotification() {
        long remainingMillis = countdownManager.getRemainingMillis();
        int[] remainingTimeComponents = Utils.getComponents(remainingMillis);
        notificationBuilder.setContentText("Remaining: " + remainingTimeComponents[2] + " hours " + remainingTimeComponents[1] + " minutes " + remainingTimeComponents[0] + " seconds");
        NotificationManagerCompat.from(this).notify(FOREGROUND_NOTIFICATION_ID, notificationBuilder.build());
    }

    private static class CountdownManager {
        final long countdownEnd;

        private CountdownManager(long durationMillis) {
            countdownEnd = System.currentTimeMillis() + durationMillis;
        }

        public static CountdownManager startCountdown(long durationMillis) {
            return new CountdownManager(durationMillis);
        }

        public boolean isCountdownComplete() {
            return System.currentTimeMillis() > countdownEnd;
        }

        public long getRemainingMillis() {
            if (isCountdownComplete()) return -1;
            return countdownEnd - System.currentTimeMillis();
        }
    }

    private class AppreciationActivityStarter extends Thread {
        @Override
        public void run() {
            Log.i(TAG, "Waiting for device to unlock...");
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            while (keyguardManager.isKeyguardLocked())
                Utils.sleepThread(500);
            Log.i(TAG, "Device unlocked after timer is finished, starting appreciation activity");
            handler.postDelayed(() -> {
                Intent intentAppreciationActivity = new Intent(BlockerService.this, AppreciationActivity.class);
                intentAppreciationActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentAppreciationActivity);
                stopSelf();
            }, 500);
        }
    }

    private class Blocker extends Thread {
        private final PowerManager powerManager;
        private final KeyguardManager keyguardManager;
        private final DevicePolicyManager dpm;

        public Blocker() {
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        }

        @Override
        public void run() {
            Log.i(TAG, "Blocker thread has started");
            isBlockerThreadRunning = true;
            while (!countdownManager.isCountdownComplete()) {
                Log.d(TAG, "Remaining Time: " + countdownManager.getRemainingMillis());
                updateNotification();
                Utils.sleepThread(200);

                // Screen is off, just pass for now
                if (!powerManager.isInteractive()) {
                    Utils.sleepThread(500);
                    continue;
                }

                // Screen is on, prevent any system dialog (for preventing power off of phone)
                if (preventPowerOff)
                    closeSystemWindows();

                // Screen is on and also device is unlocked so lock it
                if (!keyguardManager.isKeyguardLocked())
                    handleUnlockedDevice();
            }
            isBlockerThreadRunning = false;
            onCountdownCompletes();
            Log.i(TAG, "Blocker thread has ended");
        }

        private void handleUnlockedDevice() {
            handler.post(() -> mOverlay.resumeOverlay(2500)); // resume paused overlay
            long overlayEndTime = 3000 + System.currentTimeMillis(); // pause overlay when this time reached
            // while displaying overlay, prevent system dialogs from opening
            while (overlayEndTime > System.currentTimeMillis()) {
                closeSystemWindows();
                Utils.sleepThread(200);
            }
            handler.post(() -> mOverlay.pauseOverlay());
            dpm.lockNow();
        }

        private void closeSystemWindows() {
            handler.post(() -> sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)));
        }
    }
}
