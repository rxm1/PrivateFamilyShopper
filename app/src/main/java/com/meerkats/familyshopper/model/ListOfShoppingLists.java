package com.meerkats.familyshopper.model;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Rez on 17/12/2015.
 */
public class ListOfShoppingLists {
    private ArrayList<ShoppingList> shoppingList;
    private Context context;

    public ListOfShoppingLists(){
        shoppingList = new ArrayList<ShoppingList>();
    }
}
