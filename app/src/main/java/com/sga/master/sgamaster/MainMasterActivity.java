package com.sga.master.sgamaster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.java_websocket.drafts.Draft_10;

import java.net.URI;
import java.net.URISyntaxException;

public class MainMasterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_master);


        try {
            StreamClient client = new StreamClient( new URI( "ws://localhost:8887" ), new Draft_10() ); // more about drafts here: http://github.com/TooTallNate/Java-WebSocket/wiki/Drafts
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }
}
