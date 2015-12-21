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
    @Override
    public String remove(int position){
        String item = super.remove(position);
        shoppingListItems.remove(position);
        return item;
    }

    public ShoppingListItem getShoppingListItem(int index){
        return shoppingListItems.get(index);
    }
    public void setShoppingListItem(int index, ShoppingListItem shoppingListItem){
        shoppingListItems.set(index, shoppingListItem);
        this.set(index, shoppingListItem.toString());
    }

    public void setShoppingListItemIsEditing(int position){
        ShoppingListItem shoppingListItem = shoppingListItems.get(position);
        shoppingListItem.setSelectedForEdit(true);
        shoppingListItems.set(position, shoppingListItem);
    }
    public void setShoppingListItemIsNotEditing(int position){
        ShoppingListItem shoppingListItem = shoppingListItems.get(position);
        shoppingListItem.setSelectedForEdit(false);
        shoppingListItems.set(position, shoppingListItem);
    }
}
