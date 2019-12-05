package com.example.chickenrun.gameRoom;

public class item_participant_user
{
    String userName;
    boolean isHost;
    boolean isReady;
    int[] userIcon;

    public item_participant_user(String userName, boolean isHost, boolean isReady, int[] userIcon)
    {
        this.userName = userName;
        this.isHost = isHost;
        this.isReady = isReady;
        this.userIcon = userIcon;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public boolean isHost()
    {
        return isHost;
    }

    public void setHost(boolean host)
    {
        isHost = host;
    }

    public boolean isReady()
    {
        return isReady;
    }

    public void setReady(boolean ready)
    {
        isReady = ready;
    }

    public int[] getUserIcon()
    {
        return userIcon;
    }

    public void setUserIcon(int[] userIcon)
    {
        this.userIcon = userIcon;
    }
}
