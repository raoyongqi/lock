package com.tangledbytes.phoneblocker.activities;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroCustomLayoutFragment;
import com.github.appintro.AppIntroFragment;
import com.tangledbytes.phoneblocker.R;
import com.tangledbytes.phoneblocker.receivers.AdminReceiver;
import com.tangledbytes.phoneblocker.utils.AppPreferences;
import com.tangledbytes.phoneblocker.utils.Constants;
import com.tangledbytes.phoneblocker.utils.Utils;

public class AppIntroActivity extends AppIntro {
    private static final String TAG = "AppIntroActivity";
    AppIntroCustomLayoutFragment permissionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpActivity();
        if (!getIntent().getBooleanExtra(Constants.EXTRA_REQUEST_ONLY_PERMISSIONS, false))
            addIntroductorySlides();
        else addPermissionRequestSlide();
    }

    @Override
    protected void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        if (newFragment != null && newFragment.equals(permissionFragment))
            updatePermissionButtons();
        else setButtonsEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionButtons();
    }

    public void setUpActivity() {
        setButtonsEnabled(true);
        showStatusBar(true);
        setSystemBackButtonLocked(true);
        setWizardMode(true);
    }

    private void addPermissionRequestSlide() {
        addSlide(AppIntroFragment.createInstance("Permissions revoked", "Press next to take the necessary action", R.drawable.lock, R.color.materialDark));
        permissionFragment = AppIntroCustomLayoutFragment.newInstance(R.layout.layout_permission_request);
        addSlide(permissionFragment);
    }

    private void addIntroductorySlides() {
        addSlide(AppIntroFragment.createInstance("Welcome to Phone Blocker", "Conquer your phone addiction!", R.drawable.thumbs_up, R.color.materialDark));
        addSlide(AppIntroFragment.createInstance("How does this app help?", "With a single click, this app lets you to lock your device for hours so that you can spend your time in doing meaningful work", R.drawable.man, R.color.materialDark));
        permissionFragment = AppIntroCustomLayoutFragment.newInstance(R.layout.layout_permission_request);
        addSlide(permissionFragment);
    }

    private void updatePermissionButtons() {
        Button btnGrantAdminPermission = findViewById(R.id.btn_grant_admin_permission);
        Button btnGrantOverlayPermission = findViewById(R.id.btn_grant_overlay_permission);
        Button btnGrantBootPermission = findViewById(R.id.btn_grant_boot_permission);
        if (btnGrantAdminPermission == null || btnGrantBootPermission == null)
            // This might happen when for example the fragment is not loaded yet
            return;

        // style buttons based on the state of permission
        changePermissionBtnState(btnGrantAdminPermission, Utils.hasDeviceAdminPermission(this));
        changePermissionBtnState(btnGrantBootPermission, Utils.hasBootPermission(this));
        changePermissionBtnState(btnGrantOverlayPermission, Utils.hasOverlayPermission(this));

        // setup onClick listeners
        btnGrantAdminPermission.setOnClickListener(this::onClickGrantAdminPermission);
        btnGrantOverlayPermission.setOnClickListener(this::onClickGrantOverlayPermission);
        btnGrantBootPermission.setOnClickListener(AppIntroActivity::onClickGrantBootPermission);

        // Set next/done slide buttons enabled only if both the permissions are granted
        setButtonsEnabled(Utils.hasBootPermission(this) && Utils.hasDeviceAdminPermission(this));
    }

    public void changePermissionBtnState(Button button, boolean permissionGranted) {
        int color = permissionGranted ? R.color.md_green_800 : R.color.md_red_800;
        int text = permissionGranted ? R.string.granted : R.string.grant;
        button.setEnabled(!permissionGranted);
        button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(color)));
        button.setText(text);
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        new AppPreferences(this).changePreference(Constants.PREF_HAS_SEEN_APP_INTRO, true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void onClickGrantAdminPermission(View view) {
        ComponentName componentDeviceAdmin = new ComponentName(this, AdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentDeviceAdmin);
        startActivity(intent);
    }

    private void onClickGrantOverlayPermission(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private static void onClickGrantBootPermission(View view) {
    }
}