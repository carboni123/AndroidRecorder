# Voice Recording MVP Android App

This project is a minimal viable product (MVP) for a voice recording service. The app demonstrates the core functionality of capturing audio on an Android device, playing back the recording, uploading the file to a local Flask server, and displaying a server response. 

## Features

- **Record Audio:** Capture voice input using the device microphone.
- **Stop Recording:** End the recording session.
- **Playback:** Listen to the recorded audio file.
- **File Upload:** Send the recorded audio file to a local Flask server via a multipart API call.
- **Display Server Response:** Show the server’s JSON response (e.g., confirming the file upload) in the app.
- **Loading Indicator:** Display a spinner while waiting for the server response.

## Project Structure

- **Android App:**  
  - `MainActivity.kt`: Handles recording, stopping, playback, and sending the file.  
  - `SendFile.kt`: Contains an extension function for Activity to upload the audio file to the server and update the UI with the server’s response.
  - Layout files (`activity_main.xml`): Defines the UI, including buttons for recording, playback, sending, and a TextView to display the server response.

- **Flask Server:**  
  - `server.py`: A simple Flask application that receives the uploaded file, saves it, and returns a JSON response confirming the file receipt.

## Prerequisites

### For the Android App

- [Android Studio](https://developer.android.com/studio)
- Android device or emulator (if using an emulator, note that the special IP `10.0.2.2` is used to reference your host machine)
- Gradle build system

### For the Flask Server

- [Python 3.x](https://www.python.org/downloads/)
- `pip` package manager
- Flask package  
  You can install Flask and other dependencies using:

  ```bash
  pip install flask
