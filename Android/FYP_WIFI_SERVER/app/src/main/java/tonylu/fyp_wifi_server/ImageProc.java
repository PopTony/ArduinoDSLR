package tonylu.fyp_wifi_server;

/**
 * Created by TonyLu on 15/3/18.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by TonyLu on 4/3/18.
 */



public class ImageProc {

    String imageHex;
    byte[] imageByte;

    ImageView imageV;

    ImageProc(String hex, ImageView imgv){
        imageHex = hex;
        imageByte = hexStringToByteArray(hex);
        imageV = imgv;

    }

    public void imageWriteAndDisplay(String fileName) throws IOException {


        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
        bos.write(imageByte);
        bos.flush();
        bos.close();
        Bitmap bitmap = BitmapFactory.decodeFile(fileName);

        imageV.setImageBitmap(bitmap);
    }
    public byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            //System.out.println(s.substring(index, index + 2));
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
}
