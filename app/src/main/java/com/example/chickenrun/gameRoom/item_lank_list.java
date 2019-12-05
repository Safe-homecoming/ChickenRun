package com.example.chickenrun.gameRoom;

public class item_lank_list
{
    String userName;
    String runTime;

    public item_lank_list(String userName, String runTime)
    {
        this.userName = userName;
        this.runTime = runTime;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getRunTime()
    {
        return runTime;
    }

    public void setRunTime(String runTime)
    {
        this.runTime = runTime;
    }
}
