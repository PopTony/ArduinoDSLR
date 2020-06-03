package tonylu.fyp_wifi_server;
import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;


/**
 * Created by TonyLu on 15/3/18.
 */

public class TCPCom extends AsyncTask<String,String,String> implements Constants{


    ImageView imgV;
    BufferedReader rx;
    OutputStream tx;
    String fileDir;
    String Command;
    TextView tv;
    char[] imageHex = new char[10000000];
    int imageSize = 0;

    Socket socket;
    Activity ac;
    String imageHexStr = "";

    public TCPCom(MainActivity a) {
        ac = a;
        //tv = (TextView) a.findViewById(R.id.Progress);
        //tv.setText("");
        socket = a.acceptedSocket;
    }

    public TCPCom(CalibrateActivity a) {

        ac = a;
        //tv = (TextView) a.findViewById(R.id.Progress);
        //tv.setText("");
        socket = a.acceptedSocket;
    }

    public TCPCom(SuggestionActivity a) {

        ac = a;
        //tv = (TextView) a.findViewById(R.id.Progress);
        //tv.setText("");
        socket = a.acceptedSocket;
    }


    protected String doInBackground(String... inputs )  {

        String command = inputs[0];
        Command = command;

        if (socket == null){
            return "";
        }
        if (socket!=null){

            try {
                socket.setKeepAlive(true);
                socket.setOOBInline(true);
                rx = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                tx = socket.getOutputStream();
                fileDir = ac.getFilesDir().getPath();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            publishProgress("Socket rxtx done");


            try {
                if (tx!= null){
                    tx.write(command.getBytes());
                    publishProgress("Send Command: "+command);

                }
                else {

                    publishProgress("TX is null");
                }

            } catch (IOException e) {
                publishProgress("Unable to write");
                e.printStackTrace();
            }

            if (command.equals(COMMAND_CAPTURE)){

                int pos = 0;
                int read = -1;
                try {

                    if (rx== null){
                        publishProgress("RX is null");
                        return "";
                    }

                    imgV = (ImageView)ac.findViewById(R.id.imageView);

                    //String notice = rx.readLine(); // wait until image capture received
                    //System.out.println(notice);
                    //String tmp;
                    Thread.sleep(6000);
                    //tmp = rx.readLine();
                    //while(tmp != null){
                    //    System.out.println("Chars of "+ tmp.length() + " collected");
                    //    imageHexStr = imageHexStr + tmp;
                    //    tmp = rx.readLine();
                    //    Thread.sleep(1000);
                    //}

                    while (socket.getInputStream().available()>0) {
                        read = rx.read(imageHex,pos,1000000);

                        Thread.sleep(5000);
                        pos += read;

                        System.out.println("processing "+ read + " chars" + " of total "+ pos);
                        //publishProgress(line);
                    }
                    imageSize = pos;

                    //imageSize = imageHexStr.length();
                    System.out.println("Total length of "+ imageSize+ " chars collected");
                    publishProgress("Image Hex size of " + imageSize +" collected");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

            if (command.equals(COMMAND_IMAGE)) {

                int pos = 0;
                int read = -1;
                try {

                    if (rx== null){
                        publishProgress("RX is null");
                        return "";
                    }

                    imgV = (ImageView)ac.findViewById(R.id.imageView);

                    //String tmp;
                    Thread.sleep(2000);
                    //tmp = rx.readLine();
                    //while(tmp != null){
                    //    System.out.println("Chars of "+ tmp.length() + " collected");
                    //    imageHexStr = imageHexStr + tmp;
                    //    tmp = rx.readLine();
                    //    Thread.sleep(1000);
                    //}

                    while (socket.getInputStream().available()>0) {
                        read = rx.read(imageHex,pos,1000000);

                        Thread.sleep(5000);
                        pos += read;

                        System.out.println("processing "+ read + " chars" + " of total "+ pos);
                        //publishProgress(line);
                    }
                    imageSize = pos;

                    //imageSize = imageHexStr.length();
                    System.out.println("Total length of "+ imageSize+ " chars collected");
                    publishProgress("Image Hex size of " + imageSize +" collected");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            else{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
        return "" ;
    }

    protected void onProgressUpdate( String... v){

        //tv.setText(tv.getText() + "\n"+v[0]);
    }

    protected void onPostExecute(String v){

        if (Command.equals(COMMAND_IMAGE ) || Command.equals(COMMAND_CAPTURE )){

            if (imageSize >500){

                //System.out.println("Start");
                //System.out.println(imageHexStr.substring(0, 100));
                //System.out.println("End");
                //System.out.println(imageHexStr.substring(imageSize-100, imageSize));
                //ImageProc imp = new ImageProc(imageHexStr,imgV);

                imageHex = Arrays.copyOfRange(imageHex,0,imageSize);
                System.out.println("Start");
                System.out.println( Arrays.copyOfRange(imageHex,0,1000));
                System.out.println("End");
                System.out.println( Arrays.copyOfRange(imageHex,imageSize-1000,imageSize));
                ImageProc imp = new ImageProc(String.valueOf(imageHex),imgV);


                try {
                    String fileName = fileDir+"/last.jpg";
                    imp.imageWriteAndDisplay(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }



}
