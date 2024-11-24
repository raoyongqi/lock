package com.tangledbytes.phoneblocker.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;

import com.tangledbytes.phoneblocker.activities.MainActivity;
import com.tangledbytes.phoneblocker.receivers.AdminReceiver;

public class Utils {
    public static void sleepThread(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasDeviceAdminPermission(Context context) {
        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mAdminName = new ComponentName(context, AdminReceiver.class);
        return mDPM != null && mDPM.isAdminActive(mAdminName);
    }

    //TODO: Implement this method for the devices like MI. These devices do not give boot permission without enabling
    public static boolean hasBootPermission(Context context) {
        return true;
    }

    public static void disable(ViewGroup layout) {
        layout.setEnabled(false);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup)
                disable((ViewGroup) child);
            else
                child.setEnabled(false);
        }
    }

    /**
     * Returns different components of provided time.
     *
     * @return int[0] is secs, int[1] is minutes and int[2] is hours*/
    public static int[] getComponents(long millis) {
        int secs = (int) (millis / 1000) % 60;
        int min = (int) ((millis / (1000 * 60)) % 60);
        int hr = (int) (millis / (1000 * 60 * 60));
        return new int[] {secs, min, hr};
    }

    public static boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) return Settings.canDrawOverlays(context);
        return true;
    }
}