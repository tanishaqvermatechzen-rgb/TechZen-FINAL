#ifndef BLUETOOTH_HANDLER_H
#define BLUETOOTH_HANDLER_H

#include "BluetoothSerial.h"

// Protocol Headers
#define PKT_AUDIO_MIC  0x01
#define PKT_AUDIO_SPK  0x02
#define PKT_START_BUZZ 0x03
#define PKT_STOP_BUZZ  0x04
#define PKT_PING       0x05

class BluetoothHandler {
public:
    BluetoothHandler();
    bool begin(const char* deviceName);
    bool isConnected();
    
    // Write audio chunk to phone
    void sendAudioChunk(const uint8_t* data, size_t len);
    
    // Write a control command
    void sendCommand(uint8_t cmd);
    
    // Process incoming data from phone
    // Returns true if speaker audio was processed
    void update();

private:
    BluetoothSerial SerialBT;
};

extern BluetoothHandler btHandler;

#endif // BLUETOOTH_HANDLER_H
