package com.byft;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Passenger_BookRideActivity extends AppCompatActivity {

    private Spinner startLocationSpinner;
    private Spinner endLocationSpinner;
    private Spinner tripDateSpinner;
    private Button searchBusesButton;
    private ListView busListView;
    private DatabaseHelper databaseHelper;
    private List<String> busList;
    private ArrayAdapter<String> busListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_booking);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        databaseHelper = new DatabaseHelper(this);

        startLocationSpinner = findViewById(R.id.start_location);
        endLocationSpinner = findViewById(R.id.end_location);
        tripDateSpinner = findViewById(R.id.trip_date);
        searchBusesButton = findViewById(R.id.search_buses_button);
        busListView = findViewById(R.id.bus_list);

        // List of Andhra Pradesh locations
        List<String> apLocations = Arrays.asList(
                "Visakhapatnam", "Vijayawada", "Tirupati", "Rajahmundry",
                "Guntur", "Kurnool", "Nellore"
        );

        // Set up adapters for start and end location spinners
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, apLocations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startLocationSpinner.setAdapter(locationAdapter);
        endLocationSpinner.setAdapter(locationAdapter);

        // Adapter for trip dates (unchanged)
        ArrayAdapter<CharSequence> tripDateAdapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, android.R.layout.simple_spinner_item);
        tripDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tripDateSpinner.setAdapter(tripDateAdapter);

        busList = new ArrayList<>();
        busListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, busList);
        busListView.setAdapter(busListAdapter);

        searchBusesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBuses();
            }
        });

        busListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedBus = busList.get(position);
                String tripDate = tripDateSpinner.getSelectedItem().toString();
                Intent intent = new Intent(Passenger_BookRideActivity.this, SeatSelectionActivity.class);
                intent.putExtra("busNumber", selectedBus);
                intent.putExtra("email", email);
                intent.putExtra("scheduleID", getScheduleID(selectedBus, tripDate)); // Pass schedule ID
                startActivity(intent);
            }
        });
    }

    private void searchBuses() {
        String startLocation = startLocationSpinner.getSelectedItem().toString();
        String endLocation = endLocationSpinner.getSelectedItem().toString();
        String tripDate = tripDateSpinner.getSelectedItem().toString();

        if (startLocation.equals(endLocation)) {
            Toast.makeText(this, "Start and end locations cannot be the same.", Toast.LENGTH_SHORT).show();
            return;
        }

        busList.clear();
        List<String> buses = databaseHelper.getBusesForRouteAndDate(startLocation, endLocation, tripDate);
        if (buses != null && !buses.isEmpty()) {
            busList.addAll(buses);
            busListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No buses found for the selected route and date", Toast.LENGTH_SHORT).show();
        }
    }

    private int getScheduleID(String busNumber, String tripDate) {
        return databaseHelper.getScheduleID(busNumber, tripDate);
    }
}
