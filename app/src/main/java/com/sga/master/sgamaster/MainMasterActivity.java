package com.sga.master.sgamaster;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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
import com.threed.jpct.Object3D;

import org.java_websocket.drafts.Draft_10;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;

public class MainMasterActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private String TAG = "MainMasterActivity";
    //private String SGA_URI = "ws://192.168.1.57:8088";
    private String SGA_URI = "ws://192.168.1.39:8088";
    private int streamPort = 8088;

    //private String SGA_URI = "ws://192.168.1.7:8088";
    //private String SGA_URI = "ws://192.168.26.101:8088";
    private MainMasterActivity act = this;
    private MainHandler mMainHandler;
    private SurfaceView videoView;
    private Button connButt;
    private VideoDecoderThread mVideoDecoder;
    private StreamListener streamListener;
    //private StreamClient client;
    //ClientConnector connectionThread;
    private boolean connectionEstablished = false;
    private WebSocketFactory wsfactory;
    private int SURFACE_WIDTH = 480;
    private int SURFACE_HEIGHT = 640;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainHandler = new MainHandler(this);

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

                Log.e("CONNBUTT", "Connect");

                //if(connectionThread==null) {
                    connButt.setClickable(false);
                    streamListener= new StreamListener(act);
                    mVideoDecoder = new VideoDecoderThread(streamListener,videoView.getHolder().getSurface(),SURFACE_WIDTH,SURFACE_HEIGHT);
                    EditText eText = (EditText) act.findViewById(R.id.ip_address_etext);
                    SGA_URI = "ws://"+eText.getText().toString()+":"+streamPort;
                    Log.e(TAG,"inserted IP: "+SGA_URI);
                    asynchConnectionTask();
                    /*
                    connectionThread = new ClientConnector();
                    connectionThread.start();
                    */
                    connectionEstablished=true;
                    Log.e(TAG, "client connector started");
                //}

            }
        });


        tempButt = (Button) this.findViewById(R.id.tempButton);

        tempButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tempButt.setClickable(false);

                new AsyncTask<Void, Void, Void>(){

                    @Override
                    protected Void doInBackground(Void... params) {

                        ModelObject3D mObj = new ModelObject3D(
                                MainMasterActivity.this,
                                "chair.3ds",//id
                                //R.drawable.chair,//p.e R.drawable.bigoffice//textureID
                                0.025f,//scale
                                -4,//x
                                0,//y
                                -2,//z
                                1);//dim

                        mObj.obj3D.rotateX((float)Math.toRadians(90.0));
                        JSONEncoder jsonEncoder = new JSONEncoder(MainMasterActivity.this);
                        String jsonObj3d =  jsonEncoder.encodeObject3DNew("chair", R.drawable.chair, mObj.obj3D);

                        try {
                            streamListener.sendJSONObject3D(jsonObj3d);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                }.execute();

                /*
                Object3DManager o3dM = new Object3DManager(MainMasterActivity.this);

                Object3D tmpObj3d = o3dM.createObject3D();
                try {
                    streamListener.sendObject3D(tmpObj3d);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
            }
        });
        //setContentView(videoView);
        //setContentView(R.layout.customlayout);


    }
    private Button tempButt;

    public void asynchConnectionTask(){

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
            //connection done in a separate thread
            ws.connectAsynchronously();


            Log.e("ClientConnector","connected");
            //while(mVideoDecoder==null);

            Log.e("ClientConnector","mVideoDecoder created");

            //initialized in surfaceChanged()
            mVideoDecoder.start();

            Log.e("ClientConnector","mVideoDecoder started");

            Log.e("ClientConnector","end of run()");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    /*
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
    */


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


        if(connectionEstablished){
            streamListener= new StreamListener(this);
            mVideoDecoder = new VideoDecoderThread(streamListener,videoView.getHolder().getSurface(),SURFACE_WIDTH,SURFACE_HEIGHT);
            asynchConnectionTask();
            //connectionThread = new ClientConnector();
            //connectionThread.start();
        }
        else
            connButt.setClickable(true);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mVideoDecoder!=null)
            mVideoDecoder.closeVideoDecoderThread();

        if(streamListener!=null)
            streamListener.pause();


        //connectionThread=null;
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

        //streamListener= new StreamListener();
        //mVideoDecoder = new VideoDecoderThread(streamListener,videoView.getHolder().getSurface(),width,height);



    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mVideoDecoder != null) {
            mVideoDecoder.close();
        }
    }






    public MainHandler getMainHandler(){
        return mMainHandler;
    }


    /**
     * Custom message handler for main UI thread.
     * <p/>
     * Receives messages from the renderer thread with UI-related updates.
     */
    public static class MainHandler extends Handler {

        //private static final int MSG_UPDATE_CONNECT_BUTTON = 0;
        private static final int MSG_ACTIVATE_CONNECT_BUTTON = 1;
        private static final int MSG_DISABLE_CONNECT_BUTTON = 2;

        private WeakReference<MainMasterActivity> mWeakActivity;

        public MainHandler(MainMasterActivity activity) {
            mWeakActivity = new WeakReference<MainMasterActivity>(activity);
        }

        public void sendActivateConnectButton() {
            sendMessage(obtainMessage(MSG_ACTIVATE_CONNECT_BUTTON));
        }

        public void sendDisableConnectButton() {
            sendMessage(obtainMessage(MSG_DISABLE_CONNECT_BUTTON));
        }

        @Override
        public void handleMessage(Message msg) {
            MainMasterActivity activity = mWeakActivity.get();
            if (activity == null) {
                return;
            }

            switch (msg.what) {
                /*
                case MSG_UPDATE_CONNECT_BUTTON: {
                    Boolean activate = (Boolean) msg.obj;
                    activity.connButt.setClickable(activate);
                    activity.connectionEstablished=!activate;
                    break;
                }
                */
                case MSG_ACTIVATE_CONNECT_BUTTON: {

                    activity.connectionEstablished=false;
                    Log.e("ACTIVATE BUTTON", "connectionEstablished:"+activity.connectionEstablished);
                    activity.connButt.setClickable(true);
                    break;
                }
                case MSG_DISABLE_CONNECT_BUTTON: {
                    activity.connButt.setClickable(false);
                    activity.connectionEstablished=true;
                    Log.e("DISABLE BUTTON", "connectionEstablished:"+activity.connectionEstablished);
                    break;
                }
                default:
                    throw new RuntimeException("Unknown message " + msg.what);
            }
        }

    }


}
