package com.tangledbytes.phoneblocker.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;


import com.google.android.material.appbar.MaterialToolbar;
import com.tangledbytes.phoneblocker.R;
import com.tangledbytes.phoneblocker.dialogs.AboutDialog;
import com.tangledbytes.phoneblocker.services.BlockerService;
import com.tangledbytes.phoneblocker.utils.AppPreferences;
import com.tangledbytes.phoneblocker.utils.BlockerSession;
import com.tangledbytes.phoneblocker.utils.Constants;
import com.tangledbytes.phoneblocker.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.TimeZone;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Button btnLockDevice;
    private TextView tvDurationHours;

    final Calendar cal = Calendar.getInstance();

    private TextView tvDurationMinutes;
    private TextView tvDurationSeconds;
    //TODO: set time to hrs = 1, min = 30, seconds = 0 in release version
    // 假设你设置了时间格式

    // 日期和时间格式化



    private int durationHours = 0 ;  // 使用当前时间的小时数
    private int durationMinutes=0;  // 使用当前时间的分钟数
    private int durationSeconds = 10;  // 假设秒数初始化为10

    private class OnLockStateChanged extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BC_DEVICE_LOCKED))
                btnLockDevice.setEnabled(false);
            else if (intent.getAction().equals(Constants.BC_DEVICE_UNLOCKED))
                btnLockDevice.setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (shouldShowIntro()) {
            Intent intentAppIntro = new Intent(this, AppIntroActivity.class);
            startActivity(intentAppIntro);
            finish();
        } else if (hasRequiredPermissions()) {
            Intent intentAppIntro = new Intent(this, AppIntroActivity.class);
            intentAppIntro.putExtra(Constants.EXTRA_REQUEST_ONLY_PERMISSIONS, true);
            startActivity(intentAppIntro);
            finish();
        }
        new BlockerSession(this).invalidateSession();
        setContentView(R.layout.activity_main);
        setUpToolbar();
        final SimpleDateFormat duTime = new SimpleDateFormat("HH:mm", Locale.CHINA);
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");



        duTime.setTimeZone(timeZone);
        final String dufTime = duTime.format(cal.getTime());  // 获取格式化的时间字符串，如 "07:35"

        // 使用正则分割时间字符串获取小时和分钟
        String[] timeParts = dufTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);  // 获取小时
        int minute = Integer.parseInt(timeParts[1]);  // 获取分钟

        durationHours = 0;
        durationMinutes = 15;  // 默认持续时间为 15 分钟

        if ((hour >= 22 && hour <= 23) || (hour >= 0 && hour <= 5)) {
            durationHours = 1;  // 如果是晚上 10 点到早上 5 点之间，持续时间为 1 小时
            durationMinutes = 0;  // 不需要分钟
        }

        setUpViews();
        initTimeDisplay();


        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BC_DEVICE_LOCKED);
        filter.addAction(Constants.BC_DEVICE_UNLOCKED);
        registerReceiver(new OnLockStateChanged(), filter);
    }

    private boolean hasRequiredPermissions() {
        return !Utils.hasBootPermission(this) || !Utils.hasOverlayPermission(this) || !Utils.hasDeviceAdminPermission(this);
    }

    private boolean shouldShowIntro() {
        return !new AppPreferences(this).getBoolean(Constants.PREF_HAS_SEEN_APP_INTRO, false);
    }

    private void setUpToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.activity_toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener((MenuItem item) -> {
            if (item.getItemId() == R.id.menu_about)
                showAboutDialog();
            return true;
        });
    }


    private void showAboutDialog() {
        startActivity(new Intent(this, AboutDialog.class));
    }

    private void setUpViews() {

        tvDurationHours = findViewById(R.id.tv_duration_hours);
        tvDurationMinutes = findViewById(R.id.tv_duration_minutes);
        tvDurationSeconds = findViewById(R.id.tv_duration_seconds);
        btnLockDevice = findViewById(R.id.btn_lock_device);

        Button btnIncrementHours = findViewById(R.id.btn_increment_hours);
        Button btnDecrementHours = findViewById(R.id.btn_decrement_hours);
        Button btnIncrementMinutes = findViewById(R.id.btn_increment_minutes);
        Button btnDecrementMinutes = findViewById(R.id.btn_decrement_minutes);
        Button btnIncrementSeconds = findViewById(R.id.btn_increment_seconds);
        Button btnDecrementSeconds = findViewById(R.id.btn_decrement_seconds);

        btnIncrementHours.setOnClickListener(this);
        btnDecrementHours.setOnClickListener(this);
        btnIncrementMinutes.setOnClickListener(this);
        btnDecrementMinutes.setOnClickListener(this);
        btnIncrementSeconds.setOnClickListener(this);
        btnDecrementSeconds.setOnClickListener(this);
        btnLockDevice.setOnClickListener(view -> startBlocking());
        updateDurationTextViews();

        // TODO: Add code for blocking calls and notifications

    }

    private void initTimeDisplay() {
        // 假设布局中有一个 id 为 tv_current_time 的 TextView
        TextView tvCurrentTime = findViewById(R.id.tv_current_time);

        // 定义Runnable，更新每秒钟的时间
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                // 获取当前时间
                final Calendar cal = Calendar.getInstance();
                TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
                // 日期和时间格式化
                final SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.CHINA);
                sdfDate.setTimeZone(timeZone);

                final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.CHINA);
                sdfTime.setTimeZone(timeZone);

                final SimpleDateFormat sdfSeconds = new SimpleDateFormat("ss", Locale.CHINA);
                sdfSeconds.setTimeZone(timeZone);

                // 获取当前日期、时间和秒数
                final String currentDate = sdfDate.format(cal.getTime());
                final String currentTime = sdfTime.format(cal.getTime());
                final String currentSeconds = sdfSeconds.format(cal.getTime());


                // 在UI线程更新TextView
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 显示日期和时间
                        tvCurrentTime.setText(currentDate + "\n" + currentTime + ":" + currentSeconds);
                    }
                });

                // 每隔1秒更新一次
                handler.postDelayed(this, 1000);
            }
        };

        // 启动Runnable，开始更新时间
        handler.post(updateTimeRunnable);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_increment_hours) {
            if (durationHours == 16)
                Toast.makeText(this, R.string.hour_limit_exceeded, Toast.LENGTH_SHORT).show();
            durationHours++;
        } else if (id == R.id.btn_decrement_hours) {
            if (durationHours != 0) durationHours--;
        } else if (id == R.id.btn_increment_minutes) {
            durationMinutes += 5;
            if (durationMinutes == 60)
                durationMinutes = 0;
        } else if (id == R.id.btn_decrement_minutes) {
            if (durationMinutes != 0) durationMinutes -= 5;
        } else if (id == R.id.btn_increment_seconds) {
            durationSeconds += 10;
            if (durationSeconds == 60) durationSeconds = 0;
        } else if (id == R.id.btn_decrement_seconds) {
            if (durationSeconds != 0) durationSeconds -= 10;
        }
        updateDurationTextViews();
    }

    private void updateDurationTextViews() {
        tvDurationHours.setText(String.format(getString(R.string.x_hrs), durationHours));
        tvDurationMinutes.setText(String.format(getString(R.string.x_min), durationMinutes));
        tvDurationSeconds.setText(String.format(getString(R.string.x_secs), durationSeconds));
        btnLockDevice.setText(String.format(getString(R.string.lock_device_button_text), durationHours, durationMinutes, durationSeconds));
    }

    private void startBlocking() {
        BlockerSession session = new BlockerSession(this);
        if (session.invalidateSession()) {
            Toast.makeText(this, "Timer is already running", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert duration to millis
        long durationMillis = durationHours * 60 * 60 * 1000L;
        durationMillis += durationMinutes * 60 * 1000L;
        durationMillis += durationSeconds * 1000L;

        Intent intentBlockerService = new Intent(this, BlockerService.class);
        session.createSession(durationMillis, true, false, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intentBlockerService);
        else startService(intentBlockerService);
        finish();
    }
}