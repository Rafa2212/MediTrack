package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.material.snackbar.Snackbar;

import static android.content.ContentValues.TAG;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private DatabaseHelper dbHelper;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 1; // Request code for sign-in.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = DatabaseHelper.getInstance(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        // Building the GoogleSignInClient with the options specified by gso.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> signIn());

        buttonLogin.setOnClickListener(v -> login());

        buttonRegister.setOnClickListener(v -> register());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        //updateUI(user);
                    } else {
                        // If sign in fails
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }

    private void login() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Snackbar
                    .make(findViewById(android.R.id.content), "Please enter both username and password",
                            Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        User user = dbHelper.checkUser(username, password);

        if (user != null) {
            Snackbar.make(findViewById(android.R.id.content), "Login successful", Snackbar.LENGTH_SHORT)
                    .show();
            onUserLoggedIn(user.getUserId());

            if (user.getUserProfile() == null) {
                startActivity(new Intent(LoginActivity.this, ProfileSetupActivity.class));
            } else {
                Intent intent = new Intent(this, DashboardActivity.class);
                startActivity(intent);
            }

            finish();
        } else {
            Snackbar
                    .make(findViewById(android.R.id.content), "Invalid username or password",
                            Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private void register() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!isValidPassword(password)) {
            Snackbar
                    .make(findViewById(android.R.id.content), "Password must be at least 8 characters long",
                            Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        long newRowId = dbHelper.addUser(username, password);

        if (newRowId != -1) {
            Snackbar
                    .make(
                            findViewById(android.R.id.content), "Registration successful", Snackbar.LENGTH_SHORT)
                    .show();
        } else {
            Snackbar
                    .make(findViewById(android.R.id.content), "Registration failed", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    public void onUserLoggedIn(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit();
        editor.clear().apply();
        editor.putString("userId", userId);
        editor.apply();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SHAREDPREF,
                new String[] {DatabaseHelper.COLUMN_SHAREDPREF_ID},
                DatabaseHelper.COLUMN_SHAREDPREF_USER_ID + "=?", new String[] {userId}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range")
                long idFromDB = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_SHAREDPREF_ID));

                Session session = dbHelper.getSession(idFromDB);
                if (session != null) {
                    String keyFromDB = session.getKeyString();

                    editor.putString(keyFromDB, String.valueOf(idFromDB));
                    editor.apply();
                }
            }
            cursor.close();
        }
    }

    private boolean isValidPassword(String password) {
        return password.length() >= MIN_PASSWORD_LENGTH;
    }
}