package tonylu.fyp_wifi_server;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.net.Socket;

/**
 * Created by TonyLu on 24/3/18.
 */

public class TCPService extends Service {

    private final IBinder mBinder = new LocalBinder();
    public Socket acceptedSocket = null;
    public boolean connectionResult = false;


    public class LocalBinder extends Binder{
        TCPService getService(){
            return TCPService.this;
        }
    }

    @Nullable
    @Override

    public IBinder onBind(Intent intent) {
        return mBinder;
    }




    public int onStartCommand(Intent intent, int flags, int startId){

        return START_STICKY;
    }

    public Socket getClientSocket(){
        return acceptedSocket;
    }

    public void connectToClient(){
        new TCPServer(this).run();
    }

}
