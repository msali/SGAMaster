package com.sga.master.sgamaster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

/*
https://github.com/taehwandev/MediaCodecExample/tree/master/src/net/thdev/mediacodecexample/decoder
*/
public class VideoDecoderThread extends Thread {

    private static final String VIDEO = "video/";
    private static final String MIME = "video/avc";
    private static final String TAG = "VideoDecoder";
    //private MediaExtractor mExtractor;
    private MediaCodec mDecoder;

    private boolean eosReceived;

    //private StreamListener streamListener;
    private BytePicker picker;
    private Surface surface;
    private int vidW, vidH;

    public VideoDecoderThread(StreamListener streamListener, Surface surface, int vidW, int vidH) {
        //this.streamListener = streamListener;
        try {
            picker = new BytePicker(streamListener);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        this.surface = surface;
        this.vidW = vidW;
        this.vidH = vidH;

    }


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
                format.setByteBuffer("csd-0", configBuffers[0]/*sps*/);
                format.setByteBuffer("csd-1", configBuffers[1]/*pps*/);
                mDecoder.configure(format, surface, null/* crypto */, 0 /* Decoder */);

            } catch (IllegalStateException e) {
                Log.e(TAG, "codec '" + mime + "' failed configuration. " + e);
                return false;
            } /*catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }*/

            mDecoder.start();
            Log.e(TAG,"decoder started");
            //break;
            //}
            //}

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


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

    /*
    private ByteBuffer[] readConfigFrame(){

        Log.e(TAG,"readConfigFrame");

        while(currentChunk==null)currentChunk=streamListener.getNextChunk();

        ByteBuffer mConfigBuffer = ByteBuffer.wrap(currentChunk);
        byte[] array = new byte[mConfigBuffer.remaining()];
        mConfigBuffer.get(array);

        boolean lastDel = false;
        int spsIdx = -1, spsSize = 0, ppsIdx = -1, ppsSize = 0;
        for(int i=0; i <= array.length-4; i++){
            boolean delimiterFound =
                    (array[i]==0 && array[i+1]==0 && array[i+2]==0 && array[i+3]==1);
            if (spsIdx < 0 && delimiterFound){
                spsIdx = i;
                Log.e(TAG," d0: "+array[i]+" d1: "+array[i+1]+" d2: "+array[i+2]+" d3: "+array[i+3]+" t: "+array[i+4]+" spsIDX="+spsIdx);

            }
            else if (ppsIdx < 0 && delimiterFound){
                spsSize = i - spsIdx;
                ppsIdx = i;

                Log.e(TAG," d0: "+array[i]+" d1: "+array[i+1]+" d2: "+array[i+2]+" d3: "+array[i+3]+" t: "+array[i+4]+" ppsIDX="+ppsIdx);

                ppsSize = array.length - ppsIdx;

            }
            else if(spsIdx>0 && ppsIdx>0 && delimiterFound){
                Log.e(TAG," d0: "+array[i]+" d1: "+array[i+1]+" d2: "+array[i+2]+" d3: "+array[i+3]+" t: "+array[i+4]+" ppsIDX="+ppsIdx);
                ppsSize=i-ppsIdx;
                break;
            }
        }
        Log.e(TAG,"readConfigFrame");
        byte[] spsArray = new byte[spsSize], ppsArray = new byte[ppsSize];
        Log.e(TAG,"1");
        //mConfigBuffer.position(0);
        for(int i = 0; i<spsSize;i++){
            spsArray[i]=currentChunk[spsIdx+i];
        }
        //mConfigBuffer.get(spsArray, 0, spsSize);
        for(int i = 0; i<ppsSize;i++){
            ppsArray[i]=currentChunk[ppsIdx+i];
        }
        //mConfigBuffer.get(ppsArray, 0, ppsSize);
        Log.e(TAG,"3");
        ByteBuffer sps = ByteBuffer.wrap(spsArray);
        Log.e(TAG,"4");
        ByteBuffer pps = ByteBuffer.wrap(ppsArray);
        Log.e(TAG,"5");
        ByteBuffer[] configBuffers = new ByteBuffer[2];
        configBuffers[0] = sps;
        configBuffers[1] = pps;

        Log.e(TAG,"cfgNALU1="+sps.remaining());
        Log.e(TAG,"d0: "+spsArray[0]+" d1: "+spsArray[1]+" d2:"+spsArray[2]+" d3: "+spsArray[3]+" t: "+spsArray[4]);

        Log.e(TAG,"cfgNALU2="+pps.remaining());
        Log.e(TAG,"d0: "+ppsArray[0]+" d1: "+ppsArray[1]+" d2:"+ppsArray[2]+" d3: "+ppsArray[3]+" t: "+ppsArray[4]);

        int sum = pps.remaining()+sps.remaining();
        Log.e(TAG,"cfgNALUsum="+sum);
        Log.e(TAG,"firstchunk="+currentChunk.length);
        Log.e(TAG, ""+0x67);
        Log.e(TAG, ""+0x68);



        return configBuffers;

    }
    */

