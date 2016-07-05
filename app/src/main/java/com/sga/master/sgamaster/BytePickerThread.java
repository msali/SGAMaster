package com.sga.master.sgamaster;

import android.nfc.Tag;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Mario Salierno on 20/06/2016.
 */
public class BytePickerThread extends Thread {


    private VideoDecoderThread videoDecoderThread;
    private String TAG = "BytePickerThread";
    private boolean ready = false;
    private StreamListener streamListener;

    private byte[] currentChunk = null;
    private int chunkPos = 0;
    private byte[] slidingBuffer = new byte[]{0xF, 0xF, 0xF, 0xF, 0xF};
    private final int MB = 1048576;
    private boolean firstRead = true;
    private Handler pickerHandler;
    private ConcurrentLinkedQueue<byte[]> nalu_Queue = new ConcurrentLinkedQueue<byte[]>();

    //MESSAGE WHAT FIELDS
    public final int NEW_CHUNK_AVAILABLE = 1;

    public BytePickerThread(VideoDecoderThread videoDecoderThread, StreamListener streamListener) {
        this.streamListener = streamListener;
        this.videoDecoderThread = videoDecoderThread;
        Log.e(TAG, "BytePickerThread created");
    }


    public boolean isReady() {
        return ready;
    }

    public byte[] getNextNalu() {
        return nalu_Queue.poll();
    }

    @Override
    public void run() {
        //Log.e(TAG, "BytePicker run()");
        Looper.prepare();

        // We need to create the Handler before reporting ready.
        pickerHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                //Log.e(TAG, "NEW MESSAGE");

                int what = msg.what;
                switch (what) {

                    case NEW_CHUNK_AVAILABLE:
                        try {
                            decodeChunk2();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return false;
            }
        });


        //Log.e(TAG, "BytePickerThread started");

        ready = true;


