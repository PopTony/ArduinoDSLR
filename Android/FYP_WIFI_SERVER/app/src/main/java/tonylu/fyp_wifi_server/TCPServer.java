package tonylu.fyp_wifi_server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by TonyLu on 15/3/18.
 */

public class TCPServer extends Thread implements Constants {
    private ServerSocket serverSocket;
    public Socket socketClient;

    private boolean serverDetectionRunning = true;


    TCPService mService = null;
    //CalibrateActivity acCal = null;

    TCPServer( TCPService d){

        mService = d;
    }

    public void setServerLooping(boolean set){

        serverDetectionRunning = set;
    }

    public void run(){
        super.run();

                System.out.println("PA: Connecting...");
                try {
                    serverSocket = new ServerSocket(); // <-- create an unbound socket first
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(SERVERPORT));

                    String ipAddress = null;
                    try {
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                            NetworkInterface intf = en.nextElement();
                            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                if (!inetAddress.isLoopbackAddress()) {
                                    ipAddress = inetAddress.getHostAddress().toString();
                                }
                            }
                        }
                    } catch (SocketException ex) {}


                    System.out.println("Listening connection from "+ ipAddress.toString()+ ":"+SERVERPORT);
                    socketClient = serverSocket.accept();
                    if (socketClient != null){
                        //textStatus.setText("Accepted: "+ socketClient.getInetAddress());
                        mService.acceptedSocket = socketClient;
                        mService.connectionResult = true;


                        //System.out.println(("Accepted: "+ socketClient.getInetAddress().toString());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
    }

}
