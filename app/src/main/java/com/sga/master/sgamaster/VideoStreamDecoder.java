package com.sga.master.sgamaster;

import android.media.MediaCodec;
import android.media.MediaDataSource;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Mario Salierno on 23/05/2016.
 */
public class VideoStreamDecoder {

    private static final String VIDEO = "video/";
    private static final String TAG = "VideoDecoder";
    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;

    public VideoStreamDecoder() {


    }


    public void init(Surface surface, String filePath) {

        try {
            mExtractor = new MediaExtractor();

            mExtractor.setDataSource(filePath);

            for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                MediaFormat format = mExtractor.getTrackFormat(i);

                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith(VIDEO)) {
                    mExtractor.selectTrack(i);
                    mDecoder = MediaCodec.createDecoderByType(mime);
                    try {
                        Log.d(TAG, "format : " + format);
                        mDecoder.configure(format, surface, null, 0 /* Decoder */);

                    } catch (IllegalStateException e) {
                        Log.e(TAG, "codec '" + mime + "' failed configuration. " + e);
                        return;
                    }

                    mDecoder.start();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    long usec_for_new_frame = 3000;
    boolean started = true;
    public void init(byte[] received){

        //H.264/AVC video
        try {
            mDecoder = MediaCodec.createEncoderByType("video/avc");
            mDecoder.start();
            while(started) {
                int inputBufferIndex = mDecoder.dequeueInputBuffer(usec_for_new_frame);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputBufferIndex);

                    inputBuffer.put(received);

                    mDecoder.queueInputBuffer(inputBufferIndex,
                                                startingbyte,
                                                size,
                                                usec,
                                                flags);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }


}