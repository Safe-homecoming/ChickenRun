package com.example.chickenrun.gameRoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chickenrun.ApiClient;
import com.example.chickenrun.ApiInterface;
import com.example.chickenrun.Lobby.Activity_Lobby;
import com.example.chickenrun.Lobby.item_lobby_list;
import com.example.chickenrun.R;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.chickenrun.Lobby.Activity_Lobby.GET_MY_JOIN_INDEX;
import static com.example.chickenrun.Lobby.Activity_Lobby.GET_ROOM_INDEX;
import static com.example.chickenrun.Lobby.Activity_Lobby.HANDLER_DELETE;

public class Activity_Waiting_Room extends AppCompatActivity
{

    String TAG = "Activity_Waiting_Room";

    private Context mContext;
    RecyclerView Participant_list;
    Adapter_Participant adapterParticipantList;

    private List<item_participant_user> itemParticipant;

    TextView button_waiting_ready;

    // 소켓 연결 설정
    private Socket mSocket;
    {
        try
        {
            Log.e(TAG, "instance initializer: 소켓 연결 시작" );
            mSocket = IO.socket("http://chat.socket.io");
            Log.e(TAG, "instance initializer: mSocket: " + mSocket);
        }

        catch (URISyntaxException e)
        {
            Log.e(TAG, "instance initializer: URISyntaxException: " + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        mContext = Activity_Waiting_Room.this;

        // 소켓 연결
        mSocket.on("new message", onNewMessage);
        mSocket.connect();

        // 리사이클러뷰 세팅
        Participant_list = findViewById(R.id.waiting_room_participant);
        Participant_list.setHasFixedSize(true);
        Participant_list.setLayoutManager(new LinearLayoutManager(mContext));

        // 준비 버튼 클릭
        button_waiting_ready = findViewById(R.id.button_waiting_ready);
        button_waiting_ready.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        // todo: 참가중인 유저 불러오기
        getParticipantList();
    }

    // 메시지 전송
    private void attemptReadyOn()
    {
        String message = "Ready|on";
        if (TextUtils.isEmpty(message))
        {
            return;
        }

        mSocket.emit("new message", message);
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.e(TAG, "run: data: " + data );
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        Log.e(TAG, "run: username" + username );

                    } catch (JSONException e) {
                        return;
                    }

                    // 메시지 수신받기
                    // add the message to view
//                    addMessage(username, message);
                }
            });
        }
    };

    // 소켓 연결 해제
    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);

        // todo: 방 퇴장하기 (mysql)
        disconnectRoom(GET_ROOM_INDEX, GET_MY_JOIN_INDEX);
    }

    // todo: 방 퇴장하기 (mysql)
    public void disconnectRoom(final String roomIndex, final String myJoinIndex)
    {
        Log.e(TAG, "disconnectRoom: 방 퇴장 처리하기" );

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
                            Log.e(TAG, "createRoom: 방 퇴장 처리 완료" );

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

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            // 참가자 닉네임
            holder.join_user_name.setText("");

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

            public ViewHolder(@NonNull View itemView)
            {
                super(itemView);

                view = itemView;

                join_user_name = itemView.findViewById(R.id.join_user_name);
                host_mark = itemView.findViewById(R.id.host_mark);
                button_ready = itemView.findViewById(R.id.button_ready);
            }
        }

    }
}
