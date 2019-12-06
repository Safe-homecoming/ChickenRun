package com.example.chickenrun.Lobby;

import com.google.gson.annotations.SerializedName;

public class item_lobby_list
{

    @SerializedName("room_index")
    String roomIndex;

    @SerializedName("room_name")
    String roomName;

    @SerializedName("user_count")
    String roomCount;

    @SerializedName("distance")
    String distance;

    public String getRoomIndex()
    {
        return roomIndex;
    }

    public void setRoomIndex(String roomIndex)
    {
        this.roomIndex = roomIndex;
    }

    public String getRoomName()
    {
        return roomName;
    }

    public void setRoomName(String roomName)
    {
        this.roomName = roomName;
    }

    public String getRoomCount()
    {
        return roomCount;
    }

    public void setRoomCount(String roomCount)
    {
        this.roomCount = roomCount;
    }

    public String getDistance()
    {
        return distance;
    }

    public void setDistance(String distance)
    {
        this.distance = distance;
    }
}
