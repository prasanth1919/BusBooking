package com.byft;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout bookRideButton;
    private LinearLayout rideHistoryButton;
    private LinearLayout supportButton;
    private LinearLayout registerBusButton;
    private LinearLayout requestCancelBookingButton;
    private LinearLayout requestSwapSeatButton;
    private LinearLayout manageBusButton;
    private LinearLayout tripsButton;
    private LinearLayout viewBusDetailsButton;
    private DatabaseHelper databaseHelper;
    private TextView textUsername;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Retrieve Intent data
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        // Get user role
        String role = databaseHelper.getUserRole(email, password);
        String name = databaseHelper.getUserName(email, password);
        textUsername = findViewById(R.id.user_name);
        textUsername.setText("Hello,"+name);

        profileImage = findViewById(R.id.profile_image);
        byte[] profileImageData = databaseHelper.getUserProfileImage(email, password);
        if (profileImageData != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(profileImageData, 0, profileImageData.length);
            profileImage.setImageBitmap(bitmap);
        }

        // Passenger
        bookRideButton = findViewById(R.id.book_ride_button);
        rideHistoryButton = findViewById(R.id.ride_history_button);
        supportButton = findViewById(R.id.support_button);
        requestCancelBookingButton = findViewById(R.id.cancel_bookings_button);
        requestSwapSeatButton = findViewById(R.id.swap_seats_button);

        // Driver
        viewBusDetailsButton = findViewById(R.id.view_bus_details_button);
        tripsButton = findViewById(R.id.trips_button);

        // Bus Owner
        registerBusButton = findViewById(R.id.register_bus_button);
        manageBusButton = findViewById(R.id.view_buses_button);

        // Adjust visibility based on user role
        if ("PASSENGER".equalsIgnoreCase(role))
        {
            bookRideButton.setVisibility(View.VISIBLE);
            rideHistoryButton.setVisibility(View.VISIBLE);
            supportButton.setVisibility(View.VISIBLE);
            requestCancelBookingButton.setVisibility(View.VISIBLE);
            requestSwapSeatButton.setVisibility(View.VISIBLE);
        }
        else if ("BUS OWNER".equalsIgnoreCase(role))
        {
            registerBusButton.setVisibility(View.VISIBLE);
            supportButton.setVisibility(View.VISIBLE);
            manageBusButton.setVisibility(View.VISIBLE);
        }
        else if ("BUS DRIVER".equalsIgnoreCase(role))
        {
            viewBusDetailsButton.setVisibility(View.VISIBLE);
            tripsButton.setVisibility(View.VISIBLE);
            supportButton.setVisibility(View.VISIBLE);
        }


        registerBusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, Owner_RegisterBusActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        bookRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, Passenger_BookRideActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }

}