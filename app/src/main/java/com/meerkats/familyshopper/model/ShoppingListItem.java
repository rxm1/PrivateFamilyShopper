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

    private void init(){
        isCrossedOff = false;
        isDeleted = false;
        guid = UUID.randomUUID();
        lastModified = new Date();
    }

    @Override
    public String toString(){
        return "ShoppingListItem [shopping_list_item=" + shoppingListItem + ", "
                + "uuid=" + guid + ", "
                + "last_modified=" + lastModified + ", "
                + "is_crossed_off=" + isCrossedOff + ", "
                + "is_deleted=" + isDeleted + "]";
    }

    public String getShoppingListItem() { return shoppingListItem; }
    public void setShoppingListItem(String shoppingListItem) {
        this.shoppingListItem = shoppingListItem;
        lastModified = new Date();
    }
    public boolean isCrossedOff() { return isCrossedOff; }
    public void setIsCrossedOff(boolean isCrossedOff) {
        this.isCrossedOff = isCrossedOff;
        lastModified = new Date();
    }
    public boolean getIsDeleted(){return isDeleted;}
    public void setIsDeleted(boolean isDeleted){this.isDeleted=isDeleted;}
    public int getPostitonEntered(){return postitonEntered;}
    public void setPostitonEntered(int postitonEntered){this.postitonEntered = postitonEntered;}
    public void setPositionOrdered(int positionOrdered){this.positionOrdered = positionOrdered;}
    public int getPositionOrdered(){return positionOrdered;}
    public Date getLastModified(){return lastModified;}
    public void setLastModified(Date lastModified){this.lastModified = lastModified;}
    public UUID getGuid(){return guid;}

    public boolean equal(ShoppingListItem other){
        if(this.getGuid().equals(other.getGuid())
                && this.isCrossedOff()==other.isCrossedOff()
                && this.shoppingListItem.equals(other.shoppingListItem)
                && this.isDeleted==other.isDeleted){
            return true;
        }

        return false;
    }
}
