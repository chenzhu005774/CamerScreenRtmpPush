package com.cz.live;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cz.live.camera.CameraEglSurfaceView;
import com.cz.live.rtmp.OnConntionListener;
import com.cz.live.rtmp.RtmpHelper;
import com.cz.live.rtmp.encoder.BasePushEncoder;
import com.cz.live.rtmp.encoder.PushEncode;

public class LivePushActivity extends AppCompatActivity implements View.OnClickListener,
        OnConntionListener, BasePushEncoder.OnMediaInfoListener {

    private CameraEglSurfaceView cameraEglSurfaceView;

    private RtmpHelper rtmpHelper;
    private PushEncode pushEncode;
    private boolean isStart;
    private Button button;
//    String url = "rtmp://192.168.2.112:1935/live/3";
    String url = "rtmp://send3a.douyu.com/live/6441662rRTGeeLKm?wsSecret=46c2d0b986fb1cbbd21454b300d217c8&wsTime=5d49866a&wsSeek=off&wm=0&tw=0&roirecognition=0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_push);
        url= getIntent().getStringExtra("url");
        Log.d("chenzhu","=====>"+getIntent().getStringExtra("url").trim());
        cameraEglSurfaceView = findViewById(R.id.camera);
        button = findViewById(R.id.push);
        button.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraEglSurfaceView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cameraEglSurfaceView.previewAngle(this);
    }

    @Override
    public void onClick(View v) {
        if (isStart) {
            button.setText("开始");
            if (pushEncode != null) {
                pushEncode.stop();
                pushEncode = null;
            }

            if(rtmpHelper!=null){
                rtmpHelper.stop();
                rtmpHelper =null;
            }
            isStart = false;

        } else {
            button.setText("停止");
            isStart = true;
            rtmpHelper = new RtmpHelper();
            rtmpHelper.setOnConntionListener(this);
            rtmpHelper.initLivePush(url);
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
        Log.e("chenzhu", "width:"+ cameraEglSurfaceView.getWidth()+"height:"+ cameraEglSurfaceView.getCameraPrivewHeight());
        pushEncode = new PushEncode(this, cameraEglSurfaceView.getTextureId());
        pushEncode.initEncoder(cameraEglSurfaceView.getEglContext(), cameraEglSurfaceView.getWidth(),
                cameraEglSurfaceView.getCameraPrivewHeight(),44100,2,16);
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
        if (rtmpHelper == null) {
            return;
        }
        rtmpHelper.pushSPSPPS(sps, pps);
    }

    @Override
    public void onVideoDataInfo(byte[] data, boolean keyFrame) {
        if (rtmpHelper == null) {
            return;
        }
        rtmpHelper.pushVideoData(data,keyFrame);
    }

    @Override
    public void onAudioInfo(byte[] data) {
        if (rtmpHelper == null) {
            return;
        }
        rtmpHelper.pushAudioData(data);
    }


}
