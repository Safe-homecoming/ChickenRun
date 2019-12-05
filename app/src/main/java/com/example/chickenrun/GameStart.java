package com.example.chickenrun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

/*
* 사용자들이 게임을 시작하기 위한 화면, 시작후 화면
* */
public class GameStart extends AppCompatActivity {

    Socket socket;

    Button button;
    EditText editText;
    TextView  countview, timersview,distenceview;
    FrameLayout lottilay;//lotti_lay

    //count thread
    Thread thread;
    boolean isThread = false;
    int cnt =0;
    //애니메이션
    private LottieAnimationView animationView;

    //tts
    private TextToSpeech tts;

    //경기시간 재는 thread
    private Thread timeThread = null;
    private Boolean isRunning = true;


    // GPS 현재위치 관련 변수
    String provider;  //현재위치 정보
    double longitude; //위도
    double latitude; //경도
    double altitude ; // 고도

    String nowprovider ;  //이동중인  위치 정보
    double nowlongitude;
    double nowlatitude ;
    double nowaltitude ;

    private Thread gpsThread = null;
    private Boolean gisRunning = true;

     LocationManager lm ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_start);


            //안드로이드 시스템에서 GPS를 통한 위치 서비스를 가져오려고함 .
         lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Lotti 애니메이션 셋팅
        // 경기 시작후 띄워주는 애니메이션
        animationView = (LottieAnimationView) findViewById(R.id.Lottie_Intro);
        animationView.setAnimation("funky-chicken.json");
        animationView.pauseAnimation(); //애니메이션 정지


        // TTS를 생성하고 OnInitListener로 초기화 한다.
        //경기 시작을 알리는 숫자를 읽기 위한 tts
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        // count 쓰레드
        //경기 시작을 알려주는 count 쓰레드
//        isThread = true;
//        thread = new Thread(){
//            public void run(){
//                while (cnt <= 4){ //
////                    Log.i("cntcntcntcnt","      "+cnt);
//                    try{
//                        sleep(1000);
//                    }catch (InterruptedException e){
//                        e.printStackTrace();
//                        thread.interrupt();
//                    }
//                    handler.sendEmptyMessage(0);
////                    Log.i("cntcntcntcnt222222222","      "+cnt);
//                }
//            }
//        };
//        thread.start();


        timeThread = new Thread(new timeThread());
        timeThread.start();

        //현재위치 가져오기
        curLocation();



        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
        //소켓 관련
