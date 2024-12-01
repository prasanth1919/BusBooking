package com.byft;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private EditText inputName, inputEmail, inputPhone, inputPassword, inputConfirmPassword;
    private Button registerButton;
    private ImageView uploadIcon, profileImage;
    private DatabaseHelper databaseHelper;
    private TextView signInText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        registerButton = findViewById(R.id.registerButton);
        profileImage = findViewById(R.id.profileImage);
        uploadIcon = findViewById(R.id.uploadIcon);
        signInText = findViewById(R.id.signInText);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Profile picture upload icon click listener
        uploadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    showImageSourceDialog();
                } else {
                    requestPermissions();
                }
            }
        });

        // Register button click listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Sign In text click listener to redirect to Sign In Activity
        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //back to sign in activity
                finish();
            }
        });
    }

    // Show dialog to choose between Gallery and Camera
    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Profile Picture");
        builder.setItems(new CharSequence[]{"Choose from Gallery", "Take Photo"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            chooseFromGallery();
                        } else if (which == 1) {
                            takePhoto();
                        }
                    }
                });
        builder.show();
    }

    // Check permissions for camera and storage
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog();
            } else {
                Toast.makeText(this, "Permissions are required to upload profile picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] getImageBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // Launch Gallery Intent
    private void chooseFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    // Launch Camera Intent
    private void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    profileImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == TAKE_PHOTO && data != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                profileImage.setImageBitmap(bitmap);
            }
        }
    }

    private void registerUser() {
        // Get input values
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();

        // Validate input fields
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone number format (Sri Lankan format)
        if (!isValidPhone(phone)) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password strength (at least 6 characters, 1 uppercase, 1 number)
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 6 characters, with at least one uppercase letter and one number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] profileImageBytes = null;
        if (profileImage.getDrawable() != null) {
            Bitmap profileBitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
            profileImageBytes = getImageBytes(profileBitmap);
        }

        // Check if user already exists
        if (databaseHelper.checkUserExists(email)) {
            Toast.makeText(this, "An account with this email already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        final byte[] finalProfileImageBytes = profileImageBytes; // Declare as final
        final String[] userTypes = {"Passenger", "Bus driver", "Bus owner"};
        new AlertDialog.Builder(this)
                .setTitle("Select User Type")
                .setSingleChoiceItems(userTypes, -1, (dialog, which) -> {
                    String selectedUserType = userTypes[which];
                    dialog.dismiss(); // Close the dialog after a selection is made

                    // Insert user into the database
                    boolean success = databaseHelper.insertUser(name, email, phone, password, finalProfileImageBytes, selectedUserType);
                    if (success) {
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, SigninActivity.class); // Redirect to the sign-in screen or another screen
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(false)
                .show();
    }


    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.matches(emailPattern, email);
    }

    private boolean isValidPhone(String phone) {
        String sriLankanPhonePattern = "^(\\+91|0)7\\d{8}$";
        return Pattern.matches(sriLankanPhonePattern, phone);
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "(?=.*[A-Z])(?=.*\\d).{6,}";
        return password.matches(passwordPattern);
    }
}