package com.sga.master.sgamaster;

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


        // Create a web socket factory. The timeout value remains 0.
        //wsfactory = new WebSocketFactory();
        wsfactory = new WebSocketFactory().setConnectionTimeout(2000);

        videoView = new SurfaceView(this);
        videoView.getHolder().addCallback(this);
        setContentView(videoView);

        //decoder.init(videoView.getHolder().getSurface(), SGA_URI);


    }


    private class ClientConnector extends Thread{


        @Override
        public void run() {

            try {
                // Create a web socket with a socket connection timeout value.
                //WebSocket ws = wsfactory.createSocket("ws://localhost/endpoint", 5000);
                //timeout = 5000

                WebSocket ws = wsfactory.createSocket(SGA_URI, 5000);
                ws.addListener(streamListener);
                // Connect to the server and perform an opening handshake.
                // This method blocks until the opening handshake is finished.
                ws.connect();
                Log.e("ClientConnector","connected");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WebSocketException e) {
                // Failed to establish a WebSocket connection.
                e.printStackTrace();
            }

            /*
            try {
                Log.e("Connection", "starting: "+SGA_URI);
                //client = new StreamClient( new URI( SGA_URI ), new Draft_10() , decoder); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
                client = new StreamClient( new URI( SGA_URI ), new Draft_10());
                try{
                    client.connectBlocking();
                }catch (Throwable t){
                    Log.d("aaa", t.getMessage());
                }

                Log.e("Connection", "done");


            }
            catch (URISyntaxException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
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



        connectionThread = new ClientConnector();
        connectionThread.start();
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
        /*if (mVideoDecoder != null) {
            if (mVideoDecoder.init(holder.getSurface(), this.SGA_URI)) {
                mVideoDecoder.start();

            } else {
                mVideoDecoder = null;
            }

        }*/

        mVideoDecoder = new VideoDecoderThread(streamListener,videoView.getHolder().getSurface(),width,height);
        mVideoDecoder.start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mVideoDecoder != null) {
            mVideoDecoder.close();
        }
    }
}
