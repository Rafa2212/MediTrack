# MediTrack Project Guidelines

## Project Overview
MediTrack is an Android application designed to help users track and manage their health conditions. The application integrates with Fitbit API to collect health metrics and provides personalized health insights and recommendations based on the user's profile, diseases, and health data.

## Project Structure
- **app/src/main/java/com/example/myapplication**: Contains all Java source files
  - **User Management**:
    - `User.java`: Represents a user with their profile
    - `UserProfile.java`: Contains user information and health metrics
    - `LoginActivity.java`: Handles user authentication
    - `ProfileSetupActivity.java`: Allows users to set up their profile
  - **Disease Management**:
    - `Disease.java`: Represents a disease with ICD-10 code
    - `AddDiseaseActivity.java`: Allows users to add and manage diseases
    - `DiseaseAdapter.java`: Adapter for displaying diseases in RecyclerView
  - **Health Tracking**:
    - `WeeklyReportActivity.java`: Generates weekly health reports
    - `FitbitAPI.java`: Integrates with Fitbit to collect health metrics
  - **Database**:
    - `DatabaseHelper.java`: Manages SQLite database operations
    - `Session.java`: Manages user sessions
    - `TokenData.java`: Handles authentication tokens
  - **Base Components**:
    - `BaseActivity.java`: Base activity with common functionality

## Functionality
1. **User Authentication**: Users can register and log in to the application
2. **Profile Management**: Users can set up and update their profile with personal information
3. **Disease Tracking**: Users can add and manage their diseases using ICD-10 codes
4. **Health Metrics**: The application collects health metrics from Fitbit, including:
   - Steps, sedentary minutes, active zone minutes
   - Sleep data (deep sleep, light sleep, REM sleep)
   - Breathing rate, heart rate variability (RMSSD)
   - VO2Max and other fitness metrics
5. **Weekly Reports**: The application generates personalized weekly health reports based on the user's profile, diseases, and health metrics
6. **AI Integration**: Uses OpenAI services for generating health insights and recommendations

## Testing Guidelines
When testing the application, focus on the following areas:
1. **User Authentication**: Test registration, login, and session management
2. **Profile Management**: Test creating and updating user profiles
3. **Disease Management**: Test adding, viewing, and deleting diseases
4. **Health Metrics**: Test integration with Fitbit API and collection of health metrics
5. **Weekly Reports**: Test generation of weekly health reports

## Build Instructions
1. The project uses Gradle for building. Use the standard Android build process.
2. Before submitting any changes, ensure that the application builds successfully.
3. Test your changes on different Android versions to ensure compatibility.

## Code Style Guidelines
1. Follow Java naming conventions:
   - Classes: PascalCase (e.g., `UserProfile`)
   - Methods and variables: camelCase (e.g., `getUserProfile()`)
   - Constants: UPPER_SNAKE_CASE (e.g., `TABLE_USERS`)
2. Add appropriate comments for complex logic
3. Keep methods focused on a single responsibility
4. Use proper error handling with try-catch blocks
5. Follow Android best practices for UI components and lifecycle management

## Additional Notes
- The application uses SQLite for local data storage
- The application integrates with external APIs (Fitbit, OpenAI)
- The application uses ICD-10 codes for standardized disease classification