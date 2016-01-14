package com.meerkats.familyshopper.model;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Rez on 19/12/2015.
 */
public class ShoppingListItem {
    private String shoppingListItem;
    private boolean isCrossedOff;
    private Date lastModified;
    private int postitonEntered;
    private int positionOrdered;
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
        lastModified = new Date();
    }

    @Override
    public synchronized String toString(){
        return "ShoppingListItem [shopping_list_item=" + shoppingListItem + ", "
                + "uuid=" + guid + ", "
                + "last_modified=" + lastModified + ", "
                + "is_crossed_off=" + isCrossedOff + ", "
                + "is_deleted=" + isDeleted + "]";
    }

    public synchronized String getShoppingListItem() { return shoppingListItem; }
    public synchronized void setShoppingListItem(String shoppingListItem) {
        this.shoppingListItem = shoppingListItem;
        lastModified = new Date();
    }
    public synchronized boolean isCrossedOff() { return isCrossedOff; }
    public synchronized void setIsCrossedOff(boolean isCrossedOff) {
        this.isCrossedOff = isCrossedOff;
        lastModified = new Date();
    }
    public synchronized boolean getIsDeleted(){return isDeleted;}
    public synchronized void setIsDeleted(boolean isDeleted){this.isDeleted=isDeleted;}
    public synchronized int getPostitonEntered(){return postitonEntered;}
    public synchronized void setPostitonEntered(int postitonEntered){this.postitonEntered = postitonEntered;}
    public synchronized void setPositionOrdered(int positionOrdered){this.positionOrdered = positionOrdered;}
    public synchronized int getPositionOrdered(){return positionOrdered;}
    public synchronized Date getLastModified(){return lastModified;}
    public synchronized void setLastModified(Date lastModified){this.lastModified = lastModified;}
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
