#include <Arduino.h>
#include "config.h"
#include "audio_io.h"
#include "bluetooth_handler.h"

// --- Global State ---
volatile bool buzzingActive = false;
unsigned long lastBuzzToggle = 0;
bool buzzerState = false;
const unsigned long buzzInterval = 500; // Buzz toggle speed (ms)

// Button debouncing state
unsigned long lastButtonPress = 0;
const unsigned long debounceDelay = 200; // ms

// Function called by bluetooth handler when phone requests buzz state change
void setBuzzingState(bool active) {
    buzzingActive = active;
    if (!active) {
        digitalWrite(BUZZER_PIN, LOW); // Ensure buzzer is off
        noTone(BUZZER_PIN);
    }
    Serial.printf("Buzzer state set to: %s\n", active ? "ACTIVE" : "INACTIVE");
}

// Interrupt Service Routine (ISR) for the physical button
void IRAM_ATTR handleButtonPress() {
    unsigned long now = millis();
    if (now - lastButtonPress > debounceDelay) {
        lastButtonPress = now;
        
        if (buzzingActive) {
            // Stop buzzing immediately
            buzzingActive = false;
        } else {
            // Toggle voice capture if not buzzing
            if (isRecording()) {
                stopRecording();
                tone(BUZZER_PIN, TONE_STOP_REC_FREQ, 80);
            } else {
                startRecording();
                tone(BUZZER_PIN, TONE_START_REC_FREQ, 60);
            }
        }
    }
}

void setup() {
    Serial.begin(SERIAL_BAUD);
    Serial.println("Starting True Virtual Assistant Hardware...");

    // Configure pins
    pinMode(BUZZER_PIN, OUTPUT);
    digitalWrite(BUZZER_PIN, LOW);
    
    pinMode(BUTTON_PIN, INPUT_PULLUP);
    // Attach interrupt to button pin (falling edge since it has pull-up)
    attachInterrupt(digitalPinToInterrupt(BUTTON_PIN), handleButtonPress, FALLING);

    // Initialize Bluetooth
    if (btHandler.begin(DEVICE_NAME)) {
        Serial.printf("Bluetooth SPP started. Device name: %s\n", DEVICE_NAME);
    } else {
        Serial.println("Failed to start Bluetooth SPP!");
    }

    // Initialize I2S Audio
    if (!initAudio()) {
        Serial.println("Hardware audio system error! Check pin connections.");
    }
}

void loop() {
    // 1. Process incoming bluetooth data and stream audio output
    btHandler.update();

    // 2. Stream mic audio to phone if voice capture is active
    if (isRecording()) {
        updateAudioRecording();
    }

    // 3. Handle buzzing logic (non-blocking beep pattern)
    if (buzzingActive) {
        unsigned long currentMillis = millis();
        if (currentMillis - lastBuzzToggle >= buzzInterval) {
            lastBuzzToggle = currentMillis;
            buzzerState = !buzzerState;
            
            if (buzzerState) {
                // Sound a 2.5kHz beep tone on the piezo buzzer
                tone(BUZZER_PIN, 2500); 
            } else {
                noTone(BUZZER_PIN);
            }
        }
    }

    // 4. Handle connection status indicators (optional logging)
    static bool wasConnected = false;
    bool isConnected = btHandler.isConnected();
    if (isConnected != wasConnected) {
        wasConnected = isConnected;
        if (isConnected) {
            Serial.println("Phone connected via Bluetooth!");
            // Short chirp to signal connection
            tone(BUZZER_PIN, 3000, 100);
        } else {
            Serial.println("Phone disconnected. Waiting for connection...");
            setBuzzingState(false);
            stopRecording();
        }
    }

    // Small delay to prevent watchdog issues and reduce power consumption
    delay(1);
}
