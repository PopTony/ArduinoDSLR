package tonylu.fyp_wifi_server;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements Constants {





    public boolean connectionResult = false;
    private SeekBar exposureTimeBar;
    private SeekBar fNumberBar;

    private TCPService mService;
    private boolean mBound;

    TextView textStatus, textRX, textValueShutterSpeed, textValueFnumber, clientStatus;
    public Button loadImageButton , connectButton, captureButton,socketStatus , checkButton, calibrateButton,suggestionButton , listenConnectionButton;

    public Socket acceptedSocket;
    private Intent calibrateIntent,suggestionIntent;


    private ServiceConnection mConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TCPService.LocalBinder binder = (TCPService.LocalBinder) service;
            mService = binder.getService();
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
        setContentView(R.layout.activity_main);

        Intent tcpServiceIntent = new Intent(this, TCPService.class);
        startService(tcpServiceIntent);
        bindService(tcpServiceIntent, mConnection, Context.BIND_AUTO_CREATE );


        //textStatus = (TextView) findViewById(R.id.Progress);

        clientStatus = (TextView) findViewById(R.id.clientStatus);
        //textStatus = (TextView)findViewById(R.id.textStatus);
        //textRX = (TextView)findViewById(R.id.Progress);
        captureButton = (Button) findViewById(R.id.captureButton);
        suggestionButton =  (Button) findViewById(R.id.Suggestion);
        listenConnectionButton = (Button) findViewById(R.id.listenConnection);

        loadImageButton = (Button)findViewById(R.id.loadImage);
        //connectButton = (Button) findViewById(R.id.Connect);
       // socketStatus = (Button) findViewById(R.id.socketSatus);

        calibrateButton = (Button) findViewById(R.id.calibrate);

        calibrateIntent = new Intent(this, CalibrateActivity.class );

        suggestionIntent = new Intent(this, SuggestionActivity.class );


        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(calibrateIntent);
            }
        });

        suggestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(suggestionIntent);
            }
        });



        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendTCPCommand(COMMAND_CAPTURE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendTCPCommand(COMMAND_IMAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        listenConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Service Bound: "+ mBound);
                new ServerConnectionTask().execute();
            }
        });

//        socketStatus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    makeRequest();
//                } catch (IOException e) {
//                    e.printStackTrace();
 //               } catch (JSONException e) {
 //                   e.printStackTrace();
 //               }
 //           }
 //       });


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

    public void makeRequest() throws IOException, JSONException {

        // Instantiate the RequestQueue.
        //mTxtDisplay = (TextView) findViewById(R.id.txtDisplay);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://172.21.145.206:5005/test";
        JSONObject at = new JSONObject();

        at.put("Check",80);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, at, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //mTxtDisplay.setText("Response: " + response.toString());
                        System.out.println("Received Response: ");
                        System.out.println(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        System.out.println("Received Response: ");
                        System.out.println(error.toString());

                    }
                });
        queue.add(jsObjRequest);
    }


    private class ServerConnectionTask extends AsyncTask<Void, Void,Void> {

        private ProgressDialog mProgressDialog;

        protected void onPreExecute() {
            clientStatus.setText("Client:\n" + "Disconnected");
            mProgressDialog = new ProgressDialog(MainActivity.this);
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
