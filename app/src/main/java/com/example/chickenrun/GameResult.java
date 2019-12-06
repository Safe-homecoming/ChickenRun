package com.example.chickenrun;


import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.chickenrun.gameRoom.item_lank_list;
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
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.example.chickenrun.GameStart.LIST_LANK;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_IS_HOST;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_MY_NAME;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_ROOM_INDEX;


public class GameResult extends AppCompatActivity
        implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener
 {

     private GoogleMap mMap;
     private PolylineOptions polylineOptions;
     private ArrayList<LatLng> arrayPoints;


     // todo: 성훈
     TextView tv_game_result, lank_title;
     private item_lank_list item_lank_lists;


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

        // todo: (성훈) 통신 관련 코드 작성 ============================
        tv_game_result = findViewById(R.id.tv_game_result);
        lank_title = findViewById(R.id.lank_title);
        setSocket();
    }

     private String TAG = "GameResult";
     Socket socket;

     // todo: (성훈) 통신 관련 메소드
     private void setSocket()
     {
         try
         {
             //socket은 커넥션이 성공했을 때 커넥션에 대한 정보를 담고 있는 변수
             socket = IO.socket("http://ec2-13-125-121-5.ap-northeast-2.compute.amazonaws.com:3000");
         } catch (Exception e)
         {
             e.printStackTrace();
         }

         socket.on(Socket.EVENT_CONNECT, new Emitter.Listener()
         {
             @Override
             public void call(Object... args)
             {
//                // 서버에 데이터 전송
//                socket.emit("message_from_client", "Hi~ 나는 안드로이드야.");
             }
         }).on("message_from_server", new Emitter.Listener()
         {
             @Override
             public void call(final Object... args)
             {
                 //socket.on 해당 이벤트가 오기를 기다리는 페이지 전부를 의미함
                 //굳이 이벤트 리스너를 등록하지 않아도 되지만  없으면 무시됨
                 runOnUiThread(new Runnable()
                 {
                     @Override
                     public void run()
                     {
                         Log.e(TAG, "run: args[0].toString(): " + args[0].toString() );
                         // todo: 수신받은 메시지 가공해서 사용하기 (성훈)
                         getSocketMessage(args[0].toString());
                     }
                 });
             }
         });
         socket.connect();

         // 방장은 이전 액티비티에서 저장한 순위표 불러오기 (미친 도랐나;)
/*         if (GET_IS_HOST)
         {
             for (int i = 0; i < LIST_LANK.size(); i++)
             {
                 if (LIST_LANK.get(i).getUserName().equals(GET_MY_NAME))
                 {
                     // 자신의 순위 띄우기
                     lank = i + 1 + "위: " + LIST_LANK.get(i).getUserName() + " / " + LIST_LANK.get(i).getRunTime();
                     tv_game_result.setText(lank);
                 }
             }
         }*/

         if (LIST_LANK == null)
         {
             Log.e(TAG, "setSocket: 결승 지점에 도착했습니다." );
         }
         else
         {
             if (LIST_LANK.get(0).getUserName().equals(GET_MY_NAME))
             {
                 // 자신의 순위 띄우기
                 lank = 1 + "위: " + LIST_LANK.get(0).getUserName() + " / " + LIST_LANK.get(0).getRunTime();
                 tv_game_result.setText(lank);
             }
         }
     }

     String lank;
     String ioMessage[];
     String getGoalRecord[];
     String getWelcomeSignal[];

     // todo: 수신받은 메시지 가공해서 사용하기
     private void getSocketMessage(String message)
     {
         if (message.equals("hello, world"))
         {
             Log.e(TAG, "getSocketMessage: message: " + message);
         } else
         {
             ioMessage = message.split(", ");

             Log.e(TAG, "getSocketMessage: 메시지 보낸 유저  : " + ioMessage[0]);
             Log.e(TAG, "getSocketMessage: 방 번호           : " + ioMessage[1]);
             Log.e(TAG, "getSocketMessage: 타입              : " + ioMessage[2]);
             Log.e(TAG, "getSocketMessage: 내용              : " + ioMessage[3]);

             // todo: 먼저 결승 지점에 도착한 유저는 (1등 한 유저는) 이후에 들어온 참가자들의 골 인 신호 받기
             if (ioMessage[2].equals("GameProgressGoalSignal"))
             {
                 // 1위 유저 이외의 유저는 LIST_LANK 변수를 사용하지 않기 때문에 null 체크 필요함
                 if (LIST_LANK == null)
                 {
                     Log.e(TAG, "getSocketMessage: 순위표를 정리합니다. 잠시만 기다려 주세요" );
                 }

                 // 1등한 유저에게 순위표 관리 권한 전담하기
                 else
                 {
                     if (LIST_LANK.get(0).getUserName().equals(GET_MY_NAME))
                     {
                         getGoalRecord = ioMessage[3].split(" / ");

                         // 순위표에 참가자 이름, 기록 저장하기
                         item_lank_lists = new item_lank_list(getGoalRecord[0], getGoalRecord[1]);
                         LIST_LANK.add(item_lank_lists);

                         // lank 순위표 다시 정리
                         lank = null;
                         Log.e(TAG, "getSocketMessage: === 순위표 === " );
                         for (int i = 0; i < LIST_LANK.size(); i++)
                         {
                             Log.e(TAG, "onClick: UserName   : " + (i + 1) + "등: "+ LIST_LANK.get(i).getUserName() );
                             Log.e(TAG, "onClick: RunTime    : 기록: " + LIST_LANK.get(i).getRunTime() );

                             if (TextUtils.isEmpty(lank))
                             {
                                 Log.e(TAG, "getSocketMessage: lank isEmpty" );
                                 lank = i + 1 + "위: " + LIST_LANK.get(i).getUserName() + " / " + LIST_LANK.get(i).getRunTime() + "\n";
                             }

                             else
                             {
                                 Log.e(TAG, "getSocketMessage: add lank" );
                                 lank = lank + ((int)i + 1) + "위: " + LIST_LANK.get(i).getUserName() + " / " + LIST_LANK.get(i).getRunTime() + "\n";
                             }
                         }

                         Log.e(TAG, "getSocketMessage: lank: "  + lank );

                         // 순위표 정리 후 참가자들에게 순위표 전달 해주기
                         timerType = "responseLankList";
                         elapsed = 0; // 시작할 시간 설정 (0초부터)
                         TIMEOUT = 3000; // 타이머 진행할 시간 3초로 설정
                         tempTask(); // 타이머 시작

                         tv_game_result.setText(lank);
                     }                     
                 }
             }

             if(ioMessage[2].equals("GameProgressAfterGoalSignal"))
             {
                 lank = null;

                 lank =ioMessage[3];

                 tv_game_result.setText(lank);
             }
         }
     }

     // todo: 메시지 전송
     private void attemptSend(String sendUser, String currentRoomNum, String messageType, String message)
     {
         Log.e(TAG, "attemptSend: attemptSend: " );
         if (TextUtils.isEmpty(message))
         {
             Log.e(TAG, "attemptSend: message.Empty?");
             return;
         }

         String sendMessage = sendUser + ", " + currentRoomNum + ", " + messageType + ", " + message;

         socket.emit("message_from_client", sendMessage);
         Log.e(TAG, "attemptSend: 전송한 메시지: " + sendMessage);
     }

     // 타이머
     long elapsed; // 4초부터 시작
     final static long INTERVAL = 1000; // 1초 에 1씩 증가
     static long TIMEOUT; // 종료 할 시간 설정 (10000 = 10초)
     private Timer timer;
     String timerType;

     // todo: 타이머 스레드 메소드
     public void tempTask()
     {
         TimerTask task = new TimerTask()
         {
             @Override
             public void run()
             {
                 // 1 초 씩 증가
                 elapsed += INTERVAL;
                 if (elapsed >= TIMEOUT)
                 {
                     this.cancel(); // 설정한 시간까지 흐르면 타이머 중단
                     return;
                 }

                 // 현재 초를 displayText 메소드로 전달
                 displayText(String.valueOf(elapsed / 1000));
             }
         };

         timer = new Timer();
         timer.scheduleAtFixedRate(task, INTERVAL, INTERVAL);
     }

     // 시간 증가 메소드 (초당 1씩 값 증가)
     private void displayText(final String text)
     {
         this.runOnUiThread(new Runnable()
         {
             @Override
             public void run()
             {
                 Log.e(TAG, "displayText run: text: " + text);

                 if (timerType.equals("responseLankList"))
                 {
                     // 1등 유저가 1초 후 참가자들에게 순위표 나눠주기 (너무 빨리 보내주면 참가자들이 받지를 못 해서 타이머로 뺐음)
                     if (Integer.parseInt(text) == 2)
                     {
                         timer.cancel(); // 타이머 중단
                         attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "GameProgressAfterGoalSignal", lank);
                     }
                 }
             }
         });
     }

     // todo: (성훈 끝) ========================

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