    //private byte[] configNALU1;
    //private byte[] configNALU2;

    ///private byte[] currentChunk = null;
    ///private int currentChunkOffset = 0;
    ///private final int MB = 1048576;


    /*
    private ByteBuffer[] readConfigFrame() throws Exception {

        //for(currentChunkOffset=0;currentChunkOffset<)
        byte[] delimiter = new byte[4];
        byte type = 0xD;

        //get first chunk
        while (currentChunk == null)
            currentChunk = streamListener.getNextChunk();

        currentChunkOffset = 0;

        //exception to be fixed if initial chunk length is less than 5 bytes
        if (currentChunk.length < 5) throw new Exception("Initial chunk length is less than 5");

        delimiter[0] = currentChunk[currentChunkOffset];
        delimiter[1] = currentChunk[currentChunkOffset + 1];
        delimiter[2] = currentChunk[currentChunkOffset + 2];
        delimiter[3] = currentChunk[currentChunkOffset + 3];
        type = currentChunk[currentChunkOffset + 4];
        currentChunkOffset = currentChunkOffset + 5;

        while (!(delimiter[0] == 0x00 && delimiter[1] == 0x00 && delimiter[2] == 0x00 && delimiter[3] == 0x01 && type == 0x67)) {

            //Log.e(TAG+"DEL","d0:"+delimiter[0]+"d1:"+delimiter[1]+"d2:"+delimiter[2]+"d3:"+delimiter[3]+"t:"+type);

            delimiter[0] = delimiter[1];
            delimiter[1] = delimiter[2];
            delimiter[2] = delimiter[3];
            delimiter[3] = type;
            type = currentChunk[currentChunkOffset];

            currentChunkOffset++;
        }


        Log.e(TAG,"currChOff67="+currentChunkOffset+"-1");


        byte[] temp = new byte[MB];
        int offset = 0;
        temp[offset] = delimiter[0];
        temp[offset + 1] = delimiter[1];
        temp[offset + 2] = delimiter[2];
        temp[offset + 3] = delimiter[3];
        temp[offset + 4] = type;
        offset = offset + 5;


        byte[] delimiter2 = new byte[4];

        //exception to be fixed if initial chunk length is less than 5 bytes
        if (currentChunk.length - currentChunkOffset < 5)
            throw new Exception("Second config chunk length is less than 5");

        delimiter2[0] = -1;//currentChunk[currentChunkOffset];
        delimiter2[1] = -1;//currentChunk[currentChunkOffset + 1];
        delimiter2[2] = -1;//currentChunk[currentChunkOffset + 2];
        delimiter2[3] = -1;//currentChunk[currentChunkOffset + 3];
        byte type2 = 0xD;

        //type2 = -1;//currentChunk[currentChunkOffset + 4];



        while (!(delimiter2[0] == 0x00 && delimiter2[1] == 0x00 && delimiter2[2] == 0x00 && delimiter2[3] == 0x01 && type2 == 0x68)) {

            //Log.e(TAG+"del1","d0:"+delimiter2[0]+"d1:"+delimiter2[1]+"d2:"+delimiter2[2]+"d3:"+delimiter2[3]+"t:"+type);

            temp[offset] = currentChunk[currentChunkOffset];
            delimiter2[0] = delimiter2[1];
            delimiter2[1] = delimiter2[2];
            delimiter2[2] = delimiter2[3];
            delimiter2[3] = type2;
            type2 = currentChunk[currentChunkOffset];
            currentChunkOffset++;
            offset++;
        }
        Log.e(TAG,"currChOff68="+currentChunkOffset+"-1");
        //Log.e(TAG,"d0: "+delimiter2[0]+" d1: "+delimiter2[1]+" d2:"+delimiter2[2]+" d3: "+delimiter2[3]+" t: "+type2);


        byte [] configNALU1 = new byte[offset - 5];
        for (int i = 0; i < offset - 5; i++) configNALU1[i] = temp[i];

        offset = 0;
        temp[offset] = delimiter2[0];
        temp[offset + 1] = delimiter2[1];
        temp[offset + 2] = delimiter2[2];
        temp[offset + 3] = delimiter2[3];
        temp[offset + 4] = type2;
        offset = offset + 5;


        delimiter2[0] = -1;//currentChunk[currentChunkOffset];
        delimiter2[1] = -1;//currentChunk[currentChunkOffset + 1];
        delimiter2[2] = -1;//currentChunk[currentChunkOffset + 2];
        delimiter2[3] = -1;//currentChunk[currentChunkOffset + 3];

        while (!(delimiter2[0] == 0x00 && delimiter2[1] == 0x00 && delimiter2[2] == 0x00 && delimiter2[3] == 0x01)) {

            //Log.e(TAG+"del2","d0:"+delimiter2[0]+"d1:"+delimiter2[1]+"d2:"+delimiter2[2]+"d3:"+delimiter2[3]);
            temp[offset] = currentChunk[currentChunkOffset];
            delimiter2[0] = delimiter2[1];
            delimiter2[1] = delimiter2[2];
            delimiter2[2] = delimiter2[3];
            delimiter2[3] = currentChunk[currentChunkOffset];
            currentChunkOffset++;
            offset++;
        }


        byte[] configNALU2 = new byte[offset - 4];
        for (int i = 0; i < offset - 4; i++) configNALU2[i] = temp[i];

        //submitConfigNALUtoCodec(configNALU1);
        //submitConfigNALUtoCodec(configNALU2);

        ByteBuffer[] configBuffers = new ByteBuffer[2];
        configBuffers[0] = ByteBuffer.wrap(configNALU1);
        configBuffers[1] = ByteBuffer.wrap(configNALU2);

        Log.e(TAG,"cfgNALU1="+configNALU1.length);
        Log.e(TAG,"d0: "+configNALU1[0]+" d1: "+configNALU1[1]+" d2:"+configNALU1[2]+" d3: "+configNALU1[3]+" t: "+type);
        Log.e(TAG,"cfgNALU2="+configNALU2.length);
        Log.e(TAG,"d0: "+configNALU2[0]+" d1: "+configNALU2[1]+" d2:"+configNALU2[2]+" d3: "+configNALU2[3]+" t: "+type2);
        int sum = configNALU1.length+configNALU2.length;
        Log.e(TAG,"cfgNALUsum="+sum);
        Log.e(TAG,"firstchunk="+currentChunk.length);
        Log.e(TAG, ""+0x67);
        Log.e(TAG, ""+0x68);


        return configBuffers;
    }
    */

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
                mDecoder.releaseOutputBuffer(outIndex, true /* Surface init */);
                break;
        }

    }

    /*
    private void readConfigFrame() throws Exception {

        //for(currentChunkOffset=0;currentChunkOffset<)
        byte[] delimiter = new byte[4];
        byte type = 0xD;

        //get first chunk
        while(currentChunk==null)
            currentChunk = streamListener.getNextChunk();

        currentChunkOffset=0;

        //exception to be fixed if initial chunk length is less than 5 bytes
        if(currentChunk.length<5)throw new Exception("Initial chunk length is less than 5");

        delimiter[0]=currentChunk[currentChunkOffset];
        delimiter[1]=currentChunk[currentChunkOffset+1];
        delimiter[2]=currentChunk[currentChunkOffset+2];
        delimiter[3]=currentChunk[currentChunkOffset+3];
        type = currentChunk[currentChunkOffset+4];
        currentChunkOffset=currentChunkOffset+5;

        while(!(delimiter[0]==0x00 && delimiter[1]==0x00 && delimiter[2]==0x00 && delimiter[3]==0x01 && type==0x67)){

            if(currentChunk == null) {
                currentChunk = streamListener.getNextChunk();
                currentChunkOffset=0;
                continue;
            }
            if(currentChunkOffset+1>=currentChunk.length){
                currentChunk = streamListener.getNextChunk();
                currentChunkOffset=0;
                continue;
            }
            delimiter[0]=delimiter[1];
            delimiter[1]=delimiter[2];
            delimiter[2]=delimiter[3];
            delimiter[3]=type;
            type = currentChunk[currentChunkOffset];

            currentChunkOffset++;
        }

        byte[] delimiter2 = new byte[4];
        byte type2 = 0xD;

        //exception to be fixed if initial chunk length is less than 5 bytes
        if(currentChunk.length-currentChunkOffset<5)throw new Exception("Middle chunk length is less than 5");

        delimiter2[0]=currentChunk[currentChunkOffset];
        delimiter2[1]=currentChunk[currentChunkOffset+1];
        delimiter2[2]=currentChunk[currentChunkOffset+2];
        delimiter2[3]=currentChunk[currentChunkOffset+3];
        type2 = currentChunk[currentChunkOffset+4];
        currentChunkOffset=currentChunkOffset+5;


        while(!(delimiter2[0]==0x00 && delimiter2[1]==0x00 && delimiter2[2]==0x00 && delimiter2[3]==0x01 && type2==0x68)){

            if(currentChunk == null) {
                currentChunk = streamListener.getNextChunk();
                currentChunkOffset=0;
                continue;
            }
            if(currentChunkOffset+1>=currentChunk.length){
                currentChunk = streamListener.getNextChunk();
                currentChunkOffset=0;
                continue;
            }
            delimiter[0]=delimiter[1];
            delimiter[1]=delimiter[2];
            delimiter[2]=delimiter[3];
            delimiter[3]=type;
            type = currentChunk[currentChunkOffset];

            currentChunkOffset++;
        }







    }
    */


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
                    inputBuffer.put(nextNALU);

                    mDecoder.queueInputBuffer(inputIndex, 0, nextNALU.length, currentTime, 0);

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
                    if(outIndex<0)break;

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
                break;
            }
        }

        mDecoder.stop();
        mDecoder.release();
    }


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


    private class BytePicker {

        private String TAG = "BytePicker";
        private StreamListener streamListener;
        private byte[] currentChunk = null;
        private int chunkPos = 0;
        private byte[] slidingBuffer = new byte[]{0xF, 0xF, 0xF, 0xF, 0xF};
        private final int MB = 1048576;
        private boolean firstRead = true;



        public BytePicker(StreamListener streamListener) throws Exception {
            this.streamListener = streamListener;
        }

        public int getQueueSize(){
            if(streamListener!=null)
                return streamListener.getQueueSize();

            return -1;
        }

        //busy waiting of bytes
        private byte getNextByte() throws Exception {
            //Log.e(TAG, "getNextByte()");
            if (streamListener == null) throw new Exception(this.TAG + ": null StreamListener");

            while (currentChunk == null) {
                Thread.yield();
                currentChunk = streamListener.getNextChunk();
                chunkPos = 0;
            }

            byte next;

            if (chunkPos < currentChunk.length) {
                next = currentChunk[chunkPos];
                chunkPos++;
                return next;
            } else {

                currentChunk = streamListener.getNextChunk();
                while(currentChunk==null){
                    Thread.yield();
                    currentChunk = streamListener.getNextChunk();
                }

                chunkPos = 0;
                next = currentChunk[chunkPos];
                chunkPos++;
                return next;
            }
        }

        private void readTillDelimiter() throws Exception {
            Log.e(TAG, "readTillDelimiter()");
            boolean delimiterFound = false;

            while (!delimiterFound) {
                slidingBuffer[0] = slidingBuffer[1];
                slidingBuffer[1] = slidingBuffer[2];
                slidingBuffer[2] = slidingBuffer[3];
                slidingBuffer[3] = slidingBuffer[4];
                slidingBuffer[4] = getNextByte();

                delimiterFound = (slidingBuffer[0] == 0x00 && slidingBuffer[1] == 0x00 && slidingBuffer[2] == 0x00 && slidingBuffer[3] == 0x01);
            }

            Log.e(TAG, "readTillDelimiter() END");
        }



        //busy waiting of NALU
        public byte[] getNALU2() throws Exception {

            if(firstRead){
                firstRead=false;
                readTillDelimiter();
            }
            //byte[] temp = new byte[2*MB];
            ByteArrayOutputStream temp = new ByteArrayOutputStream();

            temp.write(slidingBuffer);

            int tPos = slidingBuffer.length;

            boolean delimiterFound = false;

            slidingBuffer[0] = getNextByte();
            slidingBuffer[1] = getNextByte();
            slidingBuffer[2] = getNextByte();
            slidingBuffer[3] = getNextByte();
            slidingBuffer[4] = getNextByte();

            delimiterFound = (slidingBuffer[0] == 0x00 && slidingBuffer[1] == 0x00 && slidingBuffer[2] == 0x00 && slidingBuffer[3] == 0x01);
            while (!delimiterFound) {

                temp.write(slidingBuffer[0]);
                tPos++;

                slidingBuffer[0] = slidingBuffer[1];
                slidingBuffer[1] = slidingBuffer[2];
                slidingBuffer[2] = slidingBuffer[3];
                slidingBuffer[3] = slidingBuffer[4];
                slidingBuffer[4] = getNextByte();

                delimiterFound = (slidingBuffer[0] == 0x00 && slidingBuffer[1] == 0x00 && slidingBuffer[2] == 0x00 && slidingBuffer[3] == 0x01);

            }

            Log.e("Next NALU will be:", "type=" + slidingBuffer[0] + slidingBuffer[1] + slidingBuffer[2] + slidingBuffer[3] + " " + slidingBuffer[4]);


            /*
            ByteBuffer buff = ByteBuffer.wrap(temp.toByteArray());
            byte[] chunk = new byte[temp.size()-5];
            buff.get(chunk, 0, temp.size()-5);
            return chunk;
            */
            return temp.toByteArray();
        }



        //busy waiting of NALU
        public byte[] getNALU() throws Exception {

            if(firstRead){
                firstRead=false;
                readTillDelimiter();
            }

            byte[] temp = new byte[2*MB];
            int tPos;

            for (tPos = 0; tPos < slidingBuffer.length; tPos++)
                temp[tPos] = slidingBuffer[tPos];


            boolean delimiterFound = false;

            while (!delimiterFound) {
                slidingBuffer[0] = slidingBuffer[1];
                slidingBuffer[1] = slidingBuffer[2];
                slidingBuffer[2] = slidingBuffer[3];
                slidingBuffer[3] = slidingBuffer[4];
                slidingBuffer[4] = getNextByte();
                temp[tPos] = slidingBuffer[4];
                tPos++;

                delimiterFound = (slidingBuffer[0] == 0x00 && slidingBuffer[1] == 0x00 && slidingBuffer[2] == 0x00 && slidingBuffer[3] == 0x01);
            }

            Log.e("Next NALU will be:", "type=" + slidingBuffer[0] + slidingBuffer[1] + slidingBuffer[2] + slidingBuffer[3] + " " + slidingBuffer[4]);

            int NALU_SIZE = tPos - 5;
            byte[] NALU = new byte[NALU_SIZE];

            for (int i = 0; i < NALU_SIZE; i++)
                NALU[i] = temp[i];


            return NALU;

        }


        /*
        public byte[] getNextChunk(){

            if(slidingBuffer!=null){

                byte[] temp = new byte[2*MB];
                int tPos;
                for (tPos = 0; tPos < slidingBuffer.length; tPos++)
                    temp[tPos] = slidingBuffer[tPos];

                while(currentChunk==null) {
                    currentChunk = streamListener.getNextChunk();
                    chunkPos=0;
                }

                while(chunkPos<currentChunk.length){

                    temp[tPos]=currentChunk[chunkPos];
                    tPos++;
                    chunkPos++;
                }

                slidingBuffer=null;

                String debug = "";
                byte[] chunk = new byte[tPos];
                for(int i = 0; i<tPos; i++){
                    chunk[i]=temp[i];
                    debug=debug+chunk[i];
                }
                Log.e("DEBUG",debug);
                return chunk;
            }
            else{

                currentChunk=streamListener.getNextChunk();

                while(currentChunk==null)
                    currentChunk=streamListener.getNextChunk();

                return currentChunk;
            }

        }*/


    }
}
