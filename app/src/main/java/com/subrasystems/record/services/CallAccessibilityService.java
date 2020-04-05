package com.subrasystems.record.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.subrasystems.record.R;
import com.subrasystems.record.activities.MainActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
Draw over other apps
in your setting and enable your app. For Android 8 Oreo, try
Settings > Apps & Notifications > App info > Display over other apps > Enable
*/
public class CallAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private FrameLayout mLayout;
    private boolean isStarted;
    private MediaRecorder mRecorder;

    private View mView;
    private WindowManager mWindowManager;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /*switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

                    int middleYValue = displayMetrics.heightPixels / 2;
                    final int leftSideOfScreen = displayMetrics.widthPixels / 4;
                    final int rightSizeOfScreen = leftSideOfScreen * 3;
                    GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                    Path path = new Path();

                    if (event.getText() != null && event.getText().toString().contains("1")) {
                        //Swipe left
                        path.moveTo(rightSizeOfScreen, middleYValue);
                        path.lineTo(leftSideOfScreen, middleYValue);
                    } else {
                        //Swipe right
                        path.moveTo(leftSideOfScreen, middleYValue);
                        path.lineTo(rightSizeOfScreen, middleYValue);
                    }

                    gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 50));
                    dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            super.onCompleted(gestureDescription);
                        }
                    }, null);
                }
            default: {
                break;
            }
        }*/
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        /*System.out.println("onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.eventTypes=AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = null;
        setServiceInfo(info);*/

        displayView();
    }

    private void displayView() {
        //Inflate the floating view layout we created
        mView = LayoutInflater.from(this).inflate(R.layout.action_bar, null);

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,      // | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner | Gravity.CENTER_VERTICAL|Gravity.END;
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mWindowManager != null) {
            mWindowManager.addView(mView, params);
        }

        ((ImageButton) mView.findViewById(R.id.btnClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open the application  click.
                Intent intent = new Intent(CallAccessibilityService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //close the service and remove view from the view hierarchy
                stopSelf();
            }
        });

        ((ImageButton) mView.findViewById(R.id.btnStartRecording)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
                Toast.makeText(CallAccessibilityService.this, "Playing", Toast.LENGTH_SHORT).show();
            }
        });
        ((ImageButton) mView.findViewById(R.id.btnStopRecording)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                Toast.makeText(CallAccessibilityService.this, "Stopping", Toast.LENGTH_SHORT).show();
            }
        });

        //Drag and move floating view using user's touch action.
        ((ImageButton) mView.findViewById(R.id.draggable_button)).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //params.x = (int) event.getX();
                //params.y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;
                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int xDiff = (int) (event.getRawX() - initialTouchX);
                        int yDiff = (int) (event.getRawY() - initialTouchY);
                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (xDiff < 10 && yDiff < 10) {
                            Toast.makeText(CallAccessibilityService.this, "ACTION_UP", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mView != null) mWindowManager.removeView(mView);
    }

    public void startRecording() {
        try {
            String path = getApplicationContext().getFilesDir().getPath();
            //String selectedPath = Environment.getExternalStorageDirectory() + "/Testing";
            //String selectedPath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Android/data/" + packageName + "/system_sound";

            File file = new File(path);
            if (!file.exists()){
                file.mkdirs();
            }

            mRecorder = new MediaRecorder();
            mRecorder.reset();

            //android.permission.MODIFY_AUDIO_SETTINGS
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); //turn on speaker
            if (mAudioManager != null) {
                mAudioManager.setMode(AudioManager.MODE_IN_CALL); //MODE_IN_COMMUNICATION | MODE_IN_CALL
                //mAudioManager.setSpeakerphoneOn(true);
                //mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0); // increase Volume
                //hasWiredHeadset(mAudioManager);
            }

            //android.permission.RECORD_AUDIO
            String manufacturer = Build.MANUFACTURER;
            Log.d(TAG, manufacturer);
            /*if (manufacturer.toLowerCase().contains("samsung")) {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            } else {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            }*/

            /*
            VOICE_CALL is the actual call data being sent in a call, up and down (so your side and their side). VOICE_COMMUNICATION is just the microphone, but with codecs and echo cancellation turned on for good voice quality.
            */
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION); //MIC | VOICE_COMMUNICATION (Android 10 release) | VOICE_RECOGNITION | (VOICE_CALL = VOICE_UPLINK + VOICE_DOWNLINK)
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //THREE_GPP | MPEG_4
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); //AMR_NB | AAC
            String mFilePath = file + "/" + "REC_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".3gp"; //.3gp | .mp3
            mRecorder.setOutputFile(mFilePath);
            mRecorder.prepare();
            mRecorder.start();
            isStarted = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (isStarted && mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset(); // You can reuse the object by going back to setAudioSource() step
            mRecorder.release();
            mRecorder = null;
            isStarted = false;
        }
    }

    // To detect the connected other device like headphone, wifi headphone, usb headphone etc
    private boolean hasWiredHeadset(AudioManager mAudioManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return mAudioManager.isWiredHeadsetOn();
        } else {
            final AudioDeviceInfo[] devices = mAudioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo device : devices) {
                final int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    Log.d(TAG, "hasWiredHeadset: found wired headset");
                    return true;
                } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                    Log.d(TAG, "hasWiredHeadset: found USB audio device");
                    return true;
                } else if (type == AudioDeviceInfo.TYPE_TELEPHONY) {
                    Log.d(TAG, "hasWiredHeadset: found audio signals over the telephony network");
                    return true;
                }
            }
            return false;
        }
    }


}
