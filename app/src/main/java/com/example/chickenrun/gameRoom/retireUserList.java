package com.example.chickenrun.gameRoom;


import java.util.Comparator;

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

    class comparator implements  Comparator<retireUserList>
    {

        @Override
        public int compare(retireUserList first, retireUserList second)
        {
            double firstValue = first.getRetierUSerDistance();
            double seconValue = first.getRetierUSerDistance();

            if (firstValue > seconValue)
            {
                return -1;
            }
            else if (firstValue < seconValue)
            {
                return 1;
            }
            else
            {
                return 0;
            }

        }
    }
}