        Looper.loop();

    }

    boolean firstCHUNK = true;
    private byte[] slidingChunkBuffer = new byte[]{0xF, 0xF, 0xF, 0xF};
    boolean firstChunk = true;
    ByteArrayOutputStream tempNALU = new ByteArrayOutputStream();


    private Byte getNextChunkByte() {

        if (chunkPos >= currentChunk.length) {
            chunkPos = 0;
            return null;
        }
        byte next = currentChunk[chunkPos];
        chunkPos++;
        return next;

    }

    public Byte readByteAndUpdateSlidingBufferDelimiter() {

        Byte currByte = getNextChunkByte();
        if (currByte == null) return null;
        slidingChunkBuffer[0] = slidingChunkBuffer[1];
        slidingChunkBuffer[1] = slidingChunkBuffer[2];
        slidingChunkBuffer[2] = slidingChunkBuffer[3];
        slidingChunkBuffer[3] = currByte;

        return currByte;

    }


    public void decodeChunk5() throws IOException {

        currentChunk = streamListener.getNextChunk();

        Byte current;
        boolean firstDelimiterFound = false;

        while (!firstDelimiterFound) {
            current = readByteAndUpdateSlidingBufferDelimiter();
            firstDelimiterFound = (slidingChunkBuffer[0] == 0x00 && slidingChunkBuffer[1] == 0x00 && slidingChunkBuffer[2] == 0x00 && slidingChunkBuffer[3] == 0x01);

        }
        tempNALU.write(slidingChunkBuffer);

        current = readByteAndUpdateSlidingBufferDelimiter();

        while (current != null) {
            tempNALU.write(current);


            firstDelimiterFound = (slidingChunkBuffer[0] == 0x00 && slidingChunkBuffer[1] == 0x00 && slidingChunkBuffer[2] == 0x00 && slidingChunkBuffer[3] == 0x01);
            if (firstDelimiterFound) {

                byte[] temp_buff = tempNALU.toByteArray();
                byte[] newNALU = new byte[temp_buff.length - 4];

                for (int i = 0; i < temp_buff.length - 4; i++) {
                    newNALU[i] = temp_buff[i];
                }

                nalu_Queue.offer(newNALU);
                videoDecoderThread.sendNewNaluMessage();
                //tempNALU = new ByteArrayOutputStream();
                tempNALU.reset();
                tempNALU.write(slidingChunkBuffer);


            }
            current = readByteAndUpdateSlidingBufferDelimiter();

        }

        tempNALU.reset();
        Log.e(TAG,"decodeChunk5");
    }
    public void decodeChunk4() throws IOException {
        final byte[] delimiters = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01};
        currentChunk = streamListener.getNextChunk();
        // Create an instance of ByteTokenizer
        final ByteTokenizer tokenizer = new ByteTokenizer(currentChunk, delimiters);

        // Count tokens
        final int tonkenNums = tokenizer.countTokens();
        //System.out.println("Token numbers: " + tonkenNums);

        // Print all tokens
        byte[] token;
        for (int i = 0; i < tonkenNums; i++) {
            if (i == 0) {

                tempNALU.write((byte[]) tokenizer.nexToken());
                //Log.e("QUEUE (i=0)",""+tempNALU.toByteArray().length);
                nalu_Queue.offer(tempNALU.toByteArray());
                videoDecoderThread.sendNewNaluMessage();
                tempNALU.reset();
                continue;

            } else if (i >= 0 && i < tonkenNums /*- 1*/) {
                tempNALU.reset();
                tempNALU.write(delimiters);
                tempNALU.write((byte[]) tokenizer.nexToken());
                //Log.e("QUEUE (<i<)",""+tempNALU.toByteArray().length);
                nalu_Queue.offer(tempNALU.toByteArray());
                videoDecoderThread.sendNewNaluMessage();
                tempNALU.reset();
                continue;

            } else if (i == tonkenNums - 1) {
                tempNALU.reset();
                tempNALU.write(delimiters);
                Log.e("QUEUE (i=last)",""+tempNALU.toByteArray().length);
                tempNALU.write((byte[]) tokenizer.nexToken());
                continue;
            }


        }


    }


    byte[] temppp = new byte[2 * MB];
    int j = 0;

    public void decodeChunk3() throws IOException {

        currentChunk = streamListener.getNextChunk();

        boolean firstDelimiterFound = false;
        for (int i = 0; i < currentChunk.length; i++) {
            firstDelimiterFound = (currentChunk[i] == 0x00 && currentChunk[i + 1] == 0x00 && currentChunk[i + 2] == 0x00 && currentChunk[i + 3] == 0x01);
            if (firstDelimiterFound) {
                byte[] bbb = new byte[j];
                for (int k = 0; k < j; k++) {
                    bbb[k] = temppp[k];
                }
                //nalu_Queue.offer(tempNALU.toByteArray());
                nalu_Queue.offer(bbb);

                videoDecoderThread.sendNewNaluMessage();
                //tempNALU = new ByteArrayOutputStream();
                //tempNALU.reset();
                j = 0;

            }
            //tempNALU.write(currentChunk[i]);
            temppp[j] = currentChunk[i];
            j++;
        }

    }


        /*
        String tChunk = new String(streamListener.getNextChunk());

        //StringTokenizer
        String[] result = tChunk.split("0001");
        for (int x=0; x<result.length; x++){

            String temp = "0001"+result[x];
            Log.e("PARSED", temp.substring(0, 10));
            nalu_Queue.offer(temp.getBytes());
            videoDecoderThread.sendNewNaluMessage();


        }
        */


    public void decodeChunk2() throws IOException {

        currentChunk = streamListener.getNextChunk();

        Byte current = readByteAndUpdateSlidingBufferDelimiter();
        boolean firstDelimiterFound = false;

        while (current != null) {
            tempNALU.write(current);


            firstDelimiterFound = (slidingChunkBuffer[0] == 0x00 && slidingChunkBuffer[1] == 0x00 && slidingChunkBuffer[2] == 0x00 && slidingChunkBuffer[3] == 0x01);
            if (firstDelimiterFound) {

                byte[] temp_buff = tempNALU.toByteArray();
                byte[] newNALU = new byte[temp_buff.length - 4];

                for (int i = 0; i < temp_buff.length - 4; i++) {
                    newNALU[i] = temp_buff[i];
                }

                nalu_Queue.offer(newNALU);
                videoDecoderThread.sendNewNaluMessage();
                //tempNALU = new ByteArrayOutputStream();
                tempNALU.reset();
                tempNALU.write(slidingChunkBuffer);


            }
            current = readByteAndUpdateSlidingBufferDelimiter();

        }


        /*
        String tChunk = new String(streamListener.getNextChunk());

        //StringTokenizer
        String[] result = tChunk.split("0001");
        for (int x=0; x<result.length; x++){

            String temp = "0001"+result[x];
            Log.e("PARSED", temp.substring(0, 10));
            nalu_Queue.offer(temp.getBytes());
            videoDecoderThread.sendNewNaluMessage();


        }
        */
    }


    public void decodeChunk() throws IOException {

        currentChunk = streamListener.getNextChunk();
        Log.e(TAG, "CurrentChunk.leng=" + currentChunk.length);

        if (firstCHUNK) {
            firstCHUNK = false;
            boolean firstDelimiterFound = false;
            while (!firstDelimiterFound) {
                if (readByteAndUpdateSlidingBufferDelimiter() == null) return;
                firstDelimiterFound = (slidingChunkBuffer[0] == 0x00 && slidingChunkBuffer[1] == 0x00 && slidingChunkBuffer[2] == 0x00 && slidingChunkBuffer[3] == 0x01);
            }
            tempNALU = new ByteArrayOutputStream();
            tempNALU.write(slidingChunkBuffer);
        }

        boolean endDelimiterFound = false;
        Byte currByte = readByteAndUpdateSlidingBufferDelimiter();

        while (currByte != null) {

            tempNALU.write(currByte);


            endDelimiterFound = (slidingChunkBuffer[0] == 0x00 && slidingChunkBuffer[1] == 0x00 && slidingChunkBuffer[2] == 0x00 && slidingChunkBuffer[3] == 0x01);

            if (endDelimiterFound) {
                byte[] temp_buff = tempNALU.toByteArray();
                ByteArrayOutputStream newNALU = new ByteArrayOutputStream();

                for (int i = 0; i < temp_buff.length - 4; i++) {
                    newNALU.write(temp_buff[i]);
                }

                nalu_Queue.offer(newNALU.toByteArray());
                videoDecoderThread.sendNewNaluMessage();
                //tempNALU = new ByteArrayOutputStream();
                tempNALU.reset();
                tempNALU.write(slidingChunkBuffer);

            }

            currByte = readByteAndUpdateSlidingBufferDelimiter();

        }


    }



        /*

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
        */


    public Handler getHandler() {
        return pickerHandler;
    }

    public void sendNewChunkMessage() {


        Message newChunkMessage = new Message();
        newChunkMessage.what = NEW_CHUNK_AVAILABLE;
        pickerHandler.sendMessage(newChunkMessage);

    }

}
