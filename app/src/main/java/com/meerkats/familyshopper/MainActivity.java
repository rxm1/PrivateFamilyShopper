package com.meerkats.familyshopper;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.media.Image;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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
            public boolean onItemLongClick(final AdapterView<?> parent, final View v, final int position, final long id) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(shoppingList.get(position)).setCancelable(true).setItems(R.array.shoppingListContextMenuValues,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int index) {
                                switch (index){
                                    case 0:
                                        setShoppingListItemDelete(position);
                                    case 1:
                                        setShoppingListItemEdit(parent, v, position, id);
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
    private void setShoppingListItemEdit(final AdapterView<?> parent, View v, final int position, long id){
        TextView textView = (TextView)v.findViewById(R.id.shoppingListItemTextView);
        EditText editText = (EditText)v.findViewById(R.id.shoppingListItemEditText);
        ImageButton cancelButton = (ImageButton)v.findViewById(R.id.shoppingListItemCancelButton);
        ImageButton okButton = (ImageButton)v.findViewById(R.id.shoppingListItemOKButton);
        editText.setText(textView.getText());
        //textView.setWidth(0);


        //editText.setX(textView.getX());
        //editText.setY(textView.getY());
        textView.setVisibility(View.GONE);

        editText.setVisibility(View.VISIBLE);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
        cancelButton.setVisibility(View.VISIBLE);
        okButton.setVisibility(View.VISIBLE);

        Toast.makeText(getApplicationContext(), v.getId()+"", Toast.LENGTH_SHORT).show();
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
