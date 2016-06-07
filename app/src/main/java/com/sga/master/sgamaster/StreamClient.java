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


    private VideoStreamDecoder decoder;

    public StreamClient( URI serverUri , Draft draft , VideoStreamDecoder decoder ) {
        super( serverUri, draft );

        this.decoder = decoder;


    }

    public StreamClient( URI serverURI ) {
        super( serverURI );
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

        Log.d("StreamClient","onOpen: opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }


    /*
    as long as your packet source is sending in single NAL unit mode or non-interleaved mode you can extract the NAL units
    without any further processing from the packets and dump them to disk with 0x00,0x00,0x00,0x01 as divider between them.
    */

    @Override
    public void onMessage(String msg) {

        Log.d("StreamClient","onMessage: new message received");

        if(decoder!=null) {
            byte[] received = msg.getBytes();
            decoder.getStream(received);
        }
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
