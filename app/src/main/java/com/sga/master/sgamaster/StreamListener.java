package com.sga.master.sgamaster;

import android.os.Message;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketCloseCode;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;
import com.threed.jpct.Object3D;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private boolean paused = false;
    private ConcurrentLinkedQueue<byte[]> chunks = new ConcurrentLinkedQueue<byte[]>();
    private BytePickerThread pickerThread;
    private Message newChunkMessage;
    private WebSocket wsocket=null;


    public void sendObject3D(Object3D o3d) throws IOException {

        if(wsocket!=null){
            wsocket.sendBinary(Object3DManager.serializeObject3D(o3d));
        }
        else
            throw new IOException("Attempt to send an object3D on a null websocket instance.");
    }


    public byte[] getNextChunk(){
        //Retrieves and removes the head of this queue, or returns null if this queue is empty
        return chunks.poll();
    }



    public void pause(){
        paused=true;
        if(wsocket!=null) {
            //wsocket.sendClose();
            wsocket.disconnect(WebSocketCloseCode.NORMAL,"paused activity");
            wsocket=null;
        }
    }

    public void resume(){
        paused=false;
    }


    public void closeCurrentConnection(){
        if(wsocket!=null) {
            //wsocket.sendClose();
            wsocket.disconnect(WebSocketCloseCode.NORMAL,"paused activity");
            wsocket=null;
        }
    }

    public void setBytePickerThread(BytePickerThread picker){

        pickerThread=picker;

    }

    public int getQueueSize()
    {

        if(chunks!=null)
            return chunks.size();

        return -1;
    }

    private MainMasterActivity activity;
    public StreamListener(MainMasterActivity activity){

        Log.e(TAG, "created");

        this.activity=activity;

    }



    @Override
    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {

        Log.e(TAG,"onStateChanged");

    }



    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        activity.getMainHandler().sendDisableConnectButton();
        Log.e(TAG,"onConnected");
        wsocket=websocket;


    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
        Log.e(TAG,"onConnectError");
        activity.getMainHandler().sendActivateConnectButton();

    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        Log.e(TAG,"onDisconnected: closed by server "+closedByServer);
        wsocket=null;
        pickerThread.sendOnDisconnectedMessage();

        if(closedByServer)
            activity.getMainHandler().sendActivateConnectButton();
        else if(!paused){
            activity.getMainHandler().sendActivateConnectButton();
        }

    }


    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onTextFrame");
    }

    @Override
    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onBinaryFrame");

    }

    @Override
    public void onTextMessage(WebSocket websocket, String message) throws Exception {
        // Received a text message.
        Log.e(TAG,"received message from:"+websocket.getSocket().getRemoteSocketAddress()+":"+message);

    }



    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {

        //Log.e(TAG,"onBinaryMessage");
        /*
        long newTime = System.currentTimeMillis();
        long intv=newTime-INTERVAL;
        INTERVAL=newTime;
        Log.e(TAG, "(prev onFrame) onBinaryMessage:"+binary.length+" byte received. TIMEINMILLIS:"+intv);
        Log.e(TAG, "NCHUNKS"+ chunks.size());
        */

        //if(paused)return;

        //Inserts the specified element at the tail of this queue.
        chunks.offer(binary);
        if(pickerThread!=null)
            pickerThread.sendNewChunkMessage();

    }

    @Override
    public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onSendingFrame");
    }

    @Override
    public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onFrame");
    }

    @Override
    public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onContinuationFrame");
    }

    @Override
    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        Log.e(TAG,"onCloseFrame");
    }

    @Override
    public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onPingFrame");
    }

    @Override
    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onPongFrame");
    }


    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onFrameSent");
    }

    @Override
    public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        //Log.e(TAG,"onFrameUnsent");
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        Log.e(TAG,"onError");
    }

    @Override
    public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
        Log.e(TAG,"onFrameError");
    }

    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
        Log.e(TAG,"onMessageError");
    }

    @Override
    public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
        Log.e(TAG,"onMessageDecompressionError");
    }

    @Override
    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
        Log.e(TAG,"onTextMessageError");
    }

    @Override
    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
        Log.e(TAG,"onSendError");
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
        Log.e(TAG,"onUnexpectedError");
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        Log.e(TAG,"handleCallbackError");
    }

    @Override
    public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {
        Log.e(TAG,"onSendingHandshake");
    }


}
