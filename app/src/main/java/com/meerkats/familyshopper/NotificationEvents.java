package com.meerkats.familyshopper;

/**
 * Created by Rez on 13/01/2016.
 */
public class NotificationEvents{
    public boolean remoteAdditions = false;
    public boolean localAdditions = false;
    public boolean modifications = false;
    public boolean deletions = false;

    private class NotificationEventsService extends NotificationEvents{
        public NotificationEventsService(boolean remoteAdditions, boolean modifications, boolean deletions){
            this.remoteAdditions=remoteAdditions;
            this.modifications=modifications;
            this.deletions=deletions;
        }
        @Override
        public boolean isTrue() {
            return (remoteAdditions || modifications || deletions);
        }
    }
    private class NotificationEventsClient extends NotificationEvents{
        public NotificationEventsClient(boolean localAdditions, boolean modifications, boolean deletions){
            this.localAdditions=localAdditions;
            this.modifications=modifications;
            this.deletions=deletions;
        }
        @Override
        public boolean isTrue() {
            return (localAdditions || modifications || deletions);
        }
    }

    public void setFalse(){
        remoteAdditions = false;
        localAdditions = false;
        modifications = false;
        deletions = false;
    }

    public NotificationEvents forService(){
        return new NotificationEventsService(remoteAdditions, modifications, deletions);
    }
    public NotificationEvents forClient(){
        return new NotificationEventsClient(localAdditions, modifications, deletions);
    }
    public boolean isTrue(){
        return (remoteAdditions || localAdditions || modifications || deletions);
    }
}

