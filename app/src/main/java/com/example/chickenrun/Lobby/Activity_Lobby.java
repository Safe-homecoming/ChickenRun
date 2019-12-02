package com.example.chickenrun.Lobby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chickenrun.R;

import java.util.List;

public class Activity_Lobby extends AppCompatActivity
{
    RecyclerView lobby_room_list;
    Adapter_Room_List adapterRoomList;
    private List<item_lobby_list> itemLobbyList;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        lobby_room_list = findViewById(R.id.lobby_room_list);
    }

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

        }

        @Override
        public int getItemCount()
        {
            return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public View view; // 목록 클릭용 뷰

            public ViewHolder(@NonNull View itemView)
            {
                super(itemView);
                view = itemView;
            }
        }
    }
}
