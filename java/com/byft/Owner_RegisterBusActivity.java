package com.byft;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Owner_RegisterBusActivity extends AppCompatActivity {

    private EditText busNumberEditText;
    private Spinner busSeatsSpinner;
    private Spinner driverSpinner;
    private Spinner daySpinner;
    private EditText tripTimeEditText;
    private Spinner startLocationSpinner;
    private Spinner destinationSpinner;
    private Button addScheduleButton;
    private Button registerBusButton;
    private TextView scheduleListTextView;
    private DatabaseHelper databaseHelper;

    private List<Schedule> scheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_owner_registerbus);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        busNumberEditText = findViewById(R.id.busNumber);
        busSeatsSpinner = findViewById(R.id.busSeatsSpinner);
        driverSpinner = findViewById(R.id.driverSpinner);
        daySpinner = findViewById(R.id.daySpinner);
        tripTimeEditText = findViewById(R.id.tripTimeEditText);
        startLocationSpinner = findViewById(R.id.startLocationSpinner);
        destinationSpinner = findViewById(R.id.destinationSpinner);
        addScheduleButton = findViewById(R.id.addScheduleButton);
        registerBusButton = findViewById(R.id.registerBusButton);
        scheduleListTextView = findViewById(R.id.scheduleListTextView);
        Button viewRouteButton = findViewById(R.id.view_route_button);

        // Populate location spinners with Andhra Pradesh locations
        List<String> apLocations = Arrays.asList(
                "Visakhapatnam", "Vijayawada", "Tirupati", "Rajahmundry",
                "Guntur", "Kurnool", "Nellore"
        );

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, apLocations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startLocationSpinner.setAdapter(locationAdapter);
        destinationSpinner.setAdapter(locationAdapter);

        // Load drivers list
        loadDrivers();

        // Set add schedule button click listener
        addScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSchedule();
            }
        });

        // Set register button click listener
        registerBusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerBus();
            }
        });

        // Set view route button click listener
        viewRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startLocation = startLocationSpinner.getSelectedItem().toString();
                String endLocation = destinationSpinner.getSelectedItem().toString();

                if (startLocation.isEmpty() || endLocation.isEmpty()) {
                    Toast.makeText(Owner_RegisterBusActivity.this, "Please select start and end locations", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(Owner_RegisterBusActivity.this, RouteMapActivity.class);
                intent.putExtra("start_location", startLocation);
                intent.putExtra("end_location", endLocation);
                startActivity(intent);
            }
        });
    }

    private void loadDrivers() {
        List<String> drivers = databaseHelper.getDrivers();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drivers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverSpinner.setAdapter(adapter);
    }

    private void addSchedule() {
        String day = daySpinner.getSelectedItem().toString();
        String tripTime = tripTimeEditText.getText().toString().trim();
        String startLocation = startLocationSpinner.getSelectedItem().toString();
        String endLocation = destinationSpinner.getSelectedItem().toString();

        // Validate input fields
        if (tripTime.isEmpty() || startLocation.isEmpty() || endLocation.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startLocation.equals(endLocation)) {
            Toast.makeText(this, "Start and end locations cannot be the same", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add schedule to the list
        Schedule schedule = new Schedule(day, tripTime, startLocation, endLocation);
        scheduleList.add(schedule);

        // Update the schedule list display
        updateScheduleListDisplay();
    }

    private void updateScheduleListDisplay() {
        StringBuilder scheduleDisplay = new StringBuilder();
        for (Schedule schedule : scheduleList) {
            scheduleDisplay.append(schedule.toString()).append("\n");
        }
        scheduleListTextView.setText(scheduleDisplay.toString());
    }

    private void registerBus() {
        String busNumber = busNumberEditText.getText().toString().trim();
        String busSeats = busSeatsSpinner.getSelectedItem().toString();
        String driver = driverSpinner.getSelectedItem().toString();

        // Validate bus number format
        if (!busNumber.matches("^[A-Z]{2,3}-\\d{4}$")) { // Update format if required
            Toast.makeText(this, "Invalid bus number format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if bus number already exists in the database
        if (databaseHelper.isBusNumberExists(busNumber)) {
            Toast.makeText(this, "Bus number already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert busSeats to integer
        int busSeatsInt;
        try {
            busSeatsInt = Integer.parseInt(busSeats);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert bus into the database
        boolean success = databaseHelper.insertBus(busNumber, busSeatsInt, driver);
        if (success) {
            Toast.makeText(this, "Bus registered successfully", Toast.LENGTH_SHORT).show();
            // Insert all schedules into the database
            for (Schedule schedule : scheduleList) {
                databaseHelper.insertBusSchedule(busNumber, schedule.getDay(), schedule.getTripTime(), schedule.getStartLocation(), schedule.getEndLocation());
            }
            finish();
        } else {
            Toast.makeText(this, "Bus registration failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void showTimePicker(View view) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tripTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private static class Schedule {
        private String day;
        private String tripTime;
        private String startLocation;
        private String endLocation;

        public Schedule(String day, String tripTime, String startLocation, String endLocation) {
            this.day = day;
            this.tripTime = tripTime;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
        }

        public String getDay() {
            return day;
        }

        public String getTripTime() {
            return tripTime;
        }

        public String getStartLocation() {
            return startLocation;
        }

        public String getEndLocation() {
            return endLocation;
        }

        @Override
        public String toString() {
            return "Day: " + day + ", Time: " + tripTime + ", From: " + startLocation + ", To: " + endLocation;
        }
    }
}
