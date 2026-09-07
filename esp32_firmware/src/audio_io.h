#ifndef AUDIO_IO_H
#define AUDIO_IO_H

#include <Arduino.h>

// Initialize I2S microphone and DAC
bool initAudio();

// Play raw PCM audio data on the I2S speaker
void playSpeakerAudio(const uint8_t* data, size_t len);

// Read microphone data and stream to Bluetooth
void updateAudioRecording();

// Control recording state
void startRecording();
void stopRecording();
bool isRecording();

#endif // AUDIO_IO_H
