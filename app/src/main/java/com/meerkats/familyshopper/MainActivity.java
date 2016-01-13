package com.meerkats.familyshopper;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
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

import com.meerkats.familyshopper.model.ShoppingList;

public class MainActivity extends AppCompatActivity {

    EditText enterItemEditTxt;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    ListView shoppingListView;
    MainController mainController;
    DataChangedReceiver dataChangedReceiver;
    Handler mainActivityHandler;
    Handler mainUIHandler;
    HandlerThread handlerThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handlerThread = new HandlerThread("MainActivity.HandlerThread");
        handlerThread.start();
        mainActivityHandler = new Handler(handlerThread.getLooper());
        mainUIHandler = new Handler(Looper.getMainLooper());

        mainController = new MainController(this);
        mainController.init();
        shoppingList = mainController.getShoppingList();
        shoppingListAdapter = mainController.getShoppingListAdapter();
        dataChangedReceiver = new DataChangedReceiver();


        Toolbar myToolbar = (Toolbar) findViewById(R.id.family_shopper_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(R.mipmap.ic_launcher);

        enterItemEditTxt = (EditText)findViewById(R.id.enterItemTxt);
        shoppingListView = (ListView)findViewById(R.id.shoppingListView);
        shoppingListView.setAdapter(shoppingListAdapter);

        setShoppingListOnItemClick();
        setShoppingListOnItemLongClick();

        Intent intent = new Intent(this, MainService.class);
        startService(intent);
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
                mainController.sync(false);
                return true;
            case R.id.clear_list:
                mainController.clearShoppingList();
                return true;
            case R.id.connect:
                mainController.connect(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void setShoppingListOnItemLongClick(){

        shoppingListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View v, final int position, final long id) {
                int contextMenuID = ((int) R.array.shoppingListContextMenuValues);
                if (shoppingList.getShoppingListItem(position).isCrossedOff())
                    contextMenuID = ((int) R.array.shoppingListContextMenuValuesDeleteOnly);

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.ListContextMenu));
                builder.setTitle(shoppingList.getShoppingListItem(position).getShoppingListItem()).setCancelable(true).setItems(contextMenuID,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int index) {
                                switch (index) {
                                    case 0:
                                        mainController.deleteShoppingListItem(position);
                                        break;
                                    case 1:
                                        mainController.editShoppingListItem(parent, v, position, id, MainActivity.this);
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
                mainController.crossOffShoppingItem(position);
            }
        });
    }

    public void addBtnClick(View view){
        if (enterItemEditTxt.getText().toString().trim().length() > 0)
            mainController.addItemToShoppingList(enterItemEditTxt.getText().toString().trim());
        shoppingListView.setSelection(shoppingListAdapter.getCount() - 1);
        enterItemEditTxt.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(DataHelper.FILE_CHANGED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(dataChangedReceiver, filter);

        loadLocalShoppingList();
    }
    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataChangedReceiver);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

        mainController.cleanUp();
    }

    // Define the callback for what to do when message is received
    public class DataChangedReceiver extends BroadcastReceiver {
          @Override
        public void onReceive(Context context, Intent intent) {
              loadLocalShoppingList();
        }
    }

    public void loadLocalShoppingList(){
        mainActivityHandler.post(new Runnable() {
            @Override
            public void run() {
                shoppingList.loadShoppingList(mainController.dataHelper.loadGsonFromLocalStorage());

                mainUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.shoppingListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public ShoppingListAdapter getShoppingListAdapter(){return shoppingListAdapter;}
}
