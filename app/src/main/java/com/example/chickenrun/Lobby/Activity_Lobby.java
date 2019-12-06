package com.example.chickenrun.Lobby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chickenrun.ApiClient;
import com.example.chickenrun.ApiInterface;
import com.example.chickenrun.BuyChicken;
import com.example.chickenrun.R;
import com.example.chickenrun.gameRoom.Activity_Waiting_Room;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Activity_Lobby extends AppCompatActivity
{
    String TAG = "Activity_Lobby";

    RecyclerView lobby_room_list;
    Adapter_Room_List adapterRoomList;
    private List<item_lobby_list> itemLobbyList;

    private Context mContext;
    TextView button_create_room;

    public static String GET_ROOM_INDEX, GET_MY_JOIN_INDEX, GET_ROOM_NAME, GET_MY_NAME, GET_RUN_DISTANCE;
    public static boolean GET_IS_HOST;
    public String memId;

    // 방 삭제 알림 수신 대기 (핸들러)
    public static Handler HANDLER_DELETE;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mContext = Activity_Lobby.this;

        SharedPreferences sf = getSharedPreferences("chmeminfo", MODE_PRIVATE);
        memId = sf.getString("name", null);
        GET_MY_NAME = sf.getString("name", null);

        // viewFind
        button_create_room = findViewById(R.id.button_create_room);

        // 리사이클러뷰 세팅
        lobby_room_list = findViewById(R.id.lobby_room_list);
        lobby_room_list.setHasFixedSize(true);
        lobby_room_list.setLayoutManager(new LinearLayoutManager(mContext));

        // 리사이클러뷰 새로고침
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_lobby);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // cancle the Visual indication of a refresh
                        swipeRefreshLayout.setRefreshing(false);

                        // todo: 방 목록 불러오기
                        getRoomList();
                    }
                }, 2000); // 2초 딜레이 후 리스트 새로 불러옴
            }
        });

        // todo: 방 목록 불러오기
        getRoomList();

        // todo: 방 생성 다이얼로그 실행
        button_create_room.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_create_room, null);
                builder.setView(view);

                // 시작 다이얼로그
                final EditText dialog_edit_text_room_name = view.findViewById(R.id.dialog_edit_text_room_name);
                TextView dialog_button_room_create = view.findViewById(R.id.dialog_button_room_create);
                final Spinner spinner= view.findViewById(R.id.spinner2);

                final AlertDialog dialog = builder.create();

