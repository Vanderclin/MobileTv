package com.mobiletv.app.player;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;

import com.mobiletv.app.BuildConfig;

public class OrientationDetector {

    private static final String TAG = "OrientationDetector";
    private static final int HOLDING_THRESHOLD = 1500;

    private final Context context;
    private OrientationEventListener orientationEventListener;
    private int rotationThreshold = 20;
    private long holdingTime = 0;
    private long lastCalcTime = 0;
    private Direction lastDirection = Direction.PORTRAIT;
    private int currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private OrientationChangeListener listener;

    public OrientationDetector(Context context) {
        this.context = context;
    }

    public void setOrientationChangeListener(OrientationChangeListener listener) {
        this.listener = listener;
    }

    public void enable() {
        if (orientationEventListener == null) {
            orientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {
                @Override
                public void onOrientationChanged(int orientation) {
                    Direction currDirection = calcDirection(orientation);
                    if (currDirection == null) {
                        return;
                    }

                    if (currDirection != lastDirection) {
                        resetTime();
                        lastDirection = currDirection;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, String.format("Direção alterada, tempo de início, direção atual é %s", currDirection));
                        }
                    } else {
                        calcHoldingTime();
                        if (holdingTime > HOLDING_THRESHOLD) {
                            int newScreenOrientation = getScreenOrientation(currDirection);
                            if (newScreenOrientation != currentOrientation) {
                                currentOrientation = newScreenOrientation;
                                if (listener != null) {
                                    listener.onOrientationChanged(newScreenOrientation, currDirection);
                                }
                            }
                        }
                    }
                }
            };
        }

        orientationEventListener.enable();
    }

    private void calcHoldingTime() {
        long current = System.currentTimeMillis();
        if (lastCalcTime == 0) {
            lastCalcTime = current;
        }
        holdingTime += current - lastCalcTime;
        lastCalcTime = current;
    }

    private void resetTime() {
        holdingTime = lastCalcTime = 0;
    }

    private Direction calcDirection(int orientation) {
        if (orientation <= rotationThreshold || orientation >= 360 - rotationThreshold) {
            return Direction.PORTRAIT;
        } else if (Math.abs(orientation - 180) <= rotationThreshold) {
            return Direction.REVERSE_PORTRAIT;
        } else if (Math.abs(orientation - 90) <= rotationThreshold) {
            return Direction.REVERSE_LANDSCAPE;
        } else if (Math.abs(orientation - 270) <= rotationThreshold) {
            return Direction.LANDSCAPE;
        }
        return null;
    }

    private int getScreenOrientation(Direction direction) {
        switch (direction) {
            case PORTRAIT:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            case REVERSE_PORTRAIT:
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            case LANDSCAPE:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            case REVERSE_LANDSCAPE:
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            default:
                return currentOrientation;
        }
    }

    public void setInitialDirection(Direction direction) {
        lastDirection = direction;
    }

    public void disable() {
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
    }

    public void setThresholdDegree(int degree) {
        rotationThreshold = degree;
    }

    public interface OrientationChangeListener {
        void onOrientationChanged(int screenOrientation, Direction direction);
    }

    public enum Direction {
        PORTRAIT, REVERSE_PORTRAIT, LANDSCAPE, REVERSE_LANDSCAPE
    }
}