//        try {
//            //socket은 커넥션이 성공했을 때 커넥션에 대한 정보를 담고 있는 변수
//            socket = IO.socket("http://ec2-13-125-121-5.ap-northeast-2.compute.amazonaws.com:3000");
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//                //                // 서버에 데이터 전송
//                socket.emit("message_from_client", "Hi~ 나는 안드로이드야.");
//            }
//        }).on("message_from_server", new Emitter.Listener() {
//            @Override
//            public void call(final Object... args) {
//                //socket.on 해당 이벤트가 오기를 기다리는 페이지 전부를 의미함
//                //굳이 이벤트 리스너를 등록하지 않아도 되지만  없으면 무시됨
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        textView.setText(args[0].toString());
//                    }
//                });
//            }
//        });
//        socket.connect();
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
//        editText = (EditText) findViewById(R.id.editText);
//        textView = (TextView) findViewById(R.id.result);
//        button = (Button) findViewById(R.id.button);


        lottilay = (FrameLayout)findViewById(R.id.lotti_lay);
        countview = (TextView)findViewById(R.id.countnum);
         //chronometer = (Chronometer) findViewById(R.id.timers);


        timersview =(TextView)findViewById(R.id.timers);
        distenceview = (TextView)findViewById(R.id.distence);

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                isThread = false;
//                thread.interrupt();
//                String msg = editText.getText().toString();
//                //socket.emit("message_from_client", msg);
//            }
//        });




    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
           // super.handleMessage(msg);
            cnt++;
            if(cnt <=3) {
                countview.setText("" + cnt);
                tts.setPitch(1f);         // 음성 톤을 0.5배 내려준다.
                tts.setSpeechRate(1.0f);    // 읽는 속도는 기본 설정
                tts.speak(countview.getText().toString(),TextToSpeech.QUEUE_FLUSH, null);
//                Log.i("cntcntcntcnt4444","      "+cnt);
            }else if(cnt == 4){
                countview.setTextSize(TypedValue.COMPLEX_UNIT_SP,100);
                countview.setText("START");
                tts.setPitch(1f);         // 음성 톤을 0.5배 내려준다.
                tts.setSpeechRate(1.0f);    // 읽는 속도는 기본 설정
                tts.speak(countview.getText().toString(),TextToSpeech.QUEUE_FLUSH, null);
//                Log.i("cntcntcntcnt5555","      "+cnt);
            }else if(cnt == 5){
//                Log.i("cntcntcntcnt6","      "+cnt);
                countview.setVisibility(View.INVISIBLE);
                animationView.playAnimation();//애니메이션 start


              //  chronometer.start();
                timeThread = new Thread(new timeThread());
                timeThread.start();
                gpsThread = new Thread(new gpsThread());
                gpsThread.start();


            }
        }
    };

    //timeThread 의 핸들러임 경기가 진행된 시간을 계산해서 보기 쉽게 만들어줌
    @SuppressLint("HandlerLeak")
    Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int mSec = msg.arg1 % 100;
            int sec = (msg.arg1 / 100) % 60;
            int min = (msg.arg1 / 100) / 60;
            int hour = (msg.arg1 / 100) / 360;
            //1000이 1초 1000*60 은 1분 1000*60*10은 10분 1000*60*60은 한시간

            String result = String.format("%02d:%02d:%02d:%02d", hour, min, sec, mSec);
            timersview.setText(result);
        }
    };

    // 게임이 시작 한후 경기 시간을 잰다.
    // timeThread를 이용하여 시간을 보여줌.
    public class timeThread implements Runnable {
        @Override
        public void run() {
             int i = 0;

            while (true) {
                while (isRunning) { //일시정지를 누르면 멈춤
                    final Message msg = new Message();

                    msg.arg1 = i++;
                   // handler2.sendMessage(msg);
                    try {
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              int mSec = msg.arg1 % 100;
                                              int sec = (msg.arg1 / 100) % 60;
                                              int min = (msg.arg1 / 100) / 60;
                                              int hour = (msg.arg1 / 100) / 360;
                                              String result = String.format("%02d:%02d:%02d", min, sec, mSec);
                                              timersview.setText(result);
                                          }
                                      });
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                timersview.setText("");
                                timersview.setText("00:00:00:00");
                            }
                        });
                        return; // 인터럽트 받을 경우 return
                    } //catch 문
                } // isRunnuing(while문)
            }// while(true) 문
        }
    }

    // 거리계산 핸들러
    Handler handler3 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            double mMiter = Double.parseDouble((String) msg.obj); // 스트링 - 더블형으로
            distenceview.setText(String.format("%.1f", mMiter)+"m");
        }
    };

    // 게임이 시작 한후 시작한 위치에서  현재 위치까지의 m를 보여줌
    // gpsThread 이용하여 m를 보여줌
    public class gpsThread implements Runnable {


        @Override
        public void run() {
            int i = 0;
            //출발 위치 받아오기
            final Location crntLocation = new Location("crntlocation");
            crntLocation.setLatitude(latitude);  //경도
            crntLocation.setLongitude(longitude); //위도
            float distance =0;

            //gps 좌표 저장
            SharedPreferences sharedPreferences = getSharedPreferences("gpsinfo",MODE_PRIVATE);
            //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();

            editor.putFloat("gpslatitude"+i,(float)crntLocation.getLatitude()); //출발위치 경도
            editor.putFloat("gpslongitude"+i,(float)crntLocation.getLongitude()); //출발위치 위도

            Log.i("curlocation testestset","      "+crntLocation.getLatitude()+"       "+crntLocation.getLongitude());

            editor.commit();
            while (true) {
                while (gisRunning) { //일시정지를 누르면 멈춤
                    Message msg = new Message();


                    //출발 위치 받아오기
                    final Location nowLocation = new Location("nowlocation");
                    nowLocation.setLatitude(nowlatitude);  //경도
                    nowLocation.setLongitude(nowlongitude); //위도
                    nowLocation.setAltitude(nowaltitude); //고도
                    Log.i("nowlocation testestset","      "+nowLocation.getLatitude()+"       "+nowLocation.getLongitude());
                    //Log.i("nowlocation testestset","      "+(float)nowLocation.getLatitude()+"       "+(float)nowLocation.getLongitude());

                    //  거리 계산
                    if(nowLocation.getAltitude() == 0.0 && nowLocation.getLatitude() != 0.0){
                        i++;
                        editor.putFloat("gpslatitude"+i,(float)nowLocation.getLatitude()); // key,//경도
                        editor.putFloat("gpslongitude"+i,(float)nowLocation.getLongitude()); // key,//위도
                        editor.commit();
                        distance = crntLocation.distanceTo(nowLocation);///1000; //in km
                        Log.i("Test_Log","   출발거리와 현재 위치 간 거리"+distance);
                    }


                    // 거리( 더블형 ) 스트링으로 보냄
                    String information = new String(""+distance);
                    msg.obj = information;


                    handler3.sendMessage(msg);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                distenceview.setText("");
                                distenceview.setText("0.0m");
                            }
                        });
                        return; // 인터럽트 받을 경우 return
                    }
                }
            }//while문 끝

        }
    }





    // 현재 위치 가져오기
    void curLocation(){
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( GameStart.this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }
        else{
            //가장최근의 위치정보를 가지고옵니다
            //provider = location.getProvider(); // 위치 정보
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                provider = location.getProvider(); // 위치 정보
                longitude = location.getLongitude();  // 위도
                latitude = location.getLatitude(); // 경도
                altitude = location.getAltitude(); // 고도
                Log.i("Activity_Guard_test", "위치정보 : " + provider + "\n" +
                        "위도 : " + longitude + "\n" +
                        "경도 : " + latitude + "\n" +
                        "고도  : " + altitude);

                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000,
                        1,
                        gpsLocationListener);

            }else{
                    //This is what you need:
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);

            }
        }

    }
    //현재 위도 경도 위치 가져오기 리스너
    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            nowprovider = location.getProvider();
            nowlongitude = location.getLongitude();
            nowlatitude = location.getLatitude();
            nowaltitude = location.getAltitude();
            Log.i("Guard_onLocationChanged", "위치정보 : " + nowprovider + "\n" +
                    "위도 : " + nowlongitude + "\n" +
                    "경도 : " + nowlatitude + "\n" +
                    "고도  : " + nowaltitude);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeThread.interrupt(); // 타입 쓰래드 멈춤
        gpsThread.interrupt();  //gps 도 멈춤.
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

}
