#include <ESP8266WiFi.h>
#include <usbhub.h>
#include <math.h>
#include <ptp.h>
#include <stdlib.h>
#include <stdio.h>
#include <ptpcallback.h>
#include <ptpdebug.h>
#include "ptpobjinfoparser_modified.h"
#include "ptpobjhandleparser.h"

#define ANDROIDBUFFERSIZE 500
#define SERVERBUFFERSIZE 5000

int BUFFERSIZE = 500;

const int httpPort = 5001;
const char* host = "192.168.2.14";
//const char* host = "192.168.2.14";

int httpPortLAB = 5005;
 String hostLAB = "172.21.149.166";

const  uint32_t  exposureTime[52] = {0x00000002, 0x00000003, 0x00000004, 0x00000005, 0x00000006, 0x00000008, 0x0000000A, 0x0000000C, 0x0000000F, 0x00000014, 0x00000019, 0x0000001F, 0x00000028, 0x00000032, 0x0000003E, 0x00000050, 0x00000064, 0x0000007D, 0x000000A6, 0x000000C8, 0x000000FA, 0x0000014D, 0x00000190, 0x000001F4, 0x0000029A, 0x00000301, 0x000003E8, 0x000004E2, 0x00000682, 0x000007D0, 0x000009C4, 0x00000D05, 0x00000FA0, 0x00001388, 0x0000186A, 0x00001E0C, 0x00002710, 0x000032C8, 0x00003E80, 0x00004E20, 0x000061A8, 0x00007530, 0x00009C40, 0x0000C350, 0x0000EA60, 0x00013880, 0x000186A0, 0x0001FBD0, 0x000249F0, 0x00030D40, 0x0003D090, 0x000493E0};

const  uint16_t exposureTimeCode = 0x500D;


const  uint16_t fNumber[16] = { 0x0190, 0x01C2, 0x01F4, 0x0230, 0x0276, 0x02C6, 0x0320, 0x0384, 0x03E8, 0x044C, 0x0514, 0x0578, 0x0640, 0x0708, 0x07D0, 0x0898};
const  uint16_t fNumberCode = 0x5007;

boolean SENDTOANDROID = true;
boolean GETOBJECT = false;
WiFiClient wtClient ;
WiFiClient httpClient;
uint32_t PTPObjHandleParser::last_photo ;
PTPObjHandleParser  prs;

