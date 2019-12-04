package com.example.chickenrun.gameRoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chickenrun.ApiClient;
import com.example.chickenrun.ApiInterface;
import com.example.chickenrun.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.chickenrun.Lobby.Activity_Lobby.GET_IS_HOST;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_MY_JOIN_INDEX;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_MY_NAME;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_ROOM_INDEX;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_ROOM_NAME;
import static com.example.chickenrun.Lobby.Activity_Lobby.HANDLER_DELETE;
import static com.example.chickenrun.gameRoom.adapterWaitingRoom.itemParticipant;

public class Activity_Waiting_Room extends AppCompatActivity
{
    private String TAG = "Activity_Waiting_Room";

    private Context mContext;
    private RecyclerView Participant_list;
    private int Participant_list_size;
    //    private Adapter_Participant adapterParticipantList;
    private adapterWaitingRoom adapterWaitingRoom;

    //    private List<item_participant_user> itemParticipant;
    private List<item_participant_user> itemParticipant = new ArrayList<>();
    private item_participant_user item_participant_user;

    private TextView button_waiting_ready;

    // 소켓 연결 설정
    private Socket socket;

    boolean isReady;

    // 레디 버튼 클릭 신호 수신
    public static Handler HANDLER_READY_CLICK;
    private Handler joinUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        mContext = Activity_Waiting_Room.this;

//        itemParticipant = new ArrayList<>();

        // 준비 버튼 클릭 (소켓 통신)
        button_waiting_ready = findViewById(R.id.button_waiting_ready);

        // todo: nodeJS 서버 접속 (socket.io)
        try
        {
            socket = IO.socket("http://ec2-13-125-121-5.ap-northeast-2.compute.amazonaws.com:3000");
            Log.e(TAG, "onCreate: socket: " + socket);
        }

        catch (Exception e)
        {
            Log.e(TAG, "onCreate: e: " + e.toString());
            e.printStackTrace();
        }

        // todo: 상대방의 메시지 수신 대기 (socket.io)
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener()
        {
            @Override
            public void call(Object... args)
            {

                if (GET_IS_HOST)
                {
                    Log.e(TAG, "call: 방장으로 접속함, 입장 메시지 전송 안 함");
                }

                // todo: 입장 알림 메시지 전송
                else
                {
                    Log.e(TAG, "call: 참가자로 접속함, 입장 메시지 전송");
                    attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "comeUser", GET_MY_NAME);
                }


            }
        }).on("message_from_server", new Emitter.Listener()
        {
            @Override
            public void call(final Object... args)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Log.e(TAG, "run: args[0]: " + args[0].toString());

                        // todo: 수신받은 메시지 가공해서 사용하기
                        getSocketMessage(args[0].toString());
                    }
                });
            }
        });
        socket.connect();

        TextView room_name = findViewById(R.id.room_name);
        room_name.setText(GET_ROOM_NAME);

        // 리사이클러뷰 세팅
        Participant_list = findViewById(R.id.waiting_room_participant);
        Participant_list.setHasFixedSize(true);
        Participant_list.setLayoutManager(new GridLayoutManager(mContext, 2));

        // todo: 방장 먼저 리스트에 추가하기
        if (GET_IS_HOST)
        {
            // 생성자에 데이터 세팅하기
            item_participant_user = new item_participant_user(GET_MY_NAME, GET_IS_HOST, false);
            itemParticipant.add(item_participant_user); // 생성자에 세팅한 값으로 List 추가
        }

        // 구분선 세팅
        Participant_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        Participant_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL));

        adapterWaitingRoom = new adapterWaitingRoom(mContext, itemParticipant);
        Participant_list.setAdapter(adapterWaitingRoom);


        // todo: 방 입장, 퇴장 처리
        // 방 생성자라면 방장 활성화
