package com.meerkats.familyshopper.model;

/**
 * Created by Rez on 19/12/2015.
 */
public class ShoppingListItem {
    private String shoppingListItem;
    private boolean isCrossedOff;

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
    public void setIsCrossedOff(boolean isCrossedOff) {
        this.isCrossedOff = isCrossedOff;
    }
}
