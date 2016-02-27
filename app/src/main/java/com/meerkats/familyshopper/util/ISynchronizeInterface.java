package com.meerkats.familyshopper.util;

import com.meerkats.familyshopper.NotificationEvents;
import com.meerkats.familyshopper.model.ShoppingList;

/**
 * Created by Rez on 25/02/2016.
 */
public interface ISynchronizeInterface {
    void notifyFileChanged(NotificationEvents occuredNotifications, ShoppingList mergedList);
    void postSynchronize();
}