//        userStateManagement("Come", GET_IS_HOST, GET_MY_NAME);
        Log.e(TAG, "onCreate: itemParticipant.size(): " + itemParticipant.size());
        for (int i = 0; i < itemParticipant.size(); i++)
        {
            Log.e(TAG, "onCreate: itemParticipant: Name: " + itemParticipant.get(i).getUserName());
        }

        // todo: 레디 버튼 제어
        ReadyTask();

        // todo: 참가중인 유저 불러오기
        getParticipantList();
    }

    // todo: 레디 버튼 제어
    private void ReadyTask()
    {
        button_waiting_ready.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e(TAG, "onCreate: button_waiting_ready: 클릭함");

                if (isReady)
                {
                    // 리사이클러뷰로 ready 알림 보내주기
                    Message hdmg = HANDLER_READY_CLICK.obtainMessage();
                    Log.e(TAG, "onClick: hdmg = msgHandler.obtainMessage(): " + hdmg);
                    hdmg.what = 1112;
                    hdmg.obj = "ready_off_recycler_view, " + GET_MY_NAME;
                    Log.e(TAG, "onClick: myName: ");
                    HANDLER_READY_CLICK.sendMessage(hdmg);

                    button_waiting_ready.setText("준 비");

                    // 준비 상태 활성화
                    attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "Ready", "Off");
                    isReady = false;
                }

                //
                else
                {
                    Message hdmg = HANDLER_READY_CLICK.obtainMessage();
                    Log.e(TAG, "onClick: hdmg = msgHandler.obtainMessage(): " + hdmg);
                    hdmg.what = 1112;
                    hdmg.obj = "ready_on_recycler_view, " + GET_MY_NAME;
                    HANDLER_READY_CLICK.sendMessage(hdmg);

                    button_waiting_ready.setText("준 비 취 소");

                    // 준비 상태 비활성화
                    attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "Ready", "On");

                    isReady = true;
                }
            }
        });
    }

    String ioMessage[];

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

            // todo: 유저 입장
            if (ioMessage[2].equals("comeUser"))
            {
                // 입장한 유저
                Log.e(TAG, "getSocketMessage: 입장한 유저: " + ioMessage[3]);

                // todo: 방 입장, 퇴장 처리
                userStateManagement("Come", false, ioMessage[3]);
            }

            // todo: 유저 퇴장
            if (ioMessage[2].equals("outUser"))
            {
                // 퇴장한 유저
                Log.e(TAG, "getSocketMessage: 퇴장한 유저: " + ioMessage[3]);

            }
        }
    }

    // todo: 방 입장, 퇴장 처리
    private void userStateManagement(String Task, boolean areYouHost, String userName)
    {
        // 신규 유저가 방 입장했을 때 리사이클러뷰 처리
        if (Task.equals("Come"))
        {
            // 방장에게 유저 목록 정리 맡기기 & 방장인 회원 추리기
            if (GET_IS_HOST)
            {
                Log.e(TAG, "userStateManagement: 방장 권한으로 유저를 추가함 ");

                // 생성자에 데이터 세팅하기
                item_participant_user = new item_participant_user(userName, areYouHost, false);

                if (itemParticipant == null)
                {
                    Log.e(TAG, "userStateManagement: itemParticipant: null" );
                }
                else
                {
                    itemParticipant.add(item_participant_user); // 생성자에 세팅한 값으로 List 추가
                }

                // 리사이클러뷰 갱신
                adapterWaitingRoom.notifyDataSetChanged();
                Participant_list.setAdapter(adapterWaitingRoom);

                Log.e(TAG, "userStateManagement: 방장이 유저 목록을 갱신함");
            }

            // todo: 참가자는 방장이 정리해준 유저 목록 받기, 리사이클러뷰에 세팅하기
            else
            {

            }
        }

        // 방 퇴장했을 때 리사이클러뷰 처리
        if (Task.equals("Out"))
        {
            // 리사이클러뷰 갱신
            adapterWaitingRoom.notifyDataSetChanged();
            Participant_list.setAdapter(adapterWaitingRoom);
        }
    }

    // 메시지 전송
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // 소켓 연결 해제
        socket.disconnect();
        // socket.off("new message", onNewMessage);

        // todo: 방 퇴장하기 (mysql)
        disconnectRoom(GET_ROOM_INDEX, GET_MY_JOIN_INDEX);
    }

    // todo: 방 퇴장하기 (mysql)
    public void disconnectRoom(final String roomIndex, final String myJoinIndex)
    {
        Log.e(TAG, "disconnectRoom: 방 퇴장 처리하기");

        StringRequest stringRequest
                = new StringRequest(Request.Method.POST,
                "http://ec2-13-125-121-5.ap-northeast-2.compute.amazonaws.com/chicken/outRoom.php",
                new com.android.volley.Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "addPaymentHistory onResponse: " + response.trim());

                        // 방 퇴장 처리 완료 메시지
                        if (response.trim().equals("success_room_delete"))
                        {
                            Log.e(TAG, "createRoom: 방 퇴장 처리 완료");

                            // 방 삭제되면 핸들러로 Activity_Lobby로 삭제 알림 보내주기 (방 목록 새로고침)
                            Message hdmg = HANDLER_DELETE.obtainMessage();
                            Log.e(TAG, "ReceiveThread: hdmg = msgHandler.obtainMessage(): " + hdmg);
                            hdmg.what = 1111;
                            hdmg.obj = "refreshList_deleteRoom";
                            HANDLER_DELETE.sendMessage(hdmg);

                        }
                    }
                },
                new com.android.volley.Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("VolleyError", "에러: " + error.toString());
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError
            {
                Map<String, String> params = new HashMap<>();

                params.put("roomIndex", roomIndex);
                params.put("myJoinIndex", myJoinIndex);

                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);

    }

    // todo: 참가중인 유저 불러오기
    private void getParticipantList()
    {
        Log.e(TAG, "getParticipantList: 참가중인 유저 불러오기");

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Defining retrofit api service
        ApiInterface paracticipantRequest = retrofit.create(ApiInterface.class);

        // defining the call
        Call<List<item_participant_user>> listCall = paracticipantRequest.getParacticipant("");

        listCall.enqueue(new Callback<List<item_participant_user>>()
        {
            @Override
            public void onResponse(Call<List<item_participant_user>> call, Response<List<item_participant_user>> response)
            {
                itemParticipant = response.body();
/*
                for (int i = 0; i < itemParticipant.size(); i++)
                {
                    Log.e(TAG, "onResponse: itemLobbyList: " + itemParticipant.get(i));
                }

                adapterParticipantList = new Adapter_Participant(mContext, itemParticipant);
                Participant_list.setAdapter(adapterParticipantList);
*/
            }

            @Override
            public void onFailure(Call<List<item_participant_user>> call, Throwable t)
            {
                Log.e(TAG, "onFailure: t: " + t.getMessage());
            }
        });
    }
}


/*        // todo: 내가 보낸 메시지 돌려받기 (socket.io)
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

                        // todo: 수신받은 메시지 가공해서 사용하기
                        getSocketMessage(args[0].toString());

                        // Toast.makeText(mContext, args[0].toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }); socket.connect();*/