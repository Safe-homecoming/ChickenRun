package com.example.chickenrun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.chickenrun.gameRoom.item_lank_list;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.speech.tts.TextToSpeech.ERROR;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_IS_HOST;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_MY_NAME;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_ROOM_INDEX;

/*
 * 사용자들이 게임을 시작하기 위한 화면, 시작후 화면
 **/
public class GameStart extends AppCompatActivity
{

    public static float GET_CURRENT_DISTANCE;
    private String TAG = "GameStart";
    Socket socket;

    TextView countview, timersview, distenceview;
    FrameLayout lottilay;//lotti_lay
    ImageView mini;
    //count thread
    Thread thread;
    boolean isThread = false;
    int cnt = 0;
    //애니메이션
    private LottieAnimationView animationView;

    //tts
    private TextToSpeech tts;


    // GPS 현재위치 관련 변수
    String provider;  //현재위치 정보
    double longitude; //위도
    double latitude; //경도
    double altitude; // 고도

    String nowprovider;  //이동중인  위치 정보
    double nowlongitude;
    double nowlatitude;
    double nowaltitude;

    private Thread gpsThread = null;
    private Boolean gisRunning = true;

    LocationManager lm;

    // 타임 쓰레드 관련
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;

    Handler handler4;

    int Seconds, Minutes, MilliSeconds;

