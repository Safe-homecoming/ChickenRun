package com.example.chickenrun.gameRoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

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

public class Activity_Waiting_Room extends AppCompatActivity
{
    private String TAG = "Activity_Waiting_Room";

    private Context mContext;
    private RecyclerView Participant_list;
    private int Participant_list_size;
    private Adapter_Participant adapterParticipantList;

    private List<item_participant_user> itemParticipant;
    private item_participant_user item_participant_user;

    private TextView button_waiting_ready;

    // 소켓 연결 설정
    private Socket socket;

    boolean isReady;

    // 레디 버튼 클릭 신호 수신
    public static Handler readyClick;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        mContext = Activity_Waiting_Room.this;

        itemParticipant = new ArrayList<>();

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
                    Log.e(TAG, "call: 방장으로 접속함, 입장 메시지 전송 안 함" );
                }

                // todo: 입장 알림 메시지 전송
                else
                {
                    Log.e(TAG, "call: 참가자로 접속함, 입장 메시지 전송" );
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

                        // Toast.makeText(mContext, args[0].toString(), Toast.LENGTH_SHORT).show();
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

        adapterParticipantList = new Adapter_Participant(mContext, itemParticipant);
        Participant_list.setAdapter(adapterParticipantList);

        // todo: 방 입장, 퇴장 처리
        // 방 생성자라면 방장 활성화
//        userStateManagement("Come", GET_IS_HOST, GET_MY_NAME);
        Log.e(TAG, "onCreate: itemParticipant.size(): " + itemParticipant.size() );
        for (int i = 0; i < itemParticipant.size(); i++)
        {
            Log.e(TAG, "onCreate: itemParticipant: Name: " + itemParticipant.get(i).getUserName() );
        }

        // 준비 버튼 클릭 (소켓 통신)
        button_waiting_ready = findViewById(R.id.button_waiting_ready);
        button_waiting_ready.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e(TAG, "onCreate: button_waiting_ready: 클릭함" );
//                Log.e(TAG, "onCreate: itemParticipant.size(): " + itemParticipant.size() );
                Log.e(TAG, "onCreate: adapterParticipantList.getItemCount(): " + adapterParticipantList.getItemCount() );

                for (int i = 0; i < adapterParticipantList.getItemCount(); i++)
                {
                    if (itemParticipant.get(i).getUserName().equals(GET_MY_NAME))
                    {
                        if (isReady)
                        {
                            // 리사이클러뷰로 레디 알림 보내주기
                            Message hdmg = readyClick.obtainMessage();
                            Log.e(TAG, "onClick: hdmg = msgHandler.obtainMessage(): " + hdmg);
                            hdmg.what = 1112;
                            hdmg.obj = "ready_off_recycler_view, " + GET_MY_NAME;
                            readyClick.sendMessage(hdmg);

                            // 준비 상태 활성화
                            attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "Ready", "Off");
                            isReady = false;
                        }

                        //
                        else
                        {
                            Message hdmg = readyClick.obtainMessage();
                            Log.e(TAG, "onClick: hdmg = msgHandler.obtainMessage(): " + hdmg);
                            hdmg.what = 1112;
                            hdmg.obj = "ready_on_recycler_view";
                            readyClick.sendMessage(hdmg);

                            // 준비 상태 비활성화
                            attemptSend(GET_MY_NAME, GET_ROOM_INDEX, "Ready", "On");

                            isReady = true;
                        }
                    }
                }
            }
        });

        // todo: 참가중인 유저 불러오기
        getParticipantList();
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
                for (int i = 0; i < itemParticipant.size(); i++)
                {
                    // isHost == true: 방장
                    if (itemParticipant.get(i).isHost)
                    {
                        Log.e(TAG, "userStateManagement: 방장 권한으로 유저 추가함 " + itemParticipant.get(i).isHost );

                        // 생성자에 데이터 세팅하기
                        item_participant_user = new item_participant_user(userName, areYouHost, false);
                        itemParticipant.add(item_participant_user); // 생성자에 세팅한 값으로 List 추가

                        // 리사이클러뷰 갱신
                        adapterParticipantList.notifyDataSetChanged();
                        Participant_list.setAdapter(adapterParticipantList);

                        Log.e(TAG, "userStateManagement: 방장이 유저 목록을 갱신함");
                    }
                }
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
            adapterParticipantList.notifyDataSetChanged();
            Participant_list.setAdapter(adapterParticipantList);
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

    // todo: 참가자 유저 어댑터 (리사이클러뷰)
    public class Adapter_Participant extends RecyclerView.Adapter<Adapter_Participant.ViewHolder>
    {
        public Adapter_Participant(Context context, List<item_participant_user> item_participant_users)
        {
            mContext = context;
            itemParticipant = item_participant_users;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            // 아이템 레이아웃 연결
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_waiting_user, parent, false);

            return new ViewHolder(view);
        }

        String readyReceive[];

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
        {
            // 화면 비율 구하기
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) holder.player_image.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int deviceWidth = displayMetrics.widthPixels;  // 핸드폰의 가로 해상도를 구함.
//            int deviceHeight = displayMetrics.heightPixels;  // 핸드폰의 세로 해상도를 구함.
            Log.e(TAG, "onBindViewHolder: deviceWidth    : " + deviceWidth );

            deviceWidth = deviceWidth / 2;
            Log.e(TAG, "onBindViewHolder: deviceWidth / 2: " + deviceWidth );

            int deviceHeight = (int) ((float) deviceWidth * 1.05);  // 세로의 길이를 가로의 길이의 1배로 or 1.5 = 1.5배로
