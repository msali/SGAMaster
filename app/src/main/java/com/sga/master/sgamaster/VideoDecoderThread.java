package com.sga.master.sgamaster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

/*
https://github.com/taehwandev/MediaCodecExample/tree/master/src/net/thdev/mediacodecexample/decoder
*/
public class VideoDecoderThread extends Thread {

    private static final String VIDEO = "video/";
    private static final String MIME = "video/avc";
    private static final String TAG = "VideoDecoderThread";
    private static final byte SPS_TYPE = 0x67;
    private static final byte PPS_TYPE = 0x68;

    private MediaCodec mDecoder;
    private MediaFormat format;
    boolean isInput = true;
    private BufferInfo info;
    private long currentTime;
    private ByteBuffer[] decoderInputBuffers;
    private ByteBuffer[] decoderOutputBuffers;
    private int inputIndex = -1;
    private int outIndex = -1;


    private boolean isReady=false;
    private boolean eosReceived;
    private boolean DECODER_IS_STARTED = false;

    private BytePickerThread pickerThread;
    private StreamListener streamListener;

    private Surface surface;
    private int vidW, vidH;


    public final int NEW_NALU_AVAILABLE = 0;

    //public final int SPS_NALU_AVAILABLE = 1;

    //public final int PPS_NALU_AVAILABLE = 2;


    private Handler videoDecoderHandler;


    public VideoDecoderThread(StreamListener streamListener, Surface surface, int vidW, int vidH) {

        this.streamListener=streamListener;
        this.surface = surface;
        this.vidW = vidW;
        this.vidH = vidH;

    }

    private Handler getVideoDecoderHandler(){
        return videoDecoderHandler;
    }


