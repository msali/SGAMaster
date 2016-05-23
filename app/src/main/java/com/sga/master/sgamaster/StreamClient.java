package com.sga.master.sgamaster;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Created by Mario Salierno on 23/05/2016.
 */
public class StreamClient extends WebSocketClient{



    public StreamClient( URI serverUri , Draft draft ) {
        super( serverUri, draft );


    }

    public StreamClient( URI serverURI ) {
        super( serverURI );
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

        Log.d("StreamClient","onOpen: opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }


    private final int MediaBlockSize = 1024 * 512;
    ByteBuffer buf = ByteBuffer.allocate(MediaBlockSize);

    @Override
    public void onMessage(String msg) {

        Log.d("StreamClient","onMessage: new message received");
        byte[] received = msg.getBytes();
        //VideoStreamDecoder

    }

    @Override
    public void onClose( int code, String reason, boolean remote ) {
        Log.d("StreamClient","onClose: Connection closed by"  + ( remote ? "remote peer" : "StreamClient" ));
    }

    @Override
    public void onError( Exception ex ) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }
}
