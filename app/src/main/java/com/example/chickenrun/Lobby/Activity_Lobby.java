package com.example.chickenrun.Lobby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chickenrun.ApiClient;
import com.example.chickenrun.ApiInterface;
import com.example.chickenrun.R;
import com.example.chickenrun.gameRoom.Activity_Waiting_Room;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mContext = Activity_Lobby.this;

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

        // todo: 방 생성 버튼 클릭
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

                final AlertDialog dialog = builder.create();

                dialog_button_room_create.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // 방 제목 입력받기
                        String roomTitle = dialog_edit_text_room_name.getText().toString();
                        Log.e(TAG, "onClick: roomTitle: " + roomTitle );

                        // 게임 대기방으로 이동
                        Intent intent = new Intent(mContext, Activity_Waiting_Room.class);
                        startActivity(intent);
                    }
                });
                dialog.show();
            }
        });
    }

    // todo: 방 목록 불러오기
    void getRoomList()
    {
        Log.e(TAG, "getRoomList: 방 목록 불러오기" );

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

/*                for (int i = 0; i < itemLobbyList.size(); i++)
                {
                    Log.e(TAG, "onResponse: itemLobbyList: " + itemLobbyList.get(i));
                }

                adapterRoomList = new Adapter_Room_List(mContext, itemLobbyList);
                lobby_room_list.setAdapter(adapterRoomList);*/
            }

            @Override
            public void onFailure(Call<List<item_lobby_list>> call, Throwable t)
            {
                Log.e(TAG, "onFailure: t: " + t.getMessage() );
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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            holder.room_title.setText("");

            holder.join_user_count.setText("실제 인원 수" + " / 4");

            // 참가버튼 클릭
            holder.button_join.setOnClickListener(new View.OnClickListener()
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
            return itemLobbyList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public View view; // 목록 클릭용 뷰

            public TextView room_title;
            public TextView join_user_count;
            public TextView button_join;

            public ViewHolder(@NonNull View itemView)
            {
                super(itemView);
                view = itemView;
                room_title = itemView.findViewById(R.id.room_title);
                join_user_count = itemView.findViewById(R.id.join_user_count);
                button_join = itemView.findViewById(R.id.button_join);
            }
        }
    }
}
