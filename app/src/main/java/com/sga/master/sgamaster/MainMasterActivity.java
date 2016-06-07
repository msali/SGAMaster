package com.sga.master.sgamaster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.java_websocket.drafts.Draft_10;

import java.net.URI;
import java.net.URISyntaxException;

public class MainMasterActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private String SGA_URI = "ws://192.168.1.217:8088";
    private SurfaceView videoView;
    private VideoDecoderThread mVideoDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoView = new SurfaceView(this);
        videoView.getHolder().addCallback(this);
        setContentView(videoView);

        mVideoDecoder = new VideoDecoderThread();

        //decoder.init(videoView.getHolder().getSurface(), SGA_URI);


        /*
        try {

            client = new StreamClient( new URI( SGA_URI ), new Draft_10() , decoder); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
            client.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        */

    }




    //surface callbacks
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mVideoDecoder != null) {
            if (mVideoDecoder.init(holder.getSurface(), this.SGA_URI)) {
                mVideoDecoder.start();

            } else {
                mVideoDecoder = null;
            }

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mVideoDecoder != null) {
            mVideoDecoder.close();
        }
    }
}
