package com.meerkats.familyshopper;

import android.app.AlertDialog;
import android.content.DialogInterface;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;

public class MainActivity extends AppCompatActivity {

    EditText enterItemEditTxt;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    ListView shoppingListView;

    public MainActivity(){
    shoppingList = new ShoppingList("firstList");
}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.family_shopper_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(R.mipmap.ic_launcher);

        enterItemEditTxt = (EditText)findViewById(R.id.enterItemTxt);

        shoppingList.add("aa");
        shoppingList.add("bb");
        shoppingListAdapter = new ShoppingListAdapter(this, shoppingList);
        shoppingListView = (ListView)findViewById(R.id.shoppingListView);
        shoppingListView.setAdapter(shoppingListAdapter);

        setShoppingListOnItemClick();
        setShoppingListOnItemLongClick();

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.shopping_list_action_items, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.clear_list:
                shoppingList.clear();
                shoppingListAdapter.notifyDataSetChanged();
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void setShoppingListOnItemLongClick(){

        shoppingListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View v, final int position, final long id) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(shoppingList.get(position)).setCancelable(true).setItems(R.array.shoppingListContextMenuValues,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int index) {
                                switch (index) {
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

    private void setShoppingListItemEdit(final AdapterView<?> parent, final View v, final int position, long id){
        final ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(position);
        EditShoppingItemDialog cdd=new EditShoppingItemDialog(MainActivity.this, shoppingListItem.getShoppingListItem());
        cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (((EditShoppingItemDialog) dialog).isCanceled())
                    return;

                String newData = ((EditShoppingItemDialog) dialog).getNewData();
                shoppingListItem.setShoppingListItem(newData);
                shoppingList.setShoppingListItem(position, shoppingListItem);
                //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
                shoppingListAdapter.notifyDataSetChanged();
            }
        });
        cdd.show();
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
        if (enterItemEditTxt.getText().toString().trim().length() > 0)
        shoppingList.add(enterItemEditTxt.getText().toString().trim());
        shoppingListAdapter.notifyDataSetChanged();
        shoppingListView.setSelection(shoppingListAdapter.getCount() - 1);
        enterItemEditTxt.setText("");
    }
    public void addItemTextEntered(View view){

    }


}
