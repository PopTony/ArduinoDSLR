package tonylu.fyp_wifi_server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.net.Socket;

public class SuggestionActivity extends AppCompatActivity implements Constants {

    Button loadImageButton;
    private TCPService mService;
    private boolean mBound;
    public Socket acceptedSocket;

    private ServiceConnection mConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TCPService.LocalBinder binder = (TCPService.LocalBinder) service;
            mService = binder.getService();
            System.out.println("SuggestionActivity Activity binded to TCPService");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("Service disconnected");
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadImageButton = (Button) findViewById(R.id.loadImage);

        Intent tcpServiceIntent = new Intent(this, TCPService.class);
        bindService(tcpServiceIntent, mConnection, Context.BIND_AUTO_CREATE );

        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendTCPCommand(COMMAND_LOAD);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendTCPCommand(String command) throws IOException {

        acceptedSocket = mService.getClientSocket();
        if (acceptedSocket != null ) {
            if (acceptedSocket.isConnected()) {
                new TCPCom(this).execute(command);
            }
        }
        else{
            mService.connectToClient();
        }
    }




}
