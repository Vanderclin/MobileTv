package com.javabot.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import java.io.IOException;
import java.util.Map;

@SuppressLint("ClickableViewAccessibility")
public class PlayerVideo extends SurfaceView implements PlayerController.MediaPlayerControl, PlayerOrientation.OrientationChangeListener {

    private Uri ContentUri;
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    private int mSurfaceWidth = 1280;
    private int mSurfaceHeight = 720;
    private int mCurrentBufferPercentage;
    private int mSeekWhenPrepared;

    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;
    private boolean mPreparedBeforeStart;
    private boolean mAutoRotation = true;

    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private int mAudioSession;

    private PlayerController mPlayerController;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;

    private final Context ContentContext;

    private int mVideoViewLayoutWidth = 0;
    private int mVideoViewLayoutHeight = 0;

    private PlayerOrientation mPlayerOrientation;
    private VideoViewCallback videoViewCallback;


    public PlayerVideo(Context context) {
        this(context, null);
    }

    public PlayerVideo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerVideo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ContentContext = context;
        TypedArray typedArray = ContentContext.obtainStyledAttributes(attrs, R.styleable.PlayerVideo, 0, 0);
        mAutoRotation = typedArray.getBoolean(R.styleable.PlayerVideo_player_rotation, false);
        typedArray.recycle();
        initializeVideo();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        float desiredAspectRatio = (float) mSurfaceWidth / (float) mSurfaceHeight;
        float currentAspectRatio = (float) width / (float) height;
        if (currentAspectRatio > desiredAspectRatio) {
            width = (int) (height * desiredAspectRatio);
        } else {
            height = (int) (width / desiredAspectRatio);
        }
        setMeasuredDimension(width, height);
    }



    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(PlayerVideo.class.getName());
    }

    private void initializeVideo() {
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    @Override
    public void onOrientationChanged(int screenOrientation, PlayerOrientation.Direction direction) {
        if (!mAutoRotation) {
            return;
        }
        if (direction == PlayerOrientation.Direction.PORTRAIT) {
            setFullscreen(false, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (direction == PlayerOrientation.Direction.REVERSE_PORTRAIT) {
            setFullscreen(false, ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else if (direction == PlayerOrientation.Direction.LANDSCAPE) {
            setFullscreen(true, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (direction == PlayerOrientation.Direction.REVERSE_LANDSCAPE) {
            setFullscreen(true, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }

    public void setRotation(boolean auto) {
        mAutoRotation = auto;
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        ContentUri = uri;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    private void openVideo() {
        if (ContentUri == null || mSurfaceHolder == null) {
            return;
        }
        AudioManager audioManager = (AudioManager) ContentContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        release(false);
        try {
            mMediaPlayer = new MediaPlayer();
            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(ContentContext, ContentUri);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            attachController();
        } catch (IOException ex) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    public void setMediaController(PlayerController controller) {
        if (mPlayerController != null) {
            mPlayerController.hide();
        }
        mPlayerController = controller;
        attachController();
    }

    private void attachController() {
        if (mMediaPlayer != null && mPlayerController != null) {
            mPlayerController.setMediaPlayer(this);
            mPlayerController.setEnabled(isInPlaybackState());
            mPlayerController.hide();
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = (mp, width, height) -> {
        mSurfaceWidth = mp.getVideoWidth();
        mSurfaceHeight = mp.getVideoHeight();
        if (mSurfaceWidth != 0 && mSurfaceHeight != 0) {
            getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
            requestLayout();
        }
    };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;
            mCanPause = mCanSeekBack = mCanSeekForward = true;
            mPreparedBeforeStart = true;
            if (mPlayerController != null) {
                mPlayerController.hideLoading();
            }

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mPlayerController != null) {
                mPlayerController.setEnabled(true);
            }
            mSurfaceWidth = mp.getVideoWidth();
            mSurfaceHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mSurfaceWidth != 0 && mSurfaceHeight != 0) {
                getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
                if (mSurfaceWidth == mSurfaceWidth && mSurfaceHeight == mSurfaceHeight) {
                    if (mTargetState == STATE_PLAYING) {
                        start();
                        if (mPlayerController != null) {
                            mPlayerController.show();
                        }
                    } else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mPlayerController != null) {
                            mPlayerController.show(0);
                        }
                    }
                }
            } else {

                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    private final MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mPlayerController != null) {
                boolean a = mMediaPlayer.isPlaying();
                int b = mCurrentState;
                mPlayerController.showComplete();
            }
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };

    private final MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            boolean handled = false;
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    if (videoViewCallback != null) {
                        videoViewCallback.onBufferingStart(mMediaPlayer);
                    }
                    if (mPlayerController != null) {
                        mPlayerController.showLoading();
                    }
                    handled = true;
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    if (videoViewCallback != null) {
                        videoViewCallback.onBufferingEnd(mMediaPlayer);
                    }
                    if (mPlayerController != null) {
                        mPlayerController.hideLoading();
                    }
                    handled = true;
                    break;
            }
            if (mOnInfoListener != null) {
                return mOnInfoListener.onInfo(mp, what, extra) || handled;
            }
            return handled;
        }
    };

    private final MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mPlayerController != null) {
                mPlayerController.showError();
            }
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }

            return true;
        }
    };

    private final MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
        }
    };

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mSurfaceWidth == w && mSurfaceHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            openVideo();
            enableOrientationDetect();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceHolder = null;
            if (mPlayerController != null) mPlayerController.hide();
            release(true);
            disableOrientationDetect();
        }
    };

    private void enableOrientationDetect() {
        if (mAutoRotation && mPlayerOrientation == null) {
            mPlayerOrientation = new PlayerOrientation(ContentContext);
            mPlayerOrientation.setOrientationChangeListener(PlayerVideo.this);
            mPlayerOrientation.enable();
        }
    }

    private void disableOrientationDetect() {
        if (mPlayerOrientation != null) {
            mPlayerOrientation.disable();
        }
    }

    private void release(boolean clearTargetState) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (clearTargetState) {
                mTargetState = STATE_IDLE;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mPlayerController != null) {
            toggleMediaControlsVisibility();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mPlayerController != null) {
            toggleMediaControlsVisibility();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE && keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mPlayerController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mPlayerController.show();
                } else {
                    start();
                    mPlayerController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mPlayerController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mPlayerController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisibility();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisibility() {
        if (mPlayerController.isShowing()) {
            mPlayerController.hide();
        } else {
            mPlayerController.show();
        }
    }


    @Override
    public void start() {
        if (!mPreparedBeforeStart && mPlayerController != null) {
            mPlayerController.showLoading();
        }
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            if (this.videoViewCallback != null) {
                this.videoViewCallback.onStart(mMediaPlayer);
            }
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
                if (this.videoViewCallback != null) {
                    this.videoViewCallback.onPause(mMediaPlayer);
                }
            }
        }
        mTargetState = STATE_PAUSED;
    }

    @Override
    public void aspect() {

    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public void closePlayer() {
        release(true);
    }

    @Override
    public void setFullscreen(boolean fullscreen) {
        int screenOrientation = fullscreen ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        setFullscreen(fullscreen, screenOrientation);
    }

    @Override
    public void setFullscreen(boolean fullscreen, int screenOrientation) {
        Activity activity = (Activity) ContentContext;
        if (fullscreen) {
            if (mVideoViewLayoutWidth == 0 && mVideoViewLayoutHeight == 0) {
                ViewGroup.LayoutParams params = getLayoutParams();
                mVideoViewLayoutWidth = params.width;
                mVideoViewLayoutHeight = params.height;
            }
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setRequestedOrientation(screenOrientation);
        } else {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = mVideoViewLayoutWidth;
            params.height = mVideoViewLayoutHeight;
            setLayoutParams(params);

            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setRequestedOrientation(screenOrientation);
        }
        mPlayerController.toggleButtons(fullscreen);
        if (videoViewCallback != null) {
            videoViewCallback.onScaleChange(fullscreen);
        }
    }


    public interface VideoViewCallback {
        void onScaleChange(boolean isFullscreen);

        void onPause(final MediaPlayer mediaPlayer);

        void onStart(final MediaPlayer mediaPlayer);

        void onBufferingStart(final MediaPlayer mediaPlayer);

        void onBufferingEnd(final MediaPlayer mediaPlayer);

    }

    public void setVideoViewCallback(VideoViewCallback callback) {
        this.videoViewCallback = callback;
    }
}
