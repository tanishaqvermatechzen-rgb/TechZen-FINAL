#include "audio_io.h"
#include "config.h"
#include "bluetooth_handler.h"
#include "driver/i2s.h"

static bool recordingActive = false;
static const int audio_buffer_size = 512; // PCM sample buffer size
static int16_t mic_buffer[audio_buffer_size];

bool initAudio() {
    // 1. Configure I2S Port 0 for INMP441 Microphone (RX)
    i2s_config_t mic_i2s_config = {
        .mode = (i2s_mode_t)(I2S_MODE_MASTER | I2S_MODE_RX),
        .sample_rate = SAMPLE_RATE,
        .bits_per_sample = I2S_BITS_PER_SAMPLE_16BIT,
        .channel_format = I2S_CHANNEL_FMT_ONLY_LEFT, // INMP441 is mono
        .communication_format = I2S_COMM_FORMAT_STAND_I2S,
        .intr_alloc_flags = ESP_INTR_FLAG_LEVEL1,
        .dma_buf_count = 8,
        .dma_buf_len = 64,
        .use_apll = false,
        .tx_desc_auto_clear = false,
        .fixed_mclk = 0
    };

    i2s_pin_config_t mic_pin_config = {
        .bck_io_num = I2S_MIC_SCK,
        .ws_io_num = I2S_MIC_WS,
        .data_out_num = I2S_PIN_NO_CHANGE,
        .data_in_num = I2S_MIC_SD
    };

    // Install and select pins for mic
    if (i2s_driver_install(I2S_NUM_0, &mic_i2s_config, 0, NULL) != ESP_OK) {
        Serial.println("Failed to install I2S RX (Mic) driver!");
        return false;
    }
    if (i2s_set_pin(I2S_NUM_0, &mic_pin_config) != ESP_OK) {
        Serial.println("Failed to set I2S RX pins!");
        return false;
    }

    // 2. Configure I2S Port 1 for MAX98357A DAC (TX)
    i2s_config_t dac_i2s_config = {
        .mode = (i2s_mode_t)(I2S_MODE_MASTER | I2S_MODE_TX),
        .sample_rate = SAMPLE_RATE,
        .bits_per_sample = I2S_BITS_PER_SAMPLE_16BIT,
        .channel_format = I2S_CHANNEL_FMT_ONLY_LEFT, // Speak mono output
        .communication_format = I2S_COMM_FORMAT_STAND_I2S,
        .intr_alloc_flags = ESP_INTR_FLAG_LEVEL1,
        .dma_buf_count = 8,
        .dma_buf_len = 64,
        .use_apll = false,
        .tx_desc_auto_clear = true, // Clears DMA buffer automatically on underflow to avoid noise
        .fixed_mclk = 0
    };

    i2s_pin_config_t dac_pin_config = {
        .bck_io_num = I2S_DAC_BCLK,
        .ws_io_num = I2S_DAC_LRC,
        .data_out_num = I2S_DAC_DIN,
        .data_in_num = I2S_PIN_NO_CHANGE
    };

    // Install and select pins for DAC
    if (i2s_driver_install(I2S_NUM_1, &dac_i2s_config, 0, NULL) != ESP_OK) {
        Serial.println("Failed to install I2S TX (DAC) driver!");
        return false;
    }
    if (i2s_set_pin(I2S_NUM_1, &dac_pin_config) != ESP_OK) {
        Serial.println("Failed to set I2S TX pins!");
        return false;
    }

    Serial.println("I2S audio initialized successfully!");
    return true;
}

void playSpeakerAudio(const uint8_t* data, size_t len) {
    size_t bytes_written = 0;
    
    // Apply software gain multiplier to PCM 16-bit samples
    if (len % 2 == 0) {
        int16_t* samples = (int16_t*)data;
        size_t sampleCount = len / 2;
        for (size_t i = 0; i < sampleCount; i++) {
            int32_t val = (int32_t)(samples[i] * SPEAKER_GAIN_MULT);
            if (val > 32767) val = 32767;
            else if (val < -32768) val = -32768;
            samples[i] = (int16_t)val;
        }
    }

    // Write PCM data to I2S Port 1 (DAC)
    i2s_write(I2S_NUM_1, data, len, &bytes_written, portMAX_DELAY);
}

void updateAudioRecording() {
    if (!recordingActive) return;

    size_t bytes_read = 0;
    // Read from I2S Port 0 (Mic)
    esp_err_t result = i2s_read(I2S_NUM_0, &mic_buffer, audio_buffer_size * sizeof(int16_t), &bytes_read, 10 / portTICK_PERIOD_MS);
    
    if (result == ESP_OK && bytes_read > 0) {
        size_t samplesRead = bytes_read / sizeof(int16_t);
        // Apply Noise Gate Threshold filter
        for (size_t i = 0; i < samplesRead; i++) {
            if (abs(mic_buffer[i]) < MIC_NOISE_THRESHOLD) {
                mic_buffer[i] = 0;
            }
        }
        
        // Stream the processed PCM samples to the phone app
        btHandler.sendAudioChunk((const uint8_t*)mic_buffer, bytes_read);
    }
}

void startRecording() {
    if (recordingActive) return;
    recordingActive = true;
    // Clear DMA RX buffers to avoid old voice residue
    i2s_zero_dma_buffer(I2S_NUM_0);
    Serial.println("Voice capture started.");
}

void stopRecording() {
    recordingActive = false;
    Serial.println("Voice capture stopped.");
}

bool isRecording() {
    return recordingActive;
}
