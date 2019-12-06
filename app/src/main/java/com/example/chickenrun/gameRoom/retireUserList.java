package com.example.chickenrun.gameRoom;


public class retireUserList
{
    String retierUSerName;
    Double retierUSerDistance;

    public retireUserList(String retierUSerName, Double retierUSerDistance)
    {
        this.retierUSerName = retierUSerName;
        this.retierUSerDistance = retierUSerDistance;
    }

    public String getRetierUSerName()
    {
        return retierUSerName;
    }

    public void setRetierUSerName(String retierUSerName)
    {
        this.retierUSerName = retierUSerName;
    }

    public Double getRetierUSerDistance()
    {
        return retierUSerDistance;
    }

    public void setRetierUSerDistance(Double retierUSerDistance)
    {
        this.retierUSerDistance = retierUSerDistance;
    }
}
