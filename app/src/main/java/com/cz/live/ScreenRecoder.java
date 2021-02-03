package com.cz.live;

import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import com.cz.live.rtmp.OnConntionListener;
import com.cz.live.rtmp.RtmpHelper;
import com.cz.live.rtmp.encoder.BasePushEncoder;
import com.cz.live.rtmp.encoder.PushEncode;

public class ScreenRecoder extends AppCompatActivity implements
    OnConntionListener, BasePushEncoder.OnMediaInfoListener  {
    private RtmpHelper rtmpHelper;
    private PushEncode pushEncode;
    private boolean isStart;
    MediaProjectionManager  projectionManager;
    MediaProjection mediaProjection;
    private Surface mSurface;

    String url = "rtmp://192.168.2.112:1935/live/3";
    public ScreenRecoder() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_recoder);
        url= getIntent().getStringExtra("url");
        ScreenRecoder();

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void ScreenRecoder (){
           projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), 1);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {

        try {
             mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection == null) {
                Log.e("@@", "media projection is null");
                return;
            }
            rtmpHelper = new RtmpHelper();
            rtmpHelper.setOnConntionListener(this);
            rtmpHelper.initLivePush(url);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("@@", "---------------->>>1" + e);
        }


    }




    @Override
    public void onConntecting() {
        Log.e("chenzhu", "连接中...");
    }

    @Override
    public void onConntectSuccess() {
        Log.e("chenzhu", "onConntectSuccess...");
        startPush();
    }

    private void startPush() {
        pushEncode = new PushEncode(this);
        pushEncode.initEncoder(true,mediaProjection, 480,720,44100,2,16);
        pushEncode.setOnMediaInfoListener(this);
        pushEncode.start();
    }



    @Override
    public void onConntectFail(String msg) {
        Log.e("chenzhu", "onConntectFail  " + msg);
    }

    @Override
    public void onMediaTime(int times) {

    }

    @Override
    public void onSPSPPSInfo(byte[] sps, byte[] pps) {
        if (rtmpHelper == null) return;
        rtmpHelper.pushSPSPPS(sps, pps);
    }

    @Override
    public void onVideoDataInfo(byte[] data, boolean keyFrame) {
        if (rtmpHelper == null) return;
        rtmpHelper.pushVideoData(data,keyFrame);
    }

    @Override
    public void onAudioInfo(byte[] data) {
        if (rtmpHelper == null) return;
        rtmpHelper.pushAudioData(data);
    }
}
