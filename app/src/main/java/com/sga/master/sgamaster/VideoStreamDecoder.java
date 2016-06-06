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
            /*
            Android MediaExtractor parses H264 (contained in a container format
            If I examine the H264 stream, I see that it consists of NAL units demarcated by the sequence 00 00 00 01.
            The samples returned by MediaExtractor are exactly those NAL units, each beginning with that marker -- except that, for the particular data source, the first three NAL units are concatenated.
            The first two NAL units are very short (29 and 8 bytes).
            */

            mExtractor.setDataSource(filePath);
            /*
            Sets the data source (file-path or http URL) to use.
            */
            for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                MediaFormat format = mExtractor.getTrackFormat(i);

                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith(VIDEO)) {
                    mExtractor.selectTrack(i);
                    mDecoder = MediaCodec.createDecoderByType(mime);
                    try {
                        Log.d(TAG, "format : " + format);
                        mDecoder.configure(format, surface, null, 0);

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

    //comment

    /*
    public void init(Surface surface){

        //H.264/AVC video
        try {
            mDecoder = MediaCodec.createEncoderByType("video/avc");
            //String format = MediaFormat.MIMETYPE_VIDEO_AVC;
            MediaFormat format = new MediaFormat();

            mDecoder.configure(, surface, null, 0);
            mDecoder.start();
            started = true;

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    */

    long usec_for_new_frame = 3000;
    //boolean started = false;
    //private final int MediaBlockSize = 1024 * 512;
    //ByteBuffer buf = ByteBuffer.allocate(MediaBlockSize);



    public boolean getStream(byte[] received){

        int inputBufferIndex = mDecoder.dequeueInputBuffer(usec_for_new_frame);
        if (inputBufferIndex >= 0) {

            ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputBufferIndex);

            inputBuffer.put(received);

            mDecoder.queueInputBuffer(inputBufferIndex,
                                                0,//startingbyte,
                                                received.length,
                                                usec_for_new_frame,//usec,
                                                MediaCodec.BUFFER_FLAG_KEY_FRAME);
        }

        MediaCodec.BufferInfo metaInfo = new MediaCodec.BufferInfo();
        int outputBufferId = mDecoder.dequeueOutputBuffer( metaInfo , usec_for_new_frame);
        if (outputBufferId >= 0) {
            ByteBuffer outputBuffer = mDecoder.getOutputBuffer(outputBufferId);
            MediaFormat bufferFormat = mDecoder.getOutputFormat(outputBufferId); // option A
            // bufferFormat is identical to outputFormat
            // outputBuffer is ready to be processed or rendered.

            mDecoder.releaseOutputBuffer(outputBufferId, true);
        }
        /*
        else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

            // Subsequent data will conform to new format.
            // Can ignore if using getOutputFormat(outputBufferId)
            outputFormat = mDecoder.getOutputFormat(); // option B
        }
        */

        return false;

    }


}