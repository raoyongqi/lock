package com.tangledbytes.phoneblocker.overlays;

import static android.os.Build.*;
import static android.view.WindowManager.LayoutParams.*;

import android.app.Service;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import android.graphics.PixelFormat;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tangledbytes.phoneblocker.R;
import com.tangledbytes.phoneblocker.services.BlockerService;
import com.tangledbytes.phoneblocker.utils.BlockerSession;
import com.tangledbytes.phoneblocker.utils.Utils;

import java.util.Random;

public class BlockerOverlay {
    private final Service mService;
    private final WindowManager mWindowManager;
    private final View mOverlayRoot;
    private final TextView mTvTimer;
    private final TextView mTvQuote2;
    private final ProgressBar mProgressBar;
    private final BlockerSession mBlockerSession;
    private final Handler mHandler;

    private boolean isOverlayVisible = false;
    private boolean isOverlayPaused = false;

    public WindowManager.LayoutParams generateParams() {
        int type = TYPE_SYSTEM_OVERLAY;
        if (VERSION.SDK_INT >= VERSION_CODES.O) type = TYPE_APPLICATION_OVERLAY;
        return new LayoutParams(MATCH_PARENT, MATCH_PARENT, type, FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
    }

    public BlockerOverlay(BlockerService service) {
        mService = service;
        mOverlayRoot = LayoutInflater.from(service).inflate(R.layout.overlay_blocker, null);
        mWindowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);

        mTvTimer = mOverlayRoot.findViewById(R.id.timer);
        mTvQuote2 = mOverlayRoot.findViewById(R.id.quote2);
        mProgressBar = mOverlayRoot.findViewById(R.id.progress);
        mBlockerSession = new BlockerSession(mService);
        mHandler = new Handler(mService.getMainLooper());
    }

    /* Shows overlay (if it is not already showing) and starts ViewUpdaterThread */
    public void showOverlay(long durMillis) {
        if (!isOverlayVisible)
            mWindowManager.addView(mOverlayRoot, generateParams());
        mProgressBar.setProgress(0);
        new ViewUpdaterThread(durMillis).start();
        isOverlayVisible = true;
        isOverlayPaused = false;
    }

    /* Removes overlay from window */
    public void removeOverlay() {
        if (!isOverlayVisible)
            return;
        mWindowManager.removeView(mOverlayRoot);
        isOverlayVisible = false;
    }

    /* Pauses updating of views by stopping ViewUpdaterThread */
    public void pauseOverlay() {
        if(!isOverlayVisible || isOverlayPaused)
            return;
        isOverlayPaused = true;
    }

    /* Resumes updating of views by starting the ViewUpdaterThread */
    public void resumeOverlay(long durMillis) {
        showOverlay(durMillis);
    }

    private class ViewUpdaterThread extends Thread {
        private long durMillis;
        private final String[] quotes = {
                "You may delay, but time will not.",
                "Focus on being productive instead of busy.",
                "Until we can manage time, we can manage nothing else."
        };

        public ViewUpdaterThread(long durMillis) {
            this.durMillis = durMillis;
            if (mBlockerSession.getEndTime() <= durMillis + System.currentTimeMillis())
                this.durMillis = mBlockerSession.getEndTime() - System.currentTimeMillis();
        }

        @Override
        public void run() {
            long sleepDur = durMillis / 100; // how long to sleep before incrementing progress by 1
            while (!isOverlayPaused) {
                Utils.sleepThread(sleepDur);
                mHandler.post(this::updateViews);
            }
        }

        private void updateViews() {
            int[] remainingTime = Utils.getComponents(mBlockerSession.getRemainingSessionDuration());
            if (mProgressBar.getProgress() < 100)
                updateProgressBar(mProgressBar, mProgressBar.getProgress() + 1);
            if (mProgressBar.getProgress() == 25) {
                if (mTvQuote2.getVisibility() == View.GONE) {
                    mTvQuote2.setText(getRandomQuote());
                    mTvQuote2.setVisibility(View.VISIBLE);
                }
            }
            mTvTimer.setText(String.format(mService.getString(R.string.remaining_time_format), remainingTime[2], remainingTime[1], remainingTime[0]));
        }

        private String getRandomQuote() {
            return "\"" + quotes[new Random().nextInt(quotes.length)] + "\"";
        }

        private void updateProgressBar(ProgressBar progressBar, int progress) {
            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                progressBar.setProgress(progress, true);
            } else {
                progressBar.setProgress(progress);
            }
        }
    }
}


