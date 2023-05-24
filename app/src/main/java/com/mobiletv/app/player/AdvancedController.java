package com.mobiletv.app.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.mobiletv.app.R;

import java.util.Formatter;
import java.util.Locale;

public class AdvancedController extends FrameLayout {

    private Context mContext;
    private MediaPlayerControl mPlayer;
    private ProgressBar mProgress;
    private boolean mShowing = true;
    private boolean mDragging;
    private boolean mScalable = false;
    private boolean mIsFullScreen = false;
    private boolean handled = false;
    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int SHOW_LOADING = 3;
    private static final int HIDE_LOADING = 4;
    private static final int SHOW_ERROR = 5;
    private static final int HIDE_ERROR = 6;
    private static final int SHOW_COMPLETE = 7;
    private static final int HIDE_COMPLETE = 8;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private ViewGroup advancedBanner;
    private ViewGroup advancedLoading;
    private ViewGroup advancedError;
    private View advancedLayoutTop;
    private AppCompatImageView advancedBack;
    private AppCompatTextView advancedTitle;
    private View advancedCenterPlayer;
    private View advancedLayoutBottom;
    private AppCompatImageView advancedPlayer;
    private AppCompatTextView advancedTime;
    private AppCompatTextView advancedDuration;
    private AppCompatImageView advancedScale;


