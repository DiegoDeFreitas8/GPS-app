package com.example.gpsapp;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Map1Activity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener{

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        if (lLocationPermGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if(geoApiContext == null){
                geoApiContext = new GeoApiContext.Builder()
                        .apiKey(key)
                        .build();
            }
            init();
        }
        mMap.setOnPolylineClickListener(Map1Activity.this);
    }


    private static final String TAG = "Map1";
    private String key = "AIzaSyDySPCwdauxqvmwHlTZ3DG9JMR6DCIB6gY";
    private static final String FineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String CourseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private LinearLayout LinearLayoutStartTripDetails, LinearlayoutInfo;
    private GeoApiContext geoApiContext = null;
    private  ArrayList<PollyPath> polylineDataList = new ArrayList<>();
    private TextView TxtPlaceName, TxtRoute, TxtDuration, TxtDistance;
    private Button BtnCancel;
    private Location myLocation;
    //widgets
    private ImageView mGps;

    private Boolean lLocationPermGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mGps = (ImageView) findViewById(R.id.ic_gps);




        getLocationPerm();




        TxtPlaceName = findViewById(R.id.TxtPlaceName);
        TxtRoute= findViewById(R.id.TxtRoute);
        TxtDuration= findViewById(R.id.TxtStarTime);
        TxtDistance = findViewById(R.id.TxtDistance);
        BtnCancel = findViewById(R.id.BtnCancel);
        LinearLayoutStartTripDetails = findViewById(R.id.LinearLayoutStartTripDetails);
        LinearlayoutInfo = findViewById(R.id.LinearlayoutInfo);
        LinearLayoutStartTripDetails.setVisibility(View.GONE);
        LinearlayoutInfo.setVisibility(View.GONE);

        Places.initialize(getApplicationContext(), key);

        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME));

        autocompleteFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mMap.clear();
                LatLng latLng = place.getLatLng();

                LinearLayoutStartTripDetails.setVisibility(View.VISIBLE);

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(place.getName()));
                calculateDirections(marker);

            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                polylineDataList.clear();
                getDeviceLocation();
                LinearLayoutStartTripDetails.setVisibility(View.VISIBLE);
                LinearlayoutInfo.setVisibility(View.GONE);
            }
        });

    }

    private void init(){
        Log.d(TAG, "init: initializing");

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

    }


    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(lLocationPermGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location Location = (Location) task.getResult();
                            myLocation = Location;
                            moveCamera(new LatLng(Location.getLatitude(), Location.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My location");

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(Map1Activity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
    }

    private void initializeMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(Map1Activity.this);
    }

    private void getLocationPerm(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FineLocation) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    CourseLocation) == PackageManager.PERMISSION_GRANTED){
                lLocationPermGranted = true;
                initializeMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        lLocationPermGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            lLocationPermGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    lLocationPermGranted = true;
                    //initialize our map
                    initializeMap();
                }
            }
        }
    }


    private void calculateDirections(Marker marker){
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        myLocation.getLatitude(),
                        myLocation.getLongitude()
                )
        );
        //Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if(polylineDataList.size()>0) {
                    for (int i = 0; i < polylineDataList.size(); i++) {

                        polylineDataList.get(i).getPolyline().remove();

                    }

                    polylineDataList.clear();
                    polylineDataList = new ArrayList<>();

                }
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    for(com.google.maps.model.LatLng latLng: decodedPath){


                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(Map1Activity.this, R.color.colorSkyBlue));
                    polyline.setClickable(true);
                    polylineDataList.add(new PollyPath(polyline, route.legs[0]));

                }

                double QuickestTime = 999999999;
                for (int i = 0; i < polylineDataList.size(); i++) {
                    double currentDuration = polylineDataList.get(i).getLeg().duration.inSeconds;
                    if(currentDuration<QuickestTime){
                        QuickestTime = currentDuration;
                        onPolylineClick(polylineDataList.get(i).getPolyline());


                    }
                }
                LinearlayoutInfo.setVisibility(View.VISIBLE);
            }
        });
    }
    @Override
    public void onPolylineClick(Polyline polyline) {
        for (int i = 0; i < polylineDataList.size(); i++) {

            Log.d(TAG, "onPolylineClick: toString: " + polylineDataList.toString());

            if(polyline.getId().equals( polylineDataList.get(i).getPolyline().getId())){
                polylineDataList.get(i).getPolyline().setColor(ContextCompat.getColor(this, R.color.colorYellow));
                polylineDataList.get(i).getPolyline().setZIndex(1);

                LatLng endLocatin = new LatLng(
                        polylineDataList.get(i).getLeg().endLocation.lat,
                        polylineDataList.get(i).getLeg().endLocation.lng
                );

                TxtPlaceName.setText(polylineDataList.get(i).getLeg().endAddress);
                TxtRoute.setText("Route: " + (i + 1));
                TxtDuration.setText(polylineDataList.get(i).getLeg().duration.toString());


                TxtDistance.setText(polylineDataList.get(i).getLeg().distance.toString());




                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(endLocatin)
                        .title("Trip: " + (i+1))
                        .snippet("Length: " + polylineDataList.get(i).getLeg().duration)
                );
                marker.showInfoWindow();


            }else{
                polylineDataList.get(i).getPolyline().setColor(ContextCompat.getColor(this, R.color.colorSkyBlue));
                polylineDataList.get(i).getPolyline().setZIndex(0);
            }
        }
    }
}
