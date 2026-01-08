🎾 SmartServe Ball

Tennis Training Analytics with Wearable Sensor

SmartServe Ball is a combined hardware + Android application for analyzing tennis training sessions.
Using a wearable IMU sensor and a mobile app, the system captures motion data during play, detects hits, and stores training sessions for later review.

🧠 Sensor Firmware (Hardware)

The hardware component is an Arduino-based firmware running on a Seeed Studio XIAO nRF52840 Sense.

What the sensor does

Measures acceleration, gyroscope, and temperature data

Detects motion patterns related to tennis strokes

Streams sensor data wirelessly using Bluetooth Low Energy (BLE) to the Android app

📱 Android App (Software)

The Android application is built using Jetpack Compose and acts as the main user interface for training sessions.

Main features

Connects to the BLE sensor

Displays real-time training metrics

Records complete training sessions

Saves sessions locally and makes them available in a history view

Allows users to add player name and session notes before training

🧩 How the App Works

Start on the Home Screen

Enter Player Name (optional)

Enter Session Notes (optional)

Connect to the XIAO BLE sensor

Live Training Session

Start recording

View live metrics such as:

Hit count

Power estimates

Spin estimates

Elapsed time

Each detected hit is recorded automatically

Session Summary

When the session ends, a summary is generated

Includes duration, number of hits, and calculated averages

Results / History

View all previous training sessions

See who trained and when

Review saved session data

⚙️ How to Set It Up
🔧 Hardware Setup
1. Connect your XIAO nRF52840 Sense

Plug the board into your computer using USB-C.

2. Install Required Libraries (Arduino IDE)

Make sure the following libraries are installed:

Seeed Arduino LSM6DS3 by Seeed Studio (v2.0.5)

ArduinoBLE by Arduino (v1.3.7 or later)

Seeed nRF52 Boards by Seeed Studio (v1.1.8 or later)

Seeed nRF52 mbed-enabled Boards by Seeed Studio (v2.9.2 or later)

3. Select the Correct Board

Go to:
Tools → Board → Seeed nRF52 mbed-enabled Boards → Seeed XIAO BLE Sense – nRF52840

4. Upload the Firmware

Open the sketch located in the sensor/ folder and click Upload.

5. Power the Device

Via USB: powers automatically

Via battery: disconnect USB after upload to avoid serial conflicts

The onboard LED should blink when active

💻 Android App Setup
1. Open the Project

Open the app/ folder in Android Studio

Recommended version: Android Studio Flamingo or newer

2. Run the App

Connect an Android device or start an emulator

Enable Bluetooth on the device

3. Connect to the Sensor

Scan for nearby BLE devices

Connect to the XIAO sensor

4. Start Training

Enter player name and notes (optional)

Start a live training session

End the session to save it automatically

5. View History

Open the results screen

Browse past training sessions and summaries