    private boolean init() {
        eosReceived = false;
        mDecoder=null;
        try {
            //mExtractor = new MediaExtractor();
            //mExtractor.setDataSource(filePath);

            //for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            //MediaFormat format = mExtractor.getTrackFormat(i);

            format = MediaFormat.createVideoFormat(MIME, vidW, vidH);

            //A key describing the desired clockwise rotation on an output surface.
            format.setInteger(MediaFormat.KEY_ROTATION, 0);

            String mime = MIME;//= format.getString(MediaFormat.KEY_MIME);
            //if (mime.startsWith(VIDEO)) {
            //mExtractor.selectTrack(i);
            mDecoder = MediaCodec.createDecoderByType(MIME);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
    /*
    private boolean init() {
        eosReceived = false;
        mDecoder=null;
        try {
            //mExtractor = new MediaExtractor();
            //mExtractor.setDataSource(filePath);

            //for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            //MediaFormat format = mExtractor.getTrackFormat(i);

            MediaFormat format = MediaFormat.createVideoFormat(MIME, vidW, vidH);

            //A key describing the desired clockwise rotation on an output surface.
            format.setInteger(MediaFormat.KEY_ROTATION, 0);

            String mime = MIME;//= format.getString(MediaFormat.KEY_MIME);
            //if (mime.startsWith(VIDEO)) {
            //mExtractor.selectTrack(i);
            mDecoder = MediaCodec.createDecoderByType(MIME);
            try {
                //Log.d(TAG, "format : " + format);
                ByteBuffer[] configBuffers = readConfigFrame();
                format.setByteBuffer("csd-0", configBuffers[0]);//sps
                format.setByteBuffer("csd-1", configBuffers[1]);//pps
                mDecoder.configure(format, surface, null, 0);//null = no encryption , 0 = Decoder

            } catch (IllegalStateException e) {
                Log.e(TAG, "codec '" + mime + "' failed configuration. " + e);
                return false;
            }

            mDecoder.start();
            Log.e(TAG,"decoder started");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
    */

    /*
    private ByteBuffer[] readConfigFrame() {

        Log.e(TAG,"readConfigFrame()");
        try {
            byte[] sps = picker.getNALU();
            byte[] pps = picker.getNALU();
            ByteBuffer[] cfgBuff = new ByteBuffer[2];
            cfgBuff[0] = ByteBuffer.wrap(sps);
            cfgBuff[1] = ByteBuffer.wrap(pps);
            Log.e(TAG,"readConfigFrame() END");
            return cfgBuff;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }

        return null;

    }


    private void submitConfigNALUtoCodec(byte[] configNALU) {

        BufferInfo info = new BufferInfo();
        int inputIndex = -1;
        while (inputIndex < 0)
            inputIndex = mDecoder.dequeueInputBuffer(10000);

        // fill inputBuffers[inputBufferIndex] with valid data

        ByteBuffer inputBuffer = ByteBuffer.wrap(configNALU);

        long currentTime = System.currentTimeMillis();
        mDecoder.queueInputBuffer(inputIndex, 0, configNALU.length, currentTime, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);

        int outIndex = mDecoder.dequeueOutputBuffer(info, 10000);
        switch (outIndex) {
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                mDecoder.getOutputBuffers();
                break;

            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + mDecoder.getOutputFormat());
                break;

            case MediaCodec.INFO_TRY_AGAIN_LATER:
                Log.e(TAG, "INFO_TRY_AGAIN_LATER");
                break;

            default:
                mDecoder.releaseOutputBuffer(outIndex, true);//true = surface init
                break;
        }

    }
    */

    public boolean isReady(){

        return isReady;
    }


    public void sendNewNaluMessage(){

        Message newNaluMessage = new Message();
        newNaluMessage.what = NEW_NALU_AVAILABLE;
        this.videoDecoderHandler.sendMessage(newNaluMessage);

    }


    public class VideoDecoderHandlerCallback implements Handler.Callback {


        @Override
        public boolean handleMessage(Message msg) {

            //Log.e(TAG, "NEW MESSAGE");
            final int what = msg.what;
            switch(what) {
                /*
                case SPS_NALU_AVAILABLE:
                    doThat();
                    break;
                case PPS_NALU_AVAILABLE:
                    doThat();
                    break;
                */
                case NEW_NALU_AVAILABLE:
                    handleNewNalu();
                    break;
            }

            return true;
        }


    }

    boolean isFirstSPSArrived = false;
    boolean isFirstPPSArrived = false;

    private void handleNewNalu() {

        byte[] NALU = pickerThread.getNextNalu();
        if(NALU==null){
            //Log.e(TAG, "NULL NALU");
            return;
        }

        boolean isCorrectNalu = (NALU[0] == 0x00 && NALU[1] == 0x00 && NALU[2] == 0x00 && NALU[3] == 0x01);

        if(isCorrectNalu){
            if(NALU[4] == SPS_TYPE && !isFirstSPSArrived){
                isFirstSPSArrived=true;
                Log.e(TAG,"SPS RECEIVED "+NALU[2]);
                ByteBuffer spsBuff = ByteBuffer.wrap(NALU);
                format.setByteBuffer("csd-0", spsBuff/*sps*/);//sps
                return;
            }
            if(NALU[4] == PPS_TYPE && !isFirstPPSArrived){
                isFirstPPSArrived=true;
                Log.e(TAG,"PPS RECEIVED "+ NALU[4]);
                ByteBuffer ppsBuff = ByteBuffer.wrap(NALU);
                format.setByteBuffer("csd-1", ppsBuff/*pps*/);//pps
                mDecoder.configure(format, surface, null/* crypto */, 0 /* Decoder */);
                mDecoder.start();
                Log.e(TAG,"decoder started");
                info = new BufferInfo();
                decoderInputBuffers = mDecoder.getInputBuffers();
                decoderOutputBuffers = mDecoder.getOutputBuffers();
                DECODER_IS_STARTED=true;

                return;
            }

            if(/*!DECODER_IS_STARTED*/!isFirstSPSArrived||!isFirstPPSArrived){
                Log.e(TAG,"Received a not configuration Nalu, while still waiting for sps and pps Nalus.");
                return;
            }
            else{
                //Log.e(TAG, "Handling a normal NALU.");


                if (DECODER_IS_STARTED) {


                    inputIndex = mDecoder.dequeueInputBuffer(10000);
                    if (inputIndex >= 0) {


                        if(NALU.length>131072){
                            Log.e(TAG, "NALU dim = "+NALU.length+ " while inputBuffer.limit was "+131072/*inputBuffer.limit()*/);
                            isFirstPPSArrived=false;
                            isFirstSPSArrived=false;
                            mDecoder.stop();
                            /*
                            String tempSt = "";
                            int j = 0;
                            for(int i = 0; i<NALU.length; i++){
                                tempSt=tempSt+NALU[i]+" ";

                                j++;

                                if(j==30){
                                    Log.e(TAG, " EVIL NALU:"+tempSt);
                                    tempSt="";
                                    j=0;
                                }
                            }
                            Log.e("-----------------","-------------------------");
                            System.exit(2);
                            //
                            */
                            return;
                        }
                        else {

                            currentTime = System.currentTimeMillis();
                            // fill inputBuffers[inputBufferIndex] with valid data
                            ByteBuffer inputBuffer = decoderInputBuffers[inputIndex];
                            inputBuffer.clear();
                            inputBuffer.put(NALU);
                            mDecoder.queueInputBuffer(inputIndex, 0, NALU.length, currentTime, 0);
                        }
                    }

                    outIndex = mDecoder.dequeueOutputBuffer(info, 10000);


                    Log.e(TAG, "output buffer dequeued");
                    switch (outIndex) {
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            Log.e(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                            decoderOutputBuffers = mDecoder.getOutputBuffers();
                            break;

                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            Log.e(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + mDecoder.getOutputFormat());
                            break;

                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            //Log.e(TAG, "INFO_TRY_AGAIN_LATER");
                            break;

                        default:
                    /*
                    if (!first) {
                        startWhen = System.currentTimeMillis();
                        first = true;
                    }
                    try {
                        long sleepTime = (info.presentationTimeUs / 1000) - (System.currentTimeMillis() - startWhen);
                        Log.d(TAG, "info.presentationTimeUs : " + (info.presentationTimeUs / 1000) + " playTime: " + (System.currentTimeMillis() - startWhen) + " sleepTime : " + sleepTime);

                        if (sleepTime > 0)
                            Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    */
                            if (outIndex < 0) return;

                            ByteBuffer outputFrame = decoderOutputBuffers[outIndex];
                            if (outputFrame != null) {
                                outputFrame.position(info.offset);
                                outputFrame.limit(info.offset + info.size);
                            }

                            mDecoder.releaseOutputBuffer(outIndex, true /* render to surface */);
                            break;
                    }

                    // All decoded frames have been rendered, we can stop playing now
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.e(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                        return;
                    }
                }




            }


        }
        else{
            Log.e(TAG, "UNCORRECT NALU PARSING from the chunk.");
        }


        /*
        try {



        } catch (IllegalStateException e) {
            Log.e(TAG, "codec '" + mime + "' failed configuration. " + e);
            return false;
        }
        */


    }

    public void run(){

        if(!init()){
            Log.e(TAG,"init failed.");
            return;
        }

        Looper.prepare();

        videoDecoderHandler = new Handler(new VideoDecoderHandlerCallback());

        Log.e(TAG, "VideoDecoderThread started");

        isReady=true;

        pickerThread=new BytePickerThread(this, streamListener);
        pickerThread.start();

        while (!pickerThread.isReady()){
            //Log.e(TAG, "Picker is not ready");
            Thread.yield();
        }

        streamListener.setBytePickerThread(pickerThread);


        Looper.loop();

    }

    /*
    @Override
    public void run() {



        Log.e(TAG, "run()");
        if (!init()) {
            Log.e(TAG, "ERROR while initializing MediaCodec");
            return;
        }
        else Log.e(TAG,"init done");

        BufferInfo info = new BufferInfo();
        //ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
        //mDecoder.getOutputBuffers();

        long currentTime;

        boolean isInput = true;
        //boolean first = false;
        //long startWhen = 0;


        ByteBuffer[] decoderInputBuffers = mDecoder.getInputBuffers();
        ByteBuffer[] decoderOutputBuffers = mDecoder.getOutputBuffers();

        int inputIndex = -1;
        int outIndex = -1;

        while (!eosReceived) {

            Log.e(TAG, "Entered main loop");
            if (isInput) {
                byte[] nextNALU=null;
                try{

                    Log.e(TAG,"getting a NALU");

                    nextNALU = picker.getNALU2();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }

                if (nextNALU == null) {
                    continue;
                }

                inputIndex = mDecoder.dequeueInputBuffer(10000);
                if (inputIndex >= 0) {
                    currentTime = System.currentTimeMillis();
                    // fill inputBuffers[inputBufferIndex] with valid data
                    ByteBuffer inputBuffer = decoderInputBuffers[inputIndex];
                    inputBuffer.clear();

                    if(nextNALU.length>inputBuffer.limit()){
                        Log.e(TAG, "NALU dim = "+nextNALU.length+ " while inputBuffer.limit was"+inputBuffer.limit());
                        continue;
                    }
                    else {
                        inputBuffer.put(nextNALU);
                        mDecoder.queueInputBuffer(inputIndex, 0, nextNALU.length, currentTime, 0);
                    }
                }
            }

            outIndex = mDecoder.dequeueOutputBuffer(info, 10000);
            Log.e(TAG,"output buffer dequeued");
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.e(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    mDecoder.getOutputBuffers();
                    break;

                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.e(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + mDecoder.getOutputFormat());
                    break;

                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.e(TAG, "INFO_TRY_AGAIN_LATER");
                    break;

                default:

                    //if (!first) {
                    //    startWhen = System.currentTimeMillis();
                    //    first = true;
                    //}
                    //try {
                    //    long sleepTime = (info.presentationTimeUs / 1000) - (System.currentTimeMillis() - startWhen);
                    //    Log.d(TAG, "info.presentationTimeUs : " + (info.presentationTimeUs / 1000) + " playTime: " + (System.currentTimeMillis() - startWhen) + " sleepTime : " + sleepTime);

                    //    if (sleepTime > 0)
                    //        Thread.sleep(sleepTime);
                    //} catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                    //    e.printStackTrace();
                    //}

                    if(outIndex<0)break;

                    ByteBuffer outputFrame = decoderOutputBuffers[outIndex];
                    if (outputFrame != null) {
                        outputFrame.position(info.offset);
                        outputFrame.limit(info.offset + info.size);
                    }

                    mDecoder.releaseOutputBuffer(outIndex, true  render to surface );//true = render to surface
                    break;
            }

            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.e(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }

        mDecoder.stop();
        mDecoder.release();
    }
    */

    /*
    @Override
    public void run() {

        if (!init()) {
            Log.e(TAG, "ERROR while initializing MediaCodec");
            return;
        }
        BufferInfo info = new BufferInfo();
        //ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
        //mDecoder.getOutputBuffers();


        boolean isInput = true;
        //boolean first = false;
        //long startWhen = 0;


        ByteBuffer[] decoderInputBuffers = mDecoder.getInputBuffers();
        ByteBuffer[] decoderOutputBuffers = mDecoder.getOutputBuffers();

        int inputIndex = -1;
        int outputIndex = -1;

        while (!eosReceived) {
            //byte[] chunk = streamListener.getNextChunk();
            if (isInput) {
                byte[] nextNALU=null;
                try{
                    nextNALU = picker.getNALU();   //streamListener.getNextChunk();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }

                if (nextNALU == null) {
                    continue;
                }

                inputIndex = mDecoder.dequeueInputBuffer(10000);
                if (inputIndex >= 0) {
                    // fill inputBuffers[inputBufferIndex] with valid data
                    //ByteBuffer inputBuffer = inputBuffers[inputIndex];

                    ByteBuffer inputBuffer = ByteBuffer.wrap(nextNALU);
                    //int sampleSize = mExtractor.readSampleData(inputBuffer, 0);

                    long currentTime = System.currentTimeMillis();
                    //if (mExtractor.advance() && sampleSize > 0) {
                    mDecoder.queueInputBuffer(inputIndex, 0, nextNALU.length, currentTime, 0);
                    /*
                    } else {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        mDecoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isInput = false;
                    }

                }
            }

            int outIndex = mDecoder.dequeueOutputBuffer(info, 10000);
            Log.e(TAG,"output buffer dequeued");
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.e(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    mDecoder.getOutputBuffers();
                    break;

                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.e(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + mDecoder.getOutputFormat());
                    break;

                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.e(TAG, "INFO_TRY_AGAIN_LATER");
                    break;

                default:
                    if (!first) {
                        startWhen = System.currentTimeMillis();
                        first = true;
                    }
                    try {
                        long sleepTime = (info.presentationTimeUs / 1000) - (System.currentTimeMillis() - startWhen);
                        Log.d(TAG, "info.presentationTimeUs : " + (info.presentationTimeUs / 1000) + " playTime: " + (System.currentTimeMillis() - startWhen) + " sleepTime : " + sleepTime);

                        if (sleepTime > 0)
                            Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    mDecoder.releaseOutputBuffer(outIndex, true /* Surface init );
                    break;
            }

            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.e(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }

        mDecoder.stop();
        mDecoder.release();
        //mExtractor.release();
    }
    */

    public void close() {
        eosReceived = true;
    }



}
