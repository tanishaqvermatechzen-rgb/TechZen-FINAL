#ifndef CONFIG_H
#define CONFIG_H

// --- System Configuration ---
#define DEVICE_NAME "HomeHub"
#define SERIAL_BAUD 115200

// --- I2S Microphone Pins (INMP441) ---
#define I2S_MIC_SCK  14   // Serial Clock (SCK) -> GPIO 14
#define I2S_MIC_WS   15   // Word Select (WS)   -> GPIO 15
#define I2S_MIC_SD   32   // Serial Data (SD)   -> GPIO 32

// --- I2S DAC Pins (MAX98357A) ---
#define I2S_DAC_BCLK 26   // Bit Clock (BCLK)  -> GPIO 26
#define I2S_DAC_LRC  25   // Left/Right (LRC)   -> GPIO 25
#define I2S_DAC_DIN  22   // Data In (DIN)      -> GPIO 22

// --- Buzzer and Alarms ---
#define BUZZER_PIN   4    // Active buzzer      -> GPIO 4
#define BUTTON_PIN   5    // Alarm button       -> GPIO 5

// --- Audio Processing & Quality Tuning ---
#define SAMPLE_RATE         16000  // 16kHz voice standard
#define BITS_PER_SAMPLE     16     // 16-bit PCM
#define CHANNELS            1      // Mono
#define SPEAKER_GAIN_MULT   1.4f   // Software gain multiplier for MAX98357A speaker DAC
#define MIC_NOISE_THRESHOLD 120    // Noise gate threshold to cut static microphone hiss

// --- Feedback Tone Frequencies ---
#define TONE_CONNECT_FREQ   3000   // Hz
#define TONE_START_REC_FREQ 2800   // Hz
#define TONE_STOP_REC_FREQ  1800   // Hz

#endif // CONFIG_H
