package com.example.chickenrun.gameRoom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chickenrun.GameStart;
import com.example.chickenrun.R;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.chickenrun.Lobby.Activity_Lobby.GET_IS_HOST;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_MY_JOIN_INDEX;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_MY_NAME;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_ROOM_INDEX;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_ROOM_NAME;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_RUN_DISTANCE;
import static com.example.chickenrun.Lobby.Activity_Lobby.HANDLER_DELETE;

public class Activity_Waiting_Room extends AppCompatActivity
{
    private String TAG = "Activity_Waiting_Room";

    private Context mContext;
    private RecyclerView Participant_list;
    private adapterWaitingRoom adapterWaitingRoom;

    private List<item_participant_user> itemParticipant;
    private item_participant_user item_participant_user;

    private TextView button_waiting_ready;

    // 소켓 연결 설정
    private Socket socket;

    boolean isReady;

    // 레디 버튼 클릭 신호 수신
    public static Handler HANDLER_READY_CLICK;

    private FrameLayout user_space;
    public static int GET_HEIGHT_USER_SPACE;
    public static List<item_participant_user> GET_LANK_LIST;

    private int[] imageIds=
            {
                    R.drawable.ic_person_pink,
                    R.drawable.ic_person_blue,
                    R.drawable.ic_person_black,
                    R.drawable.ic_person_gray
            };

    boolean isGameOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        mContext = Activity_Waiting_Room.this;

        // 준비 버튼 클릭 (소켓 통신)
        button_waiting_ready = findViewById(R.id.button_waiting_ready);

        // 방 제목 세팅
        TextView room_name = findViewById(R.id.room_name);
        room_name.setText(GET_ROOM_NAME + " (" + GET_RUN_DISTANCE + "m)");

        // todo: 소켓 io 통신 설정
        setSocketReceive();

