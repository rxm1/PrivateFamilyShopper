package com.meerkats.familyshopper;

import android.app.Activity;
import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
        shoppingList.add("bb");
        shoppingListAdapter = new ShoppingListAdapter(this, shoppingList);
        ListView shoppingListView = (ListView)findViewById(R.id.shoppingListView);
        shoppingListView.setAdapter(shoppingListAdapter);
        shoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), ("Item Clicked " + position + " " + adapterView.getSelectedItemId()), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void addBtnClick(View view){
        shoppingList.add(enterItemEditTxt.getText().toString());
        //Toast.makeText(this, ("second length " + shoppingList.size()), Toast.LENGTH_LONG).show();

        shoppingListAdapter.notifyDataSetChanged();
    }
    public void addItemTextEntered(View view){

    }

    public void deleteImgClick(View view){
        Toast.makeText(this, "Delete clicked", Toast.LENGTH_LONG).show();
    }
    public void shoppingListItemClick(View view){
        Toast.makeText(this, "List item clicked", Toast.LENGTH_LONG).show();
    }
}
