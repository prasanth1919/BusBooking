package com.byft;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.List;

public class RouteMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap mMap;
    private TextView routeTextView;
    private Button backButton;
    private HashMap<String, LatLng> highwayTerminals;
    private LatLng startLocation, endLocation;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        mapView = findViewById(R.id.mapView);
        routeTextView = findViewById(R.id.route_text_view);
        backButton = findViewById(R.id.back_button);

        Bundle mapViewBundle = savedInstanceState != null ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        setupHighwayTerminals();

        // Get start and end locations from intent
        String startPoint = getIntent().getStringExtra("start_location");
        String endPoint = getIntent().getStringExtra("end_location");
        startLocation = highwayTerminals.get(startPoint);
        endLocation = highwayTerminals.get(endPoint);

        // Set route text
        routeTextView.setText("Route: " + startPoint + " to " + endPoint);

        // Set back button click listener
        backButton.setOnClickListener(v -> finish());
    }

    private void setupHighwayTerminals() {
        highwayTerminals = new HashMap<>();
        highwayTerminals.put("Visakhapatnam", new LatLng(17.7041, 83.2977));
        highwayTerminals.put("Vijayawada", new LatLng(16.5062, 80.6480));
        highwayTerminals.put("Tirupati", new LatLng(13.6288, 79.4192));
        highwayTerminals.put("Rajahmundry", new LatLng(17.0005, 81.8040));
        highwayTerminals.put("Guntur", new LatLng(16.3067, 80.4365));
        highwayTerminals.put("Kurnool", new LatLng(15.8281, 78.0373));
        highwayTerminals.put("Nellore", new LatLng(14.4426, 79.9865));
    }

    private boolean isHighwayRouteSelected() {
        return startLocation != null && endLocation != null;
    }

    private void fetchRoute(LatLng origin, LatLng destination) {
        List<LatLng> routePoints = getHardcodedRoute(origin, destination);
        if (routePoints != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(origin).title("Start"));
            mMap.addMarker(new MarkerOptions().position(destination).title("End"));
            mMap.addPolyline(new PolylineOptions().addAll(routePoints).color(Color.BLUE).width(10));

            // Calculate the bounds of the route
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : routePoints) {
                builder.include(point);
            }
            LatLngBounds bounds = builder.build();

            // Move the camera to fit the bounds
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            Toast.makeText(this, "No route found between selected locations.", Toast.LENGTH_SHORT).show();
        }
    }

    private List<LatLng> getHardcodedRoute(LatLng origin, LatLng destination) {
        if (origin.equals(highwayTerminals.get("Visakhapatnam")) && destination.equals(highwayTerminals.get("Vijayawada"))) {
            return List.of(
                    new LatLng(17.7041, 83.2977), // Visakhapatnam
                    new LatLng(17.1, 82.5),       // Waypoint
                    new LatLng(16.5062, 80.6480)  // Vijayawada
            );
        } else if (origin.equals(highwayTerminals.get("Vijayawada")) && destination.equals(highwayTerminals.get("Tirupati"))) {
            return List.of(
                    new LatLng(16.5062, 80.6480), // Vijayawada
                    new LatLng(14.4426, 79.9865), // Nellore
                    new LatLng(13.6288, 79.4192)  // Tirupati
            );
        } else if (origin.equals(highwayTerminals.get("Guntur")) && destination.equals(highwayTerminals.get("Rajahmundry"))) {
            return List.of(
                    new LatLng(16.3067, 80.4365), // Guntur
                    new LatLng(17.0005, 81.8040)  // Rajahmundry
            );
        }
        return null; // Add more routes as needed
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(16.0, 80.0), 7)); // Center on Andhra Pradesh

        // Fetch and display the route when the map is ready
        if (isHighwayRouteSelected()) {
            fetchRoute(startLocation, endLocation);
        }

        // Set marker click listener to show bus departure time
        mMap.setOnMarkerClickListener(marker -> {
            String departureTime = marker.getSnippet();
            Toast.makeText(RouteMapActivity.this, "Departure Time: " + departureTime, Toast.LENGTH_SHORT).show();
            return false;
        });
    }
}
