package com.meerkats.familyshopper.model;

import java.util.ArrayList;

/**
 * Created by Rez on 17/12/2015.
 */
public class ShoppingList extends ArrayList<String> {
    private String shoppingListName;

    public ShoppingList(String name){
        shoppingListName = name;
    }

    public String getShoppingListName() {
        return shoppingListName;
    }


    public String[] toStringArray() {
        String[] shoppingListContents = new String[this.size()];
        this.toArray(shoppingListContents);
        return shoppingListContents;
    }
}
