package com.sga.master.sgamaster;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.nfc.Tag;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.java_websocket.drafts.Draft_10;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainMasterActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private String TAG = "MainMasterActivity";
    //private String SGA_URI = "ws://192.168.1.57:8088";
    private String SGA_URI = "ws://192.168.1.39:8088";
    private int streamPort = 8080;

    //private String SGA_URI = "ws://192.168.1.7:8088";
    //private String SGA_URI = "ws://192.168.26.101:8088";
    private MainMasterActivity act = this;
    private SurfaceView videoView;
    private Button connButt;
    private VideoDecoderThread mVideoDecoder;
    private StreamListener streamListener;
    //private StreamClient client;
    ClientConnector connectionThread;
    private WebSocketFactory wsfactory;
    private int SURFACE_WIDTH = 480;
    private int SURFACE_HEIGHT = 640;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        setContentView(R.layout.master_activity_layout);


        // Create a web socket factory. The timeout value remains 0.
        //wsfactory = new WebSocketFactory();
        wsfactory = new WebSocketFactory().setConnectionTimeout(5000);

        //videoView = new SurfaceView(this);//(SurfaceView) this.findViewById(R.id.videoView);
        videoView = (SurfaceView) this.findViewById(R.id.videoView);


        //videoView.getHolder().setFixedSize(720,1184);
        //videoView.getHolder().setFixedSize(720,1280);
        videoView.getHolder().setFixedSize(SURFACE_WIDTH,SURFACE_HEIGHT);
        //videoView.getHolder().setFixedSize(360,480);

        videoView.getHolder().addCallback(this);

        connButt = (Button) this.findViewById(R.id.connButton);
        connButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(connectionThread==null) {
                    connButt.setClickable(false);

                    EditText eText = (EditText) act.findViewById(R.id.ip_address_etext);
                    SGA_URI = "ws://"+eText.getText().toString()+":"+streamPort;
                    Log.e(TAG,"inserted IP:"+SGA_URI);
                    connectionThread = new ClientConnector();
                    connectionThread.start();
                    Log.e(TAG, "client connector started");
                }

            }
        });

        //setContentView(videoView);
        //setContentView(R.layout.customlayout);


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
                ws.connect();


                Log.e("ClientConnector","connected");
                while(mVideoDecoder==null);

                Log.e("ClientConnector","mVideoDecoder created");

                //initialized in surfaceChanged()
                mVideoDecoder.start();

                Log.e("ClientConnector","mVideoDecoder started");

                Log.e("ClientConnector","end of run()");
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


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG,"surfaceChanged");
        /*if (mVideoDecoder != null) {
            if (mVideoDecoder.init(holder.getSurface(), this.SGA_URI)) {
                mVideoDecoder.start();

            } else {
                mVideoDecoder = null;
            }

        }*/


        //if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE)return;
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)return;

        streamListener= new StreamListener();
        mVideoDecoder = new VideoDecoderThread(streamListener,videoView.getHolder().getSurface(),width,height);



    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mVideoDecoder != null) {
            mVideoDecoder.close();
        }
    }
}
