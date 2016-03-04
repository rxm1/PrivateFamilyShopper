package com.meerkats.familyshopper.model;

import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Rez on 19/12/2015.
 */
public class ShoppingListItem {
    private String shoppingListItem;
    private boolean isCrossedOff;
    private long lastModified;
    private long dateCreated;
    private UUID guid;
    private boolean isDeleted;

    public ShoppingListItem(){
        shoppingListItem = "";
        init();
    }
    public ShoppingListItem(String item){
        this.shoppingListItem = item;
        init();
    }

    private synchronized void init(){
        isCrossedOff = false;
        isDeleted = false;
        guid = UUID.randomUUID();
        lastModified = new Date().getTime();
        dateCreated = new Date().getTime();
    }

    @Override
    public synchronized String toString(){
        return "ShoppingListItem [shopping_list_item=" + shoppingListItem + ", "
                + "uuid=" + guid + ", "
                + "last_modified=" + lastModified + ", "
                + "date_created=" + dateCreated + ", "
                + "is_crossed_off=" + isCrossedOff + ", "
                + "is_deleted=" + isDeleted + "]";
    }

    public synchronized String getShoppingListItem() { return shoppingListItem; }
    public synchronized void setShoppingListItem(String shoppingListItem) {
        this.shoppingListItem = shoppingListItem;
        lastModified = new Date().getTime();
    }
    public synchronized boolean isCrossedOff() { return isCrossedOff; }
    public synchronized void setIsCrossedOff(boolean isCrossedOff) {
        this.isCrossedOff = isCrossedOff;
        lastModified = new Date().getTime();
    }
    public synchronized boolean getIsDeleted(){return isDeleted;}
    public synchronized void setIsDeleted(boolean isDeleted){this.isDeleted=isDeleted;}
    public synchronized long getLastModified(){return lastModified;}
    public synchronized void setLastModified(long lastModified){this.lastModified = lastModified;}
    public synchronized long getDateCreated(){return dateCreated;}
    public synchronized void setDateCreated(long dateCreated){this.dateCreated = dateCreated;}
    public synchronized UUID getGuid(){return guid;}

    public synchronized boolean equal(ShoppingListItem other){
        if(this.getGuid().equals(other.getGuid())
                && this.isCrossedOff()==other.isCrossedOff()
                && this.shoppingListItem.equals(other.shoppingListItem)
                && this.isDeleted==other.isDeleted){
            return true;
        }

        return false;
    }
}
