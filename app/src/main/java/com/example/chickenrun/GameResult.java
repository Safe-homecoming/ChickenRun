package com.example.chickenrun;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameResult extends AppCompatActivity
        implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener

 {

     private GoogleMap mMap;
     private PolylineOptions polylineOptions;
     private ArrayList<LatLng> arrayPoints;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_result);



        // BitmapDescriptorFactory 생성하기 위한 소스
        MapsInitializer.initialize(getApplicationContext());




        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);

        init();

    }

     private void init() {

         String coordinates[] = { "37.517180", "127.041268" };
         double lat = Double.parseDouble(coordinates[0]);
         double lng = Double.parseDouble(coordinates[1]);

         LatLng position = new LatLng(lat, lng);
         GooglePlayServicesUtil.isGooglePlayServicesAvailable(
                 GameResult.this);

         // 맵 위치이동.
         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

         arrayPoints = new ArrayList<LatLng>();
     }

     @Override
     public void onMapClick(LatLng latLng) {

         //add marker
         MarkerOptions marker=new MarkerOptions();
         marker.position(latLng);
         mMap.addMarker(marker);

         // 맵셋팅
         polylineOptions = new PolylineOptions();
         polylineOptions.color(Color.RED);
         polylineOptions.width(5);
         arrayPoints.add(latLng);
         polylineOptions.addAll(arrayPoints);
         mMap.addPolyline(polylineOptions);
     }

     @Override
     public void onMapLongClick(LatLng arg0) {
         mMap.clear();
         arrayPoints.clear();
     }


     @Override
     public void onMapReady(GoogleMap googleMap) {

     }
 }
