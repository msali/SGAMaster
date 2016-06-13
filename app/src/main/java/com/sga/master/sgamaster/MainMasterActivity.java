package com.sga.master.sgamaster;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.java_websocket.drafts.Draft_10;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainMasterActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private String TAG = "MainMasterActivity";
    private String SGA_URI = "ws://192.168.1.57:8088";
    private SurfaceView videoView;
    private VideoDecoderThread mVideoDecoder;
    private StreamListener streamListener = new StreamListener();
    //private StreamClient client;
    ClientConnector connectionThread;
    private WebSocketFactory wsfactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //setContentView(R.layout.customlayout);

        // Create a web socket factory. The timeout value remains 0.
        //wsfactory = new WebSocketFactory();
        wsfactory = new WebSocketFactory().setConnectionTimeout(5000);

        videoView = new SurfaceView(this);//(SurfaceView) this.findViewById(R.id.videoView);

        //videoView.getHolder().setFixedSize(720,1184);
        videoView.getHolder().setFixedSize(720,1280);
        videoView.getHolder().addCallback(this);

        setContentView(videoView);




    }


    private class ClientConnector extends Thread{


        @Override
        public void run() {

            try {
                // Create a web socket with a socket connection timeout value.
                //WebSocket ws = wsfactory.createSocket("ws://localhost/endpoint", 5000);
                //timeout = 5000

                Log.e("ClientConnector","run1");
                WebSocket ws = wsfactory.createSocket(SGA_URI, 5000);

                Log.e("ClientConnector","run2");
                ws.addListener(streamListener);

                Log.e("ClientConnector","run3");
                // Connect to the server and perform an opening handshake.
                // This method blocks until the opening handshake is finished.
                ws.connect();

                Log.e("ClientConnector","connected");
                while(mVideoDecoder==null);


                Log.e("ClientConnector","connected");
                mVideoDecoder.start();


                Log.e(TAG, "new videoencoder started");

                Log.e("ClientConnector","connected");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WebSocketException e) {
                // Failed to establish a WebSocket connection.
                e.printStackTrace();
            }


        }
    }

    @Override
    public void onStart() {
        super.onStart();

        connectionThread = new ClientConnector();
        connectionThread.start();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG,"onResume");



    }

    @Override
    protected void onPause() {
        super.onPause();

    }



    //surface callbacks
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }


    private int SURFACE_WIDTH,SURFACE_HEIGHT;
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*if (mVideoDecoder != null) {
            if (mVideoDecoder.init(holder.getSurface(), this.SGA_URI)) {
                mVideoDecoder.start();

            } else {
                mVideoDecoder = null;
            }

        }*/
        SURFACE_WIDTH=width;
        SURFACE_HEIGHT=height;
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE)return;
        Log.e(TAG, "w:"+width+" h:"+height+"   VS 720x1280");
        mVideoDecoder = new VideoDecoderThread(streamListener,videoView.getHolder().getSurface(),width,height);

        Log.e(TAG, "new videoencoder");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mVideoDecoder != null) {
            mVideoDecoder.close();
        }
    }
}