        // todo: 레디 버튼 제어
        ReadyTask();
    }

    // todo: 소켓 io 통신 설정
    private void setSocketReceive()
    {
        // todo: nodeJS 서버 접속 (socket.io)
        try
        {
            socket = IO.socket("http://ec2-13-125-121-5.ap-northeast-2.compute.amazonaws.com:3000");
            Log.e(TAG, "onCreate: socket: " + socket);
        } catch (Exception e)
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
    }

    // todo: 수신받은 메시지 가공해서 사용하기
    String ioMessage[];
    String getUserList[];
    String getUserListSplit[];
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

                // 방 입장 처리
                userStateManagement("Come", false, ioMessage[3]);
            }

            // todo: 유저 퇴장
            if (ioMessage[2].equals("outUser"))
            {
                // 퇴장한 유저
                Log.e(TAG, "getSocketMessage: 퇴장한 유저: " + ioMessage[3]);
            }

            // todo: 레디 알림 받기
            if (ioMessage[2].equals("Ready"))
            {
                // 상대방의 레디 알림 'On' 신호 받기
                if (ioMessage[3].equals("On"))
                {
                    for (int i = 0; i < itemParticipant.size(); i++)
                    {
                        // 레디 on 하는 상대방의 닉네임
                        if (itemParticipant.get(i).getUserName().equals(ioMessage[0]))
                        {
                            Log.e(TAG, "onClick: ready on: " + ioMessage[0]);
                            itemParticipant.get(i).setReady(true);
                            adapterWaitingRoom.notifyDataSetChanged();
                            Participant_list.setAdapter(adapterWaitingRoom);
                            readyCount ++;
                        }
                    }
                }

                // 상대방의 레디 알림 'Off' 신호 받기
                else
                {
                    for (int i = 0; i < itemParticipant.size(); i++)
                    {
                        // 레디 off 하는 상대방의 닉네임
                        if (itemParticipant.get(i).getUserName().equals(ioMessage[0]))
                        {
                            Log.e(TAG, "onClick: ready off: " + ioMessage[0]);
                            itemParticipant.get(i).setReady(false);
                            adapterWaitingRoom.notifyDataSetChanged();
                            Participant_list.setAdapter(adapterWaitingRoom);
                            readyCount --;
                        }
                    }
                }
            }

            // todo: 참가자가 방장에게 유저 목록 넘겨받기
            if (ioMessage[2].equals("getUserList"))
            {
                Log.e(TAG, "getSocketMessage: 방장에게 유저 목록을 넘겨 받았습니다.");

                getUserList = ioMessage[3].split(" / ");

                /** (목록 형식)
                 *
                 * userName_false / userName_false / userName_true
                 * 유저 이름_레디 여부 / 유저 이름_레디 여부 / 유저 이름_레디 여부
                 *
                 */

                // 참가자의 arrayList 초기화
                itemParticipant = new ArrayList<>();
                for (int i = 0; i < getUserList.length; i++)
                {
                    Log.e(TAG, "getSocketMessage: getUserList[" + i + "]: " + getUserList[i]);

                    getUserListSplit = getUserList[i].split("_");
                    Log.e(TAG, "getSocketMessage: getUserListSplit[0]: " + getUserListSplit[0]);
                    Log.e(TAG, "getSocketMessage: getUserListSplit[1]: " + getUserListSplit[1]);

                    item_participant_user = new item_participant_user(getUserListSplit[0], false, Boolean.valueOf(getUserListSplit[1]), imageIds);
                    itemParticipant.add(item_participant_user); // 생성자에 세팅한 값으로 List 추가
                }

                for (int i = 0; i < itemParticipant.size(); i++)
                {
                    Log.e(TAG, "getSocketMessage: itemParticipant: " + itemParticipant );
                }

                // 방장은 인덱스 0번 유저로 고정
                itemParticipant.get(0).setHost(true);

                // 참가자의 arrayList 값 새로 세팅
//                adapterWaitingRoom.notifyDataSetChanged();
                adapterWaitingRoom = new adapterWaitingRoom(mContext, itemParticipant);
                Participant_list.setAdapter(adapterWaitingRoom);
            }

            // todo: 게임 시작 신호받기
            if (ioMessage[2].equals("Game"))
            {
                if (ioMessage[3].equals("Start"))
                {
                    // 게임 진행할 총 인원 수 구하기. (리타이어 인원 수 간추릴 때 사용)
                    GET_JOIN_USER_COUNT = itemParticipant.size();

                    socket.disconnect();
                    Intent intent = new Intent(mContext, GameStart.class);
                    startActivity(intent);
                    Toast.makeText(mContext, "곧 게임이 시작됩니다! ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // todo: 방 입장, 퇴장 처리
    boolean isGameStart = false;
    private void userStateManagement(String Task, boolean areYouHost, String userName)
    {
        // 신규 유저가 방 입장했을 때 리사이클러뷰 처리
        if (Task.equals("Come"))
        {
            // 방장에게 유저 목록 정리 맡기기 (방장 = 어레이 리스트 0번 유저)
            if (GET_IS_HOST)
            {
                Log.e(TAG, "userStateManagement: 방장 권한으로 유저를 추가함 ");

                // 생성자에 데이터 세팅하기
                item_participant_user = new item_participant_user(userName, areYouHost, false, imageIds);
                itemParticipant.add(item_participant_user); // 생성자에 세팅한 값으로 List 추가

                // 리사이클러뷰 갱신
                adapterWaitingRoom.notifyDataSetChanged();
                Participant_list.setAdapter(adapterWaitingRoom);

                String userList = null;

                for (int i = 0; i < itemParticipant.size(); i++)
                {
                    if (TextUtils.isEmpty(userList) || userList.length() == 0)
                    {
                        userList = itemParticipant.get(i).getUserName() + "_" + itemParticipant.get(i).isReady();
                    } else
                    {
                        userList = userList + " / " + itemParticipant.get(i).getUserName() + "_" + itemParticipant.get(i).isReady();
                    }
                }

                Log.e(TAG, "userStateManagement: 참가자들에게 목록 전달: " + userList);

                attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "getUserList", userList);
            }

            // todo: 참가자는 방장이 정리해준 유저 목록 받기, 리사이클러뷰에 세팅하기
            else
            {
                // 생성자에 데이터 세팅하기
                item_participant_user = new item_participant_user(userName, areYouHost, false, imageIds);
                itemParticipant.add(item_participant_user); // 생성자에 세팅한 값으로 List 추가

                // 리사이클러뷰 갱신
                adapterWaitingRoom.notifyDataSetChanged();
                Participant_list.setAdapter(adapterWaitingRoom);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {

        // 유저를 표시할 공간 크기 구하기 (높이만)
        user_space = findViewById(R.id.user_space);
        GET_HEIGHT_USER_SPACE = user_space.getHeight();
        Log.e(TAG, "onCreate: GET_HEIGHT_USER_SPACE: " + GET_HEIGHT_USER_SPACE);
        Log.e(TAG, "onCreate: GET_HEIGHT_USER_SPACE / 2: " + GET_HEIGHT_USER_SPACE / 2);

        // 리사이클러뷰 세팅
        Participant_list = findViewById(R.id.waiting_room_participant);
        Participant_list.setHasFixedSize(true);
        Participant_list.setLayoutManager(new GridLayoutManager(mContext, 2));

        itemParticipant = new ArrayList<item_participant_user>();

        // todo: 방장 먼저 리스트에 추가하기
        if (GET_IS_HOST)
        {
            if (isGameStart || isGameOut)
            {
                Log.e(TAG, "onWindowFocusChanged: 게임 시작" );
            }

            else
            {
                // 생성자에 데이터 세팅하기

                /*
                    R.drawable.ic_person_pink,
                    R.drawable.ic_person_blue,
                    R.drawable.ic_person_black,
                    R.drawable.ic_person_gray
                **/

                item_participant_user = new item_participant_user(GET_MY_NAME, GET_IS_HOST, false, imageIds);
                itemParticipant.add(item_participant_user); // 생성자에 세팅한 값으로 List 추가
            }
         }

        // 구분선 세팅
        Participant_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        Participant_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL));

        adapterWaitingRoom = new adapterWaitingRoom(mContext, itemParticipant);
        Participant_list.setAdapter(adapterWaitingRoom);
    }

    // todo: 레디 버튼 제어
    int readyCount;

    public static int GET_JOIN_USER_COUNT;
    private void ReadyTask()
    {
        button_waiting_ready.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (GET_IS_HOST)
                {
                    // 참가자가 한 명일 경우
                    if (itemParticipant.size() == 1)
                    {
                        Toast.makeText(mContext, "아직 준비되지 않았습니다", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 참가자가 두 명일 경우
                    else if (itemParticipant.size() == 2)
                    {
                        if (1 > readyCount)
                        {
                            Toast.makeText(mContext, "아직 준비되지 않았습니다", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // 참가자가 세 명일 경우
                    else if (itemParticipant.size() == 3)
                    {
                        if (2 > readyCount)
                        {
                            Toast.makeText(mContext, "아직 준비되지 않았습니다", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // 참가자가 네 명일 경우
                    else if (itemParticipant.size() == 4)
                    {
                        if (3 > readyCount)
                        {
                            Toast.makeText(mContext, "아직 준비되지 않았습니다", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }


                    // 게임 진행할 총 인원 수 구하기. (리타이어 인원 수 간추릴 때 사용)
                    GET_JOIN_USER_COUNT = itemParticipant.size();

                    // 게임 시작 액티비티로 이동
                    Intent intent = new Intent(mContext, GameStart.class);
                    Toast.makeText(mContext, "곧 게임이 시작됩니다! ", Toast.LENGTH_SHORT).show();
                    // todo: 참가자들에게 게임 시작 알림 전달하기
                    attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "Game", "Start");

                    // 소켓 연결 해제
                    socket.disconnect();

                    isGameStart = true;

                    startActivity(intent);
                }

                Log.e(TAG, "onCreate: button_waiting_ready: 클릭함");
                if (isReady)
                {
                    // todo: ready off
                    for (int i = 0; i < itemParticipant.size(); i++)
                    {
                        if (itemParticipant.get(i).getUserName().equals(GET_MY_NAME))
                        {
                            Log.e(TAG, "onClick: ready off: Me");

                            itemParticipant.get(i).setReady(false);
                            adapterWaitingRoom.notifyDataSetChanged();
                            Participant_list.setAdapter(adapterWaitingRoom);
                            button_waiting_ready.setText("준 비");
                            Log.e(TAG, "onClick: itemParticipant: [" + i + "]" + itemParticipant.get(i).isReady);

                            readyCount --;

                            // '준비중이 아님' 메시지를 유저들에게 전달하기
                            attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "Ready", "Off");
                            isReady = false;
                        }
                    }
                }

                // todo: ready on
                else
                {
                    for (int i = 0; i < itemParticipant.size(); i++)
                    {
                        if (itemParticipant.get(i).getUserName().equals(GET_MY_NAME))
                        {
                            Log.e(TAG, "onClick: ready on: Me");
                            itemParticipant.get(i).setReady(true);
                            adapterWaitingRoom.notifyDataSetChanged();
                            Participant_list.setAdapter(adapterWaitingRoom);
                            button_waiting_ready.setText("준 비 취 소");
                            Log.e(TAG, "onClick: itemParticipant: [" + i + "]" + itemParticipant.get(i).isReady);

                            readyCount ++;

                            // '준비중' 메시지를 유저들에게 전달하기
                            attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "Ready", "On");
                            isReady = true;
                        }
                    }
                }
            }
        });
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // 소켓 연결 해제
//        socket.disconnect();
//        socket.off("new message", );

        // todo: 방 퇴장하기 (mysql)
        disconnectRoom(GET_ROOM_INDEX, GET_MY_JOIN_INDEX);
    }

    @Override
    protected void onPause()
    {
        isGameOut = true;
        super.onPause();
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
}


/*    // todo: 참가중인 유저 불러오기
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
*//*
                for (int i = 0; i < itemParticipant.size(); i++)
                {
                    Log.e(TAG, "onResponse: itemLobbyList: " + itemParticipant.get(i));
                }

                adapterParticipantList = new Adapter_Participant(mContext, itemParticipant);
                Participant_list.setAdapter(adapterParticipantList);
*//*
            }

            @Override
            public void onFailure(Call<List<item_participant_user>> call, Throwable t)
            {
                Log.e(TAG, "onFailure: t: " + t.getMessage());
            }
        });
    }*/

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