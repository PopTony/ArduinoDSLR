package tonylu.fyp_wifi_server;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.Socket;

public class CalibrateActivity extends AppCompatActivity implements Constants {



    public SeekBar exposureTimeBar;
    public SeekBar fNumberBar;
    public TextView textValueShutterSpeed, textValueFnumber, clientStatus;
    public boolean connectionResult;

    public Socket acceptedSocket;
    public TCPService mService;
    public boolean mBound = true;

    public ServiceConnection mConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TCPService.LocalBinder binder = (TCPService.LocalBinder) service;
            mService = binder.getService();
            System.out.println("Calibrate Activity binded to TCPService");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);
        clientStatus = (TextView) findViewById(R.id.clientStatus);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textValueShutterSpeed = (TextView) findViewById(R.id.shutterSpeedValue);
        textValueFnumber = (TextView) findViewById(R.id.FNumberValue);

        Intent tcpServiceIntent = new Intent(this, TCPService.class);
        bindService(tcpServiceIntent, mConnection, Context.BIND_AUTO_CREATE );


        exposureTimeBar = (SeekBar) findViewById(R.id.ShutterSpeed);
        //default value is 0

        exposureTimeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {


                    int progress_value;
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                        progress_value = i;
                        textValueShutterSpeed.setText(EXPOSURETIME[i]);
                        // x means exposure, followed by number of digits, followed by digits
                        //System.out.println("X"+ String.valueOf(i).length()+ String.valueOf(i));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        try {
                            sendTCPCommand("X"+ String.valueOf(progress_value).length()+ String.valueOf(progress_value));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("X"+ String.valueOf(progress_value).length()+ String.valueOf(progress_value));
                    }
                }
        );

        fNumberBar = (SeekBar) findViewById(R.id.fNumber);

        fNumberBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    int progress_value;
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        progress_value = i;
                        textValueFnumber.setText(FNUMBER[i]);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        try {
                            sendTCPCommand("F"+ String.valueOf(progress_value).length()+ String.valueOf(progress_value));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("F"+ String.valueOf(progress_value).length()+ String.valueOf(progress_value));
                    }
                }

        );


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




    public void sendTCPCommand(String command) throws IOException{

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

    private class ServerConnectionTask extends AsyncTask<Void, Void,Void> {

        private ProgressDialog mProgressDialog;

        protected void onPreExecute() {
            clientStatus.setText("Client:\n" + "Disconnected");
            mProgressDialog = new ProgressDialog(CalibrateActivity.this);
            mProgressDialog
                    .setMessage("Server is looking for client & accepting, please wait for a moment...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            connectionResult = false;
        }

        protected Void doInBackground(Void... params) {
            mService.connectToClient();
            int count = 0;
            Socket as;
            while(count<=10){
                as = mService.getClientSocket();
                if (as!=null && as.isConnected()){
                    connectionResult = true;
                    System.out.println("Connection Result true");
                    return null;
                }
                count ++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mProgressDialog.dismiss();
            if(connectionResult)
            {

                clientStatus.setText("Client:\n"+"Connected");
                //alertCenter("Web socket connect suc");
            }
            else
            {
                clientStatus.setText("Client:\n"+"Disconnected");
                //alertCenter("Web socket connect fail");
            }
        }

    }
}
