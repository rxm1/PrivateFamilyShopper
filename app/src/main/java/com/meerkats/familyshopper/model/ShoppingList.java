package com.meerkats.familyshopper.model;

import java.util.ArrayList;

/**
 * Created by Rez on 17/12/2015.
 */
public class ShoppingList extends ArrayList<String>{
    private String shoppingListName;
    private ArrayList<ShoppingListItem> shoppingListItems;

    public ShoppingList(String name){
        shoppingListName = name;
        shoppingListItems = new ArrayList<ShoppingListItem>();
    }

    public String getShoppingListName() {
        return shoppingListName;
    }

    @Override
    public boolean add(String item){
        super.add(item);
        shoppingListItems.add(new ShoppingListItem(item));
        return true;
    }

}
