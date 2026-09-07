#include "bluetooth_handler.h"
#include "audio_io.h"
#include "config.h"

extern void setBuzzingState(bool active);

BluetoothHandler btHandler;

BluetoothHandler::BluetoothHandler() {}

bool BluetoothHandler::begin(const char* deviceName) {
    return SerialBT.begin(deviceName);
}

bool BluetoothHandler::isConnected() {
    return SerialBT.hasClient();
}

void BluetoothHandler::sendAudioChunk(const uint8_t* data, size_t len) {
    if (!isConnected()) return;
    
    // Packet: [Header][Length High][Length Low][Data...]
    SerialBT.write(PKT_AUDIO_MIC);
    SerialBT.write((len >> 8) & 0xFF);
    SerialBT.write(len & 0xFF);
    SerialBT.write(data, len);
}

void BluetoothHandler::sendCommand(uint8_t cmd) {
    if (!isConnected()) return;
    SerialBT.write(cmd);
}

void BluetoothHandler::update() {
    if (!isConnected()) return;
    
    while (SerialBT.available() > 0) {
        uint8_t header = SerialBT.read();
        
        switch (header) {
            case PKT_START_BUZZ:
                setBuzzingState(true);
                break;
                
            case PKT_STOP_BUZZ:
                setBuzzingState(false);
                break;
                
            case PKT_AUDIO_SPK: {
                // Read 2 bytes length
                if (SerialBT.available() < 2) {
                    // Wait briefly for length bytes
                    unsigned long start = millis();
                    while (SerialBT.available() < 2 && millis() - start < 10) {
                        delay(1);
                    }
                }
                
                if (SerialBT.available() >= 2) {
                    uint8_t lenHigh = SerialBT.read();
                    uint8_t lenLow = SerialBT.read();
                    uint16_t len = (lenHigh << 8) | lenLow;
                    
                    // Read 'len' bytes of PCM data
                    uint8_t* buffer = (uint8_t*)malloc(len);
                    if (buffer) {
                        size_t bytesRead = 0;
                        unsigned long start = millis();
                        while (bytesRead < len && millis() - start < 100) {
                            if (SerialBT.available() > 0) {
                                buffer[bytesRead++] = SerialBT.read();
                            } else {
                                delay(1);
                            }
                        }
                        
                        if (bytesRead == len) {
                            playSpeakerAudio(buffer, len);
                        }
                        free(buffer);
                    }
                }
                break;
            }
            
            case PKT_PING:
                // Keepalive/Ack, no action needed or could echo back
                break;
                
            default:
                // Unknown packet header, flush buffer to resync
                while (SerialBT.available() > 0) {
                    SerialBT.read();
                }
                break;
        }
    }
}