//            int deviceHeight = deviceWidth * 1.1;  // 세로의 길이를 가로의 길이의 1배로 or 1.5 = 1.5배로

            // 아 시발 비율 맞추기 존나 어렵네 ㅠㅠㅠㅠㅠ
            holder.player_image.getLayoutParams().width = deviceWidth;  // 아이템 뷰의 세로 길이를 구한 길이로 변경
            holder.player_image.getLayoutParams().height = deviceHeight;  // 아이템 뷰의 세로 길이를 구한 길이로 변경
            holder.player_image.requestLayout(); // 변경 사항 적용

            /**
             이렇게 하면 동적으로 원하는 크기의 아이템 항목을 구성할 수 있고,
             재사용 시 스크롤도 자연스럽게 이동한다.
             (다만 조금 느린 감이 있다.)

             매번 디스 플레이의 값을 구하지 않고,
             메인에서 한 번만 호출하여 등록해도 된다.

             출처: https://flymogi.tistory.com/entry/안드로이드-리사이클러-뷰-그리드-레이아웃-아이템-세로-동적-비율 [하늘을 난 모기]
            */

            // 참가자 닉네임
            holder.join_user_name.setText(itemParticipant.get(position).getUserName());

            // 방 삭제 알림 수신 대기 (핸들러)
            readyClick = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    if (msg.what == 1112)
                    {
                        String receive = msg.obj.toString();
                        Log.e(TAG, "handleMessage: receive: " + receive );

                        readyReceive = receive.split(", ");

                        if (position == Integer.parseInt(readyReceive[1]))
                        {
                            if (readyReceive[0].equals("ready_off_recycler_view"))
                            {
                                Log.e(TAG, "handleMessage: " + readyReceive[1] + "유저 ready Off" );
                                holder.button_ready.setVisibility(View.GONE);
                            }
                            else if(readyReceive[0].equals("ready_on_recycler_view"))
                            {
                                Log.e(TAG, "handleMessage: " + readyReceive[1] + "유저 ready On" );
                                holder.button_ready.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            };

            // 준비 버튼 클릭
            holder.button_ready.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                }
            });
        }

        @Override
        public int getItemCount()
        {
            return itemParticipant.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public View view;
            public TextView join_user_name;
            public TextView host_mark;
            public TextView button_ready;
            public ImageView player_image;


            public ViewHolder(@NonNull View itemView)
            {
                super(itemView);

                view = itemView;

                join_user_name = itemView.findViewById(R.id.join_user_name);
                host_mark = itemView.findViewById(R.id.host_mark);
                button_ready = itemView.findViewById(R.id.button_ready);
                player_image = itemView.findViewById(R.id.player_image);
            }
        }
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