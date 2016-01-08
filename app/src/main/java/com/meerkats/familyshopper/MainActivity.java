package com.meerkats.familyshopper;

import android.app.AlertDialog;
import android.content.DialogInterface;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;

public class MainActivity extends AppCompatActivity {

    EditText enterItemEditTxt;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    ListView shoppingListView;
    MainController mainController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainController = new MainController(this);
        shoppingList = mainController.getShoppingList();
        shoppingListAdapter = new ShoppingListAdapter(this, shoppingList);
        mainController.setShoppingListAdapter(shoppingListAdapter);
        mainController.init();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.family_shopper_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(R.mipmap.ic_launcher);

        enterItemEditTxt = (EditText)findViewById(R.id.enterItemTxt);
        shoppingListView = (ListView)findViewById(R.id.shoppingListView);
        shoppingListView.setAdapter(shoppingListAdapter);

        setShoppingListOnItemClick();
        setShoppingListOnItemLongClick();
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

                return true;
            case R.id.clear_list:
                mainController.clearShoppingList();
                return true;
            case R.id.connect:
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void setShoppingListOnItemLongClick(){

        shoppingListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View v, final int position, final long id) {
                int itemsID = ((int) R.array.shoppingListContextMenuValues);
                if (shoppingList.getShoppingListItem(position).isCrossedOff())
                    itemsID = ((int) R.array.shoppingListContextMenuValuesDeleteOnly);
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.ListContextMenu));
                builder.setTitle(shoppingList.getShoppingListItem(position).getShoppingListItem()).setCancelable(true).setItems(itemsID,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int index) {
                                switch (index) {
                                    case 0:
                                        mainController.setShoppingListItemDelete(position);

                                        break;
                                    case 1:
                                        mainController.setShoppingListItemEdit(parent, v, position, id, MainActivity.this);
                                        shoppingListView.setSelection(position);
                                        break;
                                }
                            }
                        }
                ).show();
                return true;
            }
        });

    }

    private void setShoppingListOnItemClick(){
        shoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mainController.setShoppingItemCrossedOff(position);
            }
        });
    }

    public void addBtnClick(View view){
        if (enterItemEditTxt.getText().toString().trim().length() > 0)
            mainController.addItemToShoppingList(enterItemEditTxt.getText().toString().trim());
        shoppingListView.setSelection(shoppingListAdapter.getCount() - 1);
        enterItemEditTxt.setText("");
    }


}