//                Log.e(TAG, "onClick: spinner: " + spinner.getSelectedItem().toString().replace("m","") );

                // todo: 방 생성 시작하기
                dialog_button_room_create.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // 방 제목 입력받기
                        GET_ROOM_NAME = dialog_edit_text_room_name.getText().toString();
                        Log.e(TAG, "onClick: roomTitle: " + GET_ROOM_NAME);

                        // todo: 방 생성 (mysql)
                        createRoom(GET_ROOM_NAME, spinner.getSelectedItem().toString().replace("m",""));
                        GET_RUN_DISTANCE = spinner.getSelectedItem().toString().replace("m","");
                        Log.i("dsfsdfsdfsd  spinner","       "+spinner.getSelectedItem().toString().replace("m",""));
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        // 방 삭제 알림 수신 대기 (핸들러)
        HANDLER_DELETE = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.what == 1111)
                {
                    String receive = msg.obj.toString();
                    Log.e(TAG, "handleMessage: receive: " + receive );
                    if (receive.equals("refreshList_deleteRoom"))
                    {
                        // todo: 방 삭제되면 목록 불러오기
                        getRoomList();
                    }
                }
            }
        };
    }

    String createRoomData[];

    // todo: 방 생성 (mysql)
    private void createRoom(final String roomName, final String distance)
    {
        Log.e(TAG, "createRoom: 방 생성 시작");

        StringRequest stringRequest
                = new StringRequest(Request.Method.POST,
                "http://ec2-13-125-121-5.ap-northeast-2.compute.amazonaws.com/chicken/addRoom.php",
                new com.android.volley.Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.e(TAG, "addPaymentHistory onResponse: " + response.trim());

                        createRoomData = response.trim().split(", ");

                        if (createRoomData[0].equals("success"))
                        {
                            Log.e(TAG, "createRoom: 방 생성 완료");

                            // 방장에게 방 인덱스 알려주기 (방 퇴장 처리 할 때 필요)
                            GET_ROOM_INDEX = createRoomData[1];
                            GET_IS_HOST = true; // false = 방 구성원 / true = 방장

                            // 게임 대기방으로 이동
                            Intent intent = new Intent(mContext, Activity_Waiting_Room.class);
                            startActivity(intent);
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

                params.put("roomName", roomName);
                params.put("distance", distance); // 볼리는 다 ... 글자로 들어가는거임?

                return params;
            }
        };

        // requestQueue로 로그인 결과값 요청을 시작한다.
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
        requestQueue.add(stringRequest);
    }

    // todo: 방 목록 불러오기
    void getRoomList()
    {
        Log.e(TAG, "getRoomList: 방 목록 불러오기");

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Defining retrofit api service
        ApiInterface roomListRequest = retrofit.create(ApiInterface.class);

        // defining the call
        Call<List<item_lobby_list>> listCall = roomListRequest.getRoomList("");

        listCall.enqueue(new Callback<List<item_lobby_list>>()
        {
            @Override
            public void onResponse(Call<List<item_lobby_list>> call, Response<List<item_lobby_list>> response)
            {

                itemLobbyList = response.body();

                for (int i = 0; i < itemLobbyList.size(); i++)
                {
                    Log.e(TAG, "onResponse: itemLobbyList: " + itemLobbyList.get(i).getRoomName());
                }

                adapterRoomList = new Adapter_Room_List(mContext, itemLobbyList);
                lobby_room_list.setAdapter(adapterRoomList);
            }

            @Override
            public void onFailure(Call<List<item_lobby_list>> call, Throwable t)
            {
                Log.e(TAG, "onFailure: t: " + t.getMessage());
            }
        });
    }

    // todo: 방 목록 어댑터 (리사이클러뷰)
    public class Adapter_Room_List extends RecyclerView.Adapter<Adapter_Room_List.ViewHolder>
    {
        public Adapter_Room_List(Context context, List<item_lobby_list> item_lobby_lists)
        {
            mContext = context;
            itemLobbyList = item_lobby_lists;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            // 아이템 레이아웃 연결
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_lobby_room, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position)
        {
            // 방 제목
            holder.room_title.setText(itemLobbyList.get(position).getRoomName());

            // 방 인원 수
            holder.join_user_count.setText(itemLobbyList.get(position).getRoomCount() + " / 4");

            holder.distance.setText(itemLobbyList.get(position).getDistance() + "m");

            // 참가버튼 클릭
            holder.button_join.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // 방 번호 담아두기 (퇴장 할 때 필요)
                    GET_ROOM_INDEX = itemLobbyList.get(position).roomIndex;
                    GET_ROOM_NAME = itemLobbyList.get(position).getRoomName();
                    GET_IS_HOST = false; // false = 방 구성원 / true = 방장
                    GET_RUN_DISTANCE = itemLobbyList.get(position).getDistance();

                    Log.e(TAG, "onClick: " + memId + "님이 " + GET_ROOM_INDEX + "번 방에 입장합니다.");

                    // todo: 방 입장하기 (mysql)
                    joinRoom(GET_ROOM_INDEX, memId);
                }
            });
        }

        @Override
        public int getItemCount()
        {
            return itemLobbyList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public View view; // 목록 클릭용 뷰

            public TextView room_title;
            public TextView join_user_count;
            public TextView button_join;
            public TextView distance;


            public ViewHolder(@NonNull View itemView)
            {
                super(itemView);
                view = itemView;
                room_title = itemView.findViewById(R.id.room_title);
                join_user_count = itemView.findViewById(R.id.join_user_count);
                button_join = itemView.findViewById(R.id.button_join);
                distance = itemView.findViewById(R.id.room_distance);
            }
        }

        String joinResult[];

        // todo: 방 입장하기 (mysql)
        public void joinRoom(final String roomIndex, final String userName)
        {
            Log.e(TAG, "joinRoom: 방 입장 처리하기");

            StringRequest stringRequest
                    = new StringRequest(Request.Method.POST,
                    "http://ec2-13-125-121-5.ap-northeast-2.compute.amazonaws.com/chicken/joinRoom.php",
                    new com.android.volley.Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response)
                        {
                            Log.e(TAG, "addPaymentHistory onResponse: " + response.trim());

                            joinResult = response.trim().split(", ");

                            // 방 입장 처리 완료 메시지
                            if (joinResult[0].equals("success_join_room"))
                            {
                                Log.e(TAG, "createRoom: 방 입장 처리 완료");

                                // 내 방 참가정보 가지고 있기 (방에서 퇴장 하면서 내 참가정보 삭제할 때 필요)
                                GET_MY_JOIN_INDEX = joinResult[1];

                                Log.e(TAG, "onResponse: GET_MY_JOIN_INDEX: " + GET_MY_JOIN_INDEX );

                                Intent intent = new Intent(mContext, Activity_Waiting_Room.class);
                                startActivity(intent);
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
                    params.put("userName", userName);

                    return params;
                }
            };

            // requestQueue로 로그인 결과값 요청을 시작한다.
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            // stringRequest메소드에 기록한 내용들로 requestQueue를 시작한다.
            requestQueue.add(stringRequest);

        }
    }
}
