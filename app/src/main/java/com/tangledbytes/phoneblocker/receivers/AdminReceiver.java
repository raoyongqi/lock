package com.tangledbytes.phoneblocker.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class AdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "AdminReceiver";

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        // admin permission given
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        // admin permission denied
    }
}