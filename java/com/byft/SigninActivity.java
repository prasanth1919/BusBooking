package com.byft;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SigninActivity extends AppCompatActivity {
    private EditText inputEmail, inputPassword;
    private Button loginButton;
    private TextView signUpText;
    private DatabaseHelper databaseHelper;
    private ImageView viewPasswordIcon;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Initialize views
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signUpText);
        viewPasswordIcon = findViewById(R.id.viewPasswordIcon);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });

        // Sign Up text click listener to redirect to Sign Up Activity
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        // Password visibility toggle
        viewPasswordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide password
                    inputPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    viewPasswordIcon.setImageResource(R.drawable.eye); // Adjust based on your drawable
                } else {
                    // Show password
                    inputPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                    viewPasswordIcon.setImageResource(R.drawable.eye_off); // Adjust based on your drawable
                }
                isPasswordVisible = !isPasswordVisible;
                inputPassword.setSelection(inputPassword.getText().length()); // Keep cursor at the end
            }
        });
    }

    private void signInUser() {
        // Get user input
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Validate fields
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user credentials from database
        boolean success = databaseHelper.checkUserLogin(email, password);
        if (success) {
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);

            finish();
        } else {
            Toast.makeText(this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailPattern);
    }
}
