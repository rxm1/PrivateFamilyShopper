package com.meerkats.familyshopper;

import android.app.Activity;
import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;

import com.meerkats.familyshopper.model.ShoppingList;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ShoppingList shoppingList = new ShoppingList("firstList");
        shoppingList.add("aa");
        shoppingList.add("bb");
        shoppingList.add("cc");

        ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(this, shoppingList);
        //setListAdapter(shoppingListAdapter);
    }
}
