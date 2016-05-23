package com.sga.master.sgamaster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;

import org.java_websocket.drafts.Draft_10;

import java.net.URI;
import java.net.URISyntaxException;

public class MainMasterActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private String SGA_URI = "ws://localhost:8887";
    private StreamClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_master);


        try {
            client = new StreamClient( new URI( SGA_URI ), new Draft_10() ); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }




    //surface callbacks
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mVideoDecoder != null) {
            mVideoDecoder.close();
        }

    }
}
