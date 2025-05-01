# Application Installation and Setup Guide

## Prerequisites

Before you can run this application, make sure you have the following components installed on your system:

- JDK 17 (Java 17)
- Gradle 8.2.0

## Setup Instructions

### 1. Configure Local Properties

Create a file named `local.properties` at the root of the project with the following content:

```
sdk.dir=your-sdk-dir
```

Replace `your-sdk-dir` with the path to your Android SDK installation.

### 2. Create API Token Configuration

Create a Java class file named `TokenData.java` in the `src/main/java/com/example/myapplication` directory with the following content:

```java
package com.example.myapplication;

public enum TokenData {
    OPEN_AI_SERVICE_KEY("your openai key"),
    FITBIT_TOKEN("your fitbit token");
    
    private final String token;
    
    TokenData(String token) {
        this.token = token;
    }
    
    public String getToken() {
        return token;
    }
}
```

Replace `"your openai key"` with your OpenAI API key and `"your fitbit token"` with your Fitbit API token.

### 3. Building the Application

Open a terminal in the project root directory and run:

```
gradle build
```

### 4. Running the Application

After a successful build, you can run the application using:

```
gradle run
```

## Important Notes

- Make sure to keep your API keys secure and never commit them to version control
- The `local.properties` and `TokenData.java` files should be added to your `.gitignore` file
- If you encounter connection issues with OpenAI's API, check your network settings and ensure you have proper internet connectivity
