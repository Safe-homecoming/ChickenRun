package com.example.chickenrun.gameRoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chickenrun.ApiClient;
import com.example.chickenrun.ApiInterface;
import com.example.chickenrun.Lobby.Activity_Lobby;
import com.example.chickenrun.Lobby.item_lobby_list;
import com.example.chickenrun.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Activity_Waiting_Room extends AppCompatActivity
{

    String TAG = "Activity_Waiting_Room";

    private Context mContext;
    RecyclerView Participant_list;
    Adapter_Participant adapterParticipantList;

    private List<item_participant_user> itemParticipant;

    TextView button_waiting_ready;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        mContext = Activity_Waiting_Room.this;

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
                Log.e(TAG, "onFailure: t: " + t.getMessage() );
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
