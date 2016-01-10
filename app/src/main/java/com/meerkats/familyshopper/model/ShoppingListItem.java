package com.meerkats.familyshopper.model;

import java.util.Date;

/**
 * Created by Rez on 19/12/2015.
 */
public class ShoppingListItem {
    private String shoppingListItem;
    private boolean isCrossedOff;
    private Date lastModified;
    private int postitonEntered;
    private int positionOrdered;

    public ShoppingListItem(){
        shoppingListItem = "";
        isCrossedOff = false;
    }
    public ShoppingListItem(String item){
        this.shoppingListItem = item;
        isCrossedOff = false;
    }

    @Override
    public String toString(){
        return
                "ShoppingListItem [shopping_list_item=" + shoppingListItem + ", "
        + "is_crossed_off=" + isCrossedOff + "]";
    }

    public String getShoppingListItem() { return shoppingListItem; }
    public void setShoppingListItem(String shoppingListItem) { this.shoppingListItem = shoppingListItem; }
    public boolean isCrossedOff() { return isCrossedOff; }
    public void setIsCrossedOff(boolean isCrossedOff) { this.isCrossedOff = isCrossedOff; }
    public int getPostitonEntered(){return postitonEntered;}
    public void setPostitonEntered(int postitonEntered){this.postitonEntered = postitonEntered;}
    public void setPositionOrdered(int positionOrdered){this.positionOrdered = positionOrdered;}
    public int getPositionOrdered(){return positionOrdered;}
    public Date getLastModified(){return lastModified;}
    public void setLastModified(Date lastModified){this.lastModified = lastModified;}

}
