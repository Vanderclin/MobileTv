package com.javabot.player;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;

public class PlayerOrientation {

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

    public PlayerOrientation(Context context) {
        this.context = context;
    }

    public void setOrientationChangeListener(OrientationChangeListener listener) {
        this.listener = listener;
    }

    // Habilita o detector de orientação.
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

    /**
     * Calcula o tempo em que o dispositivo está sendo mantido na mesma direção.
     */
    private void calcHoldingTime() {
        long current = System.currentTimeMillis();
        if (lastCalcTime == 0) {
            lastCalcTime = current;
        }
        holdingTime += current - lastCalcTime;
        lastCalcTime = current;
    }

    /**
     * Reseta o tempo de holding.
     */
    private void resetTime() {
        holdingTime = lastCalcTime = 0;
    }

    /**
     * Calcula a direção com base na orientação do dispositivo.
     * @param orientation a orientação atual do dispositivo
     * @return a direção calculada ou null se não for possível determinar uma direção válida
     */
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

    /**
     * Obtém a orientação da tela com base na direção especificada.
     * @param direction a direção
     * @return a orientação da tela correspondente à direção especificada
     */
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

    /**
     * Define a direção inicial do detector de orientação.
     * @param direction a direção inicial
     */
    public void setInitialDirection(Direction direction) {
        lastDirection = direction;
    }

    /**
     * Desabilita o detector de orientação.
     */
    public void disable() {
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
    }

    /**
     * Define o valor do limiar de rotação em graus.
     * @param degree o valor do limiar de rotação em graus
     */
    public void setThresholdDegree(int degree) {
        rotationThreshold = degree;
    }

    /**
     * Interface para o ouvinte de mudanças de orientação.
     */
    public interface OrientationChangeListener {
        /**
         * Chamado quando a orientação da tela é alterada.
         * @param screenOrientation a nova orientação da tela
         * @param direction         a direção correspondente à orientação
         */
        void onOrientationChanged(int screenOrientation, Direction direction);
    }

    /**
     * Enumeração das direções possíveis.
     */
    public enum Direction {
        PORTRAIT, REVERSE_PORTRAIT, LANDSCAPE, REVERSE_LANDSCAPE
    }
}
