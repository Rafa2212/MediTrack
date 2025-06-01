package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Main activity that serves as the entry point of the application.
 * This activity initializes the application and displays the main user interface.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Initializes the activity, sets up UI components, and configures the main view.
     * This method is called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