    // GET_CURRENT_DISTANCE

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_start);

        // todo: context 추가 (성훈)
        mContext = GameStart.this;

        //안드로이드 시스템에서 GPS를 통한 위치 서비스를 가져오려고함 .
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Lotti 애니메이션 셋팅
        // 경기 시작후 띄워주는 애니메이션
        animationView = (LottieAnimationView) findViewById(R.id.Lottie_Intro);
        animationView.setAnimation("funky-chicken.json");
        animationView.pauseAnimation(); //애니메이션 정지


        // TTS를 생성하고 OnInitListener로 초기화 한다.
        //경기 시작을 알리는 숫자를 읽기 위한 tts
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status != ERROR)
                {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });


        //현재위치 가져오기
        curLocation();


        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
        //소켓 관련
        try
        {
            //socket은 커넥션이 성공했을 때 커넥션에 대한 정보를 담고 있는 변수
            socket = IO.socket("http://ec2-13-125-121-5.ap-northeast-2.compute.amazonaws.com:3000");
        }

        //
        catch (Exception e)
        {
            Log.e(TAG, "onCreate: e: " + e.getMessage() );
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
                        // todo: 수신받은 메시지 가공해서 사용하기 (성훈)
                        getSocketMessage(args[0].toString());
                    }
                });
            }
        });

        // todo: 내가 보낸 메시지 돌려받기 (socket.io)
        socket.on("message_return_from_server", new Emitter.Listener()
        {
            @Override
            public void call(final Object... args)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Log.e(TAG, "run: self: " + args[0].toString());
/*
                        if (args[0].toString().equals("hello, world"))
                        {
                            Log.e(TAG, "getSocketMessage: message: " + args[0].toString());
                        } else
                        {*/
                            ioMessage = args[0].toString().split(", ");

                            Log.e(TAG, "run: self:: 메시지 보낸 유저  : " + ioMessage[0]);
                            Log.e(TAG, "run: self:: 방 번호           : " + ioMessage[1]);
                            Log.e(TAG, "run: self:: 타입              : " + ioMessage[2]);
                            Log.e(TAG, "run: self:: 내용              : " + ioMessage[3]);

                            if (ioMessage[2].equals("GameProgressSignal"))
                            {
                                if (ioMessage[3].equals("countStart"))
                                {
                                    Log.e(TAG, "run: self: 게임 시작 카운트다운 시작");

                                    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
                                    // 타이머 시작!
                                    isThread = true;
                                    thread = new Thread()
                                    {
                                        public void run()
                                        {
                                            // while (cnt <= 4)
                                            for (int i = cnt; i >= -1; i--)
                                            { //
                                                Log.i("cntcntcntcnt", "      " + i);
                                                try
                                                {
                                                    sleep(1000);
                                                } catch (InterruptedException e)
                                                {
                                                    e.printStackTrace();
                                                    thread.interrupt();
                                                }
                                                handler.sendEmptyMessage(i);
                                            }
                                        }
                                    };
                                    thread.start();
                                }
                            }

//                        }
//                        // todo: 돌려받은 메시지 가공해서 사용하기 (성훈)
//                        getSocketMessage(args[0].toString());
                    }
                });
            }
        });
        socket.connect();
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

        lottilay = (FrameLayout) findViewById(R.id.lotti_lay);
        countview = (TextView) findViewById(R.id.countnum);


        timersview = (TextView) findViewById(R.id.timers);
        distenceview = (TextView) findViewById(R.id.distence);

        // TODO: (성훈) ========================================================

        if (GET_IS_HOST)
        {
            // 1. 방장이 서버로 게임 시작 신호 전달하기
            // 2. 서버에서 방장과 참가자들에게 카운트다운 시작 신호 전달하기
            // 3. 방장과 참가자들 모두 신호 받고나서 카운트다운 시작하기

            // 타이머 사용 용도 설정
            timerType = "gameStart";
            TIMEOUT = 3000; // 타이머 진행할 시간 3초로 설정
            tempTask(); // 타이머 시작
        }

        //////


        // todo: (테스트) 1등 참가자가 다른 참가자들에게 알림 전달하기
        timersview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                /** todo: 1등 한 유저가 나머지 유저들에게 카운트다운 신호 전달하는 방법
                 * 1. 1등한 유저가 자신을 순위표 arrayList에 담기
                 * 2. 자신을 제외한 참가자들에게 카운트다운 시작 신호 전달하기
                 */
                attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "GameProgressSignal", "endCountStart");

                // 1등 한 유저만 순위표에 자신의 이름, 기록 저장하고 GameResult 화면으로 이동하기
                LIST_LANK = new ArrayList<>();
                item_lank_lists = new item_lank_list(GET_MY_NAME, lastTime);

                LIST_LANK.add(item_lank_lists);

                for (int i = 0; i < LIST_LANK.size(); i++)
                {
                    Log.e(TAG, "onClick: UserName   : " + i + "등: "+ LIST_LANK.get(i).getUserName() );
                    Log.e(TAG, "onClick: RunTime    : 기록: " + LIST_LANK.get(i).getRunTime() );
                }

                // todo: 게임 결과 화면으로 이동
                startActivity_GameResult();
            }
        });

        // todo: (테스트) 1이외의 참가자가 결승 지점에 도달, 방장에게 도착 알림 전송
        distenceview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // 방장에게 도착 알림 전송
                attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "GameProgressGoalSignal", GET_MY_NAME + " / " + lastTime);

                // todo: 게임 결과 화면으로 이동
                startActivity_GameResult();
            }
        });
    }


    // todo: 게임 결과 화면으로 이동
    private void startActivity_GameResult()
    {
        // 소켓 연결 끊고 결과 화면으로 이동
        Intent intent = new Intent(mContext, GameResult.class);
        socket.disconnect();
        startActivity(intent);
        finish();
    }

    private Context mContext;
    String ioMessage[];

    String timerType;
    long elapsed; // 4초부터 시작
    final static long INTERVAL = 1000; // 1초 에 1씩 증가
    static long TIMEOUT; // 종료 할 시간 설정 (10000 = 10초)
    private Timer timer;

    public static List<item_lank_list> LIST_LANK;
    private item_lank_list item_lank_lists;

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

                if (timerType.equals("gameStart"))
                {
                    // 2초 후 게임 시작 알림 전달
                    if (Integer.parseInt(text) == 2)
                    {
                        timer.cancel(); // 타이머 중단
                        attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "GameProgressSignal", "countStart");
                    }
                }
            }
        });
    }

    String lankList;

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

            if (ioMessage[2].equals("GameProgressSignal"))
            {
                // todo: 방장에게 게임 시작 신호 받기
                if (ioMessage[3].equals("countStart"))
                {
                    Log.e(TAG, "getSocketMessage: 게임 시작 카운트다운 시작");

                    // =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
                    // 타이머 시작!
                    isThread = true;
                    thread = new Thread()
                    {
                        public void run()
                        {

                           // while (cnt <= 4)
                            for(int i = cnt; i >= -1; i--)
                            { //
                                Log.i("cntcntcntcnt", "      " + i);
                                try
                                {
                                    sleep(1000);
                                } catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                    thread.interrupt();
                                }
                                handler.sendEmptyMessage(i);
                            }
                        }
                    };
                    thread.start();
                }

                // LIST_LANK item_lank_list
                if (ioMessage[3].equals("endCountStart"))
                {
                    // 1등 참가자를 제외한 모든 참가자들에게 알림 전달
                    Log.e(TAG, "getSocketMessage: 1등 참가자가 결정 되었습니다. 10초 카운트 다운을 시작합니다.");

                    // 카운트다운 시작
                }
            }
        }
    }

    // todo: 메시지 전송
    private void attemptSend(String sendUser, String currentRoomNum, String messageType, String message)
    {
        if (TextUtils.isEmpty(message))
        {
            Log.e(TAG, "attemptSend: message.Empty?");
            return;
        }

        String sendMessage = sendUser + ", " + currentRoomNum + ", " + messageType + ", " + message;

        socket.emit("message_from_client", sendMessage);
        Log.e(TAG, "attemptSend: 전송한 메시지: " + sendMessage);
    }

    // todo: (성훈 끝) ========================================================

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            // super.handleMessage(msg);

            if (msg.what != 0 && msg.what != -1) //5,4,3,2,1
            {
                countview.setText("" + msg.what);
                tts.setPitch(1f);         // 음성 톤을 0.5배 내려준다.
                tts.setSpeechRate(1.0f);    // 읽는 속도는 기본 설정
                tts.speak(countview.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
               Log.i("cntcntcntcnt4444","      "+msg.what);
                animationView.setVisibility(View.INVISIBLE);
              //  Glide.with(GameStart.this).load(R.raw.start).into(mini);
            } else if (msg.what == 0)
            {
                countview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
                countview.setText("START");
                tts.setPitch(1f);         // 음성 톤을 0.5배 내려준다.
                tts.setSpeechRate(1.0f);    // 읽는 속도는 기본 설정
                tts.speak(countview.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                Log.i("cntcntcntcnt5555","      "+msg.what);
               // totalthread();
            }
            else if (msg.what == -1)
            {
                Log.i("cntcntcntcnt6","      "+msg.what);
                countview.setVisibility(View.INVISIBLE);
                animationView.playAnimation();//애니메이션 start

                //타임 쓰레드
                //스탑워치 형식으로 사용자의 게임시간을 측정한다.
                handler4 = new Handler() ;
                StartTime = SystemClock.uptimeMillis();// 시스템 시간을 가져온다.
                handler4.postDelayed(runnable, 0);
                //gps 쓰레드
                //위도 경도를 실시간으로 가져온다
                gpsThread = new Thread(new gpsThread());
                gpsThread.start();


            }
        }
    };

    String lastTime;


    //Time  쓰레드
    public Runnable runnable = new Runnable()
    {

        public void run()
        {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);
            // timersview.setText("" + Minutes + ":"
            timersview.setText("" + String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            // todo: 성훈 추가
            lastTime = "" + String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds);

            handler.postDelayed(this, 0);
        }

    };

    // 거리계산 핸들러
    Handler handler3 = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            double mMiter = Double.parseDouble((String) msg.obj); // 스트링 - 더블형으로
            distenceview.setText(String.format("%.1f", mMiter) + "m");
        }
    };

    // 게임이 시작 한후 시작한 위치에서  현재 위치까지의 m를 보여줌
    // gpsThread 이용하여 m를 보여줌
    public class gpsThread implements Runnable
    {


        @Override
        public void run()
        {
            int i = 0;
            //출발 위치 받아오기
            final Location crntLocation = new Location("crntlocation");
            crntLocation.setLatitude(latitude);  //경도
            crntLocation.setLongitude(longitude); //위도
            float distance = 0;

            //gps 좌표 저장
            SharedPreferences sharedPreferences = getSharedPreferences("gpsinfo", MODE_PRIVATE);
            //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();

            editor.putFloat("gpslatitude" + i, (float) crntLocation.getLatitude()); //출발위치 경도
            editor.putFloat("gpslongitude" + i, (float) crntLocation.getLongitude()); //출발위치 위도

            Log.i("curlocation testestset", "      " + crntLocation.getLatitude() + "       " + crntLocation.getLongitude());

            editor.commit();
            while (true)
            {
                while (gisRunning)
                { //일시정지를 누르면 멈춤
                    Message msg = new Message();


                    //출발 위치 받아오기
                    final Location nowLocation = new Location("nowlocation");
                    nowLocation.setLatitude(nowlatitude);  //경도
                    nowLocation.setLongitude(nowlongitude); //위도
                    nowLocation.setAltitude(nowaltitude); //고도
                    Log.i("nowlocation testestset", "      " + nowLocation.getLatitude() + "       " + nowLocation.getLongitude());
                    //Log.i("nowlocation testestset","      "+(float)nowLocation.getLatitude()+"       "+(float)nowLocation.getLongitude());

                    //  거리 계산
                   // if (nowLocation.getAltitude() == 0.0 && nowLocation.getLatitude() != 0.0)
                   // {
                        i++;
                        editor.putFloat("gpslatitude" + i, (float) nowLocation.getLatitude()); // key,//경도
                        editor.putFloat("gpslongitude" + i, (float) nowLocation.getLongitude()); // key,//위도
                        editor.commit();
                        distance = crntLocation.distanceTo(nowLocation);///1000; //in km
                        Log.i("Test_Log", "   출발거리와 현재 위치 간 거리" + distance);
                   // }


                    // 거리( 더블형 ) 스트링으로 보냄
                    String information = new String("" + distance);
                    msg.obj = information;

                    // 거리 받아오기
                    GET_CURRENT_DISTANCE = distance;
                    Log.e(TAG, "run: GET_CURRENT_DISTANCE: " + GET_CURRENT_DISTANCE );


                    handler3.sendMessage(msg);

                    try
                    {
                        Thread.sleep(1000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
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
    void curLocation()
    {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(GameStart.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        } else
        {
            //가장최근의 위치정보를 가지고옵니다
            //provider = location.getProvider(); // 위치 정보
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null)
            {
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

            } else
            {
                //This is what you need:
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);

            }
        }

    }

    //현재 위도 경도 위치 가져오기 리스너
    final LocationListener gpsLocationListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {

            nowprovider = location.getProvider();
            nowlongitude = location.getLongitude();
            nowlatitude = location.getLatitude();
            nowaltitude = location.getAltitude();
            Log.i("Guard_onLocationChanged", "위치정보 : " + nowprovider + "\n" +
                    "위도 : " + nowlongitude + "\n" +
                    "경도 : " + nowlatitude + "\n" +
                    "고도  : " + nowaltitude);
        }

        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        public void onProviderEnabled(String provider)
        {

        }

        public void onProviderDisabled(String provider)
        {

        }
    };


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        handler.removeCallbacks(runnable); //타임쓰레드 정지 시킴
        gpsThread.interrupt();  //gps 도 멈춤.
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if (tts != null)
        {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

}
