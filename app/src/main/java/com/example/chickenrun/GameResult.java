package com.example.chickenrun;


import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.Map;


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




        init();

    }

     private void init() {

         String coordinates[] = { "37.517180", "127.041268" };
         double lat = Double.parseDouble(coordinates[0]);
         double lng = Double.parseDouble(coordinates[1]);

         LatLng position = new LatLng(lat, lng);
         GooglePlayServicesUtil.isGooglePlayServicesAvailable(
                 GameResult.this);

//         // 맵 위치이동.
//         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

         arrayPoints = new ArrayList<LatLng>();
     }


     @Override
     public void onMapReady(final GoogleMap googleMap) {

         mMap = googleMap;
         //gps 좌표 저장
         SharedPreferences sharedPreferences = getSharedPreferences("gpsinfo",MODE_PRIVATE);
         Map<String, ?> totalValue = sharedPreferences.getAll();//쉐어드 프리퍼런스.....
         int gpscnt = (totalValue.entrySet().size())/2; //쉐어드 프리퍼런스 개수
         Log.i("gpscny","      "+gpscnt);

         LatLng gpsstart = new LatLng(sharedPreferences.getFloat("gpslatitude0",0),
                 sharedPreferences.getFloat("gpslongitude0",0));

         for(int i=0; i< gpscnt ; i++){
             LatLng gpsgood = new LatLng(sharedPreferences.getFloat("gpslatitude"+i,0),
                     sharedPreferences.getFloat("gpslongitude"+i,0));
             drawployline(gpsgood);
         }

//         LatLng SEOUL = new LatLng(37.56, 126.97);
//         MarkerOptions markerOptions = new MarkerOptions(); //마커 객체
//         markerOptions.position(SEOUL);
//         markerOptions.title("서울");
//         markerOptions.snippet("한국의 수도");
//         mMap.setOnMapClickListener(this);
//         mMap.setOnMapLongClickListener(this);
//         mMap.addMarker(markerOptions);

         mMap.moveCamera(CameraUpdateFactory.newLatLng(gpsstart));
         mMap.setMyLocationEnabled(true);
         mMap.animateCamera(CameraUpdateFactory.zoomTo(20));

     }
    public void drawployline(LatLng latLng){

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



 }
