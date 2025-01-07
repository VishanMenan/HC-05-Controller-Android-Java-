# Bluetooth Boat Controller App (Java & Android SDK)
This Android app allows you to control a Bluetooth-enabled boat using the HC-05 Bluetooth module. You can move the boat in four directions (forward, backward, left, right) and monitor the boat’s surroundings using two ultrasonic sensors to detect obstacles.

## Features:
- **Bluetooth Connectivity**: Connects to a Bluetooth HC-05 module to control the boat.
- **Distance Measurement**: Real-time display of left and right sensor distances.
- **User Controls**: Forward, backward, left and right movement buttons.
- **Connection Info**: Displays connection status and strength.
- **Dynamic Feedback**: Changes UI color when the distance is close to obstacles and activates vibrations for feedback.
  
## Technologies Used
- **Programming Language**: Java
- **Platform**: Android
- **Microcontroller**: Arduino Mega 2560
- **Bluetooth**: HC-05 Bluetooth module for communication
- **Sensors**: Ultrasonic sensors for distance measurement
- **Android SDK**: Used for building the app
  
## Requirements:
- Android Phone with Bluetooth support
- HC-05 Bluetooth module
- Ultrasonic sensors for obstacle detection
  
## Android SDK Version
This app is built using Android SDK version 21 (Android 5.0). It should work well on devices running Android 5.0 (Lollipop) and above.

## Installation:
1. Clone this repository to your local machine.
   ```bash
   git clone https://github.com/VishanMenan/HC-05-Controller-Android-Java-.git
2. Open the project in **Android Studio**.
3. Connect your phone via USB and enable developer options and USB debugging.
4. Build and run the app on your phone.

## Improvements
Some potential improvements for the project include:
- **Extended Connectivity**: Upgrade to better connection modules for faster data transfer and long-range connectivity.
- **Camera Integration**: Implement a camera module for visual recognition and can be programmed to detect cracks or damages on walls when moving near walls in a port.
- **Boat Propulsion**: Add a high-efficiency motor and a proper boat propeller for smoother and more controlled boat movement.
- **Boat Design**: Consider improving the boat's hull with a 3D-printed boat hull to reduce drag and increase speed.

