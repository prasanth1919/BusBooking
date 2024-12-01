package com.byft;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SeatSelectionActivity extends AppCompatActivity {

    private TextView busNumberTextView;
    private TextView busRouteTextView;
    private TextView busTimeTextView;
    private GridLayout seatGrid;
    private Button bookButton;
    private DatabaseHelper databaseHelper;
    private String busNumber;
    private int scheduleID;
    private int selectedSeat = -1;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        busNumberTextView = findViewById(R.id.bus_number);
        busRouteTextView = findViewById(R.id.bus_route);
        busTimeTextView = findViewById(R.id.bus_time);
        seatGrid = findViewById(R.id.seat_grid);
        bookButton = findViewById(R.id.book_button);
        databaseHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        busNumber = intent.getStringExtra("busNumber");
        scheduleID = intent.getIntExtra("scheduleID", -1);

        displayBusDetails();
        displaySeats();

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookSeat();
            }
        });
    }

    private void displayBusDetails() {
        // Fetch bus details from the database and display them
        String busDetails = "Bus Number: " + busNumber + "\nRoute: " + "Start - End" + "\nTime: " + "Time";
        busNumberTextView.setText("Bus Number: " + busNumber);
        busRouteTextView.setText("Route: Start - End");
        busTimeTextView.setText("Time: Time");
    }

    private void displaySeats() {
        List<Integer> bookedSeats = databaseHelper.getBookedSeats(scheduleID);
        int totalSeats = databaseHelper.getTotalSeats(busNumber);

        // Define seat sections
        int backSeats = 4;
        int leftSeats = (totalSeats - backSeats) / 2;
        int rightSeats = totalSeats - backSeats - leftSeats;

        // Add back seats
        for (int i = 1; i <= backSeats; i++) {
            addSeatButton(i, bookedSeats, 0);
        }

        // Add left and right seats with padding
        for (int i = 1; i <= leftSeats; i++) {
            addSeatButton(backSeats + i, bookedSeats, 1);
        }
        for (int i = 1; i <= rightSeats; i++) {
            addSeatButton(backSeats + leftSeats + i, bookedSeats, 2);
        }
    }

    private void addSeatButton(int seatNumber, List<Integer> bookedSeats, int section) {
        Button seatButton = new Button(this);
        seatButton.setText(String.valueOf(seatNumber));
        seatButton.setTag(seatNumber);
        seatButton.setTextColor(getResources().getColor(android.R.color.black));

        if (bookedSeats.contains(seatNumber)) {
            seatButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            seatButton.setTextColor(getResources().getColor(android.R.color.white));
            seatButton.setEnabled(false);
        } else {
            seatButton.setBackgroundColor(getResources().getColor(android.R.color.white));
            seatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectSeat((int) v.getTag());
                }
            });
        }

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec((seatNumber - 1) % 6, 1, 1f);
        params.rowSpec = GridLayout.spec((seatNumber - 1) / 6, 1, 1f);

        // Add padding between sections
        if (section == 1 && (seatNumber - 1) % 6 == 2) {
            params.setMargins(0, 0, 16, 0);
        } else if (section == 2 && (seatNumber - 1) % 6 == 3) {
            params.setMargins(16, 0, 0, 0);
        }

        seatButton.setLayoutParams(params);
        seatGrid.addView(seatButton);
    }

    private void selectSeat(int seatNumber) {
        if (selectedSeat != -1) {
            Button previousSelectedButton = seatGrid.findViewWithTag(selectedSeat);
            if (previousSelectedButton != null) {
                previousSelectedButton.setBackgroundColor(getResources().getColor(android.R.color.white));
                previousSelectedButton.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
        selectedSeat = seatNumber;
        Button selectedButton = seatGrid.findViewWithTag(seatNumber);
        if (selectedButton != null) {
            selectedButton.setBackgroundColor(getResources().getColor(android.R.color.black));
            selectedButton.setTextColor(getResources().getColor(android.R.color.white));
        }
        bookButton.setVisibility(View.VISIBLE);
    }

    private void bookSeat() {
        if (selectedSeat != -1) {
            if (isSeatAlreadyBooked(email, scheduleID)) {
                Toast.makeText(this, "You have already booked a seat on this bus for the selected date.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = databaseHelper.insertBooking(scheduleID, busNumber, selectedSeat, email);

            if (success) {
                sendBookingConfirmationEmail();
                Toast.makeText(this, "Seat booked successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to book seat. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendBookingConfirmationEmail() {
        String subject = "Booking Confirmation";
        String body = "Dear User,\n\nYour seat has been successfully booked.\n\n" +
                "Bus Number: " + busNumber + "\n" +
                "Seat Number: " + selectedSeat + "\n" +
                "Schedule ID: " + scheduleID + "\n\n" +
                "Thank you for using our service.\n\nBest Regards,\nYour Company Name";

        // Replace with your email and password
        final String username = "your-email@gmail.com";
        final String password = "your-email-password";

        EmailUtil.sendEmail(username, password, email, subject, body);
    }

    private boolean isSeatAlreadyBooked(String email, int scheduleID) {
        return databaseHelper.isSeatAlreadyBooked(email, scheduleID);
    }
}