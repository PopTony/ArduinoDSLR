package tonylu.fyp_wifi_server;

/**
 * Created by TonyLu on 15/3/18.
 */

public interface Constants {

    public static final int SERVERPORT = 5001;
    public static final String COMMAND_CAPTURE= "C";
    public static final String COMMAND_IMAGE= "I";
    public static final String COMMAND_LOAD= "L";
    public static final String ADDRESS= "192.168.4.1";
    public static final int PORT= 80;
    public static final int TIMEOUT= 5000;
    public static final String[] EXPOSURETIME = {
            "1/4000",
            "1/3200",
            "1/2500",
            "1/2000",
            "1/1600",
            "1/1250",
            "1/1000",
            "1/800",
            "1/640",
            "1/500",
            "1/400",
            "1/320",
            "1/250",
            "1/200",
            "1/160",
            "1/125",
            "1/100",
            "1/80",
            "1/60",
            "1/50",
            "1/40",
            "1/30",
            "1/25",
            "1/20",
            "1/15",
            "1/13",
            "1/10",
            "1/8",
            "1/6",
            "1/5",
            "1/4",
            "1/3",
            "1/2.5",
            "1/2",
            "1/1.6",
            "1/1.3",
            "1",
            "1.3",
            "1.6",
            "2",
            "2.5",
            "3",
            "4",
            "5",
            "6",
            "8",
            "10",
            "13",
            "15",
            "20",
            "25",
            "30"
    };

    public static final String[] FNUMBER={
            "4",
            "4.5",
            "5",
            "5.6",
            "6.3",
            "7.1",
            "8",
            "9",
            "10",
            "11",
            "13",
            "14",
            "16",
            "18",
            "20",
            "22"
    };
}
