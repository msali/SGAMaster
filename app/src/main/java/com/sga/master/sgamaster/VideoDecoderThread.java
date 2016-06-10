package com.sga.master.sgamaster;

import java.io.IOException;
import java.nio.ByteBuffer;

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

    private StreamListener streamListener;
    private Surface surface;
    private int vidW,vidH;

    public VideoDecoderThread(StreamListener streamListener, Surface surface, int vidW, int vidH) {
        this.streamListener = streamListener;
        this.surface=surface;
        this.vidW=vidW;
        this.vidH=vidH;
    }


    private boolean init() {
        eosReceived = false;
        try {
            //mExtractor = new MediaExtractor();
            //mExtractor.setDataSource(filePath);

            //for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                //MediaFormat format = mExtractor.getTrackFormat(i);

                MediaFormat format = MediaFormat.createVideoFormat(MIME, vidW, vidH);

                //A key describing the desired clockwise rotation on an output surface.
                format.setInteger(MediaFormat.KEY_ROTATION, 0);

                String mime = format.getString(MediaFormat.KEY_MIME);
                //if (mime.startsWith(VIDEO)) {
                    //mExtractor.selectTrack(i);
                    mDecoder = MediaCodec.createDecoderByType(mime);
                    try {
                        Log.d(TAG, "format : " + format);
                        ByteBuffer[] configBuffers = readConfigFrame();
                        format.setByteBuffer("csd-0", configBuffers[0]/*sps*/);
                        format.setByteBuffer("csd-1", configBuffers[1]/*pps*/);
                        mDecoder.configure(format, surface, null/* crypto */, 0 /* Decoder */);

                    } catch (IllegalStateException e) {
                        Log.e(TAG, "codec '" + mime + "' failed configuration. " + e);
                        return false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }

            mDecoder.start();
                    //break;
                //}
            //}

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    //private byte[] configNALU1;
    //private byte[] configNALU2;

    private byte[] currentChunk = null;
    private int currentChunkOffset = 0;
    private final int MB = 1048576;

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

            delimiter[0] = delimiter[1];
            delimiter[1] = delimiter[2];
            delimiter[2] = delimiter[3];
            delimiter[3] = type;
            type = currentChunk[currentChunkOffset];

            currentChunkOffset++;
        }

        byte[] delimiter2 = new byte[4];
        byte type2 = 0xD;

        //exception to be fixed if initial chunk length is less than 5 bytes
        if (currentChunk.length - currentChunkOffset < 5)
            throw new Exception("Second config chunk length is less than 5");

        delimiter2[0] = currentChunk[currentChunkOffset];
        delimiter2[1] = currentChunk[currentChunkOffset + 1];
        delimiter2[2] = currentChunk[currentChunkOffset + 2];
        delimiter2[3] = currentChunk[currentChunkOffset + 3];
        type2 = currentChunk[currentChunkOffset + 4];
        currentChunkOffset = currentChunkOffset + 5;


        byte[] temp = new byte[MB];
        int offset = 0;
        temp[offset] = delimiter[0];
        temp[offset + 1] = delimiter[1];
        temp[offset + 2] = delimiter[2];
        temp[offset + 3] = delimiter[3];
        temp[offset + 4] = type;
        offset = offset + 5;

        while (!(delimiter2[0] == 0x00 && delimiter2[1] == 0x00 && delimiter2[2] == 0x00 && delimiter2[3] == 0x01 && type2 == 0x68)) {

            temp[offset] = currentChunk[currentChunkOffset];
            delimiter2[0] = delimiter2[1];
            delimiter2[1] = delimiter2[2];
            delimiter2[2] = delimiter2[3];
            delimiter2[3] = type2;
            type2 = currentChunk[currentChunkOffset];
            currentChunkOffset++;
            offset++;
        }

        byte [] configNALU1 = new byte[offset - 5];
        for (int i = 0; i < offset - 5; i++) configNALU1[i] = temp[i];

        offset = 0;
        temp[offset] = delimiter2[0];
        temp[offset + 1] = delimiter2[1];
        temp[offset + 2] = delimiter2[2];
        temp[offset + 3] = delimiter2[3];
        temp[offset + 4] = type2;
        offset = offset + 5;

        while (!(delimiter2[0] == 0x00 && delimiter2[1] == 0x00 && delimiter2[2] == 0x00 && delimiter2[3] == 0x01)) {

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
        return configBuffers;
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

        if(!init()){
            Log.e(TAG, "ERROR while initializing MediaCoded");
            return;
        }
        BufferInfo info = new BufferInfo();
        //ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
        //mDecoder.getOutputBuffers();


        boolean isInput = true;
        boolean first = false;
        long startWhen = 0;


        while (!eosReceived) {
            byte[] chunk = streamListener.getNextChunk();
            if (isInput) {

                byte[] nextchunk = streamListener.getNextChunk();
                if (nextchunk == null) {
                    continue;
                }

                int inputIndex = mDecoder.dequeueInputBuffer(10000);
                if (inputIndex >= 0) {
                    // fill inputBuffers[inputBufferIndex] with valid data
                    //ByteBuffer inputBuffer = inputBuffers[inputIndex];

                    ByteBuffer inputBuffer = ByteBuffer.wrap(nextchunk);
                    //int sampleSize = mExtractor.readSampleData(inputBuffer, 0);

                    long currentTime = System.currentTimeMillis();
                    //if (mExtractor.advance() && sampleSize > 0) {
                    mDecoder.queueInputBuffer(inputIndex, 0, nextchunk.length, currentTime, 0);
                    /*
                    } else {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        mDecoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isInput = false;
                    }
                    */
                }
            }

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
				Log.d(TAG, "INFO_TRY_AGAIN_LATER");
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

                    mDecoder.releaseOutputBuffer(outIndex, true /* Surface init */);
                    break;
            }

            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }

        mDecoder.stop();
        mDecoder.release();
        //mExtractor.release();
    }

    public void close() {
        eosReceived = true;
    }
}