int CAPTUREPAUSE = 2;
const char* EXPOSURETIMEDECIMAL[52] = {
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

//send data to remote server using JSON format, tested
void sendHTTPPost(String sr, int segmentID, String objectID){
     if (!wtClient.connect(hostLAB, httpPortLAB)) {
    Serial.println("connection failed");
    return;
  }
    ESP.wdtFeed();
  Serial.println("Server connected");

    String postContent  =  "{\"dataBuffer\":\""+sr +"\",\"segmentID\":\""+String(segmentID)+"\",\"imageID\":\""+objectID+"\"}"; 
    
    Serial.println("Send to server");
    Serial.println("POST  /buffer HTTP/1.1\r\nHost: " + String(hostLAB) + ":" + String(httpPortLAB) + 
    "\r\nConnection: close\r\nContent-Type: application/json \r\nContent-Length: "+String(postContent.length())+" \r\n\n"+postContent+"\r\n ");
    
    wtClient.println("POST  /buffer HTTP/1.1\r\nHost: " + String(hostLAB) + ":" + String(httpPortLAB) + 
    "\r\nConnection: close\r\nContent-Type: application/json \r\nContent-Length: "+String(postContent.length())+" \r\n\n"+postContent+"\r\n ");
    delay(200);
    wtClient.stop();
    ESP.wdtFeed();
}


// send data back to android
void sendData(String sr, int segmentID, String objectID) {
  if (SENDTOANDROID ){
  Serial.println("Write data: " + String(sr));
  Serial.println("of size " + String(sr.length()));
  Serial.println("Wifi Status " + String(wtClient.status()));
  wtClient.print(sr); 
  }
  else{
    sendHTTPPost(sr, segmentID, objectID);         
  }
  //wtClient.flush();
}


// picture reader to process returned image hex string blocks
class PictureReader : public PTPReadParser {
  public:
    uint16_t pic_size = 0; // the last index
    uint32_t pic_len = 0 ;
    int count = 0;
    int pos = 0;
    String objectID ;
    char dataBuffer[SERVERBUFFERSIZE] ;
    void Parse(const uint16_t len, const uint8_t *pbuf, const uint32_t & offset) override;
    void addToBufferOrSend(String hex);
    void cleanBuffer();
    uint8_t firstLineCheck = 0;

};


void PictureReader::Parse(const uint16_t len, const uint8_t *pbuf, const uint32_t & offset) {
  uint16_t i;
  if (GETOBJECT == false){
      for ( i = 0; i < (len); i++) {
    if (firstLineCheck == 1) {
      addToBufferOrSend(String(pbuf[i], HEX) );
      //Serial.print(' ');
    }
    else if (pbuf[i] == 0xFF &&
             //recognize image signature
             pbuf[i + 1] == 0xD8 &&
             pbuf[i + 2] == 0xFF &&
             pbuf[i + 3] == 0xDB
            ) {

      addToBufferOrSend(String(pbuf[i], HEX) );

      firstLineCheck = 1;
    }
    }
  } 
  else if (GETOBJECT == true){
      for ( i = 0; i < (len); i++) {
    if (firstLineCheck == 1) {
      addToBufferOrSend(String(pbuf[i], HEX) );
      //Serial.print(' ');
    }
    else if (pbuf[i] == 0xFF &&
             //recognize image start signature
             pbuf[i + 1] == 0xD8 
            ) {

      addToBufferOrSend(String(pbuf[i], HEX) );

      firstLineCheck = 1;
    }
    }
  }
  
  ESP.wdtFeed();
}



void PictureReader::addToBufferOrSend(String hex) {
  int len = hex.length();
  if (len == 1) {
    hex = "0" + hex;
  }

  for (int i = 0; i < 2; i++) {
    if (pos == (BUFFERSIZE - 1)) {
      sendData(dataBuffer,count, objectID);
      count++;
      memset(dataBuffer, 0, sizeof(dataBuffer)); //reset char array
      pos = 0;
      //delay(500);
    }
    //Serial.println("Client connection: "+ String(wtClient.connected()));
    //Serial.println( String(hex[i]) + " at pos : " + String(pos) );
    dataBuffer[pos++] = hex[i];
  }
  pic_len = pic_len + 2;
}

void PictureReader::cleanBuffer() {
  Serial.println("Clean Buffer");
  sendData(dataBuffer,count, objectID);
  count = 0;
  memset(dataBuffer, 0, sizeof(dataBuffer)); //reset char array
  //delay(500);
}

PictureReader pr;


class CamStateHandlers : public PTPStateHandlers
{
    enum CamStates { stInitial, stDisconnected, stConnected };
    CamStates stateConnected;

  public:
    CamStateHandlers() : stateConnected(stInitial) {};
    virtual void OnDeviceDisconnectedState(PTP *ptp);
    virtual void OnDeviceInitializedState(PTP *ptp);
} CamStates;


void CamStateHandlers::OnDeviceDisconnectedState(PTP *ptp)
{
  if (stateConnected == stConnected || stateConnected == stInitial)
  {
    stateConnected = stDisconnected;
    E_Notify(PSTR("Camera disconnected\r\n"), 0x80);
  }
}





String readClientMessage(WiFiClient cl ) {
  String message = "";

  while (cl.available() > 0 ) {
    char c = cl.read();
    message +=  c;
  }

  return message;
}

void CamStateHandlers::OnDeviceInitializedState(PTP *ptp)
{
  uint32_t  obj;
  char receivedByte;
  String receivedMessage;
  int tmp, digitLength;
  int index = 0;
  String msg;

  delay(100);// reduce frequency of reading
  if (stateConnected == stDisconnected || stateConnected == stInitial) {
    stateConnected = stConnected;
    Serial.println("Camera connected\r\n");
    //delay(1000);

    if (!wtClient.connect(host, httpPort)) {
           Serial.println("connection failed");
          return;
      }
  }
  else if (stateConnected == stConnected)
  {

    // check WIFI connection 
    if (!wtClient.connected()){
        if (!wtClient.connect(host, httpPort)) {
           Serial.println("Looking for server");
          return;
        }
    }
    Serial.println("Connected to server");

    receivedMessage = readClientMessage(wtClient);
    if(receivedMessage==""){
      return;
      }
    
    ESP.wdtFeed();
    Serial.println("Received Message: ");
    Serial.println(receivedMessage);
    //msg= getWifiContent(receivedMessage);
    msg = receivedMessage;

    //parsed received message from android
    //##################### add char at 0 for testing purpose
    if (msg.charAt(0) == 'C') {
      ptp->CaptureImage();
      //Serial.println(
      //delay(2000);
      Serial.println("Blocking check");
      delay(CAPTUREPAUSE);
       ESP.wdtFeed();
      Serial.println("Image Captured");
      PTPObjHandleParser::last_photo = 0;
      ptp->GetObjectHandles(0xFFFFFFFF, 0, 0, &prs);
      obj = prs.last_photo;
      pr.firstLineCheck = 0;
      pr.pos = 0;
      pr.count = 0;
      ESP.wdtFeed();
      ptp->GetThumb(obj, &pr);  //it is blocking
      //ptp->GetObject(obj,&pr);
      
      pr.cleanBuffer();

      //wtClient.flush();
      Serial.println("Chars Sent " + String(pr.pic_len));
      //reset();
      pr.pic_len = 0 ;
      delay(2000);
      //E_Notify(PSTR("Image Captured \r\n"), 0x80);
    }

    //##################### add char at 0 for testing purpose
    else if (msg.charAt(0) == 'I') {

      PTPObjHandleParser::last_photo = 0;
      ptp->GetObjectHandles(0xFFFFFFFF, 0, 0, &prs);
      obj = prs.last_photo;
      pr.firstLineCheck = 0;
      pr.pos = 0;
      pr.count = 0 ;
      //ptp->GetThumb(obj, &pr);  //it is blocking
      ptp->GetObject(obj,&pr);
      
      pr.cleanBuffer();

      //wtClient.flush();
      Serial.println("Chars Sent " + String(pr.pic_len));
      //reset();
      pr.pic_len = 0 ;

    }
    else if (msg.charAt(0) == 'X') {
      index = 0;
      digitLength = int(msg.charAt(1) - '0');
      //Serial.println(digitLength);
      for (int i = digitLength - 1; i >= 0; i--) {
        index = index + int(msg.charAt(digitLength + 1 - i)  - '0') * pow(10, i);
      }
      //Serial.println(index);
      
      ptp->SetDevicePropValue(exposureTimeCode, exposureTime[index]);

      delay(500);
      Serial.println("Exposure time change to ");
      Serial.println(index);
      Serial.println(String(exposureTime[index], HEX));
      Serial.println("");
      
      if (EXPOSURETIMEDECIMAL[index][1] == '/' ){
        CAPTUREPAUSE = 2;
      }
      else {
        CAPTUREPAUSE = atoi(EXPOSURETIMEDECIMAL[index])+1;
      }
      Serial.println("Capture Pause change to ");
      Serial.println(CAPTUREPAUSE);
    }
    else if (msg.charAt(0) == 'F') {
      //tmp = Serial.read();
      //Serial.println(tmp);
      index = 0;
      digitLength = int(msg.charAt(1) - '0');
      //Serial.println(digitLength);
      for (int i = digitLength - 1; i >= 0; i--) {
        index = index + int(msg.charAt(digitLength + 1 - i) - '0') * pow(10, i);
      }
      //Serial.println(index);
      ptp->SetDevicePropValue(fNumberCode, fNumber[index]);
      delay(500);

      Serial.println("F number change to ");
      Serial.println(index);
      Serial.println(String(fNumber[index], HEX));
    }

    else if (msg.charAt(0) == 'L') {
      BUFFERSIZE = SERVERBUFFERSIZE;
      SENDTOANDROID = false;
      PTPObjHandleParser::last_photo = 0;
      ptp->GetObjectHandles(0xFFFFFFFF, 0, 0, &prs);
      obj = prs.last_photo;
      pr.firstLineCheck = 0;
      pr.pos = 0;
      pr.count = 0;
      pr.objectID = String(obj, HEX);
      GETOBJECT = true;
      ptp->GetObject(obj, &pr);  //it is blocking
      GETOBJECT = false;
      //ptp->GetObject(obj,&pr);
      
      pr.cleanBuffer();

      //wtClient.flush();
      Serial.println("Chars Sent " + String(pr.pic_len));
      //reset();
      pr.pic_len = 0 ;
      SENDTOANDROID = true;
      BUFFERSIZE = ANDROIDBUFFERSIZE;
    }
  }
}


USB      Usb;
USBHub   Hub1(&Usb);
PTP      Ptp(&Usb, &CamStates);

void setup() {
  // put your setup code here, to run once:
  Serial.begin( 115200 );
  //Serial.println(WiFi.softAP("..."));
  Serial.println("");
  Serial.println(WiFi.begin("Your Router Name", "Password") ? "Wifi Ready" : "Wifi Failed!");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("Assigned Local IP " );
  Serial.println(WiFi.localIP());

  ESP.wdtDisable();
  ESP.wdtEnable(8000);

  if (Usb.Init() == -1)
    Serial.println("OSC did not start.");
  
}


void loop() {
  Usb.Task();
  ESP.wdtFeed();
}