    public AdvancedController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        @SuppressLint("CustomViewStyleable") TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.MediaController);
        mScalable = a.getBoolean(R.styleable.MediaController_scalable, false);
        a.recycle();
        init(context);
    }

    public AdvancedController(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRoot = inflater.inflate(R.layout.advanced_player_controller, this);
        viewRoot.setOnTouchListener(mTouchListener);
        initControllerView(viewRoot);
    }

    private void initControllerView(View v) {
        advancedBanner = v.findViewById(R.id.advanced_banner);
        advancedLoading = v.findViewById(R.id.advanced_loading);
        advancedError = v.findViewById(R.id.advanced_error);
        advancedLayoutTop = v.findViewById(R.id.advanced_layout_top);
        advancedBack = v.findViewById(R.id.advanced_back);
        advancedTitle = v.findViewById(R.id.advanced_title);
        advancedCenterPlayer = v.findViewById(R.id.advanced_center_player);
        advancedLayoutBottom = v.findViewById(R.id.advanced_layout_bottom);
        advancedPlayer = v.findViewById(R.id.advanced_player);
        advancedTime = v.findViewById(R.id.advanced_time);
        View advancedBar = v.findViewById(R.id.advanced_progress);
        advancedDuration = v.findViewById(R.id.advanced_duration);
        advancedScale = v.findViewById(R.id.advanced_scale);

        if (advancedPlayer != null) {
            advancedPlayer.requestFocus();
            advancedPlayer.setOnClickListener(mPauseListener);
        }

        if (mScalable && advancedScale != null) {
            advancedScale.setVisibility(VISIBLE);
            advancedScale.setOnClickListener(mScaleListener);
        } else if (advancedScale != null) {
            advancedScale.setVisibility(GONE);
        }

        if (advancedCenterPlayer != null) {
            advancedCenterPlayer.setOnClickListener(mCenterPlayListener);
        }

        if (advancedBack != null) {
            advancedBack.setOnClickListener(mBackListener);
        }

        mProgress = (ProgressBar) advancedBar;
        if (mProgress instanceof SeekBar) {
            SeekBar seeker = (SeekBar) mProgress;
            seeker.setOnSeekBarChangeListener(mSeekListener);
        }
        mProgress.setMax(1000);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    public void show() {
        show(sDefaultTimeout);
    }

    public void setBanner(View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        advancedBanner.addView(view);
    }

    private void disableUnsupportedButtons() {
        try {
            if (advancedPlayer != null && mPlayer != null && !mPlayer.canPause()) {
                advancedPlayer.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // Tratamento de exceção vazio
        }
    }

    public void show(int timeout) {
        if (!mShowing) {
            setProgress();
            if (advancedPlayer != null) {
                advancedPlayer.requestFocus();
            }
            disableUnsupportedButtons();
            mShowing = true;
        }
        updatePausePlay();
        updateBackButton();

        setVisibility(VISIBLE);
        advancedLayoutTop.setVisibility(VISIBLE);
        advancedLayoutBottom.setVisibility(VISIBLE);
        advancedBanner.setVisibility(GONE);

        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void hide() {
        if (mShowing) {
            mHandler.removeMessages(SHOW_PROGRESS);
            advancedLayoutTop.setVisibility(GONE);
            advancedLayoutBottom.setVisibility(GONE);
            advancedBanner.setVisibility(VISIBLE);
            mShowing = false;
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT: //1
                    hide();
                    break;
                case SHOW_PROGRESS: //2
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer != null && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                case SHOW_LOADING: //3
                    show();
                    showCenterView(R.id.advanced_loading);
                    break;
                case SHOW_COMPLETE: //7
                    showCenterView(R.id.advanced_center_player);
                    break;
                case SHOW_ERROR: //5
                    show();
                    showCenterView(R.id.advanced_error);
                    break;
                case HIDE_LOADING: //4
                case HIDE_ERROR: //6
                case HIDE_COMPLETE: //8
                    hide();
                    hideCenterView();
                    break;
            }
        }
    };

    @SuppressLint("NonConstantResourceId")
    private void showCenterView(int resId) {
        switch (resId) {
            case R.id.advanced_loading:
                advancedLoading.setVisibility(VISIBLE);
                advancedCenterPlayer.setVisibility(GONE);
                advancedError.setVisibility(GONE);
                break;
            case R.id.advanced_center_player:
                advancedCenterPlayer.setVisibility(VISIBLE);
                advancedLoading.setVisibility(GONE);
                advancedError.setVisibility(GONE);
                break;
            case R.id.advanced_error:
                advancedError.setVisibility(VISIBLE);
                advancedCenterPlayer.setVisibility(GONE);
                advancedLoading.setVisibility(GONE);
                break;
        }
    }

    private void hideCenterView() {
        advancedCenterPlayer.setVisibility(GONE);
        advancedError.setVisibility(GONE);
        advancedLoading.setVisibility(GONE);
    }

    public void reset() {
        advancedTime.setText("00:00");
        advancedDuration.setText("00:00");
        mProgress.setProgress(0);
        advancedPlayer.setImageResource(R.drawable.ic_play);
        setVisibility(VISIBLE);
        hideLoading();
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (advancedTime != null) advancedTime.setText(stringForTime(position));
        if (advancedDuration != null) advancedDuration.setText(stringForTime(duration));

        return position;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                show(0);
                handled = false;
                break;
            case MotionEvent.ACTION_UP:
                if (!handled) {
                    // handled = false;
                    show(sDefaultTimeout);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                hide();
                break;
            default:
                break;
        }
        return true;
    }

    private final OnTouchListener mTouchListener = new OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    hide();
                    handled = true;
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (advancedPlayer != null) {
                    advancedPlayer.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private final OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer != null) {
                doPauseResume();
                show(sDefaultTimeout);
            }
        }
    };

    private final OnClickListener mScaleListener = new OnClickListener() {
        public void onClick(View v) {
            if (mPlayer != null) {
                mIsFullScreen = !mIsFullScreen;
                updateScaleButton();
                updateBackButton();
                mPlayer.setFullscreen(mIsFullScreen);
            }
        }
    };

    private final OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            if (mIsFullScreen && mPlayer != null) {
                mIsFullScreen = false;
                updateScaleButton();
                updateBackButton();
                mPlayer.setFullscreen(false);
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.only_when_playing_the_video), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final OnClickListener mCenterPlayListener = new OnClickListener() {
        public void onClick(View v) {
            hideCenterView();
            mPlayer.start();
        }
    };

    private void updatePausePlay() {
        int playIcon = mPlayer != null && mPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play;
        advancedPlayer.setImageResource(playIcon);
    }

    void updateScaleButton() {
        int scaleIcon = mIsFullScreen ? R.drawable.ic_fullscreen_close : R.drawable.ic_fullscreen_open;
        advancedScale.setImageResource(scaleIcon);
    }

    void toggleButtons(boolean isFullScreen) {
        mIsFullScreen = isFullScreen;
        updateScaleButton();
        updateBackButton();
    }

    void updateBackButton() {
        advancedBack.setVisibility(mIsFullScreen ? View.VISIBLE : View.INVISIBLE);
    }

    boolean isFullScreen() {
        return mIsFullScreen;
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }
    // OK


    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        int newPosition = 0;
        boolean change = false;

        public void onStartTrackingTouch(SeekBar bar) {
            if (mPlayer == null) {
                return;
            }
            show(3600000);
            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null || !fromuser) {
                return;
            }
            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            newPosition = (int) newposition;
            change = true;
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (mPlayer == null) {
                return;
            }
            if (change) {
                mPlayer.seekTo(newPosition);
                if (advancedTime != null) {
                    advancedTime.setText(stringForTime(newPosition));
                }
            }
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);
            mShowing = true;
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (advancedPlayer != null) {
            advancedPlayer.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        if (mScalable) {
            advancedScale.setEnabled(enabled);
        }
        if (advancedPlayer != null) {
            advancedBack.setEnabled(true);
        }
    }
    // OK

    public void showLoading() {
        mHandler.sendEmptyMessage(SHOW_LOADING);
    }

    public void hideLoading() {
        mHandler.sendEmptyMessage(HIDE_LOADING);
    }

    public void showError() {
        mHandler.sendEmptyMessage(SHOW_ERROR);
    }

    public void hideError() {
        mHandler.sendEmptyMessage(HIDE_ERROR);
    }

    public void showComplete() {
        mHandler.sendEmptyMessage(SHOW_COMPLETE);
    }

    public void hideComplete() {
        mHandler.sendEmptyMessage(HIDE_COMPLETE);
    }

    public void setTitle(String title) {
        advancedTitle.setText(title);
    }

    public void setFullscreenEnabled(boolean enabled) {
        advancedScale.setVisibility(mIsFullScreen ? View.VISIBLE : View.GONE);
    }

    public void setOnErrorView(int resId) {
        advancedError.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(resId, advancedError, true);
    }

    public void setOnErrorView(View onErrorView) {
        advancedError.removeAllViews();
        advancedError.addView(onErrorView);
    }

    public void setOnLoadingView(int resId) {
        advancedLoading.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(resId, advancedLoading, true);
    }

    public void setOnLoadingView(View onLoadingView) {
        advancedLoading.removeAllViews();
        advancedLoading.addView(onLoadingView);
    }

    public void setOnErrorViewClick(OnClickListener onClickListener) {
        advancedError.setOnClickListener(onClickListener);
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        void aspect();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        void closePlayer();

        void setFullscreen(boolean fullscreen);

        void setFullscreen(boolean fullscreen, int screenOrientation);
    }

}
