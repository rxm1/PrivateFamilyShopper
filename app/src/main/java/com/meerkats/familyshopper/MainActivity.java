package com.meerkats.familyshopper;

import android.app.Activity;
import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meerkats.familyshopper.model.ShoppingList;

public class MainActivity extends Activity {

    EditText enterItemEditTxt;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enterItemEditTxt = (EditText)findViewById(R.id.enterItemTxt);

        shoppingList = new ShoppingList("firstList");
        shoppingList.add("aa");
        shoppingList.add("xx");
        shoppingList.add("cc");

        ListView shoppingListTextView = (ListView)findViewById(R.id.shoppingListView);
        shoppingListAdapter = new ShoppingListAdapter(this, shoppingList);
        shoppingListTextView.setAdapter(shoppingListAdapter);
    }

    public void addBtnClick(View view){
        Toast.makeText(this, ("first length " + shoppingList.size()), Toast.LENGTH_LONG).show();

        ShoppingList shoppingListnew = new ShoppingList("firstList");
        shoppingListnew.add("bb");
        shoppingListnew.add("oo");
        shoppingListnew.add("hdh");
        shoppingList.clear();
        shoppingList.addAll(shoppingListnew);


        shoppingList.add(enterItemEditTxt.getText().toString());
        Toast.makeText(this, ("second length " + shoppingList.size()), Toast.LENGTH_LONG).show();
        shoppingListAdapter.clear();
        shoppingListAdapter.addAll(shoppingList);
                shoppingListAdapter.notifyDataSetChanged();
    }
    public void enterItemClick(View view){

    }
}
