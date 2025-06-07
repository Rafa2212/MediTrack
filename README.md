# Application Installation and Setup Guide

## Prerequisites

Before you can run this application, make sure you have the following components installed on your system:

- Android Studio or IntelliJ IDEA
- JDK 17 (Java 17)
- Android SDK (will be installed through Android Studio)

## Setup Instructions

### 1. Obtaining an OpenAI API Key

1. Visit the OpenAI website at [https://platform.openai.com/](https://platform.openai.com/)
2. Sign up for an account or log in if you already have one
3. Navigate to the API section by clicking on your profile icon in the top-right corner and selecting "View API keys"
4. Click on "Create new secret key"
5. Give your key a name (optional) and click "Create secret key"
6. Copy your API key immediately and store it securely (you won't be able to view it again)

### 2. Obtaining a Fitbit API Key

1. Visit the Fitbit Developer website at [https://dev.fitbit.com/](https://dev.fitbit.com/)
2. Sign up for a Fitbit account or log in if you already have one
3. Go to "Manage" > "Register an App"
4. Fill in the required information:
   - Application Name: MediTrack (or your preferred name)
   - Description: Brief description of the app
   - Application Website: You can use http://localhost for testing
   - Organization: Your name or organization
   - OAuth 2.0 Application Type: Personal
   - Redirect URL: http://localhost
   - Default Access Type: Read-Only
5. Agree to the terms and click "Register"
6. After registration, you'll be provided with:
   - Client ID
   - Client Secret
7. To get an access token:
   - Go to "OAuth 2.0 tutorial page"
   - Select your application
   - Click "Generate Token"
   - Authorize the requested permissions
   - Copy the access token provided (this is what you'll use in the app)

### 3. Configure Local Properties

Create a file named `local.properties` at the root of the project with the following content:

```
sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

Replace `YourUsername` with your Windows username. If you installed the Android SDK in a different location, use that path instead.

### 4. Create API Token Configuration

Create a Java class file named `TokenData.java` in the `app\src\main\java\com\example\myapplication` directory with the following content:

```java
package com.example.myapplication;

public enum TokenData {
    OPEN_AI_SERVICE_KEY("your-openai-key-here"),
    FITBIT_TOKEN("your-fitbit-token-here");

    private final String token;

    TokenData(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
```

Replace:
- `"your-openai-key-here"` with the OpenAI API key you obtained in step 1
- `"your-fitbit-token-here"` with the Fitbit access token you obtained in step 2

### 5. Opening and Building the Project in Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to and select the MediTrack project folder
4. Wait for the project to sync and index
5. If prompted to update Gradle or any dependencies, follow the prompts to update

### 6. Setting Up the Android SDK

1. In Android Studio, go to File > Settings (or Android Studio > Preferences on macOS)
2. Navigate to Appearance & Behavior > System Settings > Android SDK
3. Make sure the appropriate Android SDK Platform is installed (recommended: Android 13.0 "Tiramisu" or newer)
4. If needed, click on the "SDK Tools" tab and ensure that "Android SDK Build-Tools" and "Android SDK Platform-Tools" are installed
5. Click "Apply" and "OK" to save changes and install any selected components

### 7. Setting Up a Virtual Device

1. In Android Studio, click on the Device Manager icon in the toolbar (or go to Tools > Device Manager)
2. Click on "Create Device"
3. Select a phone device (e.g., Pixel 6) and click "Next"
4. Select a system image (recommended: Android 13.0 or newer) and click "Next"
   - If the system image is not downloaded, click the "Download" link next to it
5. Give your virtual device a name and click "Finish"

### 8. Running the Application

1. Make sure your virtual device is selected in the device dropdown menu in the toolbar
2. Click the green "Run" button (or press Shift+F10)
3. Wait for the app to build and launch on the virtual device
4. If you prefer to use a physical device:
   - Connect your Android device to your computer via USB
   - Enable USB debugging on your device (in Developer options)
   - Select your device from the device dropdown menu and click "Run"

## Important Notes

- Make sure to keep your API keys secure and never commit them to version control
- The `local.properties` and `TokenData.java` files should be added to your `.gitignore` file
- If you encounter connection issues with OpenAI's API, check your network settings and ensure you have proper internet connectivity
- Fitbit tokens expire after a certain period. If you encounter authentication issues, generate a new token
- For development purposes, you might need to renew your Fitbit token regularly
