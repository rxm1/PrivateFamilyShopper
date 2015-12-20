package com.meerkats.familyshopper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;

public class MainActivity extends Activity {

    EditText enterItemEditTxt;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    ListView shoppingListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enterItemEditTxt = (EditText)findViewById(R.id.enterItemTxt);

        shoppingList = new ShoppingList("firstList");
        shoppingList.add("aa");
        shoppingList.add("bb");
        shoppingListAdapter = new ShoppingListAdapter(this, shoppingList);
        shoppingListView = (ListView)findViewById(R.id.shoppingListView);
        shoppingListView.setAdapter(shoppingListAdapter);

        setShoppingListOnItemClick();
        setShoppingListOnItemLongClick();
    }

    private void setShoppingListOnItemLongClick(){
        shoppingListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(shoppingList.get(position)).setCancelable(true).setItems(R.array.shoppingListContextMenuValues,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int index) {
                                switch (index){
                                    case 0:
                                        setShoppingListItemDelete(position);
                                    case 1:
                                        setShoppingListItemEdit();
                                }
                            }
                        }
                )
                        .show();
                return true;
            }
        });
    }
    private void setShoppingListItemDelete(int position){
        shoppingList.remove(position);
        shoppingListAdapter.notifyDataSetChanged();
    }
    private void setShoppingListItemEdit(){

    }

    private void setShoppingListOnItemClick(){
        shoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(position);
                shoppingListItem.setIsCrossedOff(!shoppingListItem.isCrossedOff());
                shoppingList.setShoppingListItem(position, shoppingListItem);
                shoppingListAdapter.notifyDataSetChanged();
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


}
