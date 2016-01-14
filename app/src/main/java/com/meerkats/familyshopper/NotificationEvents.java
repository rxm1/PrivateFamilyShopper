package com.meerkats.familyshopper;

/**
 * Created by Rez on 13/01/2016.
 */
public class NotificationEvents{
    public boolean additions = false;
    public boolean modifications = false;
    public boolean deletions = false;

    public void setFalse(){
        additions = false;
        modifications = false;
        deletions = false;
    }

    public boolean isTrue(){
        return (additions || modifications || deletions);
    }

    public boolean isAllTrue(){
        return (additions && modifications && deletions);
    }
}

