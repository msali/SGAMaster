package com.sga.master.sgamaster;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Mario Salierno on 09/06/2016.
 */
public class StreamListener implements WebSocketListener {

    private String TAG = "StreamListener";
    private long INTERVAL = System.currentTimeMillis();
    boolean firstINT = true;
    private ConcurrentLinkedQueue<byte[]> chunks = new ConcurrentLinkedQueue<byte[]>();



    public byte[] getNextChunk(){
        //Retrieves and removes the head of this queue, or returns null if this queue is empty
        return chunks.poll();
    }

    public int getQueueSize()
    {

        if(chunks!=null)
            return chunks.size();

        return -1;
    }

    public StreamListener(){

        Log.e(TAG, "created");

    }



    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {

    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        Log.e(TAG,"onConnected");
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
        Log.e(TAG,"onConnectError");
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        Log.e(TAG,"onDisconnected");
    }


    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.e(TAG,"onTextFrame");
    }

    @Override
    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {



    }

    @Override
    public void onTextMessage(WebSocket websocket, String message) throws Exception {
        // Received a text message.

        Log.e(TAG,"received:"+message);

    }



    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        chunks.offer(binary);

        long newTime = System.currentTimeMillis();
        long intv=newTime-INTERVAL;
        INTERVAL=newTime;

        Log.e(TAG, "(prev onFrame) onBinaryMessage:"+binary.length+" byte received. TIMEINMILLIS:"+intv);
        Log.e(TAG, "NCHUNKS"+ chunks.size());

        //Inserts the specified element at the tail of this queue.

    }

    @Override
    public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.e(TAG,"onSendingFrame");
    }

    @Override
    public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onFrame");
    }

    @Override
    public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

    }


    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {

    }

    @Override
    public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

    }

    @Override
    public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

    }

    @Override
    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

    }

    @Override
    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {

    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {

    }

    @Override
    public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {

    }


}
